<template>
  <view class="category-container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-input">
        <uni-icons type="search" size="18" color="#999"></uni-icons>
        <input
          type="text"
          placeholder="搜索题目/知识点"
          placeholder-class="placeholder"
          v-model="searchKeyword"
          @confirm="handleSearch"
        />
        <uni-icons
          v-if="searchKeyword"
          type="clear"
          size="18"
          color="#999"
          @click="clearSearch"
        ></uni-icons>
      </view>
    </view>

    <!-- 主体内容 -->
    <view class="main-content">
      <!-- 左侧分类导航 -->
      <scroll-view class="category-nav" scroll-y>
        <view
          v-for="(category, index) in categories"
          :key="category.categoryId"
          :class="[
            'nav-item',
            activeCategoryId === category.categoryId ? 'active' : ''
          ]"
          @click="selectCategory(category.categoryId, index)"
        >
          <text class="nav-text">{{ category.name }}</text>
        </view>
      </scroll-view>

      <!-- 右侧内容区域 -->
      <scroll-view class="content-area" scroll-y>
        <!-- 分类内容显示 -->
        <view class="category-content">
          <!-- 当前分类的试卷列表 -->
          <view class="section-header">
            <text class="section-title">试卷列表</text>
            <text
              class="section-more"
              @click="viewAll(currentCategory?.categoryId)"
              >查看全部 ></text
            >
          </view>

          <!-- 试卷列表 -->
          <view class="paper-list" v-if="currentPapers.length > 0">
            <view
              v-for="paper in currentPapers"
              :key="paper.paperId"
              class="paper-card"
              @click="viewPaper(paper.paperId)"
            >
              <view class="paper-header">
                <text class="paper-title">{{ paper.name }}</text>
                <view class="paper-score">总分: {{ paper.totalScore }}</view>
              </view>
              <view class="paper-content">
                <text class="paper-desc">{{ paper.description }}</text>
              </view>
              <view class="paper-footer">
                <text class="points">所需积分: {{ paper.pointsRequired }}</text>
                <text class="question-info"
                  >{{ paper.questionCount }}题 / {{ paper.duration }}分钟</text
                >
              </view>
              <view class="paper-tags">
                <text
                  class="tag"
                  v-for="tag in getPaperTags(paper.tag)"
                  :key="tag"
                  >{{ tag }}</text
                >
              </view>
            </view>
          </view>

          <!-- 题目列表 -->
          <view class="section-header">
            <text class="section-title">热门题目</text>
          </view>
          <view class="question-list" v-if="currentQuestions.length > 0">
            <view
              v-for="(question, index) in currentQuestions"
              :key="question.questionId"
              class="question-card"
              @click="viewQuestion(question)"
            >
              <view class="question-header">
                <text class="question-title">{{ question.content }}</text>
                <view
                  :class="[
                    'difficulty-badge',
                    `difficulty-${DIFFICULTY_TYPE[question.difficulty].value}`
                  ]"
                >
                  {{ DIFFICULTY_TYPE[question.difficulty].label }}
                </view>
              </view>
              <view class="question-content">
                <text class="question-desc">{{
                  question.analysis || '暂无解析'
                }}</text>
              </view>
              <view class="question-footer">
                <text class="question-type">{{
                  QUESTION_TYPE[question.type]
                }}</text>
                <text class="answer-count"
                  >{{ question.answerCount }}人答过</text
                >
                <text class="correct-rate"
                  >正确率: {{ question.correctRate }}%</text
                >
              </view>
            </view>
          </view>

          <!-- 空状态 -->
          <view
            v-if="!currentPapers.length && !currentQuestions.length"
            class="empty-state"
          >
            <text class="empty-title">暂无相关数据</text>
            <text class="empty-tip">该分类下还没有试卷和题目</text>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, inject } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { DIFFICULTY_TYPE, QUESTION_TYPE } from '../../utils/constants'
import { useQuestionStore } from '../../stores/questionStore.js'
const questionStore = useQuestionStore()
const proxy = inject('utils')

// 搜索关键词
const searchKeyword = ref('')

// 当前激活的分类ID
const activeCategoryId = ref(1)

// 分类数据
const categories = ref([])

// 当前分类的试卷数据
const currentPapers = ref([])

// 当前分类的题目数据
const currentQuestions = ref([])

// 获取当前分类的试卷数据
const getPapersByCategory = async () => {
  let result = await proxy.Request({
    url: proxy.Api.getHotPapers,
    params: {
      categoryId: activeCategoryId.value
    }
  })
  if (!result) {
    return
  }
  currentPapers.value = result.data
}

// 获取当前分类的题目数据
const getQuestionsByCategory = async () => {
  let result = await proxy.Request({
    url: proxy.Api.getHotQuestions,
    params: {
      categoryId: activeCategoryId.value
    }
  })
  if (!result) {
    return
  }
  currentQuestions.value = result.data
}

// 当前分类信息
const currentCategory = computed(() => {
  return categories.value.find(
    (cat) => cat.categoryId === activeCategoryId.value
  )
})

// 解析试卷标签
const getPaperTags = (tagString) => {
  if (!tagString) return []
  return tagString.split(',').slice(0, 3) // 最多显示3个标签
}

// 选择分类
const selectCategory = async (categoryId, index) => {
  activeCategoryId.value = categoryId
  await getPapersByCategory()
  await getQuestionsByCategory()
}

// 处理搜索
const handleSearch = () => {
  uni.navigateTo({
    url: `/pages/category/search?keyword=${searchKeyword.value}`
  })
}

// 清空搜索
const clearSearch = () => {
  searchKeyword.value = ''
}

// 查看试卷详情
const viewPaper = (id) => {
  uni.navigateTo({
    url: `/pages/exam/paper-detail?id=${id}`
  })
}

// 查看全部分类
const viewAll = (categoryId) => {
  uni.navigateTo({
    url: `/pages/category/paperlist?id=${categoryId}`
  })
}

// 查看题目详情
const viewQuestion = (question) => {
  uni.navigateTo({
    url: `/pages/question/detail?id=${question.questionId}`
  })
}

onShow(() => {
  // 获取分类列表
  categories.value = questionStore.getCategoryList()
  // 默认选中第一个分类
  if (categories.value.length > 0) {
    selectCategory(categories.value[0].categoryId, 0)
  }
})
</script>

<style lang="scss" scoped>
.category-container {
  height: 88vh;
  display: flex;
  flex-direction: column;
  background-color: #f8f9fa;
}

/* 搜索栏样式 */
.search-bar {
  padding: 20rpx 30rpx;
  background-color: #fff;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
}

.search-input {
  display: flex;
  align-items: center;
  background-color: #f5f7fa;
  border-radius: 40rpx;
  padding: 16rpx 24rpx;

  input {
    flex: 1;
    font-size: 28rpx;
    margin: 0 20rpx;
  }

  .placeholder {
    color: #999;
  }
}

/* 主体内容样式 */
.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧分类导航 */
.category-nav {
  width: 200rpx;
  background-color: #f8f9fa;
  border-right: 1rpx solid #eee;
}

.nav-item {
  position: relative;
  padding: 30rpx 20rpx;
  font-size: 26rpx;
  color: #666;
  text-align: center;
  transition: all 0.2s;

  &.active {
    color: #6190e8;
    background-color: #fff;
    font-weight: 600;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 6rpx;
      height: 40rpx;
      background-color: #6190e8;
      border-radius: 0 6rpx 6rpx 0;
    }
  }
}

/* 右侧内容区域 */
.content-area {
  flex: 1;
  padding: 0 20rpx;
  background-color: #fff;
}

/* 分区标题样式 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 0 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.section-more {
  font-size: 24rpx;
  color: #909399;
}

/* 试卷卡片样式 */
.paper-list {
  padding-bottom: 20rpx;
}

.paper-card {
  background-color: #f8f9fa;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  transition: all 0.3s;

  &:active {
    transform: translateY(4rpx);
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  }
}

.paper-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;
}

.paper-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  flex: 1;
  margin-right: 20rpx;
}

.paper-score {
  font-size: 24rpx;
  color: #6190e8;
  font-weight: 500;
}

.paper-content {
  margin-bottom: 16rpx;
}

.paper-desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.4;
}

.paper-footer {
  display: flex;
  justify-content: space-between;
  font-size: 24rpx;
  color: #909399;
  margin-bottom: 12rpx;
}

.paper-tags {
  margin-top: 16rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.tag {
  font-size: 20rpx;
  color: #6190e8;
  background-color: #e8f0fe;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
}

/* 问题卡片样式 */
.question-list {
  padding-bottom: 30rpx;
}

.question-card {
  background-color: #f8f9fa;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  transition: all 0.3s;

  &:active {
    transform: translateY(4rpx);
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  }
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;
}

.question-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  line-height: 1.4;
  flex: 1;
  margin-right: 20rpx;
}

.difficulty-badge {
  font-size: 22rpx;
  padding: 6rpx 12rpx;
  border-radius: 20rpx;
  font-weight: 500;
  white-space: nowrap;
}

.difficulty-easy {
  background-color: #e8f5e9;
  color: #67c23a;
}

.difficulty-medium {
  background-color: #fff8e1;
  color: #e6a23c;
}

.difficulty-hard {
  background-color: #ffebee;
  color: #f56c6c;
}

.question-content {
  margin-bottom: 20rpx;
}

.question-desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.question-footer {
  display: flex;
  justify-content: space-between;
  font-size: 24rpx;
  color: #909399;
}

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 50rpx 0;
}

.empty-title {
  font-size: 32rpx;
  color: #666;
}

.empty-tip {
  font-size: 26rpx;
  color: #999;
  margin-top: 10rpx;
}
</style>
