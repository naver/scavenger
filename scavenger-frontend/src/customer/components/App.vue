<template>
  <el-scrollbar>
    <div class="common-layout">
      <el-container>
        <el-header height="250px">
          <div class="banner">
            <div class="caption">
              <h3>Scavenger</h3>
              <p>Dead Code Analysis Tool</p>
            </div>
            <el-link href="https://github.com/naver/scavenger/blob/develop/doc/user-guide.md" :underline="false" target="_blank">
              <font-awesome-icon icon="fa-solid fa-book"/>&nbsp;Learn more
            </el-link>
          </div>
        </el-header>
        <el-main>
          <el-row class="title-area">
            <el-col :span="18">
              <h1 style="display: inline-block">{{ $t("message.customer.title") }}</h1>
              <el-tag type="warning" round effect="dark">{{ filterCustomers().length }}</el-tag>
            </el-col>
            <el-col :span="6">
              <div class="search-box">
                <el-input v-model="keyword">
                  <template #prefix>
                    <font-awesome-icon icon="fa-solid fa-magnifying-glass"/>
                  </template>
                </el-input>
              </div>
            </el-col>
          </el-row>
          <el-row class="card-area" v-show="!empty && paginateCustomers().length !== 0">
            <el-space wrap size="large" alignment="normal">
              <template v-for="customer in paginateCustomers()" :key="customer.id">
                <el-card class="customer-card">
                  <template #header>
                    <a :href="`/scavenger/customers/${customer.name}`">
                      <h1 class="customer-name">{{ customer.name }}</h1>
                    </a>
                  </template>
                  <dl>
                    <dt>ID</dt>
                    <dd> {{ customer.id }}</dd>
                  </dl>
                  <el-row>
                    <el-col :span="18"></el-col>
                    <el-col :span="6">
                      <el-button round type="danger" size="small"
                                 @click="deleteCustomer(customer.id, customer.name)">
                        <font-awesome-icon icon="fa-solid fa-trash-can"/>&nbsp;{{ $t("message.common.delete") }}
                      </el-button>
                    </el-col>
                  </el-row>
                </el-card>
              </template>
            </el-space>
          </el-row>
          <el-row class="empty-card-area" v-show="empty || paginateCustomers().length === 0">
            <el-result v-show="!empty && paginateCustomers().length === 0" icon="info"
                       :title="$t('message.customer.no-result')"/>
            <el-result v-show="empty" icon="info" :title="$t('message.customer.empty')">
              <template #sub-title>
                <p>{{ $t("message.customer.empty-help") }}</p>
              </template>
            </el-result>
          </el-row>
          <el-row class="footer-area">
            <el-col :span="19">
              <el-button color="#be714f" :dark="true" @click="dialogTableVisible = true" class="create-button">
                <font-awesome-icon icon="fa-solid fa-plus"/>&nbsp;{{ $t("message.customer.customer-create") }}
              </el-button>
              <el-dialog v-model="dialogTableVisible" class="dialog" align-center width="30%"
                         :show-close="false" @open="init()">
                <template #header="{ close, titleId, titleClass }">
                  <div class="dialog-header">
                    <h4 :id="titleId" :class="titleClass">
                      {{ $t("message.customer.customer-create") }}
                    </h4>
                    <el-button text @click="close">
                      <font-awesome-icon icon="fa-solid fa-xmark"/>
                    </el-button>
                  </div>
                </template>
                <el-form label-position="left" :model="configuration" label-width="auto" ref="scavengerForm"
                         size="small" :rules="rules">
                  <el-form-item :label="$t('message.common.name')" :prop="'name'">
                    <el-input v-model="configuration.name" @keydown.enter.prevent="createCustomer('scavengerForm')"/>
                  </el-form-item>
                  <div class="form-submit">
                    <el-form-item size="default">
                      <el-button type="primary" @click="createCustomer('scavengerForm')">
                        {{ $t("message.common.create") }}
                      </el-button>
                      <el-button @click="dialogTableVisible = false">
                        {{ $t("message.common.cancel") }}
                      </el-button>
                    </el-form-item>
                  </div>
                </el-form>
              </el-dialog>
            </el-col>
            <el-col :span="5" class="pagination">
              <el-pagination layout="total, prev, pager, next" :total="filterCustomers().length"
                             v-model:page-size="pageSize"
                             v-model:current-page="currentPage"/>
            </el-col>
          </el-row>
        </el-main>
      </el-container>
    </div>
  </el-scrollbar>
</template>
<script>
import {ElMessageBox, ElNotification} from "element-plus";

export default {
  data() {
    return {
      SEARCH_FIELD: ["id", "name"],
      keyword: "",
      customers: [],
      empty: false,
      currentPage: 1,
      pageSize: 8,
      dialogTableVisible: false,
      configuration: {
        name: "",
      },
      rules: {
          name: [{ required: true, message: 'Please input workspace name', trigger: 'blur' }]
      },
    };
  },
  created() {
    this.$http.get("/customers")
      .then(res => {
        this.customers = res.data;
        if (res.data.length === 0) {
          this.empty = true;
        }
      });
  },
  methods: {
    init() {
      this.configuration.name = "";
    },
    filterCustomers() {
      const keywordRegExp = new RegExp(this.keyword, "i");
      return this.customers
        .filter((it) => this.SEARCH_FIELD.some(value => keywordRegExp.test(it[value])))
        .sort((a, b) => (b.id - a.id));
    },
    paginateCustomers() {
      return this.filterCustomers()
        .slice((this.currentPage - 1) * this.pageSize, this.currentPage * this.pageSize);
    },
    deleteCustomer(id, name) {
      const regexp = new RegExp(name);
      ElMessageBox.prompt(
        this.$t("message.customer.delete-title", [name]),
        "Warning",
        {
          confirmButtonText: this.$t("message.common.yes"),
          cancelButtonText: this.$t("message.common.no"),
          type: "warning",
          inputPattern: regexp,
          inputErrorMessage: "name is invalid.",
          inputPlaceholder: `${name}를 삭제하시려면 ${name}을(를) 입력하시기 바랍니다`,
        })
        .then(() => {
          this.$http.delete(`/customers/${id}`)
            .then(() => {
              ElNotification.success({message: this.$t("message.customer.delete-success")});
              this.customers = this.customers.filter(it => it.id !== id);
            }).catch(() => ElNotification.error({message: this.$t("message.customer.delete-fail")}))
        });
    },
    createCustomer(form) {
      if (!form) return;
      this.$refs[form].validate((valid) => {
        if (!valid) return;
        const params = {
          name: this.configuration.name,
        };

        this.$http.post("/customers", params)
          .then(res => {
            this.customers = [...this.customers, res.data];
            ElNotification.success({message: this.$t("message.customer.create-success")});
          })
          .catch(error => {
            if (error.response.status === 409) {
              ElNotification.error({message: this.$t("message.customer.duplicated")});
            } else {
              ElNotification.error({message: this.$t("message.common.create-fail")});
            }
          });
        this.dialogTableVisible = false;
        this.empty = false;
      })
    },
  },
};
</script>
