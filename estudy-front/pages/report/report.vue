<template>
	<view class="record-list-container">
		<!-- 顶部标题 -->
		<view class="list-header">
			<text class="header-title">考试记录</text>
			<text class="header-subtitle">共 {{ recordList.length }} 次考试</text>
		</view>

		<!-- 筛选栏 -->
		<view class="filter-bar">
			<view class="filter-tabs">
				<view v-for="(filter, index) in filters" :key="index"
					:class="['filter-tab', activeFilter === filter.key ? 'active' : '']"
					@click="changeFilter(filter.key)">
					<text class="filter-text">{{ filter.name }}</text>
					<text class="filter-count">({{ filter.count }})</text>
				</view>
			</view>
		</view>

		<!-- 记录列表 -->
		<scroll-view class="record-list" scroll-y>
			<view v-for="(record, index) in filteredRecords" :key="record.recordId" class="record-item"
				@click="viewRecordDetail(record.recordId)">
				<view class="record-main">
					<view class="record-header">
						<text class="record-name">{{ record.name }}</text>
						<view :class="['status-badge', getPassStatus(record) ? 'passed' : 'failed']">
							{{ getPassStatus(record) ? '通过' : '未通过' }}
						</view>
					</view>

					<text class="record-description">{{ record.description }}</text>

					<view class="record-stats">
						<view class="stat-item">
							<text class="stat-value">{{ record.score }}</text>
							<text class="stat-label">分数</text>
						</view>
						<view class="stat-item">
							<text class="stat-value">{{ record.questionCount }}</text>
							<text class="stat-label">题数</text>
						</view>
						<view class="stat-item">
							<text class="stat-value">{{ Math.round(record.duration / 60) }}</text>
							<text class="stat-label">分钟</text>
						</view>
						<view class="stat-item">
							<text class="stat-value">{{ calculateAccuracy(record) }}%</text>
							<text class="stat-label">正确率</text>
						</view>
					</view>
				</view>

				<view class="record-footer">
					<view class="record-time">
						<uni-icons type="calendar" size="16" color="#909399"></uni-icons>
						<text class="time-text">{{ formatTime(record.startTime) }}</text>
					</view>
					<view class="record-arrow">
						<uni-icons type="right" size="16" color="#C0C4CC"></uni-icons>
					</view>
				</view>
			</view>

			<!-- 空状态 -->
			<view v-if="filteredRecords.length === 0" class="empty-state">
				<image src="/static/images/empty.png" class="empty-image"></image>
				<text class="empty-title">暂无考试记录</text>
				<text class="empty-tip">快去参加考试吧</text>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
	import {
		ref,
		reactive,
		computed,
		inject
	} from 'vue'
	import {
		onLoad
	} from '@dcloudio/uni-app'
import { onReachBottom } from '@dcloudio/uni-app'
	const proxy = inject('utils')

	// 筛选条件
	const filters = ref([{
			key: 'all',
			name: '全部',
			count: 0
		},
		{
			key: 'passed',
			name: '已通过',
			count: 0
		},
		{
			key: 'failed',
			name: '未通过',
			count: 0
		}
	])

	// 当前激活的筛选条件
	const activeFilter = ref('all')

	// 考试记录数据
	const recordList = ref([])
	let page = {
		pageNo: 1,
		pageTotal: 1,
		totalCount: 0
	}
	
	const loadRecords = async () => {
		let result = await proxy.Request({
			url: proxy.Api.getExamRecord,
			showLoading: false,
			params: {
				pageNo: page.pageNo
			}
		})
		if (!result) {
			return
		}
		recordList.value = recordList.value.concat(result.data.list)
		page.pageNo = result.data.pageNo
		page.pageTotal = result.data.pageTotal
		page.totalCount = result.data.totalCount
	}

	// 计算筛选后的记录
	const filteredRecords = computed(() => {
		if (activeFilter.value === 'all') {
			return recordList.value
		} else if (activeFilter.value === 'passed') {
			return recordList.value.filter(record => getPassStatus(record))
		} else if (activeFilter.value === 'failed') {
			return recordList.value.filter(record => !getPassStatus(record))
		}
		return recordList.value
	})

	// 计算正确率
	const calculateAccuracy = (record) => {
		return Math.round((record.score / record.totalScore) * 100)
	}

	// 判断是否通过（60%为通过）
	const getPassStatus = (record) => {
		return calculateAccuracy(record) >= 60
	}

	// 格式化时间
	const formatTime = (timeString) => {
		const date = new Date(timeString)
		const now = new Date()
		const diffTime = now.getTime() - date.getTime()
		const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))

		if (diffDays === 0) {
			return '今天'
		} else if (diffDays === 1) {
			return '昨天'
		} else if (diffDays < 7) {
			return `${diffDays}天前`
		} else {
			const month = String(date.getMonth() + 1).padStart(2, '0')
			const day = String(date.getDate()).padStart(2, '0')
			return `${date.getFullYear()}-${month}-${day}`
		}
	}

	// 切换筛选条件
	const changeFilter = (filterKey) => {
		activeFilter.value = filterKey
	}

	// 查看记录详情
	const viewRecordDetail = (recordId) => {
		uni.navigateTo({
			url: `/pages/report/report-detail?id=${recordId}`
		})
	}
	
	onLoad(async () => {
		await loadRecords()
		// 计算筛选条件的数量
		filters.value[0].count = recordList.value.length
		filters.value[1].count = recordList.value.filter(record => getPassStatus(record)).length
		filters.value[2].count = recordList.value.filter(record => !getPassStatus(record)).length
	})
	
	onReachBottom(() => {
		if (page.pageNo < page.pageTotal) {
			page.pageNo++
			loadRecords()
		}
	})
</script>

<style lang="scss" scoped>
	.record-list-container {
		padding: 40rpx 30rpx;
	}
	
	/* 头部样式 */
	.list-header {
		margin-bottom: 40rpx;
	}

	.header-title {
		font-size: 42rpx;
		font-weight: 700;
		color: #1a1a1a;
		display: block;
		margin-bottom: 8rpx;
	}

	.header-subtitle {
		font-size: 28rpx;
		color: #909399;
	}

	/* 筛选栏 */
	.filter-bar {
		background-color: #fff;
		border-radius: 16rpx;
		padding: 20rpx;
		margin-bottom: 30rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
	}

	.filter-tabs {
		display: flex;
		justify-content: space-around;
	}

	.filter-tab {
		display: flex;
		align-items: center;
		font-size: 28rpx;
		color: #666;
		padding: 16rpx 24rpx;
		border-radius: 30rpx;
		transition: all 0.3s;

		&.active {
			color: #6190E8;
			background-color: rgba(97, 144, 232, 0.1);
			font-weight: 500;
		}
	}

	.filter-count {
		font-size: 24rpx;
		margin-left: 8rpx;
		opacity: 0.8;
	}

	/* 记录列表 */
	.record-list {
		height: calc(100vh - 300rpx);
	}

	.record-item {
		background-color: #fff;
		border-radius: 24rpx;
		padding: 30rpx;
		margin-bottom: 24rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
		transition: all 0.3s;

		&:active {
			transform: translateY(4rpx);
			box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
		}
	}

	.record-main {
		margin-bottom: 24rpx;
	}

	.record-header {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		margin-bottom: 16rpx;
	}

	.record-name {
		font-size: 32rpx;
		font-weight: 600;
		color: #1a1a1a;
		flex: 1;
		margin-right: 20rpx;
	}

	.status-badge {
		padding: 8rpx 20rpx;
		border-radius: 20rpx;
		font-size: 24rpx;
		font-weight: 500;
		flex-shrink: 0;

		&.passed {
			background-color: rgba(92, 216, 158, 0.1);
			color: #5CD89E;
		}

		&.failed {
			background-color: rgba(255, 94, 94, 0.1);
			color: #FF5E5E;
		}
	}

	.record-description {
		font-size: 26rpx;
		color: #909399;
		line-height: 1.4;
		margin-bottom: 24rpx;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
	}

	.record-stats {
		display: grid;
		grid-template-columns: repeat(4, 1fr);
		gap: 16rpx;
	}

	.stat-item {
		text-align: center;
		padding: 16rpx 8rpx;
		background-color: #f8f9fa;
		border-radius: 12rpx;
	}

	.stat-value {
		font-size: 28rpx;
		font-weight: 700;
		color: #6190E8;
		display: block;
		margin-bottom: 4rpx;
	}

	.stat-label {
		font-size: 22rpx;
		color: #909399;
	}

	.record-footer {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding-top: 20rpx;
		border-top: 1rpx solid #f0f0f0;
	}

	.record-time {
		display: flex;
		align-items: center;
		font-size: 24rpx;
		color: #909399;
	}

	.time-text {
		margin-left: 8rpx;
	}

	.record-arrow {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 40rpx;
		height: 40rpx;
	}

	/* 空状态 */
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 100rpx 0;
	}

	.empty-image {
		width: 100rpx;
		height: 100rpx;
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
</style>