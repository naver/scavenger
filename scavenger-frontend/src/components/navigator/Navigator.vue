<template>
  <el-scrollbar>
    <el-menu
      active-text-color="#fff"
      background-color="#333"
      class="el-menu-vertical"
      text-color="#aaa"
      :router="true"
      :default-openeds="state.defaultOpen"
      :default-active="state.currentPath"
      @open="$router.push(snapshotLink)"
    >
      <el-menu-item :index="indexLink">
        <template #title>
          <font-awesome-icon icon="fa-solid fa-chart-column"/>
          <a>{{ $t("message.navigator.dashboard") }}</a>
        </template>
      </el-menu-item>
      <el-sub-menu index="snapshot" :class="{'is-active': activeSubMenu}">
        <template #title>
          <font-awesome-icon icon="fa-solid fa-folder"/>
          <a>{{ $t("message.navigator.snapshot", [snapshots.length, snapshotLimit]) }}</a>
        </template>
        <template v-for="snapshot in snapshots" :key="snapshot.id">
          <el-menu-item :index="`${snapshotLink}/${snapshot.id}`" class="sub-menu">
            {{ snapshot.name ? snapshot.name : snapshot.id }}
          </el-menu-item>
        </template>
      </el-sub-menu>
      <el-menu-item :index="manageLink">
        <template #title>
          <font-awesome-icon icon="fa-solid fa-gear"/>
          <a>{{ $t("message.navigator.manage") }}</a>
        </template>
      </el-menu-item>
    </el-menu>
  </el-scrollbar>
</template>

<script>
import {useStore} from "../util/store";

export default {
  props: ["customerId"],
  data() {
    return {
      indexLink: "",
      manageLink: "",
      snapshotLink: "",
      activeSubMenu: false,
      state: {
        defaultOpen: ["snapshot"],
        currentPath: "/",
      }
    };
  },
  computed: {
    snapshots() {
      return useStore().snapshots;
    },
    snapshotLimit() {
      return useStore().snapshotLimit;
    }
  },
  watch: {
    customerId() {
      this.getSnapshotLimit();
    },
  },
  created() {
    this.$watch(
      () => this.$route.params,
      () => {
        if (this.$route.params.customerId !== undefined) {
          this.indexLink = `/scavenger/customers/${this.$route.params.customerId}`;
          this.manageLink = `/scavenger/customers/${this.$route.params.customerId}/manage`;
          this.snapshotLink = `/scavenger/customers/${this.$route.params.customerId}/snapshots`;
        }
        this.state.currentPath = this.$route.path;
        this.activeSubMenu = ["snapshots", "snapshot"].includes(this.$route.name);
      },
      {immediate: true}
    )
  },
  mounted() {
    if (this.customerId !== undefined && this.customerId !== "") {
      this.getSnapshotLimit();
    }
  },
  methods: {
    getSnapshotLimit() {
      this.$http.get(`/customers/${this.customerId}/summary`)
        .then(res => {
          useStore().snapshotLimit = res.data.snapshotLimit;
        });
    },
  },
};
</script>
