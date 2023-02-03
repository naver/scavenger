"
<template>
  <el-scrollbar>
    <el-table :data="rows" header-row-class-name="table-header" border style="width: 100%" size="small">
      <el-table-column :label="$t('message.snapshot.detail.result.signature', [rows.length])" align="center"
                       show-overflow-tooltip
                       class-name="signature-cell">
        <template #default="scope">
          <div class="signature-area" :class="scope.row.percentage > 0 ? 'used-signature' : 'unused-signature'">
            <span style="vertical-align: middle">
              <i class="icon fa" :class="scope.row.icon"/>
            </span>
            <a @click="clickSignature(scope.row.signature)" :title="scope.row.signature" class="signature">
              {{ scope.row.abbreviatedSignature }}
            </a>
            <span class="link">
              <a @click="copySignature(scope.row.signature)" class="function-box">
                <font-awesome-icon icon="fa-solid fa-copy"/>
              </a>
              <a v-if="scope.row.type.value !== 'PACKAGE'" @click="open(scope.row.signature)" class="function-box">
                <font-awesome-icon icon="fa-solid fa-pen-to-square"/>
              </a>
              <a v-if="githubLink(scope.row)" :href="githubLink(scope.row)" class="function-box">
                <font-awesome-icon icon="fa-brands fa-github"/>
              </a>
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="lastInvokedAtMillis" :label="$t('message.snapshot.detail.result.last-invoked-at-millis')"
                       width="200"/>
      <el-table-column prop="counts" :label="$t('message.snapshot.detail.result.method-count')" width="125"/>
      <el-table-column :label="$t('message.snapshot.detail.result.usage')" width="110">
        <template #default="scope">
          <span>{{ toPercentageStr(scope.row.percentage) }}%</span>
        </template>
      </el-table-column>
    </el-table>
  </el-scrollbar>
</template>
<script>
import copy from "copy-to-clipboard";
import {getFilePath, openLink, toPercentageStr} from "../util/util";
import {ElNotification} from "element-plus";

export default {
  props: ["rows", "updateSnapshotData", "githubLink"],
  methods: {
    toPercentageStr(percentage) {
      return toPercentageStr(percentage);
    },
    copySignature(signature) {
      if (signature.endsWith(")")) {
        const lastDotIndex = signature.split("(")[0].lastIndexOf(".");

        signature = `${signature.substring(0, lastDotIndex)}#${signature.substring(lastDotIndex + 1)}`;
      }
      copy(signature);
      ElNotification.info({message: `${signature} copied.`});
    },
    open(signature) {
      openLink(getFilePath(signature))
        .catch(err => {
          if (err.response.status === 404) {
            ElNotification.warning({
              dangerouslyUseHTMLString: true,
              message: this.$t("message.snapshot.detail.open-idea-fail")
            });
          }
        });
    },
    clickSignature(signature) {
      if (signature.includes("(")) {
        this.open(signature);
      } else {
        this.updateSnapshotData(signature);
      }
    },
  },
};
</script>
<style lang="less">
.used-signature {
  border-left: 6px solid #03c75a;
}

.unused-signature {
  border-left: 6px solid #CC0000;
}
</style>
