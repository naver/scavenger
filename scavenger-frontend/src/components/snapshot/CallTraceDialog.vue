<template>
  <el-dialog v-model="dialogTableVisible" class="dialog" align-center width="40%" :show-close="false">
    <template #header="{ close, titleId, titleClass }">
      <div class="dialog-header">
        <h4 :id="titleId" :class="titleClass">Caller Methods List</h4>
        <el-button text @click="close">
          <font-awesome-icon icon="fa-solid fa-xmark"/>
        </el-button>
      </div>
    </template>
    <div>
      <font-awesome-icon icon="fa-solid fa-bars" style="font-size: small; margin-right: 5px;"/>
      <span style="font-size: small; color: gray">{{ callee }}</span>
    </div>
    <el-table :data="callers" style="margin-top: 20px; width: 100%">
      <el-table-column label="signature">
        <template #default="scope">
          <div>
            <span>{{ scope.row }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column align="right">
        <template #default="scope">
          <el-button size="small" @click="showCaller(scope.row)">
              <font-awesome-icon icon="fa-solid fa-magnifying-glass"/>
          </el-button>
          <el-button size="small" @click="moveSignature(scope.row)">
            <font-awesome-icon icon="fa-solid fa-up-right-from-square"/>
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>
<script>
export default {
  props: ["dialogTableVisible", "callee", "callers"],
  methods: {
    moveSignature(signature) {
      const classPathIndex = signature.includes("$") ? signature.indexOf("$") : signature.lastIndexOf(".");
      const path = signature.substring(0, classPathIndex);
      const url = new URL(window.location);
      url.searchParams.set('signature', encodeURIComponent(path));
      location.href = url.toString();
    },
    showCaller(signature) {
      this.$emit("showCaller", signature);
    }
  }
}
</script>
