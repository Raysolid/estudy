<template>
  <div class="category-container">
    <!-- 搜索和操作区域 -->
    <div class="filter-container">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="分类名称">
          <el-input
            v-model="keyword"
            placeholder="请输入分类名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <div class="operation-container">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增分类
        </el-button>
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column
          prop="categoryId"
          label="分类ID"
          width="100"
          align="center"
        />
        <el-table-column
          prop="name"
          label="分类名称"
          min-width="120"
          align="center"
        />
        <el-table-column
          prop="description"
          label="分类描述"
          min-width="200"
          align="center"
          show-overflow-tooltip
        />
        <el-table-column
          prop="count"
          label="试卷数量"
          width="100"
          align="center"
        >
          <template #default="scope">
            <el-tag type="info">{{ scope.row.count }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleEdit(scope.row)"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(scope.row)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 通用弹框 -->
    <Dialog
      v-model:visible="dialogVisible"
      :title="isEdit ? '编辑分类' : '新增分类'"
      :form-config="formConfig"
      :form-data="currentRow"
      :rules="formRules"
      :loading="dialogLoading"
      :confirm-text="isEdit ? '更新' : '创建'"
      @confirm="handleDialogConfirm"
      @cancel="handleDialogCancel"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, getCurrentInstance } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import Dialog from '@/components/Dialog.vue'
const { proxy } = getCurrentInstance()

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const isEdit = ref(false)
const currentRow = ref({})

// 搜索表单
const keyword = ref('')

// 表格数据
const tableData = ref([])

// 表单配置
const formConfig = computed(() => [
  {
    prop: 'name',
    label: '分类名称',
    type: 'input',
    placeholder: '请输入分类名称',
    maxlength: 30,
    showWordLimit: true,
    required: true
  },
  {
    prop: 'description',
    label: '分类描述',
    type: 'textarea',
    placeholder: '请输入分类描述',
    maxlength: 255,
    showWordLimit: true,
    required: false
  }
])

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    {
      min: 1,
      max: 30,
      message: '分类名称长度在1~30个字符',
      trigger: 'blur'
    }
  ],
  description: [
    { max: 255, message: '分类描述不能超过 255 个字符', trigger: 'blur' }
  ]
}

// 获取分类列表
const getCategoryList = async () => {
  loading.value = true
  try {
    let result = await proxy.Request({
      url: proxy.Api.getCategoryList,
      params: {
        keyword: keyword.value
      }
    })
    if (!result) {
      return
    }
    tableData.value = result.data
    loading.value = false
  } catch (error) {
    loading.value = false
    proxy.Message.error('获取分类列表失败')
  }
}

// 搜索
const handleSearch = () => {
  getCategoryList()
}

// 重置
const handleReset = () => {
  keyword.value = ''
  getCategoryList()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  currentRow.value = {
    name: '',
    description: ''
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  currentRow.value = { ...row }
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除分类"${row.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteCategory(row.categoryId)

    proxy.Message.success('删除成功')
    getCategoryList()
  } catch (error) {
    if (error !== 'cancel') {
      proxy.Message.error('删除失败')
    }
  }
}

// 弹框确认
const handleDialogConfirm = async (formData) => {
  dialogLoading.value = true
  try {
    await editCategory(formData)
    proxy.Message.success(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false
    getCategoryList()
  } catch (error) {
    proxy.Message.error('操作失败')
  } finally {
    dialogLoading.value = false
  }
}

// 弹框取消
const handleDialogCancel = () => {
  dialogVisible.value = false
}

const editCategory = async (formData) => {
  let result = await proxy.Request({
    url: proxy.Api.editCategory,
    params: {
      ...formData
    }
  })
  if (!result) {
    return
  }
}

const deleteCategory = async (categoryId) => {
  let result = await proxy.Request({
    url: proxy.Api.deleteCategory,
    params: {
      categoryId
    }
  })
  if (!result) {
    return
  }
}

onMounted(async () => {
  await getCategoryList()
  sessionStorage.setItem('category', JSON.stringify(tableData.value))
})
</script>

<style lang="scss" scoped>
.category-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

  .filter-container {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 20px;

    .operation-container {
      margin-left: auto;
    }
  }
}

// 响应式布局
@media (max-width: 768px) {
  .category-container {
    .filter-container {
      flex-direction: column;

      .operation-container {
        margin-left: 0;
        margin-top: 15px;
        width: 100%;

        .el-button {
          width: 100%;
        }
      }
    }
  }
}
</style>
