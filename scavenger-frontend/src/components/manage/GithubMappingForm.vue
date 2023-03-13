<template>
  <el-button plain style="margin-left: 10px; margin-bottom: 4px;" @click="dialogTableVisible = true">
    <font-awesome-icon icon="fa-solid fa-plus"/>&nbsp;{{ $t("message.manage.github.create") }}
  </el-button>
  <el-dialog v-model="dialogTableVisible" class="dialog" align-center width="40%" :show-close="false" @open="init()">
    <template #header="{ close, titleId, titleClass }">
      <div class="dialog-header">
        <h4 :id="titleId" :class="titleClass">{{ $t("message.manage.github.create-title") }}</h4>
        <el-button text @click="close">
          <font-awesome-icon icon="fa-solid fa-xmark"/>
        </el-button>
      </div>
    </template>
    <el-form label-position="left" :model="configuration" label-width="auto" ref="scavengerForm" size="small"
             require-asterisk-position="right">
      <el-form-item :label="$t('message.manage.github.package')" :prop="'packages'" :rules="validate('packages')">
        <el-input v-model="configuration.packages" placeholder="com.example"/>
      </el-form-item>
      <el-form-item label="URL" :prop="'url'" :rules="validate('url')">
        <el-input v-model="configuration.url" placeholder="GITHUB_URL/tree/develop/src/main/java/com/example"/>
      </el-form-item>
      <div class="form-submit">
        <el-form-item size="default">
          <el-button type="primary" @click="clickSave('scavengerForm')">{{ $t("message.common.create") }}</el-button>
          <el-button @click="dialogTableVisible = false">{{ $t("message.common.cancel") }}</el-button>
        </el-form-item>
      </div>
    </el-form>
  </el-dialog>
</template>
<script>
import "../util/util.js"
import {useStore} from "../util/store.js";
import {ElNotification} from "element-plus";

export default {
  props: ["customerId"],
  data() {
    return {
      dialogTableVisible: false,
      configuration: {
        packages: "",
        url: "",
      }
    }
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      this.configuration.packages = "";
      this.configuration.url = "";
    },
    validate(key) {
      return [{required: true, message: `Please input ${key}`, trigger: 'blur'}]
    },
    clickSave() {
      const params = {
        basePackage: this.configuration.packages,
        url: this.configuration.url
      }

      if (params.basePackage.length === 0 || params.url.length === 0) {
        ElNotification.error({message: this.$t("message.manage.github.validation")});
        return;
      }

      ElNotification.info({message: this.$t("message.manage.github.creating")});
      this.createMapping(params);
      this.dialogTableVisible = false;
    },
    createMapping(params) {
      this.$http.post(`/customers/${this.customerId}/mappings`, params)
        .then(res => {
          const newItem = {
            "id": res.data.id,
            "basePackage": res.data.basePackage,
            "url": res.data.url
          };
          useStore().githubMappings = [...useStore().githubMappings, newItem].sort(function (a, b) {
            if (a.basePackage > b.basePackage) {
              return -1;
            }
            if (a.basePackage < b.basePackage) {
              return 1;
            }
            return 0;
          });
          ElNotification.success({message: this.$t("message.manage.github.create-success")});
        })
        .catch(error => {
          if (error.response.status === 409) {
            ElNotification.error({message: this.$t("message.manage.github.duplicated")});
          } else {
            ElNotification.error({message: this.$t("message.common.create-fail")});
          }
        });
    },
  },
};
</script>
