<template>
  <div class="content">
    <el-row>
      <el-col :span="20">
        <h1 style="display: inline-block">{{ $t("message.manage.environment.title") }}</h1>
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
          <el-table :data="filterEnvironments()" header-row-class-name="table-header" border stripe
                    class="fixed-table-area" highlight-current-row table-layout="auto">
            <el-table-column prop="id" label="ID" align="center"/>
            <el-table-column prop="name" :label="$t('message.common.name')" align="center"/>
            <el-table-column prop="jvmCount" :label="$t('message.manage.jvm-count')" align="center"/>
            <el-table-column prop="snapshotCount" :label="$t('message.manage.snapshot-count')"
                             align="center"/>
            <el-table-column prop="invocationCount" :label="$t('message.manage.invocation-count')"
                             align="center"/>
            <el-table-column prop="createdAt" :label="$t('message.common.created-at')" align="center"/>
            <el-table-column :label="$t('message.common.delete')" width="70" align="center">
              <template #default="scope">
                <el-button circle type="danger" @click="deleteEnvironment(scope.row.id, scope.row.name)">
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
  props: ["customerId", "updateSnapshot"],
  data() {
    return {
      SEARCH_FIELD: ["name"],
      keyword: "",
      environments: [],
    };
  },
  watch: {
    customerId() {
      this.getEnvironments();
    },
  },
  mounted() {
    if (this.customerId !== undefined && this.customerId !== "") {
      this.getEnvironments();
    }
  },
  methods: {
    getEnvironments() {
      this.$http.get(`/customers/${this.customerId}/environments/_detail`)
        .then(res => {
          this.environments = res.data;
        });
    },
    filterEnvironments() {
      if (!this.environments) return;

      const keywordRegExp = new RegExp(this.keyword, "i");
      return Object.values(this.environments)
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
    deleteEnvironment(environmentId, environmentName) {
      ElMessageBox.confirm(
        this.$t("message.manage.environment.delete-title", [environmentName]),
        "Warning",
        {
          confirmButtonText: this.$t("message.common.yes"),
          cancelButtonText: this.$t("message.common.no"),
          type: 'warning',
        })
        .then(() => {
          this.$http.delete(`/customers/${this.customerId}/environments/${environmentId}`)
            .then(() => {
              ElNotification.success({message: this.$t("message.manage.environment.delete-success")});
              this.environments = this.environments.filter(it => it.id !== environmentId);
              this.emitter.emit("updateSnapshot");
              this.emitter.emit("updateEnvironments");
            });
        });
    },
  },
};
</script>
