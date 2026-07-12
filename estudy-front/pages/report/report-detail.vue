<template>
  <view class="record-container" :class="{ 'no-scroll': showDetailDrawer }">
    <!-- 顶部信息卡片 -->
    <view class="record-card">
      <view class="record-header">
        <view class="title-section">
          <text class="exam-name">{{ recordData.name }}</text>
          <text class="exam-description">{{ recordData.description }}</text>
        </view>
        <view :class="['status-badge', isPassed ? 'passed' : 'failed']">
          {{ isPassed ? '通过' : '未通过' }}
        </view>
      </view>

      <view class="score-section">
        <view class="score-display">
          <text class="score-text">{{ recordData.score }}</text>
          <text class="total-score"> / {{ recordData.totalScore }}分</text>
        </view>
        <view class="accuracy">
          <text class="accuracy-text">正确率: {{ accuracy }}%</text>
        </view>
      </view>

      <view class="progress-section">
        <progress
          :percent="accuracy"
          stroke-width="8"
          active-color="#6190E8"
          background-color="#EBEEF5"
        />
      </view>
    </view>

    <!-- 详细信息 -->
    <view class="detail-section">
      <view class="section-title">考试详情</view>

      <view class="detail-grid">
        <view class="detail-item">
          <view class="detail-icon">
            <uni-icons type="calendar" size="20" color="#a85fe8"></uni-icons>
          </view>
          <view class="detail-content">
            <text class="detail-label">考试时间</text>
            <text class="detail-value">{{
              formatTime(recordData.startTime)
            }}</text>
          </view>
        </view>

        <view class="detail-item">
          <view class="detail-icon">
            <image
              src="/static/images/time-icon.png"
              mode="aspectFit"
              style="transform: scale(0.7)"
            ></image>
          </view>
          <view class="detail-content">
            <text class="detail-label">考试时长</text>
            <text class="detail-value"
              >{{ Math.floor(recordData.duration / 60) }}分{{
                recordData.duration % 60
              }}秒</text
            >
          </view>
        </view>

        <view class="detail-item">
          <view class="detail-icon">
            <uni-icons type="list" size="20" color="#6190E8"></uni-icons>
          </view>
          <view class="detail-content">
            <text class="detail-label">题目数量</text>
            <text class="detail-value">{{ recordData.questionCount }}题</text>
          </view>
        </view>

        <view class="detail-item">
          <view class="detail-icon">
            <uni-icons type="star" size="20" color="#e8cc3c"></uni-icons>
          </view>
          <view class="detail-content">
            <text class="detail-label">考试评价</text>
            <text class="detail-value">{{ examEvaluation }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 成绩分析 -->
    <view class="analysis-section">
      <view class="section-title">成绩分析</view>

      <view class="analysis-grid">
        <view class="analysis-item">
          <view
            class="analysis-value"
            :style="{ color: isPassed ? '#67C23A' : '#F56C6C' }"
          >
            {{ isPassed ? '良好' : '需努力' }}
          </view>
          <text class="analysis-label">掌握程度</text>
        </view>

        <view class="analysis-item">
          <view class="analysis-value">{{ correctCount }}</view>
          <text class="analysis-label">答对题数</text>
        </view>

        <view class="analysis-item">
          <view class="analysis-value">{{ wrongCount }}</view>
          <text class="analysis-label">答错题数</text>
        </view>

        <view class="analysis-item">
          <view class="analysis-value">{{ timePerQuestion }}秒/题</view>
          <text class="analysis-label">平均用时</text>
        </view>
      </view>

      <!-- 标签展示 -->
      <view class="tag-section">
        <view class="tag-list">
          <view
            v-for="(tag, index) in recordData.tag"
            :key="index"
            class="tag-item"
          >
            {{ tag }}
          </view>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-section">
      <button class="action-btn detail-btn" @click="showAnswerDetail">
        <uni-icons type="eye" size="18" color="#fff"></uni-icons>
        作答详情
      </button>
    </view>

    <!-- 作答详情抽屉 -->
    <view
      class="drawer-mask"
      v-if="showDetailDrawer"
      @click="hideAnswerDetail"
      @touchmove.stop.prevent
    ></view>
    <view :class="['answer-detail-drawer', showDetailDrawer ? 'show' : '']">
      <view class="drawer-header">
        <text class="drawer-title">作答详情</text>
        <view class="drawer-close" @click="hideAnswerDetail">×</view>
      </view>

      <scroll-view
        class="drawer-content"
        scroll-y
        :scroll-top="drawerScrollTop"
      >
        <view class="answer-list">
          <view
            v-for="(question, index) in recordData.answerDetails"
            :key="index"
            :class="['answer-item', question.isCorrect ? 'correct' : 'wrong']"
          >
            <view class="question-header">
              <text class="question-index">第{{ index + 1 }}题</text>
              <view
                :class="[
                  'answer-status',
                  question.isCorrect ? 'correct' : 'wrong'
                ]"
              >
                {{ question.isCorrect ? '正确' : '错误' }}
              </view>
            </view>

            <view class="question-content">
              <text class="question-title">{{ question.content }}</text>
            </view>

            <view class="answer-comparison">
              <view class="answer-row">
                <text class="answer-label">你的答案：</text>
                <text
                  :class="[
                    'user-answer',
                    question.isCorrect ? 'correct' : 'wrong'
                  ]"
                >
                  <text
                    v-if="question.userAnswer.length > 0"
                    v-for="key in question.userAnswer"
                  >
                    {{ `${key}. ${question.options[key]}\n` }}
                  </text>
                  <text v-else>未作答</text>
                </text>
              </view>
              <view class="answer-row">
                <text class="answer-label">正确答案：</text>
                <view class="correct-answer">
                  <text v-for="key in question.answer">{{
                    `${key}. ${question.options[key]}\n`
                  }}</text>
                </view>
              </view>
            </view>

            <view class="answer-actions" v-if="!question.isCorrect">
              <button
                v-if="!question.inWrongList"
                class="add-wrong-btn"
                @click="addToWrongBook(question)"
              >
                <uni-icons type="plus" size="16" color="#fff"></uni-icons>
                加入错题本
              </button>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, inject } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
const proxy = inject('utils')

// 考试记录数据
const recordData = reactive({})
const recordId = ref()
const showDetailDrawer = ref(false)
const drawerScrollTop = ref(0)

const loadData = async () => {
  let result = await proxy.Request({
    url: proxy.Api.getRecordDetail,
    showLoading: false,
    params: {
      recordId: recordId.value
    }
  })
  if (!result) {
    return
  }
  Object.assign(recordData, result.data)
  recordData.tag = recordData.tag.split(',')
  recordData.answerDetails.forEach((item) => {
    item.options = JSON.parse(item.options)
    item.answer = item.answer.split(',')
    item.userAnswer = item.userAnswer.split(',').filter(Boolean)
  })
}

// 计算正确率
const accuracy = computed(() => {
  if (Object.keys(recordData).length === 0) {
    return 0
  }
  return Math.round((recordData.score / recordData.totalScore) * 100)
})

// 计算是否通过（60%为通过）
const isPassed = computed(() => {
  if (Object.keys(recordData).length === 0) {
    return 0
  }
  return accuracy.value >= 60
})

// 计算答对题数
const correctCount = computed(() => {
  if (Object.keys(recordData).length === 0) {
    return 0
  }
  return Math.round(
    (recordData.score / recordData.totalScore) * recordData.questionCount
  )
})

// 计算答错题数
const wrongCount = computed(() => {
  if (Object.keys(recordData).length === 0) {
    return 0
  }
  return recordData.questionCount - correctCount.value
})

// 计算每题平均用时（秒）
const timePerQuestion = computed(() => {
  return Math.round(recordData.duration / recordData.questionCount)
})

// 计算考试难度
const examEvaluation = computed(() => {
  const avgScore = recordData.score / recordData.totalScore
  if (avgScore >= 0.8) return '优秀'
  if (avgScore >= 0.6) return '中等'
  return '较差'
})

// 格式化时间
const formatTime = (timeString) => {
  const date = new Date(timeString)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 显示作答详情抽屉
const showAnswerDetail = () => {
  showDetailDrawer.value = true
  // 重置滚动位置
  drawerScrollTop.value = 0
}

// 隐藏作答详情抽屉
const hideAnswerDetail = () => {
  showDetailDrawer.value = false
}

// 加入错题本
const addToWrongBook = async (question) => {
  let result = await proxy.Request({
    url: proxy.Api.addWrongQuestion,
    params: {
      questionId: question.questionId,
      userAnswer: question.userAnswer.join(',')
    }
  })
  if (!result) {
    return
  }
  proxy.Message.success('已加入错题本')
  loadData()
}

onLoad((options) => {
  recordId.value = +options.id
  loadData()
})
</script>

<style lang="scss" scoped>
.record-container {
  padding: 40rpx 30rpx;
  transition: all 0.3s ease;

  &.no-scroll {
    height: 100vh;
    overflow: hidden;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
  }
}

/* 记录卡片样式 */
.record-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8faff 100%);
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
  border: 1rpx solid rgba(30, 144, 255, 0.1);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 8rpx;
    background: linear-gradient(90deg, #1e90ff 0%, #00bfff 100%);
  }
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30rpx;
}

.title-section {
  flex: 1;
}

.exam-name {
  font-size: 36rpx;
  font-weight: 700;
  color: #1a1a1a;
  display: block;
  margin-bottom: 8rpx;
}

.exam-description {
  font-size: 26rpx;
  color: #909399;
}

.status-badge {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 500;
  margin-left: 20rpx;

  &.passed {
    background-color: rgba(92, 216, 158, 0.1);
    color: #5cd89e;
  }

  &.failed {
    background-color: rgba(255, 94, 94, 0.1);
    color: #ff5e5e;
  }
}

.score-section {
  text-align: center;
  margin-bottom: 30rpx;
}

.score-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  margin-bottom: 16rpx;
}

.score-text {
  font-size: 64rpx;
  font-weight: 800;
  color: #1e90ff;
  text-shadow: 0 4rpx 12rpx rgba(30, 144, 255, 0.2);
  line-height: 1;
}

.total-score {
  font-size: 32rpx;
  color: #999;
  margin-left: 8rpx;
  font-weight: 500;
}

.accuracy {
  font-size: 28rpx;
  font-weight: 600;
  color: #666;
}

.progress-section {
  position: relative;
  margin-top: 20rpx;
}

/* 详情区域 */
.detail-section,
.analysis-section {
  background-color: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 30rpx;
  padding-left: 16rpx;
  border-left: 4rpx solid #6190e8;
}

.detail-grid {
  display: flex;
  gap: 30rpx 15rpx;
  flex-wrap: wrap;
}

.detail-item {
  width: 48%;
  display: flex;
  align-items: center;
}

.detail-icon {
  width: 60rpx;
  height: 60rpx;
  background-color: rgba(97, 144, 232, 0.1);
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.detail-content {
  flex: 1;
}

.detail-label {
  font-size: 26rpx;
  color: #909399;
  display: block;
  margin-bottom: 4rpx;
}

.detail-value {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

/* 分析区域 */
.analysis-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30rpx;
  margin-bottom: 40rpx;
}

.analysis-item {
  background-color: #f8f9fa;
  border-radius: 16rpx;
  padding: 30rpx;
  text-align: center;
}

.analysis-value {
  font-size: 32rpx;
  font-weight: 700;
  color: #6190e8;
  display: block;
  margin-bottom: 8rpx;
}

.analysis-label {
  font-size: 24rpx;
  color: #909399;
}

.tag-section {
  margin-top: 20rpx;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.tag-item {
  padding: 12rpx 24rpx;
  background-color: rgba(97, 144, 232, 0.1);
  color: #6190e8;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 500;
}

/* 操作按钮 */
.action-section {
  margin-top: 40rpx;
}

.action-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 16rpx;
  font-size: 30rpx;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  transition: all 0.3s;

  &::after {
    border: none;
  }
}

.detail-btn {
  background: linear-gradient(135deg, #6190e8 0%, #3a6fe8 100%);
  color: #fff;
  box-shadow: 0 4rpx 16rpx rgba(97, 144, 232, 0.3);
}

/* 作答详情抽屉 */
.drawer-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
  animation: fadeIn 0.3s;
}

.answer-detail-drawer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #fff;
  border-radius: 32rpx 32rpx 0 0;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  transform: translateY(100%);
  transition: transform 0.3s ease;
  z-index: 1000;
  box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.1);
  height: 80vh;
  display: flex;
  flex-direction: column;

  &.show {
    transform: translateY(0);
  }
}

.drawer-header {
  display: flex;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
  position: relative;
  flex-shrink: 0;
}

.drawer-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1a1a1a;
}

.drawer-close {
  position: absolute;
  right: 32rpx;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  color: #999;
}

.drawer-content {
  flex: 1;
  height: 80%;
}

.answer-list {
  padding: 20rpx 30rpx;
}

.answer-item {
  background-color: #f8f9fa;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  border-left: 6rpx solid #6190e8;

  &.correct {
    border-left-color: #67c23a;
  }

  &.wrong {
    border-left-color: #f56c6c;
  }
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.question-index {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.answer-status {
  padding: 6rpx 16rpx;
  border-radius: 16rpx;
  font-size: 22rpx;
  font-weight: 500;

  &.correct {
    background-color: rgba(92, 216, 158, 0.1);
    color: #5cd89e;
  }

  &.wrong {
    background-color: rgba(255, 94, 94, 0.1);
    color: #ff5e5e;
  }
}

.question-content {
  margin-bottom: 20rpx;
}

.question-title {
  font-size: 28rpx;
  color: #333;
  line-height: 1.5;
}

.answer-comparison {
  background-color: #fff;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.answer-row {
  display: flex;
  margin-bottom: 12rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.answer-label {
  font-size: 26rpx;
  color: #666;
  width: 140rpx;
  flex-shrink: 0;
}

.user-answer {
  font-size: 26rpx;
  font-weight: 500;
  flex: 1;

  &.correct {
    color: #67c23a;
  }

  &.wrong {
    color: #f56c6c;
  }
}

.correct-answer {
  font-size: 26rpx;
  color: #67c23a;
  font-weight: 500;
  flex: 1;
}

.answer-actions {
  display: flex;
  justify-content: flex-end;
}

.add-wrong-btn {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);
  color: #fff;
  height: 64rpx;
  line-height: 64rpx;
  border-radius: 12rpx;
  font-size: 24rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;

  &::after {
    border: none;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}
</style>
