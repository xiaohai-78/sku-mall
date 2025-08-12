<template>
  <div class="page">
    <div class="toolbar">
      <h2>点卡销售</h2>
    </div>

    <el-table :data="cards" border stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="name" label="点卡名称" min-width="180" />
      <el-table-column prop="code" label="点卡编号" min-width="140" />
      <el-table-column prop="faceValue" label="面值" width="100">
        <template #default="{ row }">¥ {{ row.faceValue }}</template>
      </el-table-column>
      <el-table-column prop="price" label="销售价格" width="100">
        <template #default="{ row }">¥ {{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="可用库存" width="100" />
      <el-table-column label="操作" fixed="right" width="100">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="onSell(row)" :disabled="row.stock === 0">
            {{ row.stock === 0 ? '库存不足' : '售卖' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="销售成功" width="500px" :close-on-click-modal="false" :close-on-press-escape="false" :show-close="false">
      <el-form label-width="100px" v-if="soldItem">
        <el-form-item label="点卡名称">{{ soldItem.cardName }}</el-form-item>
        <el-form-item label="点卡编号">{{ soldItem.cardCode }}</el-form-item>
        <el-form-item label="SKU 密钥">
          <el-input v-model="soldItem.skuKey" readonly>
            <template #append>
              <el-button @click="copySkuKey(soldItem.skuKey)">复制</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="订单ID">{{ soldItem.orderId }}</el-form-item>
        <el-form-item label="成交时间">{{ soldItem.dealTime }}</el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="onCloseDialog">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import type { Card } from '../../services/card';
import { getCards } from '../../services/card';
import { sellCard } from '../../services/sales'; // 待创建

const loading = ref(false);
const cards = ref<Card[]>([]);

const dialogVisible = ref(false);
interface SoldItem {
  cardName: string;
  cardCode: string;
  skuKey: string;
  orderId: string;
  dealTime: string;
}
const soldItem = ref<SoldItem | null>(null);

async function fetchCards() {
  loading.value = true;
  try {
    // 销售页面不需要分页和复杂的筛选，只获取所有上架点卡即可
    const res = await getCards({ page: 1, pageSize: 999, status: 'on' });
    cards.value = res.list.filter(card => card.stock > 0); // 只显示有库存的点卡
  } finally {
    loading.value = false;
  }
}

async function onSell(card: Card) {
  try {
    const res = await sellCard(card.code);
    soldItem.value = res;
    dialogVisible.value = true;
    fetchCards(); // 销售成功后刷新列表，更新库存
  } catch (error: any) {
    ElMessage.error(error.message || '销售失败，请稍后再试');
  }
}

function copySkuKey(sku: string) {
  navigator.clipboard.writeText(sku).then(() => {
    ElMessage.success('SKU 密钥已复制到剪贴板！');
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制');
  });
}

function onCloseDialog() {
  dialogVisible.value = false;
  soldItem.value = null;
}

onMounted(fetchCards);
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
</style> 