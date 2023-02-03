<template>
  <div class="treemap-chart-area">
    <div class="current-node-area">
      <span></span>
      <span v-if="snapshotData.signature">
        {{ snapshotData.signature }}
        <a @click="onClipboard" class="function-box">
          <font-awesome-icon icon="fa-solid fa-copy"/>
        </a>
        <a v-if="isPackage()" @click="onLink" class="function-box">
          <font-awesome-icon icon="fa-solid fa-pen-to-square"/>
        </a>
        <a v-if="githubLink(snapshotData)" :href="githubLink(snapshotData)" class="function-box">
          <font-awesome-icon icon="fa-brands fa-github"/>
        </a>
      </span>
      <span v-else>
        {{ snapshot.name }}
      </span>
      <span>
      </span>
    </div>
    <treemap-chart :data="chartData" :options="chartOptions" @selectSeries="onSelectSeries" ref="tuiChart"
                   style="width: calc(100% + 40px); height: calc(100% + 34px);
                   position: relative; top: -44px; left: -10px"
    />
  </div>
</template>
<script>
import TreemapChart from "./chart/TreemapChart.vue";
import {openLink, toPercentageStr} from "../util/util";
import copy from "copy-to-clipboard";
import {ElNotification} from "element-plus";

export default {
  components: {TreemapChart},
  props: ["updateSnapshotData", "snapshotData", "snapshot", "isPackage", "githubLink"],
  data() {
    return {
      objectColorMap: {},
    };
  },
  computed: {
    chartData() {
      const snapshotData = this.snapshotData;

      return {
        series: snapshotData.children.map(child => {
          this.objectColorMap[child.abbreviatedLabel] = child.percentage;

          return {
            label: child.abbreviatedLabel,
            data: child.totalCount,

            lastInvokedAtMillis: child.lastInvokedAtMillis,
            signature: child.signature,
            colorValue: this.objectColorMap[child.abbreviatedLabel]
          };
        })
      };
    },
    title() {
      const found = this.snapshots.find(obj => obj.id === parseInt(this.$route.params.snapshotId));
      if (found) {
        return found.name;
      }
      return null
    },
    chartOptions() {
      return {
        chart: {
          width: "auto",
          height: "auto",
        },
        tooltip: {
          formatter: (value, value2) => {
            const colorValue = this.objectColorMap[value2.label];
            if (!isNaN(colorValue)) {
              return `사용률 : ${toPercentageStr(colorValue)
                .toString()}%`;
            }
            return "Calculating";
          },
        },
        series: {
          dataLabels: {
            visible: true,
          },
          zoomable: true,
          selectable: true,
          useColorValue: true
        },
        legend: {
          visible: false,
          align: "top"
        },
        exportMenu: {visible: false},
        theme: {
          series: {
            startColor: "#000000",
            endColor: "#03c75a",
            dataLabels: {
              fontSize: 11
            }
          },
        }
      };
    },
  },
  methods: {
    onSelectSeries(e) {
      let label = e.treemap[0].label;
      this.updateSnapshotData(concatPackages(this.snapshotData.signature, label));
    },
    onClipboard() {
      const target = this.snapshotData.signature;
      copy(target);
      ElNotification.info({message: `${target} copied.`});
    },
    onLink() {
      openLink(this.snapshotData.signature)
        .catch(err => {
          if (err.response.status === 404) {
            ElNotification.warning({
              dangerouslyUseHTMLString: true,
              message: this.$t("message.snapshot.detail.open-idea-fail")
            });
          }
        });
    },

  },
};

function concatPackages(first, second) {
  if (!first) {
    return second;
  } else {
    return `${first}.${second}`;
  }
}
</script>
