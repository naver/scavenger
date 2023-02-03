<template>
  <div class="content">
    <el-row>
      <el-col :span="20">
        <h1 style="display: inline-block">{{ $t("message.manage.github.title") }}</h1>
        <GithubMappingForm :customerId="customerId"/>
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
          <el-table :data="filterGithubMappings()" header-row-class-name="table-header" border stripe
                    class="fixed-table-area" highlight-current-row table-layout="auto">
            <el-table-column prop="basePackage" :label="$t('message.manage.github.package')" width="400"
                             header-align="center" show-overflow-tooltip/>
            <el-table-column prop="url" label="URL" header-align="center" show-overflow-tooltip/>
            <el-table-column :label="$t('message.common.delete')" width="70" align="center">
              <template #default="scope">
                <el-button circle type="danger" @click="deleteGithubMapping(scope.row.id)">
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
import GithubMappingForm from "./GithubMappingForm.vue";
import {useStore} from "../util/store";
import {ElMessageBox, ElNotification} from "element-plus";

export default {
  components: {GithubMappingForm},
  props: ["customerId"],
  data() {
    return {
      SEARCH_FIELD: ["basePackage", "url"],
      keyword: "",
    };
  },
  watch: {
    customerId() {
      this.getGithubMappings();
    },
    keyword() {
      this.filterGithubMappings();
    }
  },
  mounted() {
    if (this.customerId !== undefined && this.customerId !== "") {
      this.getGithubMappings();
    }
  },
  methods: {
    getGithubMappings() {
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
    filterGithubMappings() {
      const keywordRegExp = new RegExp(this.keyword, "i");
      return Object.values(useStore().githubMappings)
        .slice(0)
        .filter((it) => this.SEARCH_FIELD.some(value => keywordRegExp.test(it[value])))
        .sort((a, b) => (b.basePackage - a.basePackage))
        .map((it) => ({
          "basePackage": it.basePackage,
          "url": it.url,
          "id": it.id
        }));
    },
    deleteGithubMapping(mappingId) {
      ElMessageBox.confirm(
        this.$t("message.manage.github.delete-title"),
        "Warning",
        {
          confirmButtonText: this.$t("message.common.yes"),
          cancelButtonText: this.$t("message.common.no"),
          type: 'warning',
        })
        .then(() => {
          this.$http.delete(`/customers/${this.customerId}/mappings/${mappingId}`)
            .then(() => {
              ElNotification.success({message: this.$t("message.manage.github.delete-success")});
              useStore().githubMappings = useStore().githubMappings.filter(it => it.id !== mappingId);
            });
        });
    },
  }
};
</script>
