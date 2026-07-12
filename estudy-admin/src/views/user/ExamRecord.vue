<template>
  <div class="exam-record-container">
    <!-- 搜索区域 -->
    <div class="filter-container">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="用户ID">
          <el-input
            v-model="searchForm.userId"
            placeholder="请输入用户ID"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="试卷ID">
          <el-input
            v-model="searchForm.paperId"
            placeholder="请输入试卷ID"
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
          prop="recordId"
          label="记录ID"
          width="100"
          align="center"
        />
        <el-table-column
          prop="userId"
          label="用户ID"
          width="120"
          align="center"
        />
        <el-table-column
          prop="name"
          label="试卷名称"
          min-width="150"
          align="center"
          show-overflow-tooltip
        />
        <el-table-column prop="score" label="得分" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getScoreType(scope.row.score, scope.row.totalScore)">
              {{ scope.row.score }} / {{ scope.row.totalScore }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="duration"
          label="考试用时"
          width="150"
          align="center"
        >
          <template #default="scope">
            {{ formatDuration(scope.row.duration) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="startTime"
          label="开始时间"
          width="180"
          align="center"
        >
          <template #default="scope">
            {{ formatTime(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="correctRate"
          label="正确率"
          width="100"
          align="center"
        >
          <template #default="scope">
            <span :class="getCorrectRateClass(scope.row.correctRate)">
              {{ (scope.row.correctRate * 100).toFixed(1) }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleViewDetail(scope.row)"
            >
              <el-icon><View /></el-icon>
              详情
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

    <!-- 答题详情弹框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="答题详情"
      @close="handleDetailDialogCancel"
      align-center
    >
      <div class="answer-detail-container">
        <!-- 答题详情表格 -->
        <div v-if="answerDetailData.length === 0" class="no-data">
          <el-empty description="暂无答题详情数据" />
        </div>
        <el-table
          v-else
          :data="answerDetailData"
          border
          style="width: 100%"
          :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
          v-loading="detailLoading"
        >
          <el-table-column
            prop="questionId"
            label="题目ID"
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
            prop="answer"
            label="正确答案"
            width="120"
            align="center"
          />
          <el-table-column
            prop="userAnswer"
            label="用户答案"
            width="120"
            align="center"
          >
            <template #default="scope">
              <span
                :class="scope.row.isCorrect ? 'correct-answer' : 'wrong-answer'"
              >
                {{ scope.row.userAnswer || '未作答' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column
            prop="isCorrect"
            label="是否正确"
            width="100"
            align="center"
          >
            <template #default="scope">
              <el-tag :type="scope.row.isCorrect ? 'success' : 'danger'">
                {{ scope.row.isCorrect ? '正确' : '错误' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, getCurrentInstance } from 'vue'
import { Search, Refresh, View } from '@element-plus/icons-vue'
const { proxy } = getCurrentInstance()

// 响应式数据
const loading = ref(false)
const detailDialogVisible = ref(false)
const detailLoading = ref(false)

// 当前选中的考试记录
const currentRecord = ref({
  recordId: null,
  userId: '',
  name: '',
  score: 0,
  totalScore: 0,
  duration: 0,
  correctRate: 0,
  startTime: ''
})

// 答题详情数据
const answerDetailData = ref([])

// 搜索表单
const searchForm = reactive({
  userId: '',
  paperId: ''
})

// 分页数据
const pagination = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0
})

// 表格数据（使用小驼峰命名法）
const tableData = ref([])

// 获取考试记录列表
const getExamRecordList = async () => {
  loading.value = true
  try {
    let result = await proxy.Request({
      url: proxy.Api.getExamRecords,
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

    tableData.value.forEach((item) => {
      item.correctRate = item.score / item.totalScore
    })
  } catch (error) {
    loading.value = false
    console.error('获取考试记录列表失败:', error)
  }
}

// 获取答题详情
const getAnswerDetail = async (recordId) => {
  detailLoading.value = true
  try {
    let result = await proxy.Request({
      url: proxy.Api.getRecordDetail,
      params: {
        recordId
      }
    })
    if (!result) {
      return
    }
    detailLoading.value = false
    answerDetailData.value = result.data.answerDetails
  } catch (error) {
    detailLoading.value = false
    console.error('获取答题详情失败:', error)
    answerDetailData.value = []
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

// 格式化考试用时（秒转分钟）
const formatDuration = (seconds) => {
  if (!seconds) return '-'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}分${remainingSeconds}秒`
}

// 获取得分标签类型
const getScoreType = (score, totalScore) => {
  const percentage = score / totalScore
  if (percentage >= 0.8) return 'success'
  if (percentage >= 0.6) return 'warning'
  return 'danger'
}

// 获取正确率样式类
const getCorrectRateClass = (correctRate) => {
  if (correctRate >= 0.8) return 'high-rate'
  if (correctRate >= 0.6) return 'medium-rate'
  return 'low-rate'
}

// 搜索
const handleSearch = () => {
  pagination.pageNo = 1
  getExamRecordList()
}

// 重置
const handleReset = () => {
  searchForm.userId = ''
  searchForm.paperId = ''
  pagination.pageNo = 1
  getExamRecordList()
}

// 查看详情
const handleViewDetail = async (row) => {
  currentRecord.value = { ...row }
  detailDialogVisible.value = true
  await getAnswerDetail(row.recordId)
}

// 分页大小改变
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.pageNo = 1
  getExamRecordList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  pagination.pageNo = val
  getExamRecordList()
}

// 答题详情弹框取消
const handleDetailDialogCancel = () => {
  detailDialogVisible.value = false
  answerDetailData.value = []
}

onMounted(() => {
  getExamRecordList()
})
</script>

<style lang="scss" scoped>
.exam-record-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

  .filter-container {
    margin-bottom: 20px;
  }

  .table-container {
    .pagination-container {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}

// 答题详情容器
.answer-detail-container {
  .no-data {
    padding: 40px 0;
  }

  .correct-answer {
    color: #67c23a;
    font-weight: bold;
  }

  .wrong-answer {
    color: #f56c6c;
    font-weight: bold;
  }
}

// 正确率样式
.high-rate {
  color: #67c23a;
  font-weight: bold;
}

.medium-rate {
  color: #e6a23c;
  font-weight: bold;
}

.low-rate {
  color: #f56c6c;
  font-weight: bold;
}
</style>
