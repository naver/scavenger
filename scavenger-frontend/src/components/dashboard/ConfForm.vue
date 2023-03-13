<template>
  <el-button plain type="info" @click="dialogTableVisible = true">
    <font-awesome-icon icon="fa-solid fa-floppy-disk"/>&nbsp;{{ $t("message.dashboard.configuration.create") }}
  </el-button>
  <el-dialog v-model="dialogTableVisible" class="dialog" align-center width="40%" :show-close="false" @open="init()">
    <template #header="{ close, titleId, titleClass }">
      <div class="dialog-header">
        <h4 :id="titleId" :class="titleClass">
          {{ $t("message.dashboard.configuration.title") }}
          <el-link :underline="false" href="https://github.com/naver/scavenger/blob/develop/doc/user-guide.md#create-a-configuration-file" target="_blank">
            <font-awesome-icon icon="fa-solid fa-circle-question"/>
          </el-link>
        </h4>
        <el-button text @click="close">
          <font-awesome-icon icon="fa-solid fa-xmark"/>
        </el-button>
      </div>
    </template>
    <el-form label-position="left" :model="configuration" label-width="auto" size="small" ref="scavengerForm"
             require-asterisk-position="right">
      <el-form-item label="serverUrl">
        <el-input v-model="configuration.serverUrl" disabled/>
      </el-form-item>
      <el-form-item label="apiKey">
        <el-input v-model="configuration.apiKey" disabled/>
      </el-form-item>
      <el-form-item label="appName" :prop="'appName'" :rules="validate('appName')">
        <el-input v-model="configuration.appName"/>
      </el-form-item>
      <el-form-item :label="$t('message.dashboard.configuration.packages')" :prop="'packages'"
                    :rules="validate('packages')">
        <el-input v-model="configuration.packages" placeholder="com.navercorp.foo, com.navercorp.bar"/>
      </el-form-item>
      <el-form-item :label="$t('message.dashboard.configuration.codebase')">
        <el-input v-model="configuration.codeBase" :placeholder="CODEBASE_PLACEHOLDER"/>
      </el-form-item>
      <el-form-item :label="$t('message.dashboard.configuration.annotations')">
        <el-input v-model="configuration.annotations" :placeholder="ANNOTATIONS_PLACEHOLDER"/>
      </el-form-item>
      <el-form-item :label="$t('message.dashboard.configuration.method-visibility')">
        <el-radio-group v-model="configuration.methodVisibility">
          <el-radio label="public"/>
          <el-radio label="protected"/>
          <el-radio label="package-private"/>
          <el-radio label="private"/>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="$t('message.dashboard.configuration.exclude-constructors')">
        <el-checkbox v-model="configuration.excludeConstructors"
                     :label="$t('message.dashboard.configuration.exclude-constructors-checkbox')"/>
      </el-form-item>
      <el-form-item :label="$t('message.dashboard.configuration.exclude-getter-setter')">
        <el-checkbox v-model="configuration.excludeGetterSetter"
                     :label="$t('message.dashboard.configuration.exclude-getter-setter-checkbox')"/>
      </el-form-item>
      <el-form-item :label="$t('message.dashboard.configuration.environment')">
        <el-input v-model="configuration.environment" placeholder="dev"/>
      </el-form-item>
      <el-form-item :label="$t('message.dashboard.configuration.app-version')">
        <el-input v-model="configuration.appVersion" placeholder="0.0.1"/>
      </el-form-item>
      <el-form-item :label="$t('message.dashboard.configuration.exclude-packages')">
        <el-input v-model="configuration.excludePackages" placeholder="com.navercorp.baz,com.navercorp.foo"/>
      </el-form-item>
      <el-form-item :label="$t('message.dashboard.configuration.async-codebase-scan-mode')">
        <el-checkbox v-model="configuration.asyncCodeBaseScanMode"
                     :label="$t('message.dashboard.configuration.async-codebase-scan-mode-checkbox')"/>
      </el-form-item>
      <div class="form-submit">
        <el-form-item size="default">
          <el-button type="primary" @click="clickSave('scavengerForm')">
            {{ $t("message.dashboard.configuration.download") }}
          </el-button>
          <el-button @click="dialogTableVisible = false">{{ $t("message.common.cancel") }}</el-button>
        </el-form-item>
      </div>
    </el-form>
  </el-dialog>
</template>
<script>
import "../util/util.js"

export default {
  props: ["summary"],
  data() {
    return {
      configuration: {
        serverUrl: this.summary.collectorServerUrl,
        apiKey: this.summary.licenseKey,
        appName: this.$route.params.customerId,
        packages: "",
        codeBase: "",
        annotations: "",
        methodVisibility: "",
        excludeConstructors: true,
        excludeGetterSetter: false,
        environment: "",
        appVersion: "",
        excludePackages: "",
        asyncCodeBaseScanMode: false,
      },
      appName: this.$route.params.customerId,
      dialogTableVisible: false,
      CODEBASE_PLACEHOLDER: "/home1/irteam/service/tomcat/webapps/ROOT/WEB-INF, /home1/irteam/apps/tomcat/lib",
      ANNOTATIONS_PLACEHOLDER: "@org.springframework.stereotype.Controller, @org.springframework.stereotype.Service",
    };
  },
  watch: {
    summary() {
      this.configuration.serverUrl = this.summary.collectorServerUrl;
      this.configuration.apiKey = this.summary.licenseKey;
    },
  },
  mounted() {
    this.configuration.serverUrl = this.summary.collectorServerUrl;
    this.configuration.apiKey = this.summary.licenseKey;
  },
  methods: {
    init() {
      this.configuration.serverUrl = this.summary.collectorServerUrl;
      this.configuration.apiKey = this.summary.licenseKey;
      this.configuration.appName = this.$route.params.customerId;
      this.configuration.packages = "";
      this.configuration.codeBase = "";
      this.configuration.annotations = "";
      this.configuration.methodVisibility = "";
      this.configuration.excludeConstructors = true;
      this.configuration.excludeGetterSetter = false;
      this.configuration.environment = "";
      this.configuration.appVersion = "";
      this.configuration.excludePackages = "";
      this.configuration.asyncCodeBaseScanMode = false;
    },
    validate(key) {
      return [{required: true, message: `Please input ${key}`, trigger: 'blur'}]
    },
    clickSave(form) {
      this.$refs[form].validate((valid) => {
        if (valid) {
          this.download();
          this.dialogTableVisible = false;
        } else {
          return false;
        }
      });
    },
    download() {
      let confStr = "";
      Object.entries(this.configuration)
        .forEach(([key, value]) => {
          if (value) {
            confStr += `${key} = ${value}\n`;
          }
        });

      let element = document.createElement("a");
      element.setAttribute("href", "data:text/plain;charset=utf-8," + encodeURIComponent(confStr));
      element.setAttribute("download", "scavenger.conf");

      element.style.display = "none";
      document.body.appendChild(element);

      element.click();

      document.body.removeChild(element);
    }
  },
};
</script>
