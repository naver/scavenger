<template>
  <el-scrollbar>
    <el-descriptions :column="3" border class="fixed-table-area">
      <el-descriptions-item :label="$t('message.common.name')" :span="3" width="330">
        {{ $route.params.customerId }}
      </el-descriptions-item>
      <el-descriptions-item :label="$t('message.dashboard.summary.method-count')" width="330">
        {{ summary.methodCount }}
      </el-descriptions-item>
      <el-descriptions-item :label="$t('message.dashboard.summary.agent-count')" :span="2" width="330">
        {{ agentCount }}
      </el-descriptions-item>
      <el-descriptions-item :label="$t('message.dashboard.summary.application-count')" width="330">
        {{ applicationCount }}
      </el-descriptions-item>
      <el-descriptions-item :label="$t('message.dashboard.summary.environment-count')" :span="2" width="330">
        {{ environmentCount }}
      </el-descriptions-item>
      <el-descriptions-item :label="$t('message.dashboard.summary.configuration')" :span="3" width="330">
        <ConfForm :summary="summary"/>
      </el-descriptions-item>
    </el-descriptions>
  </el-scrollbar>
</template>
<script>
import Moment from "moment";
import ConfForm from "./ConfForm.vue";
import {useStore} from "../util/store";

export default {
  components: {ConfForm},
  props: ["customerId", "applicationCount", "environmentCount", "agentCount"],
  data() {
    return {
      summary: {
        agentCount: 0,
        methodCount: 0,
        snapshotLimit: 0
      }
    }
  },
  watch: {
    customerId() {
      this.getSummary();
    },
  },
  mounted() {
    if (this.customerId !== undefined && this.customerId !== "") {
      this.getSummary();
    }
  },
  methods: {
    getSummary() {
      this.$http.get(`/customers/${this.customerId}/summary`)
        .then(res => {
          this.summary = res.data;
          useStore().snapshotLimit = this.summary.snapshotLimit;
        });
    },
  }
};
</script>
