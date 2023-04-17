import "./scavenger.less";
import {createApp} from "vue";
import {createRouter, createWebHistory} from "vue-router";
import axios from "axios";
import mitt from "mitt";
import {createPinia} from "pinia";
import {library as iconRegister} from "@fortawesome/fontawesome-svg-core";
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import {
  faFloppyDisk,
  faXmark,
  faQuestionCircle,
  faMagnifyingGlass,
  faChartColumn,
  faGear,
  faFolder,
  faPlus,
  faArrowsRotate,
  faTrashCan,
  faPenToSquare,
  faCopy,
  faDesktop,
  faCaretDown,
  faFolderTree,
  faEllipsisVertical,
  faFileExport
} from "@fortawesome/free-solid-svg-icons";
import {faGithub} from "@fortawesome/free-brands-svg-icons";
import "element-plus/dist/index.css";
import App from "./components/App.vue";
import Dashboard from "./components/dashboard/Dashboard.vue";
import SnapshotDetail from "./components/snapshot/SnapshotDetail.vue";
import Manage from "./components/manage/Manage.vue";
import SnapshotTable from "./components/snapshot/SnapshotTable.vue";
import {createI18n} from "vue-i18n";
import messages from "./message.js";
import {userLocale} from "./components/util/locale";

const app = createApp(App);

iconRegister.add(
  faFloppyDisk,
  faXmark,
  faQuestionCircle,
  faMagnifyingGlass,
  faChartColumn, faGear,
  faFolder,
  faPlus,
  faArrowsRotate,
  faTrashCan,
  faPenToSquare,
  faCopy,
  faGithub,
  faDesktop,
  faCaretDown,
  faFolderTree,
  faEllipsisVertical,
  faFileExport,
);
app.component("FontAwesomeIcon", FontAwesomeIcon);

const routes = [
  {
    path: "/scavenger/customers/:customerId",
    component: Dashboard,
    name: "dashboard"
  },
  {
    path: "/scavenger/customers/:customerId/snapshots",
    component: SnapshotTable,
    name: "snapshots"
  },
  {
    path: "/scavenger/customers/:customerId/manage",
    component: Manage,
    name: "manage"
  },
  {
    path: "/scavenger/customers/:customerId/snapshots/:snapshotId",
    component: SnapshotDetail,
    name: "snapshot"
  }
];
const router = createRouter({
  history: createWebHistory(),
  routes,
});
app.use(router);

axios.interceptors.request.use(config => {
  if (typeof config.params === "undefined") {
    config.params = {};
  }
  if (typeof config.params === "object") {
    if (typeof URLSearchParams === "function" && config.params instanceof URLSearchParams) {
      config.params.append("_", Date.now().toString());
    } else {
      config.params._ = Date.now();
    }
  }
  return config;
});
axios.defaults.baseURL = "/scavenger/api";
app.config.globalProperties.$http = axios;

app.config.globalProperties.emitter = mitt();

const pinia = createPinia();
app.use(pinia);

const i18n = createI18n({
  locale: userLocale(),
  fallbackLocale: "en",
  messages
});
app.use(i18n);
app.mount("#app");
