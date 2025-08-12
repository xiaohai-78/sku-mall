<template>
  <div class="page">
    <div class="toolbar">
      <h2>订单列表</h2>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单ID">
          <el-input v-model="searchForm.orderId" placeholder="输入订单ID" clearable />
        </el-form-item>
        <el-form-item label="点卡名称">
          <el-input v-model="searchForm.cardName" placeholder="输入点卡名称" clearable />
        </el-form-item>
        <el-form-item label="点卡编号">
          <el-input v-model="searchForm.cardCode" placeholder="输入点卡编号" clearable />
        </el-form-item>
        <el-form-item label="成交时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px;"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="tableData" border stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="orderId" label="订单ID" min-width="200" />
      <el-table-column prop="dealTime" label="成交时间" min-width="180" />
      <el-table-column prop="cardName" label="点卡名称" min-width="160" />
      <el-table-column prop="cardCode" label="点卡编号" min-width="140" />
    </el-table>

    <div class="pager">
      <el-pagination
        background
        layout="total, prev, pager, next, sizes, jumper"
        :total="total"
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        :page-sizes="[10, 20, 50]"
        @change="fetchList"
        @size-change="fetchList"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue';
import type { OrderItem } from '../../services/order';
import { getOrders } from '../../services/order';

const loading = ref(false);
const tableData = ref<OrderItem[]>([]);
const total = ref(0);

const query = reactive({
  page: 1,
  pageSize: 10
});

const searchForm = reactive({
  orderId: '',
  cardName: '',
  cardCode: '',
  dateRange: [] as string[]
});

function onSearch() {
  query.page = 1;
  fetchList();
}

function onReset() {
  searchForm.orderId = '';
  searchForm.cardName = '';
  searchForm.cardCode = '';
  searchForm.dateRange = [];
  query.page = 1;
  fetchList();
}

async function fetchList() {
  loading.value = true;
  try {
    const params: any = {
      page: query.page,
      pageSize: query.pageSize,
      orderId: searchForm.orderId || undefined,
      cardName: searchForm.cardName || undefined,
      cardCode: searchForm.cardCode || undefined
    };
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.dateFrom = searchForm.dateRange[0];
      params.dateTo = searchForm.dateRange[1];
    }
    const res = await getOrders(params);
    tableData.value = res.list;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
}

onMounted(fetchList);
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
.search-form :deep(.el-form-item) {
  margin-right: 12px;
  margin-bottom: 12px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style> 