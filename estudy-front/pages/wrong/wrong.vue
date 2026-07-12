<template>
	<view class="wrong-container">
		<!-- 顶部标题栏 -->
		<view class="wrong-header">
			<view class="header-content">
				<text class="header-title">我的错题本</text>
				<text class="header-subtitle">已积累 {{wrongQuestions.length}} 道错题</text>
			</view>
			<image src="/static/images/wrong.png" class="header-icon"></image>
		</view>

		<!-- 错题列表 -->
		<view v-if="wrongQuestions.length > 0" class="wrong-list">
			<view v-for="(item, index) in wrongQuestions" :key="index" class="wrong-card"
				@click="viewWrongQuestion(item.questionId)">
				<view class="card-header">
					<text class="question-title">{{ index+1 }}. {{item.content}}</text>
					<view :class="['difficulty-badge', `difficulty-${DIFFICULTY_MAP[item.difficulty].value}`]">
						{{ DIFFICULTY_MAP[item.difficulty].label }}
					</view>
				</view>

				<view class="answer-comparison">
					<view class="answer-item wrong-answer">
						<text class="answer-label">我的答案</text>
						<text class="answer-content">{{item.userAnswer || '未作答'}}</text>
					</view>
					<view class="answer-item correct-answer">
						<text class="answer-label">正确答案</text>
						<text class="answer-content">{{item.answer}}</text>
					</view>
				</view>

				<view class="card-footer">
					<view class="category-tag">
						<image src="/static/images/backend-icon.png" class="tag-icon"></image>
						<text>{{item.name}}</text>
					</view>
					<view class="review-button">立即复习</view>
				</view>
			</view>
		</view>

		<!-- 空状态 -->
		<view v-else class="empty-state">
			<image src="/static/images/empty.png" class="empty-image"></image>
			<text class="empty-title">暂无错题记录</text>
			<text class="empty-tip">答错的题目会自动收录到这里哦</text>
			<view @click="goBack" class="back-button">
				<image src="/static/images/backend-icon.png" class="button-icon"></image>
				返回练习
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		onLoad,
		onShow,
		onReachBottom
	} from '@dcloudio/uni-app'
	import {
		inject,
		ref
	} from 'vue'
	import { DIFFICULTY_TYPE } from '../../utils/constants.js'
	import { useQuestionStore } from '../../stores/questionStore.js'
	const questionStore = useQuestionStore()
	const proxy = inject('utils')

	const DIFFICULTY_MAP = DIFFICULTY_TYPE.slice(1)
	let page = {
		pageNo: 1,
		pageTotal: 1,
		totalCount: 0
	}
	const wrongQuestions = ref([])

	// 加载数据
	const loadData = async (isRefresh = false) => {
		// 如果是刷新操作，重置分页和数据
		if (isRefresh) {
			page.pageNo = 1
			wrongQuestions.value = []
		}
		let result = await proxy.Request({
			url: proxy.Api.getWrongQuestions,
			showLoading: false,
			params: {
				pageNo: page.pageNo
			}
		})
		if (!result) {
			return
		}
		wrongQuestions.value = wrongQuestions.value.concat(result.data.list)
		page.pageNo = result.data.pageNo
		page.pageTotal = result.data.pageTotal
		page.totalCount = result.data.totalCount
		questionStore.setWrongQuestions(wrongQuestions.value)
	}

	// 获取分类文本
	const getCategoryText = (category) => {
		const map = {
			'frontend': '前端',
			'backend': '后端',
			'database': '数据库'
		}
		return map[category] || '其他'
	}

	// 查看错题详情
	const viewWrongQuestion = (id) => {
		uni.navigateTo({
			url: `/pages/wrong/wrong-detail?id=${id}`
		})
	}

	// 返回练习页面
	const goBack = () => {
		uni.switchTab({
			url: '/pages/index/index'
		})
	}

	// 生命周期
	onLoad(() => {
		loadData()
	})

	onShow(() => {
		if (questionStore.getReloadWrongQuestions()) {
			questionStore.setReloadWrongQuestions(false)
			loadData(true)
		}
	})

	onReachBottom(() => {
		if (page.pageNo < page.pageTotal) {
			page.pageNo++
			loadData()
		}
	})
</script>

<style lang="scss" scoped>
	.wrong-container {
		padding: 0 25rpx 40rpx;
	}

	/* 顶部标题栏 */
	.wrong-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 40rpx 0 30rpx;
	}

	.header-content {
		flex: 1;
	}

	.header-title {
		font-size: 42rpx;
		font-weight: 700;
		color: #1a1a1a;
		display: block;
		margin-bottom: 8rpx;
	}

	.header-subtitle {
		font-size: 26rpx;
		color: #888;
	}

	.header-icon {
		width: 60rpx;
		height: 60rpx;
	}

	/* 错题卡片 */
	.wrong-list {
		margin-top: 20rpx;
	}

	.wrong-card {
		background-color: #fff;
		border-radius: 20rpx;
		padding: 30rpx;
		margin-bottom: 25rpx;
		box-shadow: 0 6rpx 18rpx rgba(0, 0, 0, 0.04);
		transition: all 0.3s ease;

		&:active {
			transform: translateY(4rpx);
			box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
		}
	}

	.card-header {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		margin-bottom: 25rpx;
	}

	.question-title {
		font-size: 30rpx;
		font-weight: 600;
		color: #1a1a1a;
		line-height: 1.5;
		flex: 1;
		margin-right: 20rpx;
	}

	.difficulty-badge {
		font-size: 24rpx;
		padding: 6rpx 16rpx;
		border-radius: 20rpx;
		font-weight: 500;
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

	/* 答案对比 */
	.answer-comparison {
		background-color: #f9fafc;
		border-radius: 12rpx;
		padding: 20rpx;
		margin-bottom: 25rpx;
	}

	.answer-item {
		margin-bottom: 15rpx;

		&:last-child {
			margin-bottom: 0;
		}
	}

	.answer-label {
		font-size: 24rpx;
		font-weight: 500;
		margin-right: 15rpx;
	}

	.wrong-answer .answer-label {
		color: #F56C6C;
	}

	.correct-answer .answer-label {
		color: #67C23A;
	}

	.answer-content {
		font-size: 26rpx;
		color: #333;
	}

	/* 卡片底部 */
	.card-footer {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding-top: 20rpx;
		border-top: 1rpx dashed #eee;
	}

	.category-tag {
		display: flex;
		align-items: center;
		font-size: 24rpx;
		color: #666;
	}

	.tag-icon {
		width: 28rpx;
		height: 28rpx;
		margin-right: 8rpx;
	}

	.review-button {
		background-color: #6190E8;
		color: #fff;
		font-size: 26rpx;
		padding: 10rpx 25rpx;
		border-radius: 40rpx;
		box-shadow: 0 4rpx 12rpx rgba(97, 144, 232, 0.3);
	}

	/* 空状态 */
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 100rpx 0;
	}

	.empty-image {
		width: 160rpx;
		height: 160rpx;
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
		margin-bottom: 40rpx;
	}

	.back-button {
		background-color: #fff;
		color: #6190E8;
		font-size: 28rpx;
		padding: 30rpx 40rpx;
		border-radius: 30rpx;
		display: flex;
		align-items: center;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
		border: none;

		::v-deep & {
			background-color: #fff;
			color: #6190E8;
		}
	}

	.button-icon {
		width: 30rpx;
		height: 30rpx;
		margin-right: 10rpx;
	}
</style>