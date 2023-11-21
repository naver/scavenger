<template>
  <div class="snapshot-board" @keydown.delete="onPressBackButton" tabindex="0">
    <div class="content">
      <splitpanes v-show="ready" @ready="ready = true"
                  class="default-theme" @resize="resizeVertical($event[0].size)" style="flex: 1; overflow: hidden">
        <pane :size="verticalSize" style="overflow: auto">
          <ProjectTree :updateSnapshotData="updateSnapshotData"
                       :refreshing="refreshing"
                       :onClickRefresh="onClickRefresh"
                       :customerId="customerId"
          />
        </pane>
        <pane :size="100 - verticalSize">
          <splitpanes horizontal @resize="resizeHorizontal($event[0].size)">
            <pane :size="horizontalSize">
              <Treemap :updateSnapshotData="updateSnapshotData"
                       :snapshotData="snapshotData" :snapshot="this.snapshot"
                       :isPackage="this.isPackage" :githubLink="githubLink"
              />
            </pane>
            <pane :size="100 - horizontalSize" style="overflow: auto">
              <ResultTable :rows="tableResult" :updateSnapshotData="updateSnapshotData" :githubLink="githubLink"/>
            </pane>
          </splitpanes>
        </pane>
      </splitpanes>
      <div class="snapshot-summary">
        <span>{{ $t("message.common.name") }} : {{ snapshot.name }}</span>
        <span>{{ $t("message.common.application") }} : {{ getApplicationName() }}</span>
        <span>{{ $t("message.common.environment") }} : {{ getEnvironmentName() }}</span>
        <span style="text-overflow: ellipsis">
          {{ $t("message.snapshot.packages") }} :
          {{ snapshot.packages ? snapshot.packages.substring(0, 50) + "..." : "**" }}
        </span>
        <span>{{ $t("message.snapshot.filter-invoked-at-millis") }} : {{ filterInvokedAtMillisStr }}</span>
        <span>{{ $t("message.common.created-at") }} : {{ createdAtStr }}</span>
      </div>
    </div>
  </div>
</template>
<script>
import ResultTable from "./ResultTable.vue";
import Treemap from "./Treemap.vue";
import ProjectTree from "./ProjectTree.vue";
import {Pane, Splitpanes} from "splitpanes";
import "splitpanes/dist/splitpanes.css";
import Moment from "moment";
import {getFilePath} from "../util/util";
import {useStore} from "../util/store";
import "../util/util.js";
import {ElNotification} from "element-plus";

export default {
  components: {Splitpanes, Pane, ProjectTree, ResultTable, Treemap},
  props: ["customerId"],
  data() {
    return {
      TYPE_ENUM: {
        PACKAGE: {
          value: "PACKAGE",
          order: 0,
          icon: "icon-package"
        },
        CLASS: {
          value: "CLASS",
          order: 1,
          icon: "icon-class"
        },
        METHOD: {
          value: "METHOD",
          order: 2,
          icon: "icon-method"
        },
      },
      snapshotData: {
        signature: "",
        children: []
      },
      query: "",
      refreshing: false,
      verticalSize: localStorage.getItem("scavenger.snapshot.vertical-size") || 20,
      horizontalSize: localStorage.getItem("scavenger.snapshot.horizontal-size") || 40,
      ready: false,
    };
  },
  computed: {
    applications() {
      return useStore().applications;
    },
    environments() {
      return useStore().environments;
    },
    snapshot() {
      const snapshot = useStore().snapshots.find(it => it.id === parseInt(this.$route.params.snapshotId));
      return snapshot || {name: "", environments: [], applications: []};
    },

    filterInvokedAtMillisStr() {
      if (this.snapshot.filterInvokedAtMillis === 0) return "-";
      return new Moment(this.snapshot.filterInvokedAtMillis).format("YYYY.MM.DD");
    },

    createdAtStr() {
      return Moment.unix(this.snapshot.createdAt).format("YYYY.MM.DD");
    },
    tableResult() {
      if (!this.snapshotData.signature || this.snapshotData.signature.length === 0) {
        return [];
      }
      let abbreviatedCurrentSignature = this.snapshotData.signature.split(".")
        .map(value => value[0].toUpperCase() === value[0] ? value : value[0])
        .join(".");

      return this.snapshotData.children.map(child => {
        let lastInvokedAt = child.lastInvokedAtMillis === null ? "-" :
          new Moment(child.lastInvokedAtMillis).format("YYYY.MM.DD HH:mm");

        return {
          "lastInvokedAtMillis": lastInvokedAt,
          "signature": child.signature,
          "abbreviatedSignature": `${abbreviatedCurrentSignature}${child.lastDelimiter}${child.abbreviatedLabel}`,
          "counts": child.totalCount,
          "icon": child.icon,
          "type": child.type,
          "percentage": child.percentage,
        };
      });
    },
  },
  watch: {
    snapshotData: {
      handler(after, before) {
        let current = before;
        while (current) {
          current["class"] = "";
          current = current.parent;
        }

        current = after;
        while (current) {
          current.opened = true;
          current["class"] = "selected";
          current = current.parent;
        }
        history.replaceState(history.state, '', `${this.$route.path}?signature=${after.signature}`);
        this.query = after.signature;
      }
    },
    customerId() {
      if (this.customerId !== undefined && this.customerId !== "") {
        this.init();
      }
    }
  },
  created() {
    this.query = this.$route.query.signature || "";
    if (this.customerId !== undefined && this.customerId !== "") {
      useStore().snapshotIndex = {"": {signature: "", children: []}};
      this.init();
    }
  },
  methods: {
    githubLink(method) {
      const signature = getFilePath(method.signature);
      const found = useStore().githubMappings.find(mapping => {
        return signature.startsWith(mapping.basePackage);
      });
      if (found) {
        return found.url + signature.replace(found.basePackage, "")
          .replaceAll(".", "/").split("$")[0] + (method.type === this.TYPE_ENUM.PACKAGE ? "" : ".java");
      }
    },
    async init() {
      let signature = "";
      const querySignature = this.query;

      await this.updateSnapshotData(signature);
      while (signature !== querySignature) {
        let candidate = this.snapshotData.children.find(child => querySignature === child.signature);

        if (candidate) {
          signature = candidate.signature;
        } else {
          signature = this.snapshotData.children
            .find(child => this.isStartsWithSignature(querySignature, child.signature)).signature;
        }
        await this.updateSnapshotData(signature);
      }
    },
    async updateSnapshotData(nextSignature) {
      const next = useStore().snapshotIndex[nextSignature];
      if (next === undefined) return;
      await this.updateSnapshotChildren(next);
      if (next.children.length) this.snapshotData = next;
    },
    onPressBackButton(e) {
      this.backSignature(e);
    },
    backSignature(e) {
      if (this.snapshotData.parent) this.snapshotData = this.snapshotData.parent;
    },
    isPackage() {
      return this.snapshotData.type !== this.TYPE_ENUM.PACKAGE;
    },
    async onClickRefresh() {
      if (this.refreshing) {
        ElNotification.info({message: this.$t("message.snapshot.detail.on-refreshing")});
        return;
      }
      this.refreshing = true;

      ElNotification.info({message: this.$t("message.snapshot.detail.refreshing")});
      await this.$http.post(`/customers/${this.customerId}/snapshots/${this.$route.params.snapshotId}/refresh`);
      ElNotification.success({message: this.$t("message.snapshot.detail.refresh-success")});
      this.refreshing = false;
      this.snapshotData = {
        signature: "",
        children: []
      };
      useStore().snapshotIndex = {"": {signature: "", children: []}};

      if (this.customerId !== undefined) {
        this.$http.get(`/customers/${this.customerId}/snapshots`)
          .then(res =>
            useStore().snapshots = res.data.sort(function (a, b) {
              if (a.id > b.id) {
                return -1;
              }
              if (a.id < b.id) {
                return 1;
              }
              return 0;
            })
          );
      }

      await this.init();
    },

    async updateSnapshotChildren(snapshotData) {
      if (!snapshotData.children.length) {
        snapshotData.children = await this.getSnapshotData(snapshotData);
      }
      snapshotData.children.forEach(child => {
        let snapshotIndex = useStore().snapshotIndex;
        snapshotIndex[child.signature] = child;
        useStore().snapshotIndex = snapshotIndex;
      });

      snapshotData.children.sort((a, b) => {
        let labelComp = 0;
        if (a.label < b.label) {
          labelComp = -1;
        }
        if (a.label > b.label) {
          labelComp = 1;
        }
        return a.type.order - b.type.order || labelComp;

      });

      snapshotData.childrenWithoutMethods =
        snapshotData.children.filter(snapshotData => (snapshotData.type !== this.TYPE_ENUM.METHOD));
    },

    async getSnapshotData(snapshotData) {
      const baseUrl = `/customers/${this.customerId}/snapshots/${this.$route.params.snapshotId}`;
      const response = await this.$http.get(`${baseUrl}?parent=${snapshotData.signature}`);

      return response.data.map(obj => {
        let searchValue = "";
        if (snapshotData.signature !== "") {
          searchValue = snapshotData.signature + obj.signature.replace(snapshotData.signature, "").charAt(0);
        }
        obj.label = obj.signature.replace(searchValue, "");
        obj.abbreviatedLabel = this.getAbbreviatedLabel(obj.label);
        obj.lastDelimiter = searchValue.charAt(searchValue.length - 1);

        obj.totalCount = obj.usedCount + obj.unusedCount;
        obj.percentage = obj.usedCount / obj.totalCount;
        obj.parent = snapshotData;

        obj.type = this.getType(obj.type);
        obj.text = obj.label;
        obj.opened = true;
        obj.icon = `fa ${obj.type.icon}`;
        obj.children = [];
        obj.maxHeight = 0;
        return obj;
      });
    },
    getAbbreviatedLabel(label) {
      const firstSplit = label.split("(");
      if (!firstSplit[1]) return label;
      const parameters = firstSplit[1].split(")")[0].split(",")
        .map(value => {
          const split = value.split(/\.|\$/);
          return split[split.length - 1];
        })
        .join(", ");
      return `${firstSplit[0]}(${parameters})`;
    },
    getType(type) {
      if (type === "METHOD") {
        return this.TYPE_ENUM.METHOD;
      } else if (type === "CLASS") {
        return this.TYPE_ENUM.CLASS;
      } else {
        return this.TYPE_ENUM.PACKAGE;
      }
    },
    getApplicationName() {
      if (useStore().applications === null || Object.keys(useStore().applications).length === 0) {
        return "";
      }
      return this.snapshot.applications.map(application => useStore().applications[application].name).join(", ") || "";
    },
    getEnvironmentName() {
      if (useStore().environments === null || Object.keys(useStore().environments).length === 0) {
        return "";
      }
      return this.snapshot.environments.map(environment => useStore().environments[environment].name).join(", ") || "";
    },
    resizeVertical(size) {
      localStorage.setItem("scavenger.snapshot.vertical-size", size);
      this.emitter.emit("resizeVertical");
    },
    resizeHorizontal(size) {
      localStorage.setItem("scavenger.snapshot.horizontal-size", size);
    },
    isStartsWithSignature(querySignature, signature) {
      if (!querySignature.startsWith(signature)) {
        return false;
      }

      if (querySignature === signature) {
        return true;
      }

      return querySignature.substring(signature.length).startsWith(".");
    },
  },
};
</script>
