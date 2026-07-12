<template>
	<view class="container">
		<!-- 题目卡片 -->
		<view class="question-card">
			<!-- 题目头部 -->
			<view class="question-header">
				<view class="meta-tags">
					<view :class="['question-difficulty', DIFFICULTY_MAP[wrongQuestion.difficulty]?.className]">
						{{ DIFFICULTY_MAP[wrongQuestion.difficulty]?.text }}
					</view>
					<view class="question-category">
						{{ QUESTION_TYPE[wrongQuestion.type] }}
					</view>
				</view>
				<view class="question-number">错题 #{{ currentIndex + 1 }}</view>
			</view>

			<!-- 题目内容 -->
			<view class="question-content">
				<view class="question-title">{{ wrongQuestion.content }}</view>

				<!-- 选项列表 -->
				<view class="options">
					<view v-for="(text, label) in wrongQuestion.options" :key="label"
						:class="['option', getOptionClass(label)]">
						<view class="option-content">
							<view class="option-label">{{ label }}.</view>
							<view class="option-text">{{ text }}</view>
						</view>
						<view class="option-status">
							<text v-if="wrongQuestion.userAnswer.includes(label)" class="user-answer">⭕ 你的选择</text>
							<text v-if="wrongQuestion.answer.includes(label)" class="correct-answer">✅ 正确答案</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 题目解析 -->
			<view class="explanation">
				<view class="explanation-header">
					<image src="/static/images/analysis.png" class="explanation-icon"></image>
					<text>题目解析</text>
				</view>
				<view class="explanation-content">{{ wrongQuestion.analysis }}</view>
			</view>
		</view>

		<!-- 导航按钮 -->
		<view class="nav-buttons" v-if="showButton">
			<button @click="prevQuestion" v-if="currentIndex !== 0" class="btn prev-btn" hover-class="btn-hover">
				上一题
			</button>
			<button @click="nextQuestion" v-if="currentIndex !== wrongQuestionIds.length - 1" class="btn next-btn"
				hover-class="btn-hover">
				下一题
			</button>
		</view>

		<view class="action-buttons">
			<button class="remove-btn" @click="removeWrongQuestion" hover-class="btn-hover">
				<uni-icons type="trash" size="18" color="#fff"></uni-icons>
				移出错题本
			</button>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		reactive,
		onMounted,
		inject
	} from 'vue'
	import {
		onLoad
	} from '@dcloudio/uni-app'
	import {
		QUESTION_TYPE
	} from '../../utils/constants.js'
	import {
		useQuestionStore
	} from '../../stores/questionStore.js'
	const questionStore = useQuestionStore()
	const proxy = inject('utils')

	const DIFFICULTY_MAP = [{
			text: '简单',
			className: 'easy'
		},
		{
			text: '中等',
			className: 'medium'
		},
		{
			text: '困难',
			className: 'hard'
		}
	]

	// 响应式数据
	const wrongQuestionIds = ref([])
	const currentIndex = ref(0)
	const wrongQuestion = ref({})
	const showButton = ref(true)

	const loadData = async (id) => {
		let result = await proxy.Request({
			url: proxy.Api.getWrongQuestionById,
			showLoading: false,
			params: {
				questionId: id
			}
		})
		if (!result) {
			return
		}
		result.data.options = JSON.parse(result.data.options)
		wrongQuestion.value = result.data
	}

	// 获取选项类名
	const getOptionClass = (value) => {
		if (wrongQuestion.value.answer.includes(value)) return 'correct'
		if (wrongQuestion.value.userAnswer.includes(value)) return 'wrong'
		return ''
	}

	// 上一题
	const prevQuestion = () => {
		if (currentIndex.value > 0) {
			currentIndex.value -= 1
			loadData(wrongQuestionIds.value[currentIndex.value])
		}
	}

	// 下一题
	const nextQuestion = () => {
		if (currentIndex.value < wrongQuestionIds.value.length - 1) {
			currentIndex.value += 1
			loadData(wrongQuestionIds.value[currentIndex.value])
		}
	}

	// 移除错题
	const removeWrongQuestion = async () => {
		const wrongId = wrongQuestion.value.wrongId
		let result = await proxy.Request({
			url: proxy.Api.removeWrongQuestion,
			showLoading: false,
			params: {
				wrongId: wrongId
			}
		})
		if (!result) {
			return
		}
		proxy.Message.success('移除成功')
		questionStore.removeWrongQuestionById(wrongId)
		wrongQuestionIds.value = questionStore.getWrongQuestions().map(q => q.questionId)
		questionStore.setReloadWrongQuestions(true)
		// 没有错题了，返回上一页
		if (wrongQuestionIds.value.length <= 0) {
			setTimeout(() => uni.navigateBack(), 1000)
			return
		}
		if (currentIndex.value >= wrongQuestionIds.value.length) {
			currentIndex.value = wrongQuestionIds.value.length - 1
		}
		loadData(wrongQuestionIds.value[currentIndex.value])
	}

	onLoad((options) => {
		const id = parseInt(options.id) || 0
		showButton.value = options.s === undefined
		loadData(id)
		wrongQuestionIds.value = questionStore.getWrongQuestions().map(q => q.questionId)
		currentIndex.value = wrongQuestionIds.value.findIndex(item => item === id)
	})
</script>

<style lang="scss" scoped>
	.container {
		padding: 20rpx;
		display: flex;
		flex-direction: column;
		min-height: 100vh;
		background-color: #f8f9fa;
	}

	/* 题目卡片 */
	.question-card {
		background-color: #fff;
		border-radius: 24rpx;
		padding: 32rpx;
		margin-bottom: 30rpx;
		box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.05);
	}

	/* 题目头部 */
	.question-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 32rpx;
	}

	.meta-tags {
		display: flex;
		gap: 16rpx;
	}

	.question-number {
		font-size: 26rpx;
		color: #909399;
		background-color: #f5f7fa;
		padding: 8rpx 16rpx;
		border-radius: 20rpx;
	}

	/* 难度标签 */
	.question-difficulty {
		font-size: 24rpx;
		padding: 6rpx 16rpx;
		border-radius: 20rpx;
		font-weight: 500;
	}

	.question-difficulty.easy {
		background-color: rgba(103, 194, 58, 0.1);
		color: #67c23a;
	}

	.question-difficulty.medium {
		background-color: rgba(230, 162, 60, 0.1);
		color: #e6a23c;
	}

	.question-difficulty.hard {
		background-color: rgba(245, 108, 108, 0.1);
		color: #f56c6c;
	}

	/* 分类标签 */
	.question-category {
		font-size: 24rpx;
		padding: 6rpx 16rpx;
		border-radius: 20rpx;
		background-color: rgba(64, 158, 255, 0.1);
		color: #409eff;
	}

	/* 题目内容 */
	.question-content {
		margin-bottom: 40rpx;
	}

	.question-title {
		font-size: 34rpx;
		color: #303133;
		line-height: 1.6;
		margin-bottom: 40rpx;
		font-weight: 500;
	}

	/* 选项样式 */
	.options {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.option {
		border-radius: 16rpx;
		padding: 24rpx;
		display: flex;
		justify-content: space-between;
		align-items: center;
		transition: all 0.3s;
		border: 2rpx solid #ebeef5;

		&.correct {
			background-color: rgba(103, 194, 58, 0.1);
			border-color: #67c23a;
		}

		&.wrong {
			background-color: rgba(245, 108, 108, 0.1);
			border-color: #f56c6c;
		}
	}

	.option-content {
		flex: 1;
		display: flex;
		align-items: center;
	}

	.option-label {
		font-size: 30rpx;
		color: #303133;
		margin-right: 16rpx;
		font-weight: bold;
	}

	.option-text {
		font-size: 30rpx;
		color: #303133;
	}

	.option-status {
		display: flex;
		flex-direction: column;
		row-gap: 10rpx;
	}

	.user-answer {
		font-size: 24rpx;
		color: #f56c6c;
		font-weight: 500;
	}

	.correct-answer {
		font-size: 24rpx;
		color: #67c23a;
		font-weight: 500;
	}

	/* 解析区域 */
	.explanation {
		background-color: #f5f7fa;
		border-radius: 16rpx;
		padding: 24rpx;
		margin-top: 40rpx;
	}

	.explanation-header {
		display: flex;
		align-items: center;
		margin-bottom: 20rpx;
		font-size: 28rpx;
		font-weight: 500;
		color: #303133;
	}

	.explanation-icon {
		width: 32rpx;
		height: 32rpx;
		margin-right: 12rpx;
	}

	.explanation-content {
		font-size: 28rpx;
		color: #606266;
		line-height: 1.6;
	}

	/* 导航按钮 */
	.nav-buttons {
		display: flex;
		justify-content: space-between;
		margin-top: 40rpx;
		gap: 20rpx;
	}

	.btn {
		flex: 1;
		height: 88rpx;
		line-height: 88rpx;
		border-radius: 44rpx;
		font-size: 30rpx;
		font-weight: 500;
		display: flex;
		align-items: center;
		justify-content: center;
		transition: all 0.3s;

		&::after {
			border: none;
		}
	}

	.btn-hover {
		opacity: 0.9;
		transform: translateY(-2rpx);
	}

	.prev-btn {
		background-color: #e3e3e3;
		color: #333;
	}

	.next-btn {
		background-color: #409eff;
		color: #fff;
	}

	/* 禁用按钮样式 */
	button[disabled] {
		opacity: 0.6;
	}

	/* 移除错题 */
	.action-buttons {
		margin-top: 30rpx;
	}

	.remove-btn {
		width: 100%;
		height: 80rpx;
		line-height: 80rpx;
		border-radius: 16rpx;
		background: linear-gradient(135deg, #ff6b6b 0%, #ff4757 100%);
		color: #fff;
		font-size: 30rpx;
		font-weight: 500;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 12rpx;
		box-shadow: 0 4rpx 16rpx rgba(255, 107, 107, 0.3);

		&::after {
			border: none;
		}
	}
</style>