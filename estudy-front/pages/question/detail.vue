<template>
	<view class="question-detail-container">
		<!-- 题目卡片 -->
		<view class="question-card" v-if="questionData">
			<!-- 题目头部 -->
			<view class="question-header">
				<view :class="['difficulty-badge', DIFFICULTY_TYPE[questionData.difficulty]?.value]">
					{{ DIFFICULTY_TYPE[questionData.difficulty]?.label || '未知' }}
				</view>
				<view class="question-type">
					{{ QUESTION_TYPE[questionData.type] }}
				</view>
			</view>

			<!-- 题目内容 -->
			<view class="question-content">
				<text class="question-title">{{ questionData.content }}</text>
			</view>

			<!-- 未解锁提示 -->
			<view class="unlock-tip" v-if="!questionData.unLock">
				<view class="tip-content">
					<uni-icons type="lock" size="32" color="#FF9F43"></uni-icons>
					<text class="tip-title">题目未解锁</text>
					<text class="tip-text">请先购买相关试卷以解锁该题目</text>
					<button class="unlock-btn" @click="goToExam">前往解锁</button>
				</view>
			</view>

			<!-- 题目选项 -->
			<view class="options-section"
				v-if="questionData.options && parsedOptions.length > 0 && questionData.unLock">
				<view class="options-title">选项</view>
				<view class="options-list">
					<view v-for="(option, index) in parsedOptions" :key="index"
						:class="['option-item', isCorrectOption(option.key) ? 'correct' : '']">
						<view class="option-label">{{ option.key }}.</view>
						<view class="option-text">{{ option.value }}</view>
						<view class="option-check" v-if="isCorrectOption(option.key)">
							<uni-icons type="checkmark" size="16" color="#5CD89E"></uni-icons>
						</view>
					</view>
				</view>
			</view>

			<!-- 答案区域 -->
			<view class="answer-section" v-if="questionData.unLock">
				<view class="answer-header">
					<uni-icons type="info" size="20" color="#6190E8"></uni-icons>
					<text class="answer-title">正确答案</text>
				</view>
				<view class="answer-content">
					<text class="answer-text">{{ formatAnswer(questionData.answer) }}</text>
				</view>
			</view>

			<!-- 解析区域 -->
			<view class="analysis-section" v-if="questionData.analysis && questionData.unLock">
				<view class="analysis-header">
					<uni-icons type="help" size="20" color="#FF9F43"></uni-icons>
					<text class="analysis-title">题目解析</text>
				</view>
				<view class="analysis-content">
					<text class="analysis-text">{{ questionData.analysis }}</text>
				</view>
			</view>
		</view>

		<!-- 加载状态 -->
		<view class="loading-section" v-if="isLoading">
			<uni-icons type="spinner-cycle" size="32" color="#6190E8" class="loading-icon"></uni-icons>
			<text class="loading-text">加载题目详情中...</text>
		</view>

		<!-- 错误状态 -->
		<view class="error-section" v-if="!isLoading && !questionData">
			<image src="/static/error.png" class="error-image"></image>
			<text class="error-title">题目加载失败</text>
			<text class="error-tip">请检查网络连接或刷新重试</text>
			<button class="retry-btn" @click="loadQuestionDetail">重新加载</button>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		computed,
		inject
	} from 'vue'
	import { onLoad } from '@dcloudio/uni-app'
	import {
		DIFFICULTY_TYPE,
		QUESTION_TYPE
	} from '../../utils/constants.js'
	const proxy = inject('utils')

	// 响应式数据
	const questionData = ref(null)
	const isLoading = ref(false)
	const questionId = ref('')

	// 解析后的选项
	const parsedOptions = computed(() => {
		if (!questionData.value || !questionData.value.options) return []

		try {
			// 假设options是JSON字符串，如：{"A": "选项A内容", "B": "选项B内容"}
			const optionsObj = JSON.parse(questionData.value.options)
			return Object.keys(optionsObj).map(key => ({
				key: key,
				value: optionsObj[key]
			}))
		} catch (e) {
			console.error('解析选项失败:', e)
			return []
		}
	})

	// 加载题目详情
	const loadQuestionDetail = async () => {
		if (!questionId.value) return

		isLoading.value = true
		questionData.value = null

		try {
			let result = await proxy.Request({
				url: proxy.Api.getQuestionDetail,
				params: {
					questionId: questionId.value,
				}
			})
			if (!result) {
				return
			}
			isLoading.value = false
			questionData.value = result.data

		} catch (error) {
			isLoading.value = false
			proxy.Message.error('加载失败')
		}
	}

	// 检查是否为正确答案
	const isCorrectOption = (optionKey) => {
		if (!questionData.value || !questionData.value.answer) return false
		return questionData.value.answer.includes(optionKey)
	}

	// 格式化答案显示
	const formatAnswer = (answer) => {
		if (!answer) return ''

		// 如果是多选题，答案可能是"AB"、"AC"等形式
		if (questionData.value.type === 1 && answer.length > 1) {
			return answer.split('').join(' ')
		}

		return answer
	}

	// 前往解锁
	const goToExam = () => {
		uni.navigateTo({
			url: `/pages/exam/paper-detail?id=${questionData.value.paperId}`
		})
	}

	onLoad((options) => {
		if (options.id) {
			questionId.value = options.id
			loadQuestionDetail()
		}
	})
</script>

<style lang="scss" scoped>
	.question-detail-container {
		padding: 30rpx;
		min-height: 100vh;
		background: linear-gradient(135deg, #f8faff 0%, #e6f0ff 100%);
	}

	/* 题目卡片 */
	.question-card {
		background-color: #fff;
		border-radius: 24rpx;
		padding: 40rpx;
		box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
		margin-bottom: 30rpx;
	}

	/* 题目头部 */
	.question-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 30rpx;
		padding-bottom: 20rpx;
		border-bottom: 1rpx solid #f0f0f0;
	}

	.difficulty-badge {
		font-size: 24rpx;
		padding: 8rpx 20rpx;
		border-radius: 40rpx;
		font-weight: 500;

		&.easy {
			background-color: rgba(92, 216, 158, 0.1);
			color: #5CD89E;
		}

		&.medium {
			background-color: rgba(255, 159, 67, 0.1);
			color: #FF9F43;
		}

		&.hard {
			background-color: rgba(255, 94, 94, 0.1);
			color: #FF5E5E;
		}
	}

	.question-type {
		font-size: 24rpx;
		color: #909399;
		background-color: #f8f9fa;
		padding: 6rpx 16rpx;
		border-radius: 20rpx;
	}

	/* 题目内容 */
	.question-content {
		margin-bottom: 40rpx;
	}

	.question-title {
		font-size: 34rpx;
		font-weight: 600;
		color: #1a1a1a;
		line-height: 1.6;
	}

	/* 未解锁提示 */
	.unlock-tip {
		background: linear-gradient(135deg, #FFF9F0 0%, #FFF3E0 100%);
		border-radius: 16rpx;
		padding: 50rpx 30rpx;
		margin-bottom: 40rpx;
		text-align: center;
		border: 2rpx dashed #FF9F43;
	}

	.tip-content {
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.tip-title {
		font-size: 32rpx;
		font-weight: 600;
		color: #FF9F43;
		margin: 20rpx 0 12rpx;
	}

	.tip-text {
		font-size: 26rpx;
		color: #666;
		margin-bottom: 30rpx;
		line-height: 1.5;
	}

	.unlock-btn {
		background: linear-gradient(135deg, #FF9F43 0%, #FF8C00 100%);
		color: #fff;
		height: 70rpx;
		line-height: 70rpx;
		border-radius: 12rpx;
		font-size: 28rpx;
		font-weight: 500;
		padding: 0 40rpx;

		&::after {
			border: none;
		}
	}

	/* 选项区域 */
	.options-section {
		margin-bottom: 40rpx;
	}

	.options-title {
		font-size: 28rpx;
		font-weight: 600;
		color: #333;
		margin-bottom: 24rpx;
	}

	.options-list {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.option-item {
		display: flex;
		align-items: center;
		background-color: #f8f9fa;
		border-radius: 12rpx;
		padding: 24rpx;
		transition: all 0.3s;
		border: 2rpx solid transparent;

		&.correct {
			background-color: rgba(92, 216, 158, 0.1);
			border-color: #5CD89E;
		}
	}

	.option-label {
		font-size: 28rpx;
		font-weight: 600;
		color: #6190E8;
		margin-right: 20rpx;
	}

	.option-text {
		flex: 1;
		font-size: 28rpx;
		color: #333;
		line-height: 1.5;
	}

	.option-check {
		margin-left: 16rpx;
		flex-shrink: 0;
	}

	/* 答案区域 */
	.answer-section {
		background-color: rgba(97, 144, 232, 0.05);
		border-radius: 0 16rpx 16rpx 0;
		padding: 30rpx;
		margin-bottom: 30rpx;
		border-left: 4rpx solid #6190E8;
		box-shadow: 5rpx 5rpx 8rpx rgba(104, 143, 231, 0.25);
	}

	.answer-header {
		display: flex;
		align-items: center;
		margin-bottom: 16rpx;
	}

	.answer-title {
		font-size: 28rpx;
		font-weight: 600;
		color: #6190E8;
		margin-left: 12rpx;
	}

	.answer-content {
		padding-left: 32rpx;
	}

	.answer-text {
		font-size: 30rpx;
		font-weight: 600;
		color: #1a1a1a;
		line-height: 1.5;
	}

	/* 解析区域 */
	.analysis-section {
		background-color: rgba(255, 159, 67, 0.05);
		border-radius: 0 16rpx 16rpx 0;
		padding: 30rpx;
		border-left: 4rpx solid #FF9F43;
		box-shadow: 5rpx 5rpx 8rpx rgba(250, 160, 72, 0.25);
	}

	.analysis-header {
		display: flex;
		align-items: center;
		margin-bottom: 16rpx;
	}

	.analysis-title {
		font-size: 28rpx;
		font-weight: 600;
		color: #FF9F43;
		margin-left: 12rpx;
	}

	.analysis-content {
		padding-left: 32rpx;
	}

	.analysis-text {
		font-size: 28rpx;
		color: #666;
		line-height: 1.6;
	}

	/* 加载状态 */
	.loading-section {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 100rpx 0;
	}

	.loading-icon {
		animation: rotate 1s linear infinite;
		margin-bottom: 20rpx;
	}

	.loading-text {
		font-size: 28rpx;
		color: #909399;
	}

	@keyframes rotate {
		from {
			transform: rotate(0deg);
		}

		to {
			transform: rotate(360deg);
		}
	}

	/* 错误状态 */
	.error-section {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 100rpx 0;
		text-align: center;
	}

	.error-image {
		width: 200rpx;
		height: 200rpx;
		margin-bottom: 30rpx;
		opacity: 0.8;
	}

	.error-title {
		font-size: 32rpx;
		color: #666;
		margin-bottom: 15rpx;
	}

	.error-tip {
		font-size: 26rpx;
		color: #999;
		margin-bottom: 40rpx;
	}

	.retry-btn {
		background: linear-gradient(135deg, #6190E8 0%, #3A6FE8 100%);
		color: #fff;
		height: 80rpx;
		line-height: 80rpx;
		border-radius: 16rpx;
		font-size: 28rpx;
		font-weight: 500;
		padding: 0 40rpx;

		&::after {
			border: none;
		}
	}
</style>