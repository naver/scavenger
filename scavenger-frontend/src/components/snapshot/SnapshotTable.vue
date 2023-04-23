<template>
  <div class="without-card">
    <el-scrollbar>
      <div class="content">
        <el-row>
          <el-col :span="20">
            <h1 style="display: inline-block">{{ $t("message.snapshot.title") }}</h1>
            <el-button plain style="margin-left: 10px; margin-bottom: 4px;" @click="openSnapshotCreateDialog()">
              <font-awesome-icon icon="fa-solid fa-plus"/>
              &nbsp;{{ $t("message.snapshot.create") }}
            </el-button>
          </el-col>
          <el-col :span="4">
            <div class="search-box">
              <el-input v-model="keyword">
                <template #prefix>
                  <font-awesome-icon icon="fa-solid fa-magnifying-glass"/>
                </template>
              </el-input>
            </div>
          </el-col>
        </el-row>
        <SnapshotForm :customerId="customerId" :dialogTableVisible="dialogTableVisible"/>
        <el-row>
          <el-col :span="24">
            <div>
              <el-table :data="filterSnapshots()" header-row-class-name="table-header" border stripe
                        highlight-current-row table-layout="auto">
                <el-table-column align="center" width="70">
                  <template #default="scope">
                    <el-button circle @click="navigateSnapshotDetail(scope.row.id)">
                      <font-awesome-icon icon="fa-solid fa-magnifying-glass"/>
                    </el-button>
                  </template>
                </el-table-column>
                <el-table-column prop="id" label="ID" align="center"/>
                <el-table-column prop="name" :label="$t('message.common.name')" align="center"/>
                <el-table-column prop="application" :label="$t('message.common.application')" align="center"
                                 min-width="160"/>
                <el-table-column prop="environment" :label="$t('message.common.environment')" align="center"
                                 min-width="100"/>
                <el-table-column prop="packages" :label="$t('message.snapshot.packages')" align="center"
                                 min-width="160"/>
                <el-table-column prop="filterInvokedAtMillis" :label="$t('message.snapshot.filter-invoked-at-millis')"
                                 align="center"/>
                <el-table-column prop="createdAt" :label="$t('message.common.created-at')" align="center"/>
                <el-table-column :label="$t('message.snapshot.edit')" width="70" align="center">
                  <template #default="scope">
                    <el-button circle type="primary" @click="openSnapshotEditDialog(scope.row.id)">
                      <font-awesome-icon icon="fa-solid fa-pen-to-square"/>
                    </el-button>
                  </template>
                </el-table-column>
                <el-table-column :label="$t('message.snapshot.refresh')" width="70" align="center">
                  <template #default="scope">
                    <el-button circle type="primary" @click="refreshSnapshot(scope.row.id)">
                      <font-awesome-icon icon="fa-solid fa-arrows-rotate"/>
                    </el-button>
                  </template>
                </el-table-column>
                <el-table-column :label="$t('message.snapshot.export')" width="80" align="center">
                  <template #default="scope">
                    <el-button circle type="primary" @click="exportSnapshot(scope.row.id)">
                      <font-awesome-icon icon="fa-solid fa-file-export"/>
                    </el-button>
                  </template>
                </el-table-column>
                <el-table-column :label="$t('message.common.delete')" width="70" align="center">
                  <template #default="scope">
                    <el-button circle type="danger" @click="deleteSnapshot(scope.row.id)">
                      <font-awesome-icon icon="fa-solid fa-trash-can"/>
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-scrollbar>
  </div>
</template>
<script>
import SnapshotForm from "./SnapshotForm.vue";
import Momnet from "moment";
import {useStore} from "../util/store";
import {ElMessageBox, ElNotification} from "element-plus";

export default {
  components: {SnapshotForm},
  props: ["customerId", "onClickRefresh"],
  data() {
    return {
      SEARCH_FIELD: ["id", "application", "environment", "name"],
      keyword: "",
      dialogTableVisible: false
    };
  },
  computed: {
    applications() {
      return useStore().applications;
    },
    environments() {
      return useStore().environments;
    },
  },
  watch: {
    customerId() {
      this.getApplications();
      this.getEnvironments();
      this.emitter.emit("updateSnapshot");
    },
    applications() {
      if (Object.keys(useStore().applications).length === 0) {
        ElNotification.error({message: this.$t("message.snapshot.empty-agent")});
      }
    }
  },
  mounted() {
    if (this.customerId !== undefined && this.customerId !== "") {
      this.getApplications();
      this.getEnvironments();
      this.emitter.emit("updateSnapshot");
    }
    this.emitter.on("hideDialogTableVisible", () => this.dialogTableVisible = false);
  },
  methods: {
    getApplications() {
      if (useStore().applications === null || Object.keys(useStore().applications).length === 0) {
        this.emitter.emit("updateApplications");
      }
    },
    getEnvironments() {
      if (useStore().environments === null || Object.keys(useStore().environments).length === 0) {
        this.emitter.emit("updateEnvironments");
      }
    },
    filterSnapshots() {
      if (this.applications === null || this.environments === null || !useStore().snapshots) {
        return;
      }

      const keywordRegExp = new RegExp(this.keyword, "i");
      return useStore().snapshots
        .filter((it) => this.SEARCH_FIELD.some(value => keywordRegExp.test(it[value])))
        .map(obj => (
          {
            "id": obj.id,
            "name": obj.name,
            "application": obj.applications.map(obj => this.applications[obj].name).join(", "),
            "environment": obj.environments.map(obj => this.environments[obj].name).join(", "),
            "filterInvokedAtMillis": obj.filterInvokedAtMillis > 0 ?
              new Momnet(obj.filterInvokedAtMillis).format("YYYY.MM.DD HH:mm") : "-",
            "packages": obj.packages,
            "createdAt": Momnet.unix(obj.createdAt).fromNow(),
          }),
        );
    },
    navigateSnapshotDetail(id) {
      this.$router.push(`${this.$router.currentRoute.value.path}/${id}?signature=`);
    },
    refreshSnapshot(id) {
      this.$http.post(`/customers/${this.customerId}/snapshots/${id}/refresh`)
        .then(() => {
          ElNotification.success({message: this.$t("message.snapshot.refresh-success")});
        })
    },
    exportSnapshot(id) {
      const fileName = `snapshot${id}.tsv`;
      this.$http.get(`/customers/${this.customerId}/snapshot/${id}/export?fn=${fileName}`)
        .then((response) => {
          const csvFile = new Blob([response.data], {type: 'text/csv'});
          const downloadLink = document.createElement("a");
          downloadLink.download = fileName;
          downloadLink.href = window.URL.createObjectURL(csvFile);
          downloadLink.style.display = "none";
          document.body.appendChild(downloadLink);
          downloadLink.click();
          window.URL.revokeObjectURL(downloadLink.href);
          document.body.removeChild(downloadLink);
        })
        .catch(() => {
          ElNotification.error({message: this.$t("message.snapshot.export-fail")});
        });
    },
    deleteSnapshot(id) {
      ElMessageBox.confirm(
        this.$t("message.snapshot.delete-title"),
        "Warning",
        {
          confirmButtonText: this.$t("message.common.yes"),
          cancelButtonText: this.$t("message.common.no"),
          type: 'warning',
        })
        .then(() => {
          this.$http.delete(`/customers/${this.customerId}/snapshots/${id}`)
            .then(() => {
              ElNotification.success({message: this.$t("message.snapshot.delete-success")});
              useStore().snapshots = useStore().snapshots.filter(it => it.id !== id);
            });
        });
    },
    openSnapshotCreateDialog() {
      if (useStore().snapshots.length >= useStore().snapshotLimit) {
        ElNotification.error({message: this.$t("message.snapshot.limit")});
        return;
      }
      useStore().selectedSnapshot = {};
      this.dialogTableVisible = true;
    },
    openSnapshotEditDialog(id) {
      useStore().selectedSnapshot = useStore().snapshots.find(it => it.id === id);
      this.dialogTableVisible = true;
    },
  },
};
</script>
