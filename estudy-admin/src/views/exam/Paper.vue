<template>
  <div class="paper-container">
    <!-- 搜索和操作区域 -->
    <div class="filter-container">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="试卷名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入试卷名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            style="width: 150px"
            v-model="searchForm.categoryId"
            placeholder="请选择分类"
            clearable
          >
            <el-option
              v-for="category in categoryOptions"
              :key="category.categoryId"
              :label="category.name"
              :value="category.categoryId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input
            v-model="searchForm.tag"
            placeholder="请输入标签"
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
          新增试卷
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
          prop="paperId"
          label="试卷ID"
          width="80"
          align="center"
        />
        <el-table-column
          prop="categoryName"
          label="所属分类"
          width="120"
          align="center"
        />
        <el-table-column
          prop="name"
          label="试卷名称"
          min-width="150"
          align="center"
        />
        <el-table-column
          prop="description"
          label="试卷描述"
          min-width="200"
          align="center"
          show-overflow-tooltip
        />
        <el-table-column
          prop="questionCount"
          label="题目数量"
          width="100"
          align="center"
        >
          <template #default="scope">
            <el-tag type="info">{{ scope.row.questionCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="duration"
          label="答题时间(分钟)"
          width="120"
          align="center"
        />
        <el-table-column
          prop="totalScore"
          label="总分"
          width="80"
          align="center"
        />
        <el-table-column
          prop="tag"
          label="标签"
          min-width="150"
          align="center"
          show-overflow-tooltip
        />
        <el-table-column
          prop="pointsRequired"
          label="所需积分"
          width="100"
          align="center"
        >
          <template #default="scope">
            <el-tag v-if="scope.row.pointsRequired > 0" type="warning">
              {{ scope.row.pointsRequired }}
            </el-tag>
            <span v-else>-</span>
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

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNo"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 通用弹框 -->
    <Dialog
      v-model:visible="dialogVisible"
      :title="isEdit ? '编辑试卷' : '新增试卷'"
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

// 分类选项
const categoryOptions = ref([])

// 搜索表单
const searchForm = reactive({
  name: '',
  categoryId: '',
  tag: ''
})

// 分页数据
const pagination = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0
})

// 表格数据
const tableData = ref([])

// 表单配置
const formConfig = computed(() => [
  {
    prop: 'categoryId',
    label: '所属分类',
    type: 'select',
    placeholder: '请选择分类',
    options: categoryOptions.value.map((cat) => ({
      label: cat.name,
      value: cat.categoryId
    })),
    required: true
  },
  {
    prop: 'name',
    label: '试卷名称',
    type: 'input',
    placeholder: '请输入试卷名称',
    maxlength: 20,
    showWordLimit: true,
    required: true
  },
  {
    prop: 'description',
    label: '试卷描述',
    type: 'textarea',
    placeholder: '请输入试卷描述',
    maxlength: 50,
    showWordLimit: true,
    required: false
  },
  {
    prop: 'duration',
    label: '答题时间',
    type: 'number',
    placeholder: '请输入答题时间(分钟)',
    min: 1,
    max: 300,
    required: true
  },
  {
    prop: 'tag',
    label: '标签',
    type: 'input',
    placeholder: '请输入标签，多个标签用逗号分隔',
    maxlength: 255,
    showWordLimit: true,
    required: false
  },
  {
    prop: 'pointsRequired',
    label: '所需积分',
    type: 'number',
    placeholder: '请输入所需积分',
    min: 0,
    max: 1000,
    required: false
  }
])

// 表单验证规则
const formRules = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  name: [
    { required: true, message: '请输入试卷名称', trigger: 'blur' },
    {
      min: 1,
      max: 20,
      message: '试卷名称长度在 1 到 20 个字符',
      trigger: 'blur'
    }
  ],
  description: [
    { max: 50, message: '试卷描述不能超过 50 个字符', trigger: 'blur' }
  ],
  questionCount: [
    { required: true, message: '请输入题目数量', trigger: 'blur' },
    {
      type: 'number',
      min: 1,
      max: 100,
      message: '题目数量范围在 1-100',
      trigger: 'blur'
    }
  ],
  duration: [
    { required: true, message: '请输入答题时间', trigger: 'blur' },
    {
      type: 'number',
      min: 1,
      max: 300,
      message: '答题时间范围在 1-300 分钟',
      trigger: 'blur'
    }
  ],
  totalScore: [
    { required: true, message: '请输入试卷总分', trigger: 'blur' },
    {
      type: 'number',
      min: 1,
      max: 200,
      message: '总分范围在 1-200',
      trigger: 'blur'
    }
  ],
  tag: [{ max: 255, message: '标签不能超过 255 个字符', trigger: 'blur' }],
  pointsRequired: [
    {
      type: 'number',
      min: 0,
      max: 1000,
      message: '所需积分范围在 0-1000',
      trigger: 'blur'
    }
  ]
}

// 获取试卷列表
const getPaperList = async () => {
  loading.value = true
  try {
    let result = await proxy.Request({
      url: proxy.Api.getPaperList,
      params: {
        ...searchForm,
        pageNo: pagination.pageNo,
        pageSize: pagination.pageSize
      }
    })
    if (!result) {
      return
    }
    loading.value = false

    tableData.value = result.data.list
    pagination.total = result.data.totalCount

    tableData.value.forEach((paper) => {
      const category = categoryOptions.value.find(
        (cat) => cat.categoryId === paper.categoryId
      )
      paper.categoryName = category ? category.name : '未知分类'
    })
  } catch (error) {
    loading.value = false
    proxy.Message.error('获取试卷列表失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNo = 1
  getPaperList()
}

// 重置
const handleReset = () => {
  searchForm.name = ''
  searchForm.categoryId = ''
  searchForm.tag = ''
  pagination.pageNo = 1
  getPaperList()
}

// 新增
const handleAdd = () => {
  // 检查是否有分类数据
  if (categoryOptions.value.length === 0) {
    proxy.Message.warning('请先添加分类')
    return
  }

  isEdit.value = false
  currentRow.value = {
    categoryId: '',
    name: '',
    description: '',
    duration: 0,
    tag: '',
    pointsRequired: 0
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
    await ElMessageBox.confirm(`确定要删除试卷"${row.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    let result = await proxy.Request({
      url: proxy.Api.deletePaper,
      params: {
        paperId: row.paperId
      }
    })
    if (!result) {
      return
    }

    proxy.Message.success('删除成功')
    getPaperList()
  } catch (error) {
    if (error !== 'cancel') {
      proxy.Message.error('删除失败')
    }
  }
}

// 分页大小改变
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.pageNo = 1
  getPaperList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  pagination.pageNo = val
  getPaperList()
}

// 弹框确认
const handleDialogConfirm = async (formData) => {
  dialogLoading.value = true
  try {
    await editPaper(formData)
    proxy.Message.success(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false
    getPaperList()
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

const editPaper = async (formData) => {
  let result = await proxy.Request({
    url: proxy.Api.editPaper,
    params: {
      ...formData
    }
  })
  if (!result) {
    return
  }
}

onMounted(() => {
  const category = JSON.parse(sessionStorage.getItem('category'))
  categoryOptions.value = category || []
  getPaperList()
})
</script>

<style lang="scss" scoped>
.paper-container {
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

  .table-container {
    .pagination-container {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}

// 响应式布局
@media (max-width: 768px) {
  .paper-container {
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
