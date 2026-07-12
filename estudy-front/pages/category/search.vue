<template>
	<view class="search-container">
		<!-- 搜索栏 -->
		<view class="search-bar">
			<view class="search-input-wrapper">
				<uni-icons type="search" size="18" color="#999" class="search-icon"></uni-icons>
				<input type="text" placeholder="搜索题目或知识点" placeholder-class="placeholder" v-model="searchKeyword"
					@confirm="handleSearch" class="search-input" />
				<uni-icons v-if="searchKeyword" type="clear" size="18" color="#999" @click="clearSearch"
					class="clear-icon"></uni-icons>
			</view>
			<text class="cancel-btn" @click="goBack">取消</text>
		</view>

		<!-- 筛选条件 -->
		<view class="filter-bar">
			<view class="filter-tabs">
				<view v-for="(filter, index) in filters" :key="index"
					:class="['filter-tab', activeFilter === filter.key ? 'active' : '']" @click="changeFilter(filter.key)">
					<text class="filter-text">{{ filter.name }}</text>
					<uni-icons v-if="index === 3" :type="sortOrder === 'desc' ? 'down' : 'up'" size="14"
						color="currentColor"></uni-icons>
				</view>
			</view>

			<!-- 筛选下拉菜单 -->
			<view class="filter-dropdown" v-if="showFilterDropdown">
				<view v-for="(option, key) in getFilterOptions(activeFilter)" :key="key"
					:class="['filter-option', isOptionSelected(option) ? 'selected' : '']"
					@click="selectFilterOption(option)">
					<text>{{ option.label }}</text>
					<uni-icons v-if="isOptionSelected(option)" type="checkmark" size="16" color="#6190E8"></uni-icons>
				</view>
			</view>
		</view>

		<!-- 搜索结果 -->
		<scroll-view class="result-list" scroll-y>
			<!-- 结果统计 -->
			<view class="result-stats">
				<text>找到 {{ searchResults.length }} 条相关结果</text>
			</view>

			<!-- 结果列表 -->
			<view class="result-items">
				<view v-for="(item, index) in searchResults" :key="index" class="result-card"
					@click="viewQuestion(item)">
					<view class="question-header">
						<text class="question-title">{{ item.content }}</text>
						<view :class="['difficulty-badge', `difficulty-${DIFFICULTY_TYPE[item.difficulty].value}`]">
							{{ DIFFICULTY_TYPE[item.difficulty].label }}
						</view>
					</view>

					<view class="question-content">
						<text class="question-desc">{{ item.analysis }}</text>
					</view>

					<view class="question-footer">
						<view class="category-tag">
							<text>{{ categoryOptions[item.categoryId].label }}</text>
						</view>
						<view class="stats">
							<text class="answer-count">{{ item.answerCount }}人答过</text>
							<text class="success-rate">正确率: {{ item.correctRate }}%</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 空状态 -->
			<view v-if="searchResults.length === 0" class="empty-state">
				<image src="/static/search-empty.png" class="empty-image"></image>
				<text class="empty-title">未找到相关题目</text>
				<text class="empty-tip">换个关键词试试吧</text>
			</view>

			<view v-if="!hasMore" class="no-more">
				<text>没有更多数据了</text>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
	import {
		onLoad,
		onReachBottom
	} from '@dcloudio/uni-app'
	import {
		ref,
		reactive,
		computed,
		inject
	} from 'vue'
	import { DIFFICULTY_TYPE } from '../../utils/constants.js'
	import { useQuestionStore } from '../../stores/questionStore.js'
	const questionStore = useQuestionStore()
	const proxy = inject('utils')

	// 搜索关键词
	const searchKeyword = ref('')

	// 筛选条件
	const filters = ref([
		{ key: 'all', name: '全部' },
		{ key: 'category', name: '分类' },
		{ key: 'difficulty', name: '难度' },
		{ key: 'sort', name: '排序' }
	])

	// 当前激活的筛选条件
	const activeFilter = ref('all')
	const showFilterDropdown = ref(false)

	// 排序方式
	const sortOrder = ref('desc')

	// 选中的筛选选项
	const selectedOptions = reactive({
		category: null,
		difficulty: null,
		sort: null
	})

	// 分类选项
	const categoryOptions = computed(() => {
		const categories = {}
		questionStore.getCategoryList().forEach(item => {
			categories[item.categoryId] = {
				value: item.categoryId,
				label: item.name
			}
		})
		return categories
	})
	
	// 难度选项
	const difficultyOptions = computed(() => {
		const difficulty = {}
		for (let i = 1; i <= 3; i++) {
			difficulty[i] = {
				label: DIFFICULTY_TYPE[i].label,
				value: i
			}
		}
		return difficulty
	})

	// 排序选项
	const sortOptions = [
		{ value: 'new', label: '最新' },
		{ value: 'difficulty', label: '难度' },
		{ value: 'hot', label: '热度' }
	]

	let page = {
		pageNo: 1,
		pageTotal: 1,
		totalCount: 0
	}

	// 是否还有更多数据
	const hasMore = computed(() => page.pageNo < page.pageTotal)

	// 处理搜索
	const handleSearch = () => {
		uni.redirectTo({
			url: `/pages/category/search?keyword=${searchKeyword.value}`
		})
	}

	// 清空搜索
	const clearSearch = () => {
		searchKeyword.value = ''
	}

	// 返回上一页
	const goBack = () => {
		uni.navigateBack()
	}

	// 切换筛选条件
	const changeFilter = (filter) => {
		showFilterDropdown.value = (activeFilter.value === filter) ? !showFilterDropdown.value : true
		activeFilter.value = filter
		if (activeFilter.value === 'all') {
			Object.keys(selectedOptions).forEach(key => selectedOptions[key] = null)
			resetData()
			loadData()
		}
	}

	// 获取筛选选项
	const getFilterOptions = (filter) => {
		switch (filter) {
			case 'category':
				return categoryOptions.value
			case 'difficulty':
				return difficultyOptions.value
			case 'sort':
				return sortOptions
			default:
				return []
		}
	}

	// 检查选项是否被选中
	const isOptionSelected = (option) => {
		return selectedOptions[activeFilter.value] === option.value
	}

	// 选择筛选选项
	const selectFilterOption = (option) => {
		selectedOptions[activeFilter.value] = option.value
		showFilterDropdown.value = false

		// 如果是排序选项，切换排序方式
		if (activeFilter.value === 3) {
			sortOrder.value = sortOrder.value === 'desc' ? 'asc' : 'desc'
		}
		
		resetData()
		loadData()
	}

	// 模拟搜索结果数据
	const searchResults = ref([])
	
	const loadData = async () => {
		let result = await proxy.Request({
			url: proxy.Api.search,
			showLoading: false,
			params: {
				keyword: searchKeyword.value,
				categoryId: selectedOptions.category,
				difficulty: selectedOptions.difficulty,
				sort: selectedOptions.sort,
				sortOrder: sortOrder.value,
				pageNo: page.pageNo
			}
		})
		if (!result) {
			return
		}
		searchResults.value = searchResults.value.concat(result.data.list)
		page.pageNo = result.data.pageNo
		page.pageTotal = result.data.pageTotal
		page.totalCount = result.data.totalCount
	}
	
	const resetData = () => {
		page = {
			pageNo: 1,
			pageTotal: 1,
			totalCount: 0
		}
		searchResults.value = []
	}
	
	// 查看题目详情
	const viewQuestion = (question) => {
		uni.navigateTo({
			url: `/pages/question/detail?id=${question.questionId}`
		})
	}

	onLoad((options) => {
		searchKeyword.value = options.keyword
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
	.search-container {
		background-color: #f8f9fa;
	}

	/* 搜索栏样式 */
	.search-bar {
		display: flex;
		align-items: center;
		padding: 20rpx 30rpx;
		background-color: #fff;
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
		position: sticky;
		top: 5vh;
		z-index: 100;
	}

	.search-input-wrapper {
		flex: 1;
		display: flex;
		align-items: center;
		background-color: #f5f7fa;
		border-radius: 40rpx;
		padding: 16rpx 24rpx;
		margin-right: 20rpx;
	}

	.search-input {
		flex: 1;
		font-size: 28rpx;
		margin: 0 20rpx;
	}

	.placeholder {
		color: #999;
	}

	.cancel-btn {
		font-size: 30rpx;
		color: #6190E8;
	}

	/* 筛选栏样式 */
	.filter-bar {
		background-color: #fff;
		position: relative;
		z-index: 90;
	}

	.filter-tabs {
		display: flex;
		justify-content: space-around;
		padding: 20rpx 0;
		border-bottom: 1rpx solid #eee;
	}

	.filter-tab {
		display: flex;
		align-items: center;
		font-size: 28rpx;
		color: #666;
		padding: 10rpx 20rpx;
		border-radius: 30rpx;
		transition: all 0.3s;

		&.active {
			color: #6190E8;
			background-color: rgba(97, 144, 232, 0.1);
		}
	}

	.filter-dropdown {
		position: absolute;
		top: 100%;
		left: 0;
		right: 0;
		background-color: #fff;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
		border-radius: 0 0 16rpx 16rpx;
		z-index: 95;
		max-height: 560rpx;
		overflow-y: auto;
	}

	.filter-option {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 24rpx 30rpx;
		font-size: 28rpx;
		color: #333;
		border-bottom: 1rpx solid #f5f7fa;

		&.selected {
			color: #6190E8;
		}
	}

	/* 结果列表样式 */
	.result-list {
		width: auto;
		padding: 0 30rpx;
	}

	.result-stats {
		padding: 30rpx 0 20rpx;
		font-size: 26rpx;
		color: #909399;
	}

	.result-items {
		padding-bottom: 30rpx;
	}

	.result-card {
		background-color: #fff;
		border-radius: 16rpx;
		padding: 24rpx;
		margin-bottom: 20rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
		transition: all 0.3s;

		&:active {
			transform: translateY(4rpx);
			box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
		}
	}

	.question-header {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		margin-bottom: 16rpx;
	}

	.question-title {
		font-size: 30rpx;
		font-weight: 600;
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
		color: #67C23A;
	}

	.difficulty-medium {
		background-color: #fff8e1;
		color: #E6A23C;
	}

	.difficulty-hard {
		background-color: #ffebee;
		color: #F56C6C;
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
		align-items: center;
	}

	.category-tag {
		display: flex;
		align-items: center;
		font-size: 24rpx;
		color: #666;
	}

	.stats {
		display: flex;
		flex-direction: column;
		align-items: flex-end;
		font-size: 24rpx;
		color: #909399;
	}

	.answer-count {
		margin-bottom: 4rpx;
	}

	/* 空状态样式 */
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 100rpx 0;
	}

	.empty-image {
		width: 300rpx;
		height: 300rpx;
		margin-bottom: 30rpx;
		opacity: 0.8;
	}

	.empty-title {
		font-size: 32rpx;
		color: #666;
		margin-bottom: 15rpx;
	}

	.empty-tip {
		font-size: 26rpx;
		color: #999;
	}

	.no-more {
		padding: 0 0 30rpx;
		display: flex;
		justify-content: center;
		align-items: center;
		color: #282c35;
		font-size: 28rpx;
	}
</style>