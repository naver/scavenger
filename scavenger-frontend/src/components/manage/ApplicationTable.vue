<template>
  <div class="content">
    <el-row>
      <el-col :span="20">
        <h1 style="display: inline-block">{{ $t("message.manage.application.title") }}</h1>
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
    <el-scrollbar>
      <el-row>
        <el-col :span="24">
          <el-table :data="filterApplications()" header-row-class-name="table-header" border stripe
                    class="fixed-table-area" highlight-current-row table-layout="auto">
            <el-table-column prop="id" label="ID" align="center"/>
            <el-table-column prop="name" :label="$t('message.common.name')" align="center"/>
            <el-table-column prop="jvmCount" :label="$t('message.manage.jvm-count')" align="center"/>
            <el-table-column prop="snapshotCount" :label="$t('message.manage.snapshot-count')" align="center"/>
            <el-table-column prop="invocationCount" :label="$t('message.manage.invocation-count')" align="center"/>
            <el-table-column prop="createdAt" :label="$t('message.common.created-at')" align="center"/>
            <el-table-column :label="$t('message.common.delete')" width="70" align="center">
              <template #default="scope">
                <el-button circle type="danger" @click="deleteApplication(scope.row.id, scope.row.name)">
                  <font-awesome-icon icon="fa-solid fa-trash-can"/>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
    </el-scrollbar>
  </div>
</template>
<script>
import Moment from "moment";
import {ElMessageBox, ElNotification} from "element-plus";

export default {
  props: ["customerId"],
  data() {
    return {
      SEARCH_FIELD: ["name"],
      keyword: "",
      applications: [],
    };
  },
  watch: {
    customerId() {
      this.getApplications();
    },
  },
  mounted() {
    if (this.customerId !== undefined && this.customerId !== "") {
      this.getApplications();
    }
  },
  methods: {
    getApplications() {
      this.$http.get(`/customers/${this.customerId}/applications/_detail`)
        .then(res => {
          this.applications = res.data;
        });
    },
    filterApplications() {
      if (!this.applications) return;

      const keywordRegExp = new RegExp(this.keyword, "i");
      return Object.values(this.applications)
        .slice(0)
        .filter((it) => this.SEARCH_FIELD.some(value => keywordRegExp.test(it[value])))
        .sort((a, b) => (b.name - a.name))
        .map((obj) => ({
          "id": obj.id,
          "name": obj.name,
          "invocationCount": obj.invocationCount,
          "jvmCount": obj.jvmCount,
          "snapshotCount": obj.snapshotCount,
          "createdAt": Moment.unix(obj.createdAt).format("YYYY.MM.DD HH:mm"),
        }));
    },
    deleteApplication(applicationId, applicationName) {
      ElMessageBox.confirm(
        this.$t("message.manage.application.delete-title", [applicationName]),
        "Warning",
        {
          confirmButtonText: this.$t("message.common.yes"),
          cancelButtonText: this.$t("message.common.no"),
          type: 'warning',
        })
        .then(() => {
          this.$http.delete(`/customers/${this.customerId}/applications/${applicationId}`)
            .then(() => {
              ElNotification.success({message: this.$t("message.manage.application.delete-success")});
              this.applications = this.applications.filter(it => it.id !== applicationId);
              this.emitter.emit("updateSnapshot");
              this.emitter.emit("updateApplications");
            });
        });
    },
  },
};
</script>
