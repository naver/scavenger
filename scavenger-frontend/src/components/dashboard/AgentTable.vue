<template>
  <el-row>
    <el-col :span="20">
      <h1>{{ $t("message.dashboard.agent.title") }}</h1>
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
  <el-row>
    <el-col :span="24">
      <el-table :data="updateAgents()" size="small" header-row-class-name="table-header"
                stripe highlight-current-row>
        <el-table-column prop="number" label="" width="40" align="center" show-overflow-tooltip/>
        <el-table-column prop="hostname" :label="$t('message.dashboard.agent.host')" width="300" align="center"/>
        <el-table-column prop="jvmUuid" label="JVM UUID" width="300" align="center" show-overflow-tooltip/>
        <el-table-column prop="application" :label="$t('message.common.application')" align="center"
                         show-overflow-tooltip/>
        <el-table-column prop="environment" :label="$t('message.common.environment')" align="center"
                         show-overflow-tooltip/>
        <el-table-column prop="version" :label="$t('message.dashboard.agent.version')" align="center"
                         show-overflow-tooltip/>
        <el-table-column prop="lastPolledAt" :label="$t('message.dashboard.agent.last-polled-at')" align="center"
                         show-overflow-tooltip/>
        <el-table-column prop="nextPollExpectedAt" :label="$t('message.dashboard.agent.next-poll-expected-at')"
                         align="center" show-overflow-tooltip/>
        <el-table-column prop="createdAt" :label="$t('message.common.created-at')" align="center"
                         show-overflow-tooltip/>
      </el-table>
    </el-col>
  </el-row>
</template>
<script>
import Moment from "moment";

export default {
  props: ["agents", "applications", "environments"],
  data() {
    return {
      SEARCH_FIELD: ["application", "environment", "version", "hostname", "jvmUuid"],
      keyword: "",
    }
  },
  watch: {
    agents() {
      this.updateAgents();
    },
    applications() {
      this.updateAgents();
    },
    environments() {
      this.updateAgents();
    },
    keyword() {
      this.updateAgents();
    }
  },
  methods: {
    updateAgents() {
      if (!this.agents || !this.applications || !this.environments) return;

      const keywordRegExp = new RegExp(this.keyword, "i");
      return this.agents.slice(0)
        .filter((it) => this.SEARCH_FIELD.some(value => keywordRegExp.test(it[value])))
        .sort((a, b) => (b.createdAt - a.createdAt))
        .map((obj, index) => ({
          "number": index + 1,
          "hostname": obj.hostname,
          "jvmUuid": obj.jvmUuid,
          "environment": obj.environmentName,
          "application": obj.applicationName,
          "version": obj.applicationVersion,
          "createdAt": Moment.unix(obj.createdAt).format("YYYY.MM.DD HH:mm"),
          "lastPolledAt": Moment.unix(obj.lastPolledAt).fromNow(),
          "nextPollExpectedAt": Moment.unix(obj.nextPollExpectedAt).fromNow(),
        }));
    },
  },
};
</script>
