<template>
  <div class="compose-paper-container">
    <!-- 试卷选择区域 -->
    <div class="paper-selection">
      <el-card class="paper-card">
        <template #header>
          <div class="card-header">
            <h3 v-if="selectedPaper.paperId">
              已选试卷：{{ selectedPaper.name }}
            </h3>
            <h3 v-else>选择试卷</h3>
            <el-button type="primary" @click="openPaperDialog">
              <el-icon><Search /></el-icon>
              选择试卷
            </el-button>
          </div>
        </template>

        <div v-if="selectedPaper.paperId" class="selected-paper-info">
          <div class="paper-details">
            <p><strong>试卷描述：</strong>{{ selectedPaper.description }}</p>
            <p>
              <strong>题目数量：</strong>{{ selectedPaper.questionCount }} 题
            </p>
            <p><strong>答题时间：</strong>{{ selectedPaper.duration }} 分钟</p>
            <p><strong>总分：</strong>{{ selectedPaper.totalScore }} 分</p>
            <p><strong>标签：</strong>{{ selectedPaper.tag }}</p>
            <p><strong>所需积分：</strong>{{ selectedPaper.pointsRequired }}</p>
          </div>
        </div>
        <div v-else class="no-paper">
          <el-empty description="请先选择试卷" />
        </div>
      </el-card>
    </div>

    <!-- 题目选择区域 -->
    <div class="question-selection" v-if="selectedPaper.paperId">
      <el-card class="question-card">
        <template #header>
          <div class="card-header">
            <span>题目选择</span>
            <div class="header-actions">
              <el-button
                type="primary"
                @click="saveComposition"
                :loading="saving"
              >
                <el-icon><Check /></el-icon>
                保存组卷
              </el-button>
              <el-button @click="resetSelection">
                <el-icon><Refresh /></el-icon>
                重置选择
              </el-button>
            </div>
          </div>
        </template>

        <!-- 自定义双列表选择器 -->
        <div class="custom-double-list">
          <!-- 左侧：未选题目 -->
          <div class="list-container left-list">
            <div class="list-header">
              <h3>未选题目</h3>
              <div class="search-box">
                <el-input
                  v-model="leftSearchQuery"
                  placeholder="请输入题目内容搜索"
                  clearable
                  @input="handleLeftSearch"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </div>
            </div>
            <div class="table-container">
              <el-table
                ref="leftTableRef"
                :data="filteredLeftQuestions"
                @selection-change="handleLeftSelectionChange"
                style="width: 100%"
                height="400"
                v-loading="questionLoading"
              >
                <el-table-column type="selection" width="55" />
                <el-table-column
                  prop="content"
                  label="题目内容"
                  min-width="260"
                  show-overflow-tooltip
                >
                  <template #default="{ row }">
                    <span class="question-item">
                      <el-tag size="small" :type="getQuestionTypeTag(row.type)">
                        {{ getTypeText(row.type) }}
                      </el-tag>
                      <span class="question-content">{{ row.content }}</span>
                    </span>
                  </template>
                </el-table-column>
                <el-table-column
                  prop="difficulty"
                  label="难度"
                  width="80"
                  align="center"
                >
                  <template #default="{ row }">
                    <el-tag
                      size="small"
                      :type="getDifficultyType(row.difficulty)"
                    >
                      {{ getDifficultyText(row.difficulty) }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <!-- 左侧分页 -->
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="leftPagination.pageNo"
                v-model:page-size="leftPagination.pageSize"
                :total="leftPagination.total"
                layout="total, prev, pager, next, jumper"
                @size-change="handleLeftSizeChange"
                @current-change="handleLeftCurrentChange"
              />
            </div>
          </div>

          <!-- 中间操作按钮 -->
          <div class="operation-buttons">
            <el-button
              :disabled="rightSelectedQuestions.length === 0"
              @click="removeSelectedFromRight"
            >
              <el-icon size="20"><ArrowLeft /></el-icon>
            </el-button>
            <el-button
              type="primary"
              :disabled="leftSelectedQuestions.length === 0"
              @click="addSelectedToRight"
            >
              <el-icon size="20"><ArrowRight /></el-icon>
            </el-button>
          </div>

          <!-- 右侧：已选题目 -->
          <div class="list-container right-list">
            <div class="list-header">
              <h3>已选题目</h3>
              <div class="selected-count">
                已选 {{ rightQuestionData.length }} 题
              </div>
            </div>
            <div class="table-container">
              <el-table
                ref="rightTableRef"
                :data="rightQuestionData"
                @selection-change="handleRightSelectionChange"
                style="width: 100%"
                height="400"
              >
                <el-table-column type="selection" width="55" />
                <el-table-column
                  prop="content"
                  label="题目内容"
                  min-width="280"
                  show-overflow-tooltip
                >
                  <template #default="{ row }">
                    <span class="question-item">
                      <el-tag size="small" :type="getQuestionTypeTag(row.type)">
                        {{ getTypeText(row.type) }}
                      </el-tag>
                      <span class="question-content">{{ row.content }}</span>
                    </span>
                  </template>
                </el-table-column>
                <el-table-column
                  prop="difficulty"
                  label="难度"
                  width="80"
                  align="center"
                >
                  <template #default="{ row }">
                    <el-tag
                      size="small"
                      :type="getDifficultyType(row.difficulty)"
                    >
                      {{ getDifficultyText(row.difficulty) }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <!-- 右侧不需要分页 -->
          </div>
        </div>
      </el-card>
    </div>

    <!-- 试卷选择弹框 -->
    <el-dialog
      v-model="paperDialogVisible"
      title="选择试卷"
      width="80%"
      :before-close="handlePaperDialogClose"
    >
      <div class="paper-dialog-content">
        <!-- 搜索区域 -->
        <div class="search-container">
          <el-form :inline="true" :model="paperSearchForm">
            <el-form-item label="试卷名称">
              <el-input
                v-model="paperSearchForm.name"
                placeholder="请输入试卷名称"
                clearable
              />
            </el-form-item>
            <el-form-item label="分类">
              <el-select
                v-model="paperSearchForm.categoryId"
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
            <el-form-item>
              <el-button type="primary" @click="handlePaperSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handlePaperReset">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 试卷表格 -->
        <el-table
          v-loading="paperLoading"
          :data="paperTableData"
          @row-click="handlePaperSelect"
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
          />
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
          />
          <el-table-column label="操作" width="100" align="center">
            <template #default="scope">
              <el-button
                type="primary"
                size="small"
                @click.stop="selectPaper(scope.row)"
              >
                选择
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="paperPagination.pageNo"
            v-model:page-size="paperPagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="paperPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handlePaperSizeChange"
            @current-change="handlePaperCurrentChange"
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import {
  ref,
  reactive,
  onMounted,
  computed,
  getCurrentInstance,
  nextTick
} from 'vue'
import {
  Search,
  Refresh,
  Check,
  ArrowLeft,
  ArrowRight
} from '@element-plus/icons-vue'
import { useCursor } from 'element-plus'
const { proxy } = getCurrentInstance()

// 响应式数据
const paperDialogVisible = ref(false)
const paperLoading = ref(false)
const questionLoading = ref(false)
const saving = ref(false)

// 选中的试卷
const selectedPaper = ref({
  paperId: null,
  name: '',
  description: '',
  questionCount: 0,
  duration: 0,
  totalScore: 0,
  tag: '',
  pointsRequired: 0
})

// 左侧表格引用
const leftTableRef = ref(null)
// 右侧表格引用
const rightTableRef = ref(null)

// 左侧选中的题目
const leftSelectedQuestions = ref([])
// 右侧选中的题目
const rightSelectedQuestions = ref([])

// 左侧搜索查询
const leftSearchQuery = ref('')

// 试卷搜索表单
const paperSearchForm = reactive({
  name: '',
  categoryId: ''
})

// 试卷分页数据
const paperPagination = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0
})

// 左侧题目分页数据（未选题目）
const leftPagination = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0
})

// 分类选项
const categoryOptions = ref([])

// 试卷表格数据
const paperTableData = ref([])

// 左侧题目数据（未选题目）
const leftQuestionData = ref([])

// 右侧题目数据（已选题目）
const rightQuestionData = ref([])

// 移出试卷的题目ID数组
const removeIds = ref([])

// 过滤后的左侧题目数据
const filteredLeftQuestions = computed(() => {
  if (!leftSearchQuery.value) {
    return leftQuestionData.value
  }

  const query = leftSearchQuery.value.toLowerCase()
  return leftQuestionData.value.filter((question) =>
    question.content.toLowerCase().includes(query)
  )
})

// 打开试卷选择弹框
const openPaperDialog = () => {
  paperDialogVisible.value = true
  loadPaperList()
}

// 加载试卷列表
const loadPaperList = async () => {
  paperLoading.value = true
  try {
    let result = await proxy.Request({
      url: proxy.Api.getPaperList,
      params: {
        ...paperSearchForm,
        pageNo: paperPagination.pageNo,
        pageSize: paperPagination.pageSize
      }
    })
    if (!result) {
      return
    }
    paperLoading.value = false

    paperTableData.value = result.data.list
    paperPagination.total = result.data.totalCount

    paperTableData.value.forEach((paper) => {
      const category = categoryOptions.value.find(
        (cat) => cat.categoryId === paper.categoryId
      )
      paper.categoryName = category ? category.name : '-'
    })
  } catch (error) {
    paperLoading.value = false
    proxy.Message.error('获取试卷列表失败')
  }
}

// 选择试卷
const selectPaper = (paper) => {
  selectedPaper.value = { ...paper }
  paperDialogVisible.value = false

  // 加载题目数据
  loadLeftQuestions()
  loadRightQuestions()
}

// 加载左侧题目列表（未选题目）
const loadLeftQuestions = async () => {
  questionLoading.value = true
  try {
    let result = await proxy.Request({
      url: proxy.Api.getNoComposeQuestions,
      params: {
        pageNo: leftPagination.pageNo,
        pageSize: leftPagination.pageSize
      }
    })
    if (!result) {
      return
    }
    questionLoading.value = false

    const excludeIds = new Set(
      rightQuestionData.value.map((item) => item.questionId)
    )
    leftQuestionData.value = result.data.list.filter(
      (q) => !excludeIds.has(q.questionId)
    )
    leftPagination.total = result.data.totalCount

    // 清空左侧选中
    leftSelectedQuestions.value = []
  } catch (error) {
    questionLoading.value = false
    proxy.Message.error('获取未选题目列表失败')
  }
}

// 加载右侧题目列表（已选题目）
const loadRightQuestions = async () => {
  try {
    let result = await proxy.Request({
      url: proxy.Api.getQuestionsByPaper,
      params: {
        paperId: selectedPaper.value.paperId
      }
    })
    if (!result) {
      return
    }

    rightQuestionData.value = result.data
    // 清空右侧选中
    rightSelectedQuestions.value = []
  } catch (error) {
    proxy.Message.error('获取已选题目列表失败')
  }
}

// 处理左侧表格选择变化
const handleLeftSelectionChange = (selection) => {
  leftSelectedQuestions.value = selection
}

// 处理右侧表格选择变化
const handleRightSelectionChange = (selection) => {
  rightSelectedQuestions.value = selection
}

// 添加选中题目到右侧
const addSelectedToRight = () => {
  if (leftSelectedQuestions.value.length === 0) {
    proxy.Message.warning('请先选择要添加的题目')
    return
  }

  // 过滤掉已经在右侧的题目
  const newQuestions = leftSelectedQuestions.value.filter(
    (question) =>
      !rightQuestionData.value.some(
        (rightQuestion) => rightQuestion.questionId === question.questionId
      )
  )

  // 添加到右侧
  rightQuestionData.value.push(...newQuestions)

  // 从左侧移除已添加的题目
  leftQuestionData.value = leftQuestionData.value.filter(
    (question) =>
      !leftSelectedQuestions.value.some(
        (selected) => selected.questionId === question.questionId
      )
  )

  // 清空左侧选中
  leftSelectedQuestions.value = []
  if (leftTableRef.value) {
    leftTableRef.value.clearSelection()
  }
}

// 从右侧移除选中题目
const removeSelectedFromRight = () => {
  if (rightSelectedQuestions.value.length === 0) {
    proxy.Message.warning('请先选择要移除的题目')
    return
  }

  // 记录要移除的题目ID
  const removedIds = rightSelectedQuestions.value.map((q) => q.questionId)
  removeIds.value = [...new Set([...removeIds.value, ...removedIds])]

  // 从右侧移除
  rightQuestionData.value = rightQuestionData.value.filter(
    (question) =>
      !rightSelectedQuestions.value.some(
        (selected) => selected.questionId === question.questionId
      )
  )

  // 添加到左侧
  leftQuestionData.value.push(...rightSelectedQuestions.value)

  // 清空右侧选中
  rightSelectedQuestions.value = []
  if (rightTableRef.value) {
    rightTableRef.value.clearSelection()
  }
}

// 保存组卷
const saveComposition = async () => {
  if (rightQuestionData.value.length === 0) {
    proxy.Message.warning('请至少选择一个题目')
    return
  }

  saving.value = true
  try {
    const addIds = rightQuestionData.value.map((q) => q.questionId)

    let result = await proxy.Request({
      url: proxy.Api.composePaper,
      params: {
        paperId: selectedPaper.value.paperId,
        addIds: addIds,
        removeIds: removeIds.value
      }
    })
    if (!result) {
      return
    }
    selectedPaper.value = result.data
    proxy.Message.success('组卷成功')

    // 重置移除ID数组
    removeIds.value = []
  } catch (error) {
    proxy.Message.error('组卷失败')
  } finally {
    saving.value = false
  }
}

// 重置选择
const resetSelection = () => {
  rightQuestionData.value = []
  removeIds.value = []
  loadRightQuestions()
  loadLeftQuestions()
}

// 试卷搜索
const handlePaperSearch = () => {
  paperPagination.pageNo = 1
  loadPaperList()
}

// 试卷重置
const handlePaperReset = () => {
  paperSearchForm.name = ''
  paperSearchForm.categoryId = ''
  paperPagination.pageNo = 1
  loadPaperList()
}

// 左侧搜索
const handleLeftSearch = () => {
  // 搜索时重置到第一页
  leftPagination.pageNo = 1
  // 这里可以根据需要实现服务端搜索或继续使用前端过滤
}

// 试卷分页大小改变
const handlePaperSizeChange = (val) => {
  paperPagination.pageSize = val
  paperPagination.pageNo = 1
  loadPaperList()
}

// 试卷当前页改变
const handlePaperCurrentChange = (val) => {
  paperPagination.pageNo = val
  loadPaperList()
}

// 左侧题目分页大小改变
const handleLeftSizeChange = (val) => {
  leftPagination.pageSize = val
  leftPagination.pageNo = 1
  loadLeftQuestions()
}

// 左侧题目当前页改变
const handleLeftCurrentChange = (val) => {
  leftPagination.pageNo = val
  loadLeftQuestions()
}

// 获取题型文本
const getTypeText = (type) => {
  const typeMap = {
    0: '单选',
    1: '多选',
    2: '判断'
  }
  return typeMap[type] || '未知'
}

// 获取难度文本
const getDifficultyText = (difficulty) => {
  const difficultyMap = {
    1: '简单',
    2: '中等',
    3: '困难'
  }
  return difficultyMap[difficulty] || '未知'
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

// 获取题型对应的标签类型
const getQuestionTypeTag = (type) => {
  const typeMap = {
    0: 'primary', // 单选题
    1: 'success', // 多选题
    2: 'warning' // 判断题
  }
  return typeMap[type] || 'info'
}

// 处理试卷弹框关闭
const handlePaperDialogClose = (done) => {
  done()
}

// 处理试卷行点击
const handlePaperSelect = (row) => {
  selectPaper(row)
}

onMounted(() => {
  const category = JSON.parse(sessionStorage.getItem('category'))
  categoryOptions.value = category || []
})
</script>

<style lang="scss" scoped>
.compose-paper-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);

  .paper-selection {
    margin-bottom: 20px;

    .paper-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        h3 {
          margin-bottom: 15px;
          color: #409eff;
        }
      }

      .selected-paper-info {
        .paper-details {
          p {
            margin: 8px 0;
            line-height: 1.5;
          }
        }
      }

      .no-paper {
        text-align: center;
        padding: 40px 0;
      }
    }
  }

  .question-selection {
    .question-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .header-actions {
          display: flex;
          gap: 10px;
        }
      }

      // 自定义双列表样式
      .custom-double-list {
        display: flex;
        gap: 20px;
        align-items: stretch;

        .list-container {
          flex: 1;
          border: 1px solid #e4e7ed;
          border-radius: 4px;
          background-color: #fff;
          display: flex;
          flex-direction: column;

          .list-header {
            padding: 12px 16px;
            border-bottom: 1px solid #e4e7ed;
            display: flex;
            justify-content: space-between;
            align-items: center;
            background-color: #f5f7fa;

            h3 {
              margin: 0;
              font-size: 16px;
              color: #303133;
            }

            .search-box {
              width: 200px;
            }

            .selected-count {
              font-size: 14px;
              color: #606266;
            }
          }

          .table-container {
            flex: 1;
            overflow: hidden;
          }

          .pagination-container {
            padding: 12px 16px;
            border-top: 1px solid #e4e7ed;
            display: flex;
            justify-content: center;
          }
        }

        .operation-buttons {
          display: flex;
          flex-direction: column;
          justify-content: center;
          gap: 12px;
          padding: 0 10px;

          .el-button {
            margin-left: 0;
            min-width: 60px;
          }
        }

        .left-list {
          // 左侧列表特定样式
        }

        .right-list {
          // 右侧列表特定样式
        }
      }

      .question-item {
        display: flex;
        align-items: center;
        gap: 8px;

        .question-content {
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
  }

  .paper-dialog-content {
    .search-container {
      margin-bottom: 20px;
    }

    .pagination-container {
      margin-top: 20px;
      display: flex;
      justify-content: center;
    }
  }
}

// 响应式布局
@media (max-width: 1200px) {
  .compose-paper-container {
    .question-selection {
      .question-card {
        .custom-double-list {
          flex-direction: column;

          .operation-buttons {
            flex-direction: row;
            justify-content: center;
            padding: 10px 0;
          }
        }
      }
    }
  }
}
</style>
