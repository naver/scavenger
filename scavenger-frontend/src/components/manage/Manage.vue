<template>
  <el-scrollbar>
    <div class="content">
      <el-space direction="vertical" size="large" alignment="normal">
        <el-card class="box-card">
          <GithubTable :customerId="customerId"/>
        </el-card>
        <el-card class="box-card">
          <ApplicationTable :customerId="customerId"/>
        </el-card>
        <el-card class="box-card">
          <EnvironmentTable :customerId="customerId"/>
        </el-card>
      </el-space>
      <div style="margin: 20px">
        <el-button type="warning" @click="clickReset">{{ $t("message.manage.reset") }}</el-button>
      </div>
    </div>
  </el-scrollbar>
</template>
<script>
import ApplicationTable from "./ApplicationTable.vue";
import EnvironmentTable from "./EnvironmentTable.vue";
import GithubTable from "./GithubTable.vue";
import {ElMessageBox, ElNotification} from "element-plus";

export default {
  components: {GithubTable, ApplicationTable, EnvironmentTable},
  props: ["customerId", "updateSnapshot"],
  methods: {
    clickReset() {
      ElMessageBox.confirm(
        "워크스페이스를 초기화하시겠습니까? 전체 데이터가 삭제되며 되돌릴 수 없습니다.",
        "Warning",
        {
          confirmButtonText: "예",
          cancelButtonText: "아니오",
          type: 'warning',
        })
        .then(() => {
          this.$http.post(`/customers/${this.customerId}/reset`)
            .then(() => {
              ElNotification.success({message: "워크스페이스를 초기화 하였습니다."});
              location.reload();
            });
        });
    },
  },
};
</script>
