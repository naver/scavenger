import "./customer.less";
import {createApp} from "vue";
import App from "./components/App.vue";
import {library as iconRegister} from "@fortawesome/fontawesome-svg-core";
import {faBook, faMagnifyingGlass, faTrashCan, faPlus, faXmark} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import axios from "axios";
import "element-plus/dist/index.css";
import {createI18n} from "vue-i18n";
import messages from "../message.js";
import {userLocale} from "@/components/util/locale";

const app = createApp(App);

iconRegister.add(faBook, faMagnifyingGlass, faTrashCan, faPlus, faXmark);
app.component("FontAwesomeIcon", FontAwesomeIcon);

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

const i18n = createI18n({
  locale: userLocale(),
  fallbackLocale: "en",
  messages
});

app.use(i18n);
app.mount("#app");
