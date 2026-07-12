<template>
	<view class="result-container">
		<!-- 统计卡片 -->
		<view class="stats-card" v-if="resultData">
			<view class="stats-row">
				<view class="stat-item">
					<text class="stat-number">{{ resultData.totalQuestions || 0 }}</text>
					<text class="stat-label">总题数</text>
				</view>
				<view class="stat-item">
					<text class="stat-number" :class="{'correct': resultData.correctCount > 0}">
						{{ resultData.correctCount || 0 }}
					</text>
					<text class="stat-label">正确</text>
				</view>
				<view class="stat-item">
					<text class="stat-number" :class="{'wrong': resultData.wrongCount > 0}">
						{{ resultData.wrongCount || 0 }}
					</text>
					<text class="stat-label">错误</text>
				</view>
				<view class="stat-item">
					<text class="stat-number">{{ resultData.unansweredCount || 0 }}</text>
					<text class="stat-label">未答</text>
				</view>
			</view>
			<view class="accuracy" v-if="resultData.totalQuestions">
				正确率: {{ Math.round((resultData.correctCount / resultData.totalQuestions) * 100) }}%
			</view>
		</view>

		<!-- 结果内容 -->
		<scroll-view class="content-scroll" scroll-y="true">
			<view class="markdown-content" v-if="resultData && resultData.content">
				<!-- 题目列表 -->
				<view class="question-section" v-for="(question, index) in resultData.questions" :key="index">
					<view class="question-header">
						<text class="question-title">题目 {{ index + 1 }}</text>
						<view class="question-status" :class="getStatusClass(question)">
							{{ getStatusText(question) }}
						</view>
					</view>

					<view class="question-content">
						<text class="question-text">{{ question.question }}</text>
					</view>

					<view class="options-list">
						<text class="options-title">选项：</text>
						<view class="option-item" v-for="(option, optIndex) in question.options" :key="optIndex">
							<text class="option-text">{{ option }}</text>
						</view>
					</view>

					<view class="answer-info">
						<view class="answer-row">
							<text class="answer-label">用户答案：</text>
							<text class="answer-value" :class="{'no-answer': !question.userAnswer || question.userAnswer === '未提供'}">
								{{ question.userAnswer || '未作答' }}
							</text>
						</view>
						<view class="answer-row">
							<text class="answer-label">正确答案：</text>
							<text class="answer-value correct-answer">{{ question.correctAnswer }}</text>
						</view>
					</view>

					<view class="explanation" v-if="question.explanation">
						<text class="explanation-title">解析：</text>
						<text class="explanation-text">{{ question.explanation }}</text>
					</view>
				</view>

				<!-- 答题情况 -->
				<view class="summary-section" v-if="resultData.answerSummary">
					<view class="section-title">答题情况</view>
					<view class="summary-content">
						<text class="summary-text">{{ resultData.answerSummary }}</text>
					</view>
				</view>

				<!-- 学习建议 -->
				<view class="suggestion-section" v-if="resultData.suggestions">
					<view class="section-title">学习建议</view>
					<view class="suggestion-content">
						<text class="suggestion-text">{{ resultData.suggestions }}</text>
					</view>
				</view>
			</view>

			<!-- 空状态 -->
			<view class="empty-state" v-else>
				<view class="empty-icon">📝</view>
				<text class="empty-text">暂无批改结果</text>
				<text class="empty-hint">请先上传图片进行批改</text>
				<button class="empty-action" @click="handleBack">去上传图片</button>
			</view>
		</scroll-view>

		<!-- 操作按钮 -->
		<view class="action-bar" v-if="resultData">
			<button class="action-btn secondary" @click="handleBack">继续批改</button>
			<button class="action-btn primary" @click="copyResult">复制结果</button>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		computed
	} from 'vue'
	import { onLoad } from '@dcloudio/uni-app'
	import { useTaskStore } from '../../stores/taskStore.js'
	const taskStore = useTaskStore()

	// 响应式数据
	const resultData = ref(null)

	// 解析新的结果格式
	const parseMarkdownResult = (markdownText) => {
		if (!markdownText) return null
	
		const result = {
			questions: [],
			answerSummary: '',
			suggestions: '',
			totalQuestions: 0,
			correctCount: 0,
			wrongCount: 0,
			unansweredCount: 0,
			content: markdownText
		}
	
		try {
			const sections = markdownText.split('\n\n')
			let currentQuestion = null
			
			for (let i = 0; i < sections.length; i++) {
				const section = sections[i].trim()
				if (!section) continue

				// 检查是否是题目部分
				if (section.startsWith('【题目】')) {
					// 保存上一个题目
					if (currentQuestion) {
						result.questions.push(currentQuestion)
					}
					
					// 解析新题目
					currentQuestion = parseQuestionSection(section)
					
					// 统计答题情况
					if (currentQuestion) {
						if (!currentQuestion.userAnswer || currentQuestion.userAnswer === '未提供') {
							result.unansweredCount++
						} else if (currentQuestion.userAnswer[0] !== currentQuestion.correctAnswer) {
							result.wrongCount++
						} else {
							result.correctCount++
						}
					}
				}
				// 检查是否是答题情况、学习建议
				else if (section.startsWith('【答题情况】')) {
					let tmp = section.split('\n')
					result.answerSummary = tmp[0].replace('【答题情况】', '').trim()
					result.suggestions = tmp[1].replace('【学习建议】', '').trim()
				}
			}
			
			// 确保添加最后一个题目
			if (currentQuestion) {
				result.questions.push(currentQuestion)
			}
	
			// 设置总题数
			result.totalQuestions = result.questions.length
	
		} catch (error) {
			console.error('解析markdown失败:', error)
			// 如果解析失败，至少显示原始内容
			result.content = markdownText
		}
	
		return result
	}

	// 解析单个题目部分
	const parseQuestionSection = (section) => {
		const question = {
			question: '',
			options: [],
			userAnswer: '',
			correctAnswer: '',
			explanation: ''
		}
		
		const lines = section.split('\n')
		
		for (const line of lines) {
			const trimmedLine = line.trim()
			
			if (trimmedLine.startsWith('【题目】')) {
				question.question = trimmedLine.replace('【题目】', '').trim()
			} else if (trimmedLine.startsWith('【选项】')) {
				const optionsText = trimmedLine.replace('【选项】', '').trim()
				// 解析选项，如：A.冒泡排序 B.快速排序 C.堆排序 D.基数排序
				if (optionsText) {
					question.options = optionsText.split('|').map(opt => opt.trim())
				}
			} else if (trimmedLine.startsWith('【用户答案】')) {
				question.userAnswer = trimmedLine.replace('【用户答案】', '').trim()
			} else if (trimmedLine.startsWith('【正确答案】')) {
				question.correctAnswer = trimmedLine.replace('【正确答案】', '').trim()
			} else if (trimmedLine.startsWith('【解析】')) {
				question.explanation = trimmedLine.replace('【解析】', '').trim()
			}
		}
		
		return question
	}

	// 获取状态类名
	const getStatusClass = (question) => {
		if (!question.userAnswer || question.userAnswer === '未提供') {
			return 'unanswered'
		}
		return question.userAnswer !== question.correctAnswer ? 'wrong' : 'correct'
	}

	// 获取状态文本
	const getStatusText = (question) => {
		if (!question.userAnswer || question.userAnswer === '未提供') {
			return '未作答'
		}
		return question.userAnswer[0] !== question.correctAnswer ? '错误' : '正确'
	}

	// 复制结果
	const copyResult = () => {
		if (!resultData.value || !resultData.value.content) {
			uni.showToast({
				title: '没有可复制的内容',
				icon: 'none'
			})
			return
		}

		uni.setClipboardData({
			data: resultData.value.content,
			success: () => {
				uni.showToast({
					title: '结果已复制',
					icon: 'success'
				})
			}
		})
	}

	// 刷新结果
	const refreshResult = () => {
		loadResultFromStore()
		uni.showToast({
			title: '已刷新',
			icon: 'success'
		})
	}

	// 返回上一页
	const handleBack = () => {
		uni.switchTab({
			url: '/pages/agent/agent'
		})
	}

	// 从store加载结果
	const loadResultFromStore = () => {
		const result = taskStore.getTaskResult()
		if (result) {
			resultData.value = parseMarkdownResult(result)
		} else {
			resultData.value = null
		}
	}

	// 生命周期
	onLoad(() => {
		loadResultFromStore()
	})
</script>

<style lang="scss" scoped>
	/* 样式保持不变 */
	.result-container {
		display: flex;
		flex-direction: column;
		background-color: #f5f7fa;
	}

	.stats-card {
		background: white;
		margin: 30rpx;
		padding: 40rpx;
		border-radius: 20rpx;
		box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
	}

	.stats-row {
		display: flex;
		justify-content: space-between;
		margin-bottom: 30rpx;
	}

	.stat-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		flex: 1;
	}

	.stat-number {
		font-size: 48rpx;
		font-weight: bold;
		color: #2c3e50;
		margin-bottom: 10rpx;

		&.correct {
			color: #2ecc71;
		}

		&.wrong {
			color: #e74c3c;
		}
	}

	.stat-label {
		font-size: 24rpx;
		color: #7f8c8d;
	}

	.accuracy {
		text-align: center;
		font-size: 32rpx;
		font-weight: bold;
		color: #3498db;
		padding-top: 20rpx;
		border-top: 1rpx solid #ecf0f1;
	}

	.content-scroll {
		flex: 1;
	}

	.markdown-content {
		padding: 0 30rpx 120rpx;
	}

	.question-section {
		background: white;
		margin-bottom: 30rpx;
		padding: 40rpx;
		border-radius: 20rpx;
		box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
	}

	.question-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 30rpx;
		padding-bottom: 20rpx;
		border-bottom: 1rpx solid #ecf0f1;
	}

	.question-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #2c3e50;
	}

	.question-status {
		padding: 12rpx 24rpx;
		border-radius: 20rpx;
		font-size: 24rpx;
		font-weight: bold;

		&.correct {
			background: #d5f4e6;
			color: #27ae60;
		}

		&.wrong {
			background: #fde8e6;
			color: #e74c3c;
		}

		&.unanswered {
			background: #f8f9fa;
			color: #7f8c8d;
		}
	}

	.question-content {
		margin-bottom: 30rpx;
	}

	.question-text {
		font-size: 30rpx;
		color: #2c3e50;
		line-height: 1.6;
	}

	.options-list {
		margin-bottom: 30rpx;
	}

	.options-title {
		font-size: 28rpx;
		color: #7f8c8d;
		margin-bottom: 20rpx;
		display: block;
	}

	.option-item {
		padding: 20rpx 0;
		border-bottom: 1rpx solid #f8f9fa;
	}

	.option-text {
		font-size: 28rpx;
		color: #2c3e50;
		line-height: 1.5;
	}

	.answer-info {
		background: #f8f9fa;
		padding: 30rpx;
		border-radius: 12rpx;
		margin-bottom: 30rpx;
	}

	.answer-row {
		display: flex;
		align-items: center;
		margin-bottom: 20rpx;

		&:last-child {
			margin-bottom: 0;
		}
	}

	.answer-label {
		font-size: 28rpx;
		color: #7f8c8d;
		min-width: 160rpx;
	}

	.answer-value {
		font-size: 28rpx;
		font-weight: bold;

		&.no-answer {
			color: #95a5a6;
		}

		&.correct-answer {
			color: #2ecc71;
		}
	}

	.explanation {
		background: #e8f4fd;
		padding: 30rpx;
		border-radius: 12rpx;
	}

	.explanation-title {
		font-size: 28rpx;
		color: #3498db;
		font-weight: bold;
		display: block;
		margin-bottom: 15rpx;
	}

	.explanation-text {
		font-size: 28rpx;
		color: #2c3e50;
		line-height: 1.6;
	}

	.summary-section,
	.suggestion-section {
		background: white;
		margin-bottom: 30rpx;
		padding: 40rpx;
		border-radius: 20rpx;
		box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
	}

	.section-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #2c3e50;
		margin-bottom: 30rpx;
		padding-bottom: 20rpx;
		border-bottom: 1rpx solid #ecf0f1;
	}

	.summary-content,
	.suggestion-content {
		font-size: 28rpx;
		color: #2c3e50;
		line-height: 1.6;
	}

	.suggestion-content {
		background: #fff9e6;
		padding: 30rpx;
		border-radius: 12rpx;
	}

	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 120rpx 40rpx;
		text-align: center;
	}

	.empty-icon {
		font-size: 120rpx;
		margin-bottom: 40rpx;
	}

	.empty-text {
		font-size: 36rpx;
		color: #2c3e50;
		margin-bottom: 20rpx;
	}

	.empty-hint {
		font-size: 28rpx;
		color: #7f8c8d;
		margin-bottom: 60rpx;
	}

	.empty-action {
		background: #3498db;
		color: white;
		padding: 24rpx 60rpx;
		border-radius: 12rpx;
		font-size: 28rpx;
		border: none;
	}

	.action-bar {
		position: fixed;
		bottom: 0;
		left: 0;
		right: 0;
		background: white;
		padding: 20rpx 40rpx;
		display: flex;
		gap: 20rpx;
		border-top: 1rpx solid #e1e5eb;
	}

	.action-btn {
		flex: 1;
		padding: 15rpx;
		border-radius: 12rpx;
		font-size: 28rpx;
		font-weight: bold;
		border: none;

		&.primary {
			background: #3498db;
			color: white;
		}

		&.secondary {
			background: #ecf0f1;
			color: #2c3e50;
		}
	}
</style>