<template>
  <div class="question-container">
    <!-- 搜索和操作区域 -->
    <div class="filter-container">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="题目内容">
          <el-input
            v-model="searchForm.content"
            placeholder="请输入题目内容"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="题目类型">
          <el-select
            style="width: 120px"
            v-model="searchForm.type"
            placeholder="请选择题型"
            clearable
          >
            <el-option label="单选题" :value="0" />
            <el-option label="多选题" :value="1" />
            <el-option label="判断题" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select
            style="width: 120px"
            v-model="searchForm.difficulty"
            placeholder="请选择难度"
            clearable
          >
            <el-option label="简单" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="困难" :value="3" />
          </el-select>
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
          新增题目
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
        :cell-style="tableCellStyle"
        class="wrap-table"
      >
        <el-table-column
          prop="questionId"
          label="题目ID"
          width="80"
          align="center"
        />
        <el-table-column
          prop="paperStatus"
          label="组卷状态"
          width="100"
          align="center"
        >
          <template #default="scope">
            <el-tag :type="scope.row.paperId ? 'success' : 'info'">
              {{ scope.row.paperId ? '已组卷' : '未组卷' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="typeText"
          label="题目类型"
          width="100"
          align="center"
        />
        <el-table-column
          prop="content"
          label="题目内容"
          min-width="200"
          align="center"
          show-overflow-tooltip
        />
        <el-table-column
          prop="difficultyText"
          label="难度"
          width="80"
          align="center"
        >
          <template #default="scope">
            <el-tag :type="getDifficultyType(scope.row.difficulty)">
              {{ getDifficultyText(scope.row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="optionsText"
          label="选项"
          min-width="150"
          align="center"
          show-overflow-tooltip
        />
        <el-table-column
          prop="answer"
          label="答案"
          width="120"
          align="center"
        />
        <el-table-column
          prop="analysis"
          label="解析"
          min-width="150"
          align="center"
          show-overflow-tooltip
        />
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

    <!-- 在 Question.vue 的 Dialog 组件中添加答案插槽 -->
    <Dialog
      v-model:visible="dialogVisible"
      :title="isEdit ? '编辑题目' : '新增题目'"
      :form-config="formConfig"
      :form-data="currentRow"
      :rules="formRules"
      :loading="dialogLoading"
      :confirm-text="isEdit ? '更新' : '创建'"
      destroy-on-close
      @confirm="handleDialogConfirm"
      @cancel="handleDialogCancel"
    >
      <!-- 自定义选项插槽 -->
      <template #options-slot="{ formData }">
        <div class="options-container">
          <div class="options-header">
            <el-button
              type="primary"
              size="small"
              @click="addOption"
              :disabled="optionList.length >= 8"
            >
              <el-icon><Plus /></el-icon>
              添加选项
            </el-button>
          </div>
          <div class="options-list">
            <div
              v-for="(option, index) in optionList"
              :key="index"
              class="option-item"
            >
              <span class="option-label">{{ getOptionLabel(index) }}.</span>
              <el-input
                v-model="option.value"
                :placeholder="`请输入选项${getOptionLabel(index)}的内容`"
                class="option-input"
              />
              <el-button
                type="danger"
                size="small"
                @click="removeOption(index)"
                :disabled="optionList.length <= 2"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </template>

      <!-- 弹框 -->
      <template #answer-slot="{ formData }">
        <div class="answer-container">
          <el-radio-group
            v-if="formData.type === 0 || formData.type === 2"
            v-model="formData.answer"
            class="answer-radio-group"
          >
            <div class="answer-options">
              <el-radio
                v-for="(option, index) in optionList"
                :key="index"
                :label="getOptionLabel(index)"
                class="answer-option"
              >
                {{ getOptionLabel(index) }}
              </el-radio>
            </div>
          </el-radio-group>

          <!-- 多选题使用多选按钮 -->
          <el-checkbox-group
            v-else-if="formData.type === 1"
            v-model="multipleAnswer"
            class="answer-checkbox-group"
          >
            <div class="answer-options">
              <el-checkbox
                v-for="(option, index) in optionList"
                :key="index"
                :label="getOptionLabel(index)"
                class="answer-option"
              >
                {{ getOptionLabel(index) }}
              </el-checkbox>
            </div>
          </el-checkbox-group>
        </div>
      </template>
    </Dialog>
  </div>
</template>

<script setup>
import {
  ref,
  reactive,
  onMounted,
  computed,
  watch,
  getCurrentInstance
} from 'vue'
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
const optionList = ref([])

// 搜索表单
const searchForm = reactive({
  content: '',
  type: '',
  difficulty: ''
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
    prop: 'type',
    label: '题目类型',
    type: 'select',
    placeholder: '请选择题型',
    options: [
      { label: '单选题', value: 0 },
      { label: '多选题', value: 1 },
      { label: '判断题', value: 2 }
    ],
    required: true
  },
  {
    prop: 'content',
    label: '题目内容',
    type: 'textarea',
    placeholder: '请输入题目内容',
    rows: 3,
    required: true
  },
  {
    prop: 'difficulty',
    label: '难度',
    type: 'select',
    placeholder: '请选择难度',
    options: [
      { label: '简单', value: 1 },
      { label: '中等', value: 2 },
      { label: '困难', value: 3 }
    ],
    required: true
  },
  {
    prop: 'options',
    label: '选项',
    type: 'slot',
    slotName: 'options-slot'
  },
  {
    prop: 'answer',
    label: '答案',
    type: 'slot',
    slotName: 'answer-slot',
    required: true
  },
  {
    prop: 'analysis',
    label: '题目解析',
    type: 'textarea',
    placeholder: '请输入题目解析',
    rows: 3,
    required: false
  }
])

// 表单验证规则
const formRules = {
  type: [{ required: true, message: '请选择题型', trigger: 'change' }],
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  answer: [{ required: true, message: '请输入答案', trigger: 'blur' }]
}

// 表格单元格样式
const tableCellStyle = ({ row, column, rowIndex, columnIndex }) => {
  // 为内容较多的列设置最小高度
  if (['content', 'optionsText', 'analysis'].includes(column.property)) {
    return {
      'min-height': '60px',
      padding: '8px 0'
    }
  }
  return {}
}

// 获取选项标签 (A, B, C, ...)
const getOptionLabel = (index) => {
  return String.fromCharCode(65 + index) // 65 is 'A' in ASCII
}

// 添加选项
const addOption = () => {
  if (optionList.value.length < 8) {
    optionList.value.push({ value: '' })
  }
}

// 删除选项
const removeOption = (index) => {
  if (optionList.value.length > 2) {
    optionList.value.splice(index, 1)
  }
}

// 解析选项字符串为选项列表
const parseOptions = (optionsStr) => {
  try {
    if (!optionsStr) return []
    const optionsObj = JSON.parse(optionsStr)
    return Object.keys(optionsObj).map((key) => ({
      key,
      value: optionsObj[key]
    }))
  } catch (error) {
    console.error('解析选项失败:', error)
    return []
  }
}

// 将选项列表转换为选项字符串
const stringifyOptions = (options) => {
  const optionsObj = {}
  options.forEach((option, index) => {
    const key = getOptionLabel(index)
    optionsObj[key] = option.value
  })
  return JSON.stringify(optionsObj)
}

// 获取题目列表
const getQuestionList = async () => {
  loading.value = true
  try {
    let result = await proxy.Request({
      url: proxy.Api.getQuestionList,
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

    // 处理显示文本
    tableData.value.forEach((question) => {
      question.typeText = getTypeText(question.type)
      // 解析选项并生成显示文本
      const optionsObj = JSON.parse(question.options)
      question.optionsText = Object.keys(optionsObj)
        .map((key) => `${key}.${optionsObj[key]}`)
        .join(' ')
    })
  } catch (error) {
    loading.value = false
    proxy.Message.error('获取题目列表失败')
  }
}

// 获取题型文本
const getTypeText = (type) => {
  const typeMap = {
    0: '单选题',
    1: '多选题',
    2: '判断题'
  }
  return typeMap[type] || '未知类型'
}

// 获取难度文本
const getDifficultyText = (difficulty) => {
  const difficultyMap = {
    1: '简单',
    2: '中等',
    3: '困难'
  }
  return difficultyMap[difficulty] || '未知难度'
}

// 获取难度对应的标签类型
const getDifficultyType = (difficulty) => {
  const typeMap = {
    1: 'success', // 简单 - 绿色
    2: 'warning', // 中等 - 黄色
    3: 'danger' // 困难 - 红色
  }
  return typeMap[difficulty] || 'info'
}

// 多选答案（用于多选题）
const multipleAnswer = ref([])

// 监听题目类型变化，重置答案
watch(
  () => currentRow.value.type,
  (newType) => {
    if (newType === 1) {
      // 多选题，将字符串答案转换为数组
      if (currentRow.value.answer) {
        multipleAnswer.value = currentRow.value.answer.split(',')
      } else {
        multipleAnswer.value = []
      }
    } else {
      // 单选题和判断题，清空多选答案
      multipleAnswer.value = []
    }
  }
)

// 监听多选答案变化，更新表单答案
watch(multipleAnswer, (newValue) => {
  if (currentRow.value.type === 1) {
    currentRow.value.answer = newValue.join(',')
  }
})

// 监听弹框显示状态
watch(dialogVisible, (newVal) => {
  if (newVal) {
    // 弹框打开时初始化选项列表和多选答案
    if (currentRow.value.options) {
      optionList.value = parseOptions(currentRow.value.options)
    } else {
      // 默认添加两个选项
      optionList.value = [{ value: '' }, { value: '' }]
    }

    // 初始化多选答案
    if (currentRow.value.type === 1 && currentRow.value.answer) {
      multipleAnswer.value = currentRow.value.answer.split(',')
    } else {
      multipleAnswer.value = []
    }
  } else {
    // 弹框关闭时清空选项列表和多选答案
    optionList.value = []
    multipleAnswer.value = []

    // 重置当前行数据
    if (!isEdit.value) {
      currentRow.value = {
        type: 0,
        content: '',
        difficulty: 1,
        options: '',
        answer: '',
        analysis: ''
      }
    }
  }
})

// 搜索
const handleSearch = () => {
  pagination.pageNo = 1
  getQuestionList()
}

// 重置
const handleReset = () => {
  searchForm.content = ''
  searchForm.type = ''
  searchForm.difficulty = ''
  pagination.pageNo = 1
  getQuestionList()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  currentRow.value = {
    type: 0,
    content: '',
    difficulty: 1,
    options: '',
    answer: '',
    analysis: ''
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
    await ElMessageBox.confirm(
      `确定要删除题目"${row.content.substring(0, 30)}..."吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    let result = await proxy.Request({
      url: proxy.Api.deleteQuestion,
      params: {
        questionId: row.questionId
      }
    })
    if (!result) {
      return
    }

    proxy.Message.success('删除成功')
    getQuestionList()
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
  getQuestionList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  pagination.pageNo = val
  getQuestionList()
}

// 弹框确认
const handleDialogConfirm = async (formData) => {
  // 验证选项
  const emptyOptions = optionList.value.filter((option) => !option.value.trim())
  if (emptyOptions.length > 0) {
    proxy.Message.error('请填写所有选项内容')
    return
  }

  // 验证答案
  if (!formData.answer) {
    proxy.Message.error('请选择答案')
    return
  }

  // 将选项列表转换为字符串
  formData.options = stringifyOptions(optionList.value)

  dialogLoading.value = true
  try {
    await editQuestion(formData)
    proxy.Message.success(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false
    getQuestionList()
  } catch (error) {
    proxy.Message.error('操作失败')
  } finally {
    dialogLoading.value = false
  }
}

const editQuestion = async (formData) => {
  let result = await proxy.Request({
    url: proxy.Api.editQuestion,
    params: {
      ...formData
    }
  })
  if (!result) {
    return
  }
}

// 弹框取消
const handleDialogCancel = () => {
  dialogVisible.value = false
}

onMounted(() => {
  getQuestionList()
})
</script>

<style lang="scss" scoped>
.question-container {
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

// 选项容器样式
.options-container {
  .options-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
  }

  .options-list {
    .option-item {
      display: flex;
      align-items: center;
      margin-bottom: 10px;

      .option-label {
        width: 30px;
        font-weight: bold;
        color: #409eff;
      }

      .option-input {
        flex: 1;
        margin: 0 10px;
      }
    }
  }
}

// 表格内容换行样式
:deep(.wrap-table) {
  .el-table__body {
    .el-table__cell {
      .cell {
        white-space: normal;
        word-break: break-word;
        line-height: 1.5;
      }
    }
  }

  // 为内容单元格设置固定高度和滚动（如果需要）
  .el-table__body-wrapper {
    .el-table__body {
      tr {
        td {
          padding: 8px 0;

          // 内容较多的列设置最小高度
          &.el-table__cell:nth-child(4), // 题目内容列
          &.el-table__cell:nth-child(6), // 选项列
          &.el-table__cell:nth-child(8) {
            // 解析列
            min-height: 60px;
          }
        }
      }
    }
  }
}

// 单元格内容样式
.cell-content {
  white-space: pre-line;
  word-break: break-word;
  line-height: 1.5;
  padding: 4px 8px;
}

// 响应式布局
@media (max-width: 768px) {
  .question-container {
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
