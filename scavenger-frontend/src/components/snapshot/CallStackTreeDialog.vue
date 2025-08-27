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
      <el-table :data="callStack"
                style="width: 100%"
                row-key="signature"
                :tree-props="{ children: 'callers', hasChildren: 'hasCallers' }"
                default-expand-all
      >
        <el-table-column prop="signature" label="Method Signature"/>
        <el-table-column align="right" width="70px">
          <template #default="scope">
            <el-button size="small" @click="moveSignature(scope.row.signature)">
              <font-awesome-icon icon="fa-solid fa-up-right-from-square"/>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-dialog>
</template>
<script>

export default {
  props: ["dialogTableVisible", "callStack"],
  methods: {
    moveSignature(signature) {
      const classPathIndex = signature.includes("$") ? signature.indexOf("$") : signature.lastIndexOf(".");
      const path = signature.substring(0, classPathIndex);
      const url = new URL(window.location);
      url.searchParams.set('signature', encodeURIComponent(path));
      location.href = url.toString();
    }
  }
}
</script>
