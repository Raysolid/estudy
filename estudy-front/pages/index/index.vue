<template>
	<view class="container">
		<!-- 学习进度横幅 -->
		<view class="progress-banner">
			<view class="banner-content">
				<view class="banner-text">
					<text class="banner-title">今日学习进度</text>
					<text class="banner-subtitle">学无止境，习无终点！</text>
				</view>
				<view class="trophy">
					<uni-icons type="medal" size="40" color="#ffd700"></uni-icons>
				</view>
			</view>
			<view class="banner-stats">
				<view class="stat-item">
					<text class="stat-value">{{ studyData.todayTime }}分钟</text>
					<text class="stat-label">今日学习</text>
				</view>
				<view class="stat-item">
					<text class="stat-value">{{ studyData.continuousSignCount }}</text>
					<text class="stat-label">连续天数</text>
				</view>
				<view class="stat-item">
					<text class="stat-value">{{ studyData.totalExams }}</text>
					<text class="stat-label">累计练习</text>
				</view>
			</view>
		</view>
		
		<!-- 快速操作 -->
		<view class="quick-actions">
			<view class="action-item" @click="startRandomPractice">
				<view class="action-icon">
					<uni-icons type="refresh" size="24" color="#6190E8"></uni-icons>
				</view>
				<text class="action-text">随机练习</text>
			</view>
			<view class="action-item" @click="viewWrongQuestions">
				<view class="action-icon">
					<uni-icons type="info" size="24" color="#FF6B6B"></uni-icons>
				</view>
				<text class="action-text">错题复习</text>
			</view>
			<view class="action-item" @click="viewStudyReport">
				<view class="action-icon">
					<uni-icons type="bars" size="24" color="#5CD89E"></uni-icons>
				</view>
				<text class="action-text">学习记录</text>
			</view>
		</view>

		<!-- 推荐试卷模块 -->
		<view class="recommend-section" v-if="recommendPapers.length > 0">
			<view class="section-header">
				<text class="section-title">推荐试题</text>
			</view>
			<view class="paper-list" scroll-y>
				<view 
					v-for="paper in recommendPapers" 
					:key="paper.paperId" 
					class="paper-card"
					@click="startPaper(paper.paperId)"
				>
					<view class="paper-header">
						<text class="paper-name">{{ paper.name }}</text>
						<view class="paper-points" v-if="paper.pointsRequired">
							<uni-icons type="star" size="16" color="#FFB800"></uni-icons>
							<text>{{ paper.pointsRequired }}积分</text>
						</view>
					</view>
					<text class="paper-desc">{{ paper.description }}</text>
					<view class="paper-meta">
						<view class="meta-item">
							<text>{{ paper.questionCount }}题</text>
						</view>
						<view class="meta-item">
							<text>{{ paper.duration }}分钟</text>
						</view>
						<view class="meta-item">
							<text>{{ paper.totalScore }}分</text>
						</view>
					</view>
					<view class="paper-tags">
						<text 
							v-for="(tag, index) in getPaperTags(paper.tag)" 
							:key="index"
							class="tag"
						>
							{{ tag }}
						</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 空状态 -->
		<view class="empty-state" v-if="recommendPapers.length === 0 && !loading">
			<image src="/static/empty-paper.png" class="empty-image"></image>
			<text class="empty-title">暂无推荐试卷</text>
			<text class="empty-tip">先去完成一些练习吧</text>
		</view>
	</view>
</template>

<script setup>
	import {
		onShow
	} from '@dcloudio/uni-app'
	import {
		reactive,
		ref,
		inject
	} from 'vue'
	import { LOCAL_STORAGE_KEY } from '../../utils/constants'
	import { useQuestionStore } from '../../stores/questionStore.js'
	const questionStore = useQuestionStore()
	const proxy = inject('utils')

	// 学习数据
	const studyData = reactive({
		todayTime: 15,
		continuousSignCount: 0,
		totalExams: 0
	})

	// 推荐试卷数据
	const recommendPapers = ref([])
	const loading = ref(false)
	
	// 获取统计数据
	const getStudyData = async () => {
		let result = await proxy.Request({
			url: proxy.Api.getUserStats,
			showLoading: false
		})
		if (!result) {
			return
		}
		Object.assign(studyData, result.data)
	}
	
	// 获取分类列表
	const getCategoryList = async () => {
		let result = await proxy.Request({
			url: proxy.Api.getCategoryList,
			showLoading: false
		})
		if (!result) {
			return
		}
		questionStore.setCategoryList(result.data)
	}

	// 获取推荐试卷
	const getRecommendPapers = async () => {
		loading.value = true
		let result = await proxy.Request({
			url: proxy.Api.recommendPapers,
			showLoading: false
		})
		loading.value = false
		if (!result) {
			return
		}
		recommendPapers.value = result.data
	}

	// 解析试卷标签
	const getPaperTags = (tagString) => {
		if (!tagString) return []
		return tagString.split(',').slice(0, 2) // 只显示前两个标签
	}

	// 快速操作功能
	const startRandomPractice = () => {
		const randomId = Math.floor(Math.random() * studyData.paperCount) + 1
		uni.navigateTo({
			url: `/pages/exam/paper-detail?id=${randomId}`
		})
	}

	const viewWrongQuestions = () => {
		uni.navigateTo({
			url: '/pages/wrong/wrong'
		})
	}

	const viewStudyReport = () => {
		uni.navigateTo({
			url: '/pages/report/report'
		})
	}

	// 开始做试卷
	const startPaper = (paperId) => {
		uni.navigateTo({
			url: `/pages/exam/paper-detail?id=${paperId}`
		})
	}
	
	// 自动登录
	const autoLogin = async () => {
		let result = await proxy.Request({
			url: proxy.Api.autoLogin,
			showLoading: false
		})
		if (!result) {
			return
		}
		uni.setStorageSync(LOCAL_STORAGE_KEY.token.key, result.data.token)
	}
	
	onShow(() => {
		autoLogin()
		getStudyData()
		getCategoryList()
		getRecommendPapers()
	})
</script>

<style lang="scss" scoped>
	.container {
		padding: 20rpx 25rpx 40rpx;
		background-color: #f8f9fa;
	}

	// 学习进度横幅样式
	.progress-banner {
		background: linear-gradient(135deg, #6190E8 0%, #3A6FE8 100%);
		border-radius: 20rpx;
		padding: 30rpx;
		margin-bottom: 30rpx;
		box-shadow: 0 8rpx 24rpx rgba(97, 144, 232, 0.3);
		color: #fff;
	}

	.banner-content {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 25rpx;
	}

	.banner-text {
		flex: 1;
	}

	.banner-title {
		display: block;
		font-size: 34rpx;
		font-weight: 600;
		margin-bottom: 8rpx;
	}

	.banner-subtitle {
		font-size: 26rpx;
		opacity: 0.9;
	}

	.trophy {
		width: 100rpx;
		height: 100rpx;
		background-color: rgba(255, 255, 255, 0.15);
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.banner-stats {
		display: flex;
		justify-content: space-around;
		border-top: 1rpx solid rgba(255, 255, 255, 0.2);
		padding-top: 25rpx;
	}

	.stat-item {
		text-align: center;
	}

	.stat-value {
		display: block;
		font-size: 32rpx;
		font-weight: 600;
		margin-bottom: 5rpx;
	}

	.stat-label {
		font-size: 24rpx;
		opacity: 0.8;
	}

	// 快速操作样式
	.quick-actions {
		display: flex;
		justify-content: space-between;
		column-gap: 30rpx;
		margin: 30rpx 0;
	}

	.action-item {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 20rpx;
		background-color: #fff;
		border-radius: 16rpx;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
		transition: all 0.3s ease;

		&:active {
			transform: translateY(4rpx);
			box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
		}
	}

	.action-icon {
		width: 60rpx;
		height: 60rpx;
		border-radius: 50%;
		background-color: #f0f4ff;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-bottom: 15rpx;
	}

	.action-text {
		font-size: 24rpx;
		color: #666;
		font-weight: 500;
	}

	// 推荐试卷模块样式
	.recommend-section {
		background-color: #fff;
		border-radius: 20rpx;
		padding: 30rpx;
		margin-top: 30rpx;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
	}

	.section-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 25rpx;
	}

	.section-title {
		font-size: 32rpx;
		font-weight: 600;
		color: #1a1a1a;
		position: relative;
		padding-left: 16rpx;
		
		&::before {
			content: '';
			position: absolute;
			left: 0;
			top: 8rpx;
			bottom: 8rpx;
			width: 6rpx;
			background-color: #6190E8;
			border-radius: 3rpx;
		}
	}

	.paper-list {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.paper-card {
		background-color: #f8f9fa;
		border-radius: 16rpx;
		padding: 24rpx;
		flex-shrink: 0;
		display: flex;
		flex-direction: column;
		transition: all 0.3s ease;
		
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

	.paper-name {
		font-size: 28rpx;
		font-weight: 600;
		color: #333;
		line-height: 1.4;
		flex: 1;
		margin-right: 12rpx;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
		white-space: normal;
	}

	.paper-points {
		display: flex;
		align-items: center;
		background-color: rgba(255, 184, 0, 0.1);
		color: #FFB800;
		font-size: 20rpx;
		padding: 4rpx 8rpx;
		border-radius: 8rpx;
		flex-shrink: 0;
		
		text {
			margin-left: 4rpx;
		}
	}

	.paper-desc {
		font-size: 24rpx;
		color: #666;
		line-height: 1.4;
		margin-bottom: 20rpx;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
	}

	.paper-meta {
		display: flex;
		justify-content: space-between;
		margin-bottom: 20rpx;
	}

	.meta-item {
		display: flex;
		align-items: center;
		font-size: 22rpx;
		color: #909399;
		
		text {
			margin-left: 4rpx;
		}
	}

	.paper-tags {
		display: flex;
		flex-wrap: wrap;
		gap: 8rpx;
	}

	.tag {
		font-size: 20rpx;
		color: #6190E8;
		background-color: rgba(97, 144, 232, 0.1);
		padding: 4rpx 12rpx;
		border-radius: 12rpx;
	}

	// 空状态样式
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 80rpx 0;
		background-color: #fff;
		border-radius: 20rpx;
		margin-top: 30rpx;
	}

	.empty-image {
		width: 200rpx;
		height: 200rpx;
		margin-bottom: 30rpx;
		opacity: 0.6;
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
</style>