<template>
  <el-scrollbar>
    <div class="content">
      <el-space direction="vertical" size="large" alignment="normal">
        <el-card class="box-card">
          <SummaryTable :agentCount="agents.length"
                        :applicationCount="applicationCount"
                        :environmentCount="environmentCount"
                        :customerId="customerId"
          />
        </el-card>
        <el-card class="box-card">
          <AgentTable :agents="agents" :applications="applications" :environments="environments"/>
        </el-card>
      </el-space>
    </div>
  </el-scrollbar>
</template>

<script>
import SummaryTable from "./SummaryTable.vue";
import AgentTable from "./AgentTable.vue";
import {useStore} from "../util/store";

export default {
  components: {SummaryTable, AgentTable,},
  props: ["customerId"],
  data() {
    return {
      title: "",
      agents: [],
    };
  },
  computed: {
    applications() {
      return useStore().applications;
    },
    environments() {
      return useStore().environments;
    },
    applicationCount() {
      if (this.applications === null) return;
      return Object.keys(this.applications).length;
    },
    environmentCount() {
      if (this.environments === null) return;
      return Object.keys(this.environments).length;
    },
  },
  watch: {
    customerId() {
      this.getApplications();
      this.getEnvironments();
      this.getAgents();
    },
  },
  mounted() {
    if (this.customerId !== undefined && this.customerId !== "") {
      this.getApplications();
      this.getEnvironments();
      this.getAgents();
    }
    this.$nextTick(function () {
      this.interval = window.setInterval(() => {
        this.getAgents();
      }, 5000);
    });
  },
  beforeUnmount() {
    clearInterval(this.interval);
  },
  methods: {
    getAgents() {
      if (!this.customerId) return;
      this.$http.get(`/customers/${this.customerId}/agents`)
        .then(res => {
          this.agents = res.data;
        });
    },
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
  },
};
</script>
