<template>
  <div class="layout">
    <div class="topbar">
      <div class="brand">点卡后台</div>
      <div class="nav">
        <router-link to="/cards">
          <el-button :type="isCards ? 'primary' : 'info'" :plain="!isCards" size="large" round>
            点卡
          </el-button>
        </router-link>
        <router-link to="/stock">
          <el-button :type="isStock ? 'primary' : 'info'" :plain="!isStock" size="large" round>
            库存
          </el-button>
        </router-link>
        <router-link to="/sales">
          <el-button :type="isSales ? 'primary' : 'info'" :plain="!isSales" size="large" round>
            销售
          </el-button>
        </router-link>
        <router-link to="/orders">
          <el-button :type="isOrders ? 'success' : 'info'" :plain="!isOrders" size="large" round>
            订单
          </el-button>
        </router-link>
      </div>
    </div>
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const isCards = computed(() => route.path.startsWith('/cards'));
const isOrders = computed(() => route.path.startsWith('/orders'));
const isStock = computed(() => route.path.startsWith('/stock'));
const isSales = computed(() => route.path.startsWith('/sales'));
</script>

<style>
html, body, #app {
  height: 100%;
}
.layout {
  min-height: 100%;
}
.topbar {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid #ebeef5;
  background: #fff;
}
.topbar .brand {
  font-size: 18px;
  font-weight: 700;
}
.topbar .nav a + a {
  margin-left: 12px;
}
.topbar .nav .el-button {
  font-weight: 600;
}
</style> 