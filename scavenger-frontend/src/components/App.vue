<template>
  <div class="common-layout">
    <el-container>
      <el-header v-if="header !== 'false'" height="45px">
        <el-breadcrumb separator=">">
          <el-breadcrumb-item>
            <a href="/scavenger"><img :src="logo" class="logo"></a>
          </el-breadcrumb-item>
          <el-breadcrumb-item>
            <el-dropdown trigger="click">
              <span class="el-dropdown-link">
                <font-awesome-icon icon="fa-solid fa-desktop"/>
                <span class="customer-dropdown">&nbsp;{{ currentCustomer }}&nbsp;</span>
                <font-awesome-icon icon="fa-solid fa-caret-down"/>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="(customer,index) in customers" :key="customer.id"
                                    :divided="index !== 0"
                                    @click="navigateCustomer(`/scavenger/customers/${customer.name}`)">
                    <span class="customer-name">{{ customer.name }}</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-breadcrumb-item>
        </el-breadcrumb>
      </el-header>
      <el-container>
        <el-aside>
          <Navigator :customerId="customerId"/>
        </el-aside>
        <el-main>
          <router-view :key="this.$route.fullPath" :customerId="customerId">
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>
<script>
import Navigator from "./navigator/Navigator.vue";
import {useStore} from "./util/store";
import "moment/locale/ko";
import logo from "/assets/scavenger-logo.png"

export default {
  components: {Navigator},
  data() {
    return {
      customerId: "",
      logo: logo,
      currentCustomer: "",
      customers: [],
      header: "",
    };
  },
  created() {
    this.header = document.getElementById("app").getAttribute("header");
    this.$http.get("/customers")
      .then(res => this.customers = res.data);
  },
  mounted() {
    this.$watch(
      () => this.$route.params,
      () => {
        if (this.$route.params.customerId !== undefined) {
          this.$http.get(`/customers/_query?name=${this.$route.params.customerId}`)
            .then(res => this.customerId = res.data.id)
            .then(() => this.updateSnapshot())
            .then(() => this.updateGithubMappings());

          this.currentCustomer = this.$route.params.customerId;
        }
      },
      {immediate: true}
    );
    this.emitter.on("updateSnapshot", () => this.updateSnapshot());
    this.emitter.on("updateApplications", () => this.updateApplications());
    this.emitter.on("updateEnvironments", () => this.updateEnvironments());
  },
  methods: {
    updateSnapshot() {
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
    },
    updateApplications() {
      this.$http.get(`/customers/${this.customerId}/applications`)
        .then(res => {
          useStore().applications = Object.fromEntries(res.data.map(obj => {
            return [obj.id, obj];
          }));
        });
    },
    updateEnvironments() {
      this.$http.get(`/customers/${this.customerId}/environments`)
        .then(res => {
          useStore().environments = Object.fromEntries(res.data.map(obj => {
            return [obj.id, obj];
          }));
        });
    },
    updateGithubMappings() {
      this.$http.get(`/customers/${this.customerId}/mappings`)
        .then(res => {
          res.data.sort(function (a, b) {
            if (a.basePackage > b.basePackage) {
              return -1;
            }
            if (a.basePackage < b.basePackage) {
              return 1;
            }

            return 0;
          });
          useStore().githubMappings = res.data;
        });
    },
    navigateCustomer(path) {
      window.location.href = path;
    }
  }
};

</script>
