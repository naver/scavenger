<template>
  <el-dialog :model-value="dialogTableVisible" class="dialog" align-center width="40%" :show-close="false"
             @open="init()" :before-close="hideDialogTableVisible">
    <template #header="{ titleId, titleClass }">
      <div class="dialog-header">
        <h4 :id="titleId" :class="titleClass">
          {{
            Object.keys(this.selectedSnapshot).length !== 0 ?
              $t("message.snapshot.form.edit-title", [this.selectedSnapshot.id]) :
              $t("message.snapshot.form.create-title")
          }}
          <el-link :underline="false" href="https://github.com/naver/scavenger/blob/develop/doc/user-guide.md#create-snapshot" target="_blank">
            <font-awesome-icon icon="fa-solid fa-circle-question"/>
          </el-link>
        </h4>
        <el-button text @click="hideDialogTableVisible()">
          <font-awesome-icon icon="fa-solid fa-xmark"/>
        </el-button>
      </div>
    </template>
    <el-form label-position="left" :model="configuration" label-width="auto" ref="scavengerForm" size="small"
             require-asterisk-position="right">
      <el-form-item :label="$t('message.common.name')" :prop="'name'" :rules="validateInput('name', 'input')">
        <el-input v-model="configuration.name"/>
      </el-form-item>
      <el-form-item :label="$t('message.common.application')" :prop="'applications'"
                    :rules="validateCheckBox('application')">
        <el-checkbox-group v-model="configuration.applications">
          <template v-for="application in applications" :key="application.id">
            <el-checkbox :label="application.name"/>
          </template>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item :label="$t('message.common.environment')" :prop="'environments'"
                    :rules="validateCheckBox('environment')">
        <el-checkbox-group v-model="configuration.environments">
          <template v-for="environment in environments" :key="environment.id">
            <el-checkbox :label="environment.name"/>
          </template>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item :label="$t('message.snapshot.packages')">
        <el-input v-model="configuration.packages" :placeholder="'com.navercorp.**, **.*Controller.**'"/>
      </el-form-item>
      <div class="date-time-picker">
        <el-form-item :label="$t('message.snapshot.filter-invoked-at-millis')">
          <template v-if="this.$i18n.locale === 'ko'">
            <el-col :span="8">
              <el-date-picker
                v-model="configuration.date"
                type="datetime"
                placeholder="Select date and time"
                style="width: 100%"
              />
            </el-col>
            <el-col :span="15">
              {{ $t("message.snapshot.form.filter-invoked-at-millis-description") }}
            </el-col>
          </template>
          <template v-else>
            <el-col :span="4">
              {{ $t("message.snapshot.form.filter-invoked-at-millis-description") }}
            </el-col>
            <el-col :span="8">
              <el-date-picker
                v-model="configuration.date"
                type="datetime"
                placeholder="Select date and time"
                style="width: 100%"
              />
            </el-col>
          </template>
        </el-form-item>
      </div>
      <div class="form-submit">
        <el-form-item size="default">
          <el-button type="primary" @click="clickSave('scavengerForm')">
            {{
              Object.keys(this.selectedSnapshot).length !== 0 ?
                $t("message.snapshot.edit") : $t("message.common.create")
            }}
          </el-button>
          <el-button @click="hideDialogTableVisible()">{{ $t("message.common.cancel") }}</el-button>
        </el-form-item>
      </div>
    </el-form>
  </el-dialog>
</template>
<script>
import Moment from "moment";
import {useStore} from "../util/store";
import "../util/util.js";
import {ElNotification} from "element-plus";

export default {
  props: ["customerId", "dialogTableVisible"],
  data() {
    return {
      configuration: {
        name: "",
        applications: [],
        environments: [],
        packages: "",
        date: null,
      },
    };
  },
  computed: {
    applications() {
      return useStore().applications;
    },
    environments() {
      return useStore().environments;
    },
    selectedSnapshot() {
      return useStore().selectedSnapshot;
    },
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      this.configuration.name = "";
      this.configuration.packages = "";
      this.configuration.date = "";
      if (useStore().applications !== null && Object.keys(useStore().applications).length !== 0) {
        this.configuration.applications = Object.values(useStore().applications).map(it => it.name);
      }
      if (useStore().environments !== null && Object.keys(useStore().environments).length !== 0) {
        this.configuration.environments = Object.values(useStore().environments).map(it => it.name);
      }

      if (Object.keys(useStore().selectedSnapshot).length !== 0) {
        this.configuration.name = useStore().selectedSnapshot.name;
        this.configuration.packages = useStore().selectedSnapshot.packages;
        this.configuration.applications = Object.values(useStore().applications)
          .filter(it => useStore().selectedSnapshot.applications.includes(it.id))
          .map(it => it.name);
        this.configuration.environments = Object.values(useStore().environments)
          .filter(it => useStore().selectedSnapshot.environments.includes(it.id))
          .map(it => it.name);
        if (useStore().selectedSnapshot.filterInvokedAtMillis > 0) {
          this.configuration.date =
            new Moment(useStore().selectedSnapshot.filterInvokedAtMillis).format("YYYY-MM-DDTHH:mm");
        }
      }
    },
    validateInput(key) {
      return [{required: true, message: `Please input ${key}`, trigger: "blur"}]
    },
    validateCheckBox(key) {
      return [{type: "array", required: true, message: `Please select at least one ${key} type`, trigger: 'change'}]
    },
    clickSave() {
      const params = {
        applicationIdList: Object.values(useStore().applications)
          .filter(it => this.configuration.applications.includes(it.name))
          .map(it => it.id),
        environmentIdList: Object.values(useStore().environments)
          .filter(it => this.configuration.environments.includes(it.name))
          .map(it => it.id),
        filterInvokedAtMillis: 0,
        packages: this.configuration.packages,
        name: this.configuration.name
      };

      if (this.configuration.date !== null && this.configuration.date !== "") {
        params.filterInvokedAtMillis = new Moment(this.configuration.date).unix() * 1000;
      }

      if (params.applicationIdList.length === 0 ||
        params.environmentIdList.length === 0 ||
        params.name === "") {
        ElNotification.error({message: this.$t("message.snapshot.form.validation")});
        return;
      }

      if (Object.keys(useStore().selectedSnapshot).length !== 0) {
        ElNotification.info({message: this.$t("message.snapshot.form.update-start")});
        this.editSnapshot(params);
      } else {
        ElNotification.info({message: this.$t("message.snapshot.form.create-start")});
        this.createSnapshot(params);
      }
      this.hideDialogTableVisible();
    },
    createSnapshot(params) {
      this.$http.post(`/customers/${this.customerId}/snapshots`, params)
        .then(() => {
          this.emitter.emit("updateSnapshot");
          ElNotification.success({message: this.$t("message.snapshot.form.create-success")});
        })
        .catch(error => {
            if (error.response.status === 422) {
              ElNotification.error({message: this.$t("message.snapshot.form.limit")});
              this.emitter.emit("updateSnapshot");
            } else {
              ElNotification.error({message: this.$t("message.common.create-fail")});
            }
          }
        );
    },
    editSnapshot(params) {
      this.$http.put(`/customers/${this.customerId}/snapshots/${useStore().selectedSnapshot.id}`, params)
        .then(() => {
          this.emitter.emit("updateSnapshot");
          ElNotification.success({message: this.$t("message.snapshot.form.update-success")});
        })
        .catch(() => {
          ElNotification.error({message: this.$t("message.snapshot.form.update-fail")});
        });
    },
    hideDialogTableVisible() {
      this.emitter.emit("hideDialogTableVisible");
    }
  }
};
</script>
