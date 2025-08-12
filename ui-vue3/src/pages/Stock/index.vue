<template>
  <div class="page">
    <div class="toolbar">
      <h2>库存管理</h2>
    </div>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>添加点卡库存 (SKU 密钥)</span>
        </div>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="点卡编号" prop="cardCode">
          <el-input v-model="form.cardCode" placeholder="请输入点卡编号，例如: CARD-1001" />
        </el-form-item>
        <el-form-item label="SKU 密钥" prop="skuKey">
          <el-input v-model="form.skuKey" placeholder="请输入唯一的 SKU 密钥" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSubmit">添加库存</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-alert title="注意" type="info" show-icon :closable="false" style="margin-top: 20px;">
      请确保输入的点卡编号存在，并且 SKU 密钥未被添加过。每个 SKU 密钥代表一个实际的点卡库存。
    </el-alert>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { addStock } from '../../services/stock'; // 待创建

const formRef = ref<FormInstance>();
const form = reactive({
  cardCode: '',
  skuKey: '',
});

const rules: FormRules = {
  cardCode: [{ required: true, message: '请输入点卡编号', trigger: 'blur' }],
  skuKey: [{ required: true, message: '请输入 SKU 密钥', trigger: 'blur' }],
};

async function onSubmit() {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return;
    try {
      await addStock(form.cardCode, form.skuKey);
      ElMessage.success('库存添加成功！');
      form.cardCode = '';
      form.skuKey = '';
    } catch (error: any) {
      ElMessage.error(error.message || '库存添加失败，请检查点卡编号和 SKU 密钥是否重复');
    }
  });
}
</script>

<style scoped>
.page {
  padding: 16px;
}
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.card-header {
  font-weight: 600;
}
</style> 