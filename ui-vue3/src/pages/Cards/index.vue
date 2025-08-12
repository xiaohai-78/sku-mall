<template>
  <div class="page">
    <div class="toolbar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="名称">
          <el-input v-model="searchForm.name" placeholder="输入点卡名称" clearable />
        </el-form-item>
        <el-form-item label="编号">
          <el-input v-model="searchForm.code" placeholder="输入点卡编号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px;" @change="onSearch" @clear="onSearch">
            <el-option label="全部" value="" />
            <el-option label="上架" value="on" />
            <el-option label="下架" value="off" />
          </el-select>
        </el-form-item>
        <el-form-item label="有效期">
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
      <div>
        <el-button type="primary" @click="openCreate">新增点卡</el-button>
      </div>
    </div>

    <el-table :data="tableData" border stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="code" label="编号" min-width="120" />
      <el-table-column prop="faceValue" label="面值" width="100">
        <template #default="{ row }">¥ {{ row.faceValue }}</template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">¥ {{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="90" />
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.status === 'on' ? 'success' : 'info'">
            {{ row.status === 'on' ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="有效期" min-width="200">
        <template #default="{ row }">
          {{ row.validFrom }} ~ {{ row.validTo }}
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新于" min-width="160" />
      <el-table-column label="操作" fixed="right" width="240">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="toggleStatus(row)">
            {{ row.status === 'on' ? '下架' : '上架' }}
          </el-button>
          <el-button size="small" type="danger" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑点卡' : '新增点卡'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="编号" prop="code">
          <el-input v-model="form.code" placeholder="请输入编号" />
        </el-form-item>
        <el-form-item label="面值" prop="faceValue">
          <el-input-number v-model="form.faceValue" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="statusSwitch"
            active-text="上架"
            inactive-text="下架"
          />
        </el-form-item>
        <el-form-item label="有效期" prop="dateRange">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="可填写详细说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="onSubmit">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed } from 'vue';
import type { Card, CardStatus } from '../../services/card';
import { getCards, createCard, updateCard, deleteCard, updateCardStatus } from '../../services/card';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';

const loading = ref(false);

const query = reactive({
  page: 1,
  pageSize: 10
});

const total = ref(0);
const tableData = ref<Card[]>([]);

const searchForm = reactive({
  name: '',
  code: '',
  status: '' as '' | CardStatus,
  dateRange: [] as string[]
});

function onSearch() {
  query.page = 1;
  fetchList();
}

function onReset() {
  searchForm.name = '';
  searchForm.code = '';
  searchForm.status = '';
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
      name: searchForm.name || undefined,
      code: searchForm.code || undefined,
      status: searchForm.status || undefined,
    };
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.dateFrom = searchForm.dateRange[0];
      params.dateTo = searchForm.dateRange[1];
    }
    const res = await getCards(params);
    tableData.value = res.list;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
}

onMounted(fetchList);

const dialogVisible = ref(false);
const isEdit = ref(false);
const editingId = ref<number | null>(null);

const formRef = ref<FormInstance>();

const form = reactive({
  name: '',
  code: '',
  faceValue: 0,
  price: 0,
  // stock: 0, // 库存通过 SKU 数量计算，不再手动输入
  status: 'on' as CardStatus,
  validFrom: '',
  validTo: '',
  description: ''
});

const dateRange = ref<string[]>([]);
const statusSwitch = computed({
  get: () => form.status === 'on',
  set: (val: boolean) => { form.status = val ? 'on' : 'off'; }
});

const rules: FormRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入编号', trigger: 'blur' }],
  faceValue: [{ required: true, message: '请输入面值', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'change' }],
  // stock: [{ required: true, message: '请输入库存', trigger: 'change' }],
  dateRange: [{ required: true, message: '请选择有效期', trigger: 'change' }]
};

function resetForm() {
  form.name = '';
  form.code = '';
  form.faceValue = 0;
  form.price = 0;
  // form.stock = 0;
  form.status = 'on';
  form.validFrom = '';
  form.validTo = '';
  form.description = '';
  dateRange.value = [];
}

function openCreate() {
  isEdit.value = false;
  editingId.value = null;
  resetForm();
  dialogVisible.value = true;
}

function openEdit(row: Card) {
  isEdit.value = true;
  editingId.value = row.id;
  form.name = row.name;
  form.code = row.code;
  form.faceValue = row.faceValue;
  form.price = row.price;
  // form.stock = row.stock; // 库存通过 SKU 数量计算，不再手动输入
  form.status = row.status;
  form.validFrom = row.validFrom;
  form.validTo = row.validTo;
  form.description = row.description || '';
  dateRange.value = [row.validFrom, row.validTo];
  dialogVisible.value = true;
}

async function onSubmit() {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return;
    form.validFrom = dateRange.value?.[0] || '';
    form.validTo = dateRange.value?.[1] || '';

    if (isEdit.value && editingId.value != null) {
      await updateCard(editingId.value, { ...form });
    } else {
      await createCard({ ...form });
    }
    dialogVisible.value = false;
    ElMessage.success('保存成功');
    fetchList();
  });
}

async function onDelete(row: Card) {
  await ElMessageBox.confirm(`确定要删除点卡 “${row.name}” 吗？`, '提示', { type: 'warning' });
  await deleteCard(row.id);
  ElMessage.success('删除成功');
  fetchList();
}

async function toggleStatus(row: Card) {
  const next = row.status === 'on' ? 'off' : 'on';
  await updateCardStatus(row.id, next);
  ElMessage.success(`${next === 'on' ? '上架' : '下架'}成功`);
  fetchList();
}
</script>

<style scoped>
.page {
  padding: 16px;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
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