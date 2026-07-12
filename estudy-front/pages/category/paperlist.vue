<template>
  <view class="container">
    <!-- 难度筛选 -->
    <view class="section-title">难度</view>
    <view class="difficulty-container">
      <view
        v-for="(item, index) in DIFFICULTY_TYPE"
        :key="index"
        :class="['difficulty-item', activeDifficulty === index ? 'active' : '']"
        @click="changeDifficulty(index)"
      >
        {{ item.label }}
        <view :class="['difficulty-dot', `difficulty-${item.value}`]"></view>
      </view>
    </view>

    <!-- 试卷列表 -->
    <view class="section-title">题库</view>
    <view class="exam-list">
      <view
        v-for="(item, index) in filteredExams"
        :key="index"
        class="exam-card"
        @click="goDetail(item)"
      >
        <view class="exam-header">
          <image src="/static/images/code-icon.png" class="exam-icon"></image>
          <view class="exam-title-container">
            <text class="exam-title">{{ item.name }}</text>
            <view class="exam-meta">
              <text
                :class="[
                  'exam-difficulty',
                  DIFFICULTY_TYPE[item.difficulty].value
                ]"
              >
                {{ DIFFICULTY_TYPE[item.difficulty].label }}
              </text>
              <text class="exam-count">{{ item.questionCount }}题</text>
            </view>
          </view>
        </view>

        <view class="exam-footer">
          <view class="exam-info">
            <view class="info-item">
              <image
                src="/static/images/time-icon.png"
                class="info-icon"
              ></image>
              <text>{{ item.duration }}分钟</text>
            </view>
            <view class="info-item">
              <image
                src="/static/images/score-icon.png"
                class="info-icon"
              ></image>
              <text>{{ item.totalScore }}分</text>
            </view>
          </view>
          <view class="start-button">开始练习</view>
        </view>

        <view class="exam-tags">
          <text
            v-for="(tag, tagIndex) in item.tag.split(',')"
            :key="tagIndex"
            class="tag"
            >{{ tag }}</text
          >
        </view>
      </view>

      <view v-if="filteredExams.length === 0" class="empty-state">
        <image src="/static/images/empty.png" class="empty-img"></image>
        <text class="empty-text">没有找到符合条件的题目</text>
        <text class="empty-subtext">尝试调整筛选条件</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, inject } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { DIFFICULTY_TYPE } from '../../utils/constants.js'
const proxy = inject('utils')

const categoryId = ref()

// 考试数据
const exams = ref([])
// 选中难度
const activeDifficulty = ref(0)
let page = {
  pageNo: 1,
  pageTotal: 1,
  totalCount: 0
}

const loadData = async () => {
  let result = await proxy.Request({
    url: proxy.Api.getPapers,
    params: {
      categoryId: categoryId.value,
      pageNo: page.pageNo
    }
  })
  if (!result) {
    return
  }
  exams.value = exams.value.concat(result.data.list)
  page.pageNo = result.data.pageNo
  page.pageTotal = result.data.pageTotal
  page.totalCount = result.data.totalCount
}

// 计算属性 - 筛选后的考试列表
const filteredExams = computed(() => {
  if (Object.keys(exams.value).length === 0) {
    return []
  }
  return exams.value.filter(
    (exam) =>
      activeDifficulty.value === 0 || exam.difficulty === activeDifficulty.value
  )
})

const changeDifficulty = (value) => {
  activeDifficulty.value = value
}

const goDetail = (item) => {
  uni.navigateTo({
    url: `/pages/exam/paper-detail?id=${item.paperId}`
  })
}

onLoad((options) => {
  categoryId.value = +options.id
  loadData()
})

onReachBottom(() => {
  if (page.pageNo < page.pageTotal) {
    page.pageNo++
    loadData()
  }
})
</script>

<style lang="scss" scoped>
$primary-color: #6190e8;
$success-color: #67c23a;
$warning-color: #e6a23c;
$danger-color: #f56c6c;
$text-dark: #1a1a1a;
$text-light: #999;
$text-gray: #666;
$transition: all 0.3s ease;

.container {
  padding: 20rpx 25rpx 40rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: $text-dark;
  margin: 30rpx 0 20rpx;
  padding-left: 10rpx;
}

// 难度筛选样式
.difficulty-container {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 10rpx;

  .difficulty-item {
    padding: 14rpx 28rpx;
    margin: 0 15rpx 15rpx 0;
    background-color: #fff;
    border-radius: 40rpx;
    font-size: 26rpx;
    color: $text-gray;
    display: flex;
    align-items: center;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
    transition: $transition;

    &.active {
      background-color: $primary-color;
      color: #fff;
      box-shadow: 0 4rpx 12rpx rgba(97, 144, 232, 0.3);

      .difficulty-dot {
        background-color: #fff;
      }
    }

    .difficulty-dot {
      width: 12rpx;
      height: 12rpx;
      border-radius: 50%;
      margin-left: 10rpx;

      &.difficulty-easy {
        background-color: $success-color;
      }

      &.difficulty-medium {
        background-color: $warning-color;
      }

      &.difficulty-hard {
        background-color: $danger-color;
      }
    }
  }
}

// 题目卡片样式
.exam-list {
  margin-top: 10rpx;

  .exam-card {
    background-color: #fff;
    border-radius: 20rpx;
    padding: 30rpx;
    margin-bottom: 25rpx;
    box-shadow: 0 6rpx 18rpx rgba(0, 0, 0, 0.04);
    transition: $transition;
    position: relative;
    overflow: hidden;

    &:active {
      transform: translateY(4rpx);
      box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
    }

    .exam-header {
      display: flex;
      margin-bottom: 25rpx;

      .exam-icon {
        padding: 15rpx;
        width: 60rpx;
        height: 60rpx;
        border-radius: 16rpx;
        margin-right: 20rpx;
        background-color: #f0f4ff;
      }

      .exam-title-container {
        flex: 1;

        .exam-title {
          font-size: 32rpx;
          font-weight: 600;
          color: $text-dark;
          line-height: 1.4;
          display: -webkit-box;
          -webkit-box-orient: vertical;
          -webkit-line-clamp: 2;
          overflow: hidden;
        }

        .exam-meta {
          display: flex;
          margin-top: 12rpx;

          .exam-difficulty {
            font-size: 24rpx;
            padding: 4rpx 12rpx;
            border-radius: 20rpx;
            margin-right: 15rpx;

            &.easy {
              background-color: #e8f5e9;
              color: $success-color;
            }

            &.medium {
              background-color: #fff8e1;
              color: $warning-color;
            }

            &.hard {
              background-color: #ffebee;
              color: $danger-color;
            }
          }

          .exam-count {
            font-size: 24rpx;
            color: $text-light;
            display: flex;
            align-items: center;
          }
        }
      }
    }

    // 卡片底部样式
    .exam-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .exam-info {
        display: flex;

        .info-item {
          display: flex;
          align-items: center;
          margin-right: 30rpx;
          font-size: 24rpx;
          color: $text-gray;

          .info-icon {
            width: 28rpx;
            height: 28rpx;
            margin-right: 8rpx;
          }
        }
      }

      .start-button {
        background-color: $primary-color;
        color: #fff;
        font-size: 26rpx;
        padding: 10rpx 25rpx;
        border-radius: 40rpx;
        box-shadow: 0 4rpx 12rpx rgba(97, 144, 232, 0.3);
      }
    }

    // 标签样式
    .exam-tags {
      display: flex;
      flex-wrap: wrap;
      margin-top: 25rpx;
      padding-top: 25rpx;
      border-top: 1rpx dashed #eee;

      .tag {
        font-size: 22rpx;
        padding: 6rpx 16rpx;
        margin: 0 15rpx 10rpx 0;
        background-color: #f0f4ff;
        color: $primary-color;
        border-radius: 20rpx;
      }
    }
  }

  // 空状态样式
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 60rpx 0;

    .empty-img {
      width: 150rpx;
      height: 150rpx;
      margin-bottom: 30rpx;
      opacity: 0.6;
    }

    .empty-text {
      font-size: 30rpx;
      color: $text-gray;
      margin-bottom: 15rpx;
    }

    .empty-subtext {
      font-size: 26rpx;
      color: $text-light;
    }
  }
}
</style>
