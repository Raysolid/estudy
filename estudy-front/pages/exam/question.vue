<template>
  <view class="container">
    <!-- 顶部信息栏 -->
    <view class="header">
      <view class="header-left">
        <text class="exam-title">答题进度</text>
        <text class="timer" v-if="timer">{{ timer }}</text>
      </view>
      <view class="header-right">
        <text class="progress-text"
          >{{ currentIndex + 1 }}/{{ questions.length }}</text
        >
      </view>
    </view>

    <!-- 进度条 -->
    <progress
      :percent="progress"
      stroke-width="6"
      active-color="#6190E8"
      background-color="#EBEEF5"
    />

    <!-- 题目卡片 -->
    <view class="question-card">
      <view class="question-header">
        <view
          :class="[
            'question-difficulty',
            DIFFICULTY_TYPE[currentQuestion.difficulty]?.value
          ]"
        >
          {{ DIFFICULTY_TYPE[currentQuestion.difficulty]?.label }}
        </view>
        <view class="question-type">
          {{ QUESTION_TYPE[currentQuestion.type] }}
        </view>
      </view>

      <view class="question-title">{{ currentQuestion.content }}</view>

      <!-- 单选题和判断题 -->
      <radio-group
        class="options"
        @change="handleOptionChange"
        v-if="currentQuestion.type === 0 || currentQuestion.type === 2"
      >
        <label
          v-for="(text, key) in currentQuestion.options"
          :key="key"
          :class="['option', selectedAnswer === key ? 'selected' : '']"
        >
          <radio
            :value="key"
            :checked="selectedAnswer === key"
            color="#5B8EFF"
            style="display: none"
          />
          <view class="option-content">
            <view class="option-label">{{ key }}.</view>
            <view class="option-text">{{ text }}</view>
          </view>
          <view class="option-check">
            <view class="check-icon" v-if="selectedAnswer === key"></view>
          </view>
        </label>
      </radio-group>

      <!-- 多选题 -->
      <checkbox-group
        class="options"
        @change="handleCheckboxChange"
        v-if="currentQuestion.type === 1"
      >
        <label
          v-for="(text, key) in currentQuestion.options"
          :key="key"
          :class="['option', selectedAnswers.includes(key) ? 'selected' : '']"
        >
          <checkbox
            :value="key"
            :checked="selectedAnswers.includes(key)"
            color="#5B8EFF"
            style="display: none"
          />
          <view class="option-content">
            <view class="option-label">{{ key }}.</view>
            <view class="option-text">{{ text }}</view>
          </view>
          <view class="option-check">
            <view class="check-icon" v-if="selectedAnswers.includes(key)"
              >✓</view
            >
          </view>
        </label>
      </checkbox-group>

      <!-- 题目解析 -->
      <view
        class="explanation"
        v-if="showExplanation && currentQuestion.analysis"
      >
        <view class="explanation-header">
          <text class="explanation-title">题目解析</text>
          <view class="explanation-close" @click="toggleExplanation">×</view>
        </view>
        <view class="correct-answer">
          <text>正确答案：{{ formatAnswer(currentQuestion.answer) }}</text>
        </view>
        <text class="explanation-content">{{ currentQuestion.analysis }}</text>
      </view>
    </view>

    <!-- 导航按钮 -->
    <view class="nav-buttons">
      <button
        @click="prevQuestion"
        v-if="currentIndex !== 0"
        :disabled="currentIndex === 0"
        class="btn prev-btn"
        hover-class="btn-hover"
      >
        上一题
      </button>
      <button
        @click="toggleExplanation"
        class="btn explanation-btn"
        hover-class="btn-hover"
        v-if="mode === 'practice' && hasAnswer"
      >
        查看解析
      </button>
      <button
        @click="nextQuestion"
        class="btn next-btn"
        hover-class="btn-hover"
      >
        {{ currentIndex === questions.length - 1 ? '提交答案' : '下一题' }}
      </button>
    </view>

    <!-- 答题卡按钮 -->
    <view class="sheet-btn" @click="toggleAnswerSheet">
      <image
        src="/static/images/list.png"
        mode="aspectFit"
        style="width: 40rpx; height: 40rpx"
      ></image>
    </view>

    <!-- 抽屉式答题卡 -->
    <view
      class="drawer-mask"
      v-if="showAnswerSheet"
      @click="toggleAnswerSheet"
    ></view>
    <view :class="['answer-sheet-drawer', showAnswerSheet ? 'show' : '']">
      <view class="drawer-header">
        <text class="drawer-title">答题卡</text>
        <view class="drawer-close" @click="toggleAnswerSheet">×</view>
      </view>
      <view class="sheet-content">
        <view
          v-for="(question, index) in questions"
          :key="question.id"
          :class="[
            'sheet-item',
            userAnswers[index] ? 'answered' : 'unanswered',
            index === currentIndex ? 'current' : ''
          ]"
          @click="jumpToQuestion(index)"
        >
          {{ index + 1 }}
        </view>
      </view>
      <view class="drawer-footer">
        <view class="answer-count"
          >已答 {{ answeredCount }}/{{ questions.length }} 题</view
        >
        <button @click="submitAnswers" class="submit-btn">
          {{ mode === 'exam' ? '提交试卷' : '结束练习' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, inject } from 'vue'
import { DIFFICULTY_TYPE, QUESTION_TYPE } from '../../utils/constants.js'
const proxy = inject('utils')

// 页面参数
const props = defineProps({
  examId: String,
  mode: String
})

const examInfo = reactive({})
const questions = ref([])
const currentIndex = ref(0)
const currentQuestion = computed(
  () => questions.value[currentIndex.value] || {}
)

// 单选题和判断题的选中答案
const selectedAnswer = ref(null)
// 多选题的选中答案数组
const selectedAnswers = ref([])

const userAnswers = ref([])
const progress = ref(0)
const showExplanation = ref(false)
const showAnswerSheet = ref(false)
const timer = ref(null)
const timeLeft = ref(0)

// 计算是否有答案
const hasAnswer = computed(() => {
  if (currentQuestion.value.type === 1) {
    // 多选题
    return selectedAnswers.value.length > 0
  } else {
    // 单选题和判断题
    return selectedAnswer.value !== null
  }
})

const answeredCount = computed(
  () => userAnswers.value.filter((answer) => answer !== null).length
)

// 定时器引用
let timerInterval = null

// 格式化答案显示
const formatAnswer = (answer) => {
  return answer || ''
}

// 初始化考试数据
const initExamData = async (paperId) => {
  let result = await proxy.Request({
    url: proxy.Api.getExamQuestions,
    params: {
      paperId: +paperId,
      isExam: props.mode === 'exam' ? true : false
    }
  })
  if (!result) {
    return
  }

  questions.value = result.data.questions
  questions.value.forEach((q) => {
    q.options = JSON.parse(q.options)
  })
  Object.assign(examInfo, result.data.examInfo)

  // 初始化用户答案数组
  userAnswers.value = new Array(examInfo.questionCount).fill(null)

  progress.value = Math.round((1 / examInfo.questionCount) * 100)
  timeLeft.value = examInfo.duration * 60

  // 如果是考试模式，启动计时器
  if (props.mode === 'exam') {
    startTimer(timeLeft.value)
  }
}

// 启动计时器
const startTimer = (seconds) => {
  examInfo.startTime = new Date()
  timeLeft.value = seconds
  updateTimerDisplay()

  timerInterval = setInterval(() => {
    timeLeft.value--
    updateTimerDisplay()

    if (timeLeft.value <= 0) {
      clearInterval(timerInterval)
      submitAnswers()
    }
  }, 1000)
}

// 更新计时器显示
const updateTimerDisplay = () => {
  const minutes = Math.floor(timeLeft.value / 60)
  const seconds = timeLeft.value % 60
  timer.value = `${minutes}:${seconds < 10 ? '0' + seconds : seconds}`
}

// 处理单选题和判断题选项变化
const handleOptionChange = (e) => {
  selectedAnswer.value = e.detail.value
  userAnswers.value[currentIndex.value] = selectedAnswer.value
  showExplanation.value = false
}

// 处理多选题选项变化
const handleCheckboxChange = (e) => {
  selectedAnswers.value = e.detail.value
  userAnswers.value[currentIndex.value] = [...selectedAnswers.value]
  showExplanation.value = false
}

// 上一题
const prevQuestion = () => {
  if (currentIndex.value > 0) {
    const newIndex = currentIndex.value - 1
    updateQuestion(newIndex)
  }
}

// 下一题
const nextQuestion = () => {
  if (!hasAnswer.value) {
    proxy.Message.warning('请选择一个答案')
    return
  }

  if (currentIndex.value < examInfo.questionCount - 1) {
    const newIndex = currentIndex.value + 1
    updateQuestion(newIndex)
  } else {
    submitAnswers()
  }
}

// 跳转到指定题目
const jumpToQuestion = (index) => {
  updateQuestion(index)
  showAnswerSheet.value = false
}

// 更新题目显示
const updateQuestion = (newIndex) => {
  currentIndex.value = newIndex

  // 根据题目类型设置选中状态
  const savedAnswer = userAnswers.value[newIndex]
  if (currentQuestion.value.type === 1) {
    // 多选题
    selectedAnswers.value = savedAnswer ? [...savedAnswer] : []
    selectedAnswer.value = null
  } else {
    // 单选题和判断题
    selectedAnswer.value = savedAnswer || null
    selectedAnswers.value = []
  }

  showExplanation.value = false
  progress.value = Math.round(((newIndex + 1) / examInfo.questionCount) * 100)

  uni.pageScrollTo({
    scrollTop: 0,
    duration: 300
  })
}

// 切换答题卡显示
const toggleAnswerSheet = () => {
  showAnswerSheet.value = !showAnswerSheet.value
}

// 切换解析显示
const toggleExplanation = () => {
  if (!hasAnswer.value) {
    proxy.Message.warning('请先选择答案')
    return
  }
  showExplanation.value = !showExplanation.value
}

// 提交答案
const submitAnswers = async () => {
  if (props.mode === 'practice') {
    proxy.Message.success('练习完成')
    setTimeout(() => {
      uni.reLaunch({
        url: '/pages/index/index'
      })
    }, 1000)
    return
  }

  if (answeredCount.value < examInfo.questionCount) {
    uni.showModal({
      title: '提示',
      content: '还有题目未作答，确认要提交吗？',
      success: async (res) => {
        if (res.confirm) {
          await uploadData()
        }
      }
    })
    return
  }

  await uploadData()
}

// 上传考试数据
const uploadData = async () => {
  if (timerInterval) clearInterval(timerInterval)

  const examData = {
    paperId: examInfo.paperId,
    startTime: examInfo.startTime,
    userAnswers: userAnswers.value.map((item) =>
      Array.isArray(item) ? item.join('-') : item
    )
  }

  uni.showLoading({
    mask: true
  })
  let result = await proxy.Request({
    url: proxy.Api.submitExam,
    params: {
      ...examData
    }
  })
  if (!result) {
    return
  }
  uni.hideLoading()
  proxy.Message.success('交卷成功')
  setTimeout(() => {
    uni.redirectTo({
      url: `/pages/report/report-detail?id=${result.data}`
    })
  }, 1000)
}

// 生命周期
onMounted(() => {
  initExamData(props.examId)
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
})
</script>

<style lang="scss" scoped>
.container {
  padding: 24rpx 32rpx 120rpx;
  position: relative;
  box-sizing: border-box;
}

/* 头部样式 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;
}

.header-left {
  display: flex;
  align-items: center;
}

.exam-title {
  margin-right: 30rpx;
  font-size: 36rpx;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  margin-bottom: 10rpx;
}

.timer {
  font-size: 26rpx;
  color: #ff5e5e;
  display: flex;
  align-items: center;
  background: rgba(255, 94, 94, 0.1);
  padding: 8rpx 16rpx;
  border-radius: 40rpx;
  width: fit-content;
}

.progress-text {
  font-size: 32rpx;
  color: #333;
  font-weight: 500;
}

/* 进度条样式 */
progress {
  width: 100%;
  border-radius: 12rpx;
}

/* 题目卡片样式 */
.question-card {
  background-color: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin: 32rpx 0;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.04);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;
}

.question-difficulty {
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 40rpx;
  font-weight: 500;

  &.easy {
    background-color: rgba(92, 216, 158, 0.1);
    color: #5cd89e;
  }

  &.medium {
    background-color: rgba(255, 184, 0, 0.1);
    color: #ffb800;
  }

  &.hard {
    background-color: rgba(255, 94, 94, 0.1);
    color: #ff5e5e;
  }
}

.question-type {
  font-size: 24rpx;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  background-color: rgba(97, 144, 232, 0.1);
  color: #6190e8;
  font-weight: 500;
}

.question-title {
  font-size: 34rpx;
  color: #1a1a1a;
  line-height: 1.6;
  margin-bottom: 48rpx;
  font-weight: 500;
}

/* 选项样式 */
.options {
  display: flex;
  flex-direction: column;
}

.option {
  margin-bottom: 24rpx;
  background-color: #f7f9fc;
  border-radius: 16rpx;
  padding: 28rpx 24rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s;
  border: 2rpx solid transparent;

  &.selected {
    background-color: rgba(91, 142, 255, 0.1);
    border-color: #5b8eff;
  }
}

.option-content {
  flex: 1;
  display: flex;
  align-items: center;
}

.option-label {
  font-size: 32rpx;
  color: #5b8eff;
  margin-right: 20rpx;
  font-weight: 600;
  min-width: 40rpx;
}

.option-text {
  font-size: 30rpx;
  color: #333;
  flex: 1;
  line-height: 1.5;
}

.option-check {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  border: 2rpx solid #e0e0e0;
  margin-left: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.option.selected .option-check {
  border-color: #5b8eff;
  background-color: #5b8eff;
}

.check-icon {
  width: 24rpx;
  height: 24rpx;
  background-color: #fff;
  border-radius: 50%;
  font-size: 18rpx;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 解析样式 */
.explanation {
  margin-top: 40rpx;
  background-color: #f7f9fc;
  border-radius: 16rpx;
  padding: 32rpx;
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.explanation-header {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.explanation-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1a1a1a;
  flex: 1;
}

.explanation-close {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  color: #999;
}

.correct-answer {
  font-size: 28rpx;
  font-weight: 600;
  color: #5cd89e;
  margin-bottom: 20rpx;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #e8e8e8;
}

.explanation-content {
  font-size: 28rpx;
  color: #666;
  line-height: 1.8;
}

/* 导航按钮 */
.nav-buttons {
  display: flex;
  justify-content: space-between;
  margin-top: 40rpx;
  gap: 20rpx;
}

.btn {
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 16rpx;
  font-size: 30rpx;
  padding: 0 32rpx;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;

  &::after {
    border: none;
  }
}

.btn-hover {
  opacity: 0.9;
  transform: translateY(-2rpx);
}

.prev-btn {
  background-color: #f0f0f0;
  color: #5b8eff;
  font-weight: 500;
}

.next-btn {
  background-color: #5b8eff;
  color: white;
  font-weight: 500;
}

.explanation-btn {
  background-color: #ffb800;
  color: white;
  font-weight: 500;
}

/* 答题卡按钮 */
.sheet-btn {
  position: fixed;
  right: 40rpx;
  bottom: 160rpx;
  background: #5b8eff;
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 96rpx;
  height: 96rpx;
  box-shadow: 0 8rpx 24rpx rgba(91, 142, 255, 0.3);
  z-index: 100;
}

/* 抽屉式答题卡 */
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

.answer-sheet-drawer {
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

.sheet-content {
  padding: 32rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.sheet-item {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 16rpx;
  font-size: 32rpx;
  font-weight: 500;
  background-color: #f7f9fc;
  color: #999;

  &.answered {
    background-color: #5b8eff;
    color: white;
  }

  &.current {
    border: 2rpx solid #5b8eff;
    box-sizing: border-box;
    color: #5b8eff;
    background-color: white;
  }
}

.drawer-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  border-top: 1rpx solid #f0f0f0;

  .answer-count {
    flex: 1;
    font-size: 28rpx;
    color: #666;
  }

  .submit-btn {
    background: linear-gradient(90deg, #5b8eff, #3a6fe8);
    color: white;
    height: 80rpx;
    line-height: 80rpx;
    border-radius: 16rpx;
    padding: 0 40rpx;
    font-size: 28rpx;
    font-weight: 500;
    display: flex;
    align-items: center;
    box-shadow: 0 4rpx 16rpx rgba(91, 142, 255, 0.3);

    &::after {
      border: none;
    }
  }
}
</style>
