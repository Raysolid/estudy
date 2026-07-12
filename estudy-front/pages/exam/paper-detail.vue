<template>
  <view class="exam-detail-container">
    <!-- 试卷信息卡片 -->
    <view class="exam-info-card">
      <view class="exam-header">
        <text class="exam-title">{{ examInfo.name }}</text>
      </view>

      <view class="exam-stats">
        <view class="stat-item">
          <uni-icons type="list" size="20" color="#6190E8"></uni-icons>
          <text class="stat-text">{{ examInfo.questionCount }}道题目</text>
        </view>
        <view class="stat-item">
          <image src="/static/images/time-icon.png" class="clock-icon"></image>
          <text class="stat-text">{{ examInfo.duration }}分钟</text>
        </view>
        <view class="stat-item">
          <uni-icons type="checkbox" size="20" color="#50d84e"></uni-icons>
          <text class="stat-text">{{ examInfo.totalScore }}分</text>
        </view>
      </view>

      <view class="exam-description">
        <text>{{ examInfo.description }}</text>
      </view>
    </view>

    <!-- 考试须知 -->
    <view class="exam-notice">
      <view class="notice-header">
        <text class="notice-title">考试须知</text>
      </view>
      <view class="notice-content">
        <view
          class="notice-item"
          v-for="(item, index) in examNotice"
          :key="index"
        >
          <view class="notice-number">{{ index + 1 }}</view>
          <text class="notice-text">{{ item }}</text>
        </view>
      </view>
    </view>

    <!-- 模式选择（已解锁试卷显示） -->
    <view class="mode-selection" v-if="examInfo.unlock">
      <view class="mode-header">
        <text class="mode-title">选择模式</text>
      </view>

      <view class="mode-cards">
        <view
          :class="[
            'mode-card',
            'practice-mode',
            selectedMode === 'practice' ? 'selected' : ''
          ]"
          @click="selectMode('practice')"
        >
          <view class="mode-icon">
            <uni-icons type="gear" size="32" color="#6190E8"></uni-icons>
          </view>
          <text class="mode-name">练习模式</text>
          <text class="mode-desc">不限时间，随时查看解析</text>
        </view>

        <view
          :class="[
            'mode-card',
            'exam-mode',
            selectedMode === 'exam' ? 'selected' : ''
          ]"
          @click="selectMode('exam')"
        >
          <view class="mode-icon">
            <uni-icons type="flag" size="32" color="#FF6B6B"></uni-icons>
          </view>
          <text class="mode-name">考试模式</text>
          <text class="mode-desc">模拟真实考试环境</text>
        </view>
      </view>
    </view>

    <!-- 解锁试卷区域（未解锁试卷显示） -->
    <view class="unlock-section" v-else>
      <view class="unlock-card">
        <view class="unlock-icon">
          <uni-icons type="locked" size="48" color="#FF9F43"></uni-icons>
        </view>
        <text class="unlock-title">试卷未解锁</text>
        <text class="unlock-desc">解锁后即可开始练习和考试</text>

        <view class="points-info">
          <text class="points-label">解锁需要</text>
          <text class="points-required">{{ examInfo.pointsRequired }}积分</text>
        </view>
      </view>
    </view>

    <!-- 开始按钮（已解锁试卷显示） -->
    <view class="start-button" v-if="examInfo.unlock">
      <button
        :class="['start-btn', selectedMode ? 'active' : '']"
        :disabled="!selectedMode"
        @click="startExam"
      >
        <text v-if="selectedMode">{{
          selectedMode === 'practice' ? '开始练习' : '开始考试'
        }}</text>
        <text v-else>请选择模式</text>
      </button>
    </view>

    <!-- 解锁按钮（未解锁试卷显示） -->
    <view class="unlock-button" v-else>
      <button class="unlock-btn can-unlock" @click="showUnlockConfirm">
        立即解锁
      </button>
    </view>

    <!-- 解锁确认弹窗 -->
    <uni-popup ref="unlockPopup" type="dialog">
      <uni-popup-dialog
        type="info"
        cancelText="取消"
        confirmText="确认解锁"
        title="解锁试卷"
        :content="unlockConfirmContent"
        @confirm="confirmUnlock"
      ></uni-popup-dialog>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, inject } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
const proxy = inject('utils')

// 试卷ID
const examId = ref('')

// 选中的模式
const selectedMode = ref('')

// 试卷信息
const examInfo = reactive({})

// 解锁弹窗引用
const unlockPopup = ref()

// 考试须知
const examNotice = ref([
  '请在规定时间内完成所有题目',
  '考试过程中请勿刷新或关闭页面',
  '考试结束后系统会自动评分',
  '考试成绩将计入学习记录'
])

// 解锁确认内容
const unlockConfirmContent = computed(
  () => `解锁此试卷需要消耗 ${examInfo.pointsRequired} 积分`
)

// 加载试卷信息
const loadExamInfo = async () => {
  let result = await proxy.Request({
    url: proxy.Api.getPaperInfo,
    showLoading: false,
    params: {
      paperId: examId.value
    }
  })
  if (!result) {
    return
  }
  Object.assign(examInfo, result.data)
  authPaper()
}

// 检查是否已解锁试卷
const authPaper = async () => {
  let result = await proxy.Request({
    url: proxy.Api.authPaper,
    showLoading: false,
    params: {
      paperId: examInfo.paperId
    }
  })
  if (!result) {
    return
  }
  examInfo.unlock = result.data
}

// 选择模式
const selectMode = (mode) => {
  selectedMode.value = mode
}

// 开始考试/练习
const startExam = () => {
  if (!selectedMode.value) {
    proxy.Message.warning('请选择模式')
    return
  }

  // 根据选择模式跳转到不同页面
  if (selectedMode.value === 'practice') {
    uni.navigateTo({
      url: `/pages/exam/question?examId=${examInfo.paperId}&mode=practice`
    })
  } else {
    uni.navigateTo({
      url: `/pages/exam/question?examId=${examInfo.paperId}&mode=exam`
    })
  }
}

// 显示解锁确认弹窗
const showUnlockConfirm = () => {
  unlockPopup.value.open()
}

// 确认解锁试卷
const confirmUnlock = async () => {
  let result = await proxy.Request({
    url: proxy.Api.unlockPaper,
    params: {
      paperId: examInfo.paperId
    }
  })
  if (!result) {
    return
  }
  proxy.Message.success('解锁成功')
  loadExamInfo()
}

onLoad((options) => {
  examId.value = options.id
  loadExamInfo()
})
</script>

<style lang="scss" scoped>
.exam-detail-container {
  padding: 30rpx;
  min-height: 100vh;
  background: linear-gradient(135deg, #f8faff 0%, #e6f0ff 100%);
}

/* 试卷信息卡片 */
.exam-info-card {
  background-color: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
  margin-bottom: 30rpx;
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30rpx;
}

.exam-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #1a1a1a;
  flex: 1;
  margin-right: 20rpx;
  line-height: 1.4;
}

.exam-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 30rpx;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.stat-item {
  display: flex;
  align-items: center;
  flex: 1;
  justify-content: center;

  .clock-icon {
    width: 34rpx;
    height: 34rpx;
  }

  .stat-text {
    font-size: 26rpx;
    color: #666;
    margin-left: 10rpx;
  }
}

.exam-description {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
}

/* 考试须知 */
.exam-notice {
  background-color: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
  margin-bottom: 30rpx;
}

.notice-header {
  margin-bottom: 30rpx;
}

.notice-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1a1a1a;
  padding-left: 16rpx;
  border-left: 4rpx solid #6190e8;
}

.notice-content {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.notice-item {
  display: flex;
  align-items: flex-start;
}

.notice-number {
  width: 40rpx;
  height: 40rpx;
  background: linear-gradient(135deg, #6190e8 0%, #3a6fe8 100%);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.notice-text {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
  flex: 1;
}

/* 模式选择 */
.mode-selection {
  background-color: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
  margin-bottom: 30rpx;
}

.mode-header {
  margin-bottom: 30rpx;
}

.mode-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1a1a1a;
  padding-left: 16rpx;
  border-left: 4rpx solid #6190e8;
}

.mode-cards {
  display: flex;
  gap: 24rpx;
}

.mode-card {
  flex: 1;
  border: 2rpx solid #f0f0f0;
  border-radius: 16rpx;
  padding: 30rpx;
  transition: all 0.3s;
  position: relative;

  /* 选中状态的视觉反馈 */
  &.selected {
    transform: translateY(-4rpx);
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
  }

  &.practice-mode {
    &.selected {
      border-color: #6190e8;
      background-color: rgba(97, 144, 232, 0.25);
    }
  }

  &.exam-mode {
    &.selected {
      border-color: #ff6b6b;
      background-color: rgba(255, 107, 107, 0.25);
    }
  }
}

.mode-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20rpx;

  .practice-mode & {
    background-color: rgba(97, 144, 232, 0.1);
  }

  .exam-mode & {
    background-color: rgba(255, 107, 107, 0.1);
  }
}

.mode-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #1a1a1a;
  display: block;
  margin-bottom: 8rpx;
}

.mode-desc {
  font-size: 26rpx;
  color: #909399;
  display: block;
  margin-bottom: 20rpx;
}

/* 解锁试卷区域 */
.unlock-section {
  background-color: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
  margin-bottom: 30rpx;
}

.unlock-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.unlock-icon {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background-color: rgba(255, 159, 67, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
}

.unlock-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 12rpx;
}

.unlock-desc {
  font-size: 26rpx;
  color: #909399;
  margin-bottom: 40rpx;
}

.points-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.points-label {
  font-size: 28rpx;
  color: #666;
}

.points-required {
  font-size: 28rpx;
  font-weight: 600;
  color: #ff9f43;
}

.points-current {
  font-size: 28rpx;
  font-weight: 600;
  color: #6190e8;
}

/* 开始按钮 */
.start-button {
  padding: 20rpx 0 40rpx;
}

.start-btn {
  background: linear-gradient(135deg, #6190e8 0%, #3a6fe8 100%);
  color: #fff;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 16rpx;
  font-size: 32rpx;
  font-weight: 500;
  transition: all 0.3s;

  &:not(.active) {
    background: #ccc;
    color: #999;
  }

  &::after {
    border: none;
  }
}

/* 解锁按钮 */
.unlock-button {
  padding: 20rpx 0 40rpx;
}

.unlock-btn {
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 16rpx;
  font-size: 32rpx;
  font-weight: 500;
  transition: all 0.3s;

  &.can-unlock {
    background: linear-gradient(135deg, #ff9f43 0%, #ff8c00 100%);
    color: #fff;
  }

  &::after {
    border: none;
  }
}
</style>
