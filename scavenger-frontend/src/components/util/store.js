import {defineStore} from "pinia";

export const useStore = defineStore('store', {
  state: () => {
    return {
      snapshotLimit: 0,
      selectedSnapshot: {},
      snapshots: [],
      snapshotIndex: {
        "": {
          signature: "",
          children: []
        }
      },
      githubMappings: [],
      applications: null,
      environments: null,
    }
  },
})
