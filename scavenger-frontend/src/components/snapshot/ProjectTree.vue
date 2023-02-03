<template>
  <el-scrollbar>
    <div class="project-tree">
      <div class="control-box">
        <el-popover placement="bottom-start" trigger="focus" :width="popoverWidth" :show-after="50" :show-arrow="false"
                    :offset="-0.5" :disabled="searchText.length < 5" :visible="popoverVisible">
          <template #reference>
            <el-input v-model="searchText" id="search-everywhere-bar" size="small"
                      :placeholder="$t('message.snapshot.detail.search')"
                      @keyup="onKeyUpSearch()"
                      @blur="popoverVisible = false"
                      @focus="popoverVisible = true">
              <template #prefix>
                <font-awesome-icon icon="fa-solid fa-magnifying-glass"/>
              </template>
            </el-input>
          </template>
          <el-table :data="signatures" height="300" :empty-text="'Nothing found'">
            <el-table-column prop="date" show-overflow-tooltip>
              <template #default="scope">
                <div @click="navigate(scope.row)" class="search-everywhere-table-column">
                  <font-awesome-icon icon="fa-solid fa-magnifying-glass"/>&nbsp;{{ scope.row.signature }}
                </div>
              </template>
            </el-table-column>
            <template #append>
              <tr v-show="moreVisible" class="el-table__row">
                <td class="el-table_1_column_1 el-table__cell" rowspan="1" colspan="1">
                  <div class="cell el-tooltip" :style="{width: (popoverWidth - 26)+ 'px'}">
                    <div class="search-everywhere-table-column" @click="searchSignature()">
                      <font-awesome-icon icon="fa-solid fa-ellipsis-vertical"/>&nbsp;&nbsp;More
                    </div>
                  </div>
                </td>
              </tr>
            </template>
          </el-table>
        </el-popover>
        <span class="button-area">
          <a class="text" @click="this.onClickRefresh">
            {{ $t("message.snapshot.refresh") }}
            <font-awesome-icon icon="fa-solid fa-arrows-rotate" :class="{'fa-spin': refreshing}"/>
          </a>
        </span>
      </div>
      <v-jstree :data="data" children-field-name="childrenWithoutMethods" @item-click="itemClick"
                style="width: fit-content" ref="tree">
        <template v-slot="_">
          <div style="display: inherit; font-size: 11px" :class="_.model.class" :ref="_.model.signature">
            <i :class="_.vm.themeIconClasses" role="presentation"/>
            <span v-text="_.model.text"/>
            <span style="font-size: 7px; margin-left: 3px">
              <span style="font-style: italic; color: #999999">
                {{ _.model.totalCount }},
                <span :style="utilizationStyle(_.model.percentage)">{{ utilization(_.model.percentage) }}%</span>
              </span>
            </span>
          </div>
        </template>
      </v-jstree>
    </div>
  </el-scrollbar>
</template>
<script>
import VJstree from "./tree/tree.vue";
import {getGradientColor, toPercentageStr} from "../util/util";
import {useStore} from "../util/store";

export default {
  components: {VJstree},
  props: ["updateSnapshotData", "refreshing", "onClickRefresh", "customerId"],
  data() {
    return {
      searchText: "",
      itemClick: node => {
        this.updateSnapshotData(node.model.signature);
      },
      signatures: [],
      popoverVisible: true,
      popoverWidth: 0,
      moreVisible: false,
    };
  },
  computed: {
    data() {
      if (useStore().snapshotIndex['']) {
        return useStore().snapshotIndex[''].children;
      }
      return [];
    },
  },
  mounted() {
    this.$nextTick(() => this.popoverWidth = document.getElementById("search-everywhere-bar").offsetWidth + 35);
    this.emitter.on("resizeVertical",
      () => this.popoverWidth = document.getElementById("search-everywhere-bar").offsetWidth + 35
    );
  },
  methods: {
    traverseTree(node, func) {
      func(node);
      node.children.forEach(child => {
        this.traverseTree(child, func);
      });
    },
    utilization(utilization) {
      return toPercentageStr(utilization);
    },
    utilizationStyle(utilization) {
      return {
        color: getGradientColor("#000000", "#03c75a", utilization),
      };
    },
    onKeyUpSearch() {
      if (this.searchText.length < 5) {
        this.popoverVisible = false;
        return;
      }

      this.signatures = [];
      this.moreVisible = false;
      this.searchSignature();
    },
    searchSignature() {
      document.getElementById("search-everywhere-bar").focus();
      this.$http.get(`/customers/${this.customerId}/snapshots/${this.$route.params.snapshotId}/nodes`
        + `?signature=${this.searchText}`
        + (this.signatures.length !== 0 ? `&snapshotNodeId=${this.signatures[this.signatures.length - 1].id}` : "")
      ).then(res => {
        if (this.signatures.length === 0) {
          this.signatures = res.data;
        } else {
          this.signatures = this.signatures.concat(res.data);
          this.signatures = this.signatures.filter((item, index) => this.signatures.indexOf(item) === index);
        }

        this.moreVisible = res.data.length !== 0;
        this.popoverVisible = true;
      });
    },
    navigate(signature) {
      const query = signature.type === "METHOD" ? signature.parent : signature.signature;
      this.$router.push(`${this.$router.currentRoute.value.path}?signature=${query}`);
    }
  },
};
</script>
