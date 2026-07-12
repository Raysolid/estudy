<template>
	<view class="container">
		<!-- 标题 -->
		<view class="header">
			<text class="subtitle">上传题目图片，AI智能批改</text>
			<text class="subtitle" v-if="uploading">批改过程耗时较长，可以先刷点题目！</text>
		</view>

		<!-- 上传区域 -->
		<view class="upload-area" @click="chooseImage" v-if="!imagePath && !uploading">
			<view class="upload-icon">
				<text class="icon-text">+</text>
			</view>
			<text class="upload-text">点击选择图片</text>
			<text class="upload-hint">支持 JPG、PNG 格式，最大 10MB</text>
		</view>

		<!-- 图片预览 -->
		<view class="preview-area" v-if="imagePath && !uploading && !result">
			<image :src="imagePath" mode="aspectFit" class="preview-image"></image>
			<view class="preview-actions">
				<button class="action-btn rechoose" @click="chooseImage">重新选择</button>
				<button class="action-btn upload" @click="uploadImage">智能批改</button>
			</view>
		</view>

		<!-- 上传中状态 -->
		<view class="uploading-area" v-if="uploading">
			<view class="loading-spinner"></view>
			<text class="uploading-text">AI正在批改中，请稍候...</text>
			<text class="progress-text">{{ progressText }}</text>
		</view>

		<!-- 结果显示 -->
		<view class="result-area" v-if="result">
			<button class="action-btn view-result" @click="viewResult" v-if="result">查看结果</button>
		</view>

		<!-- 错误提示 -->
		<view class="error-message" v-if="error">
			<text class="error-text">{{ error }}</text>
			<button class="error-retry" @click="reset">重试</button>
		</view>

		<!-- 底部信息 -->
		<view class="footer">
			<text class="footer-text">图片解析服务由 AI 提供</text>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		computed,
		inject,
		onUnmounted
	} from 'vue'
	import { onShow } from '@dcloudio/uni-app'
	import { LOCAL_STORAGE_KEY } from '../../utils/constants.js'
	import { useTaskStore } from '../../stores/taskStore.js'
	const proxy = inject('utils')
	const taskStore = useTaskStore()

	// 响应式数据
	const imagePath = ref('') // 图片路径
	const uploading = ref(false) // 上传状态
	const result = ref('') // 解析结果
	const error = ref('') // 错误信息
	const uploadProgress = ref(0) // 上传进度
	const pollingInterval = ref(null) // 轮询定时器
	const currentTaskId = ref('') // 当前任务ID

	const baseUrl = computed(() => uni.getSystemInfoSync().uniPlatform === 'web' ? '/api' : `${api.domain}/api`)

	// 计算属性
	const progressText = computed(() => {
		if (uploadProgress.value < 30) return '上传图片中...'
		if (uploadProgress.value < 70) return '调用解析服务...'
		return '处理中，请稍候...'
	})

	// 选择图片
	const chooseImage = () => {
		reset()
		uni.chooseImage({
			count: 1,
			sizeType: ['compressed'], // 可以指定是原图还是压缩图，默认二者都有
			sourceType: ['album', 'camera'], // 可以指定来源是相册还是相机，默认二者都有
			success: (res) => {
				// 获取图片临时路径
				const tempFilePaths = res.tempFilePaths
				imagePath.value = tempFilePaths[0]
				error.value = '' // 清除错误信息
			},
			fail: (err) => {
				console.error('选择图片失败:', err)
				error.value = '选择图片失败，请重试'
			}
		})
	}

	// 上传图片到服务器
	const uploadImage = async () => {
		if (!imagePath.value) {
			error.value = '请先选择图片'
			return
		}

		uploading.value = true
		uploadProgress.value = 10
		error.value = ''
		result.value = ''

		const startTime = Date.now()

		try {
			// 模拟进度更新
			const progressInterval = setInterval(() => {
				if (uploadProgress.value < 90) {
					uploadProgress.value += 10
				}
			}, 500)

			// 执行上传 - 使用任务接口
			const response = await uni.uploadFile({
				url: `${baseUrl.value}${proxy.Api.uploadAndParse}`,
				filePath: imagePath.value,
				name: 'file',
				header: {
					'token': uni.getStorageSync(LOCAL_STORAGE_KEY.token.key)
				}
			})

			clearInterval(progressInterval)
			uploadProgress.value = 100

			// 处理响应
			if (response.statusCode === 200) {
				const data = JSON.parse(response.data)
				if (data.code === 200) {
					// 获取任务ID并保存到store
					const taskId = data.data
					currentTaskId.value = taskId
					taskStore.setTaskId(taskId)

					// 开始轮询任务结果
					startPolling(taskId)
				} else {
					error.value = data.message || '任务创建失败'
					uploading.value = false
				}
			} else {
				error.value = `上传失败，状态码: ${response.statusCode}`
				uploading.value = false
			}

		} catch (err) {
			console.error('上传失败:', err)
			error.value = '上传失败，请检查网络连接'
			uploading.value = false
			uploadProgress.value = 0
		}
	}

	// 开始轮询任务结果
	const startPolling = (taskId) => {
		// 清除之前的轮询
		if (pollingInterval.value) {
			clearInterval(pollingInterval.value)
		}

		// 立即查询一次
		checkTaskResult(taskId)

		// 设置轮询间隔（每2秒查询一次）
		pollingInterval.value = setInterval(() => {
			checkTaskResult(taskId)
		}, 5000)
	}

	// 检查任务结果
	const checkTaskResult = async (taskId) => {
		try {
			const response = await uni.request({
				url: `${baseUrl.value}${proxy.Api.getTaskResult}/${taskId}`,
				method: 'GET',
				header: {
					'token': uni.getStorageSync(LOCAL_STORAGE_KEY.token.key)
				}
			})

			if (response.statusCode === 200) {
				const data = response.data
				if (data.code === 200 && data.data) {
					const taskResult = data.data

					if (taskResult.completed) {
						// 任务完成
						clearInterval(pollingInterval.value)
						pollingInterval.value = null

						if (taskResult.result) {
							// 成功
							result.value = taskResult.result
							// 保存结果到store
							taskStore.setTaskResult(taskResult.result)
							// 清除任务ID
							taskStore.setTaskId('')
							currentTaskId.value = ''
						} else if (taskResult.error) {
							// 失败
							error.value = taskResult.error
						}

						uploading.value = false
						uploadProgress.value = 0
					}
					// 如果任务未完成，继续轮询
				} else {
					// 任务不存在或其他错误
					clearInterval(pollingInterval.value)
					pollingInterval.value = null
					error.value = data.message || '批改失败'
					uploading.value = false
				}
			} else {
				console.error('查询任务结果失败:', response.statusCode)
			}
		} catch (err) {
			console.error('查询任务结果异常:', err)
		}
	}

	// 查看结果
	const viewResult = () => {
		uni.navigateTo({
			url: '/pages/agent/result'
		})
	}

	// 重置状态
	const reset = () => {
		imagePath.value = ''
		uploading.value = false
		result.value = ''
		error.value = ''
		uploadProgress.value = 0
		currentTaskId.value = ''

		// 清除store中的任务信息
		taskStore.setTaskId()
		taskStore.setTaskResult()

		// 清除轮询
		if (pollingInterval.value) {
			clearInterval(pollingInterval.value)
			pollingInterval.value = null
		}
	}

	// 页面显示时检查是否有未完成的任务
	onShow(() => {
		const storedTaskId = taskStore.getTaskId()
		if (storedTaskId && storedTaskId !== '') {
			currentTaskId.value = storedTaskId
			uploading.value = true
			startPolling(storedTaskId)
		}

		// 检查是否有存储的结果
		const storedResult = taskStore.getTaskResult()
		if (storedResult && storedResult !== '') {
			result.value = storedResult
		}
	})

	// 页面卸载时清除轮询
	onUnmounted(() => {
		if (pollingInterval.value) {
			clearInterval(pollingInterval.value)
			pollingInterval.value = null
		}
	})
</script>

<style lang="scss" scoped>
	.container {
		padding: 40rpx;
		min-height: 100vh;
		background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
		display: flex;
		flex-direction: column;
	}

	.header {
		text-align: center;
		margin-bottom: 60rpx;
	}

	.subtitle {
		display: block;
		font-size: 28rpx;
		color: #7f8c8d;
	}

	.upload-area {
		background: #ffffff;
		border: 2rpx dashed #3498db;
		border-radius: 20rpx;
		padding: 80rpx 40rpx;
		text-align: center;
		transition: all 0.3s ease;

		&:active {
			background: #f8f9fa;
			transform: scale(0.98);
		}
	}

	.upload-icon {
		margin-bottom: 40rpx;
	}

	.icon-img {
		width: 120rpx;
		height: 120rpx;
	}

	.icon-text {
		font-size: 80rpx;
		color: #3498db;
		font-weight: lighter;
	}

	.upload-text {
		display: block;
		font-size: 36rpx;
		color: #2c3e50;
		margin-bottom: 20rpx;
	}

	.upload-hint {
		display: block;
		font-size: 24rpx;
		color: #95a5a6;
	}

	.preview-area {
		background: #ffffff;
		border-radius: 20rpx;
		padding: 40rpx;
		box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.1);
	}

	.preview-image {
		width: 100%;
		height: 400rpx;
		border-radius: 12rpx;
		margin-bottom: 40rpx;
	}

	.preview-actions {
		display: flex;
		justify-content: space-between;
	}

	.action-btn {
		flex: 1;
		margin: 0 20rpx;
		padding: 24rpx;
		border-radius: 12rpx;
		font-size: 28rpx;
		font-weight: bold;
		border: none;

		&.rechoose {
			background: #e74c3c;
			color: white;
		}

		&.upload {
			background: #3498db;
			color: white;
		}

		&.new-upload {
			background: #2ecc71;
			color: white;
		}

		&.view-result {
			background: #9b59b6;
			color: white;
		}
	}

	.uploading-area {
		background: #ffffff;
		border-radius: 20rpx;
		padding: 80rpx 40rpx;
		text-align: center;
		box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.1);
	}

	.loading-spinner {
		width: 80rpx;
		height: 80rpx;
		border: 8rpx solid #f3f3f3;
		border-top: 8rpx solid #3498db;
		border-radius: 50%;
		animation: spin 1s linear infinite;
		margin: 0 auto 40rpx;
	}

	@keyframes spin {
		0% {
			transform: rotate(0deg);
		}

		100% {
			transform: rotate(360deg);
		}
	}

	.uploading-text {
		display: block;
		font-size: 32rpx;
		color: #2c3e50;
		margin-bottom: 20rpx;
	}

	.progress-text {
		display: block;
		font-size: 26rpx;
		color: #7f8c8d;
	}
	
	.result-area {
	  background: #ffffff;
	  border-radius: 20rpx;
	  padding: 40rpx;
	  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.1);
	}

	.error-message {
		background: #e74c3c;
		border-radius: 12rpx;
		padding: 30rpx;
		text-align: center;
		margin-bottom: 40rpx;
	}

	.error-text {
		display: block;
		font-size: 28rpx;
		color: white;
		margin-bottom: 20rpx;
	}

	.error-retry {
		background: rgba(255, 255, 255, 0.2);
		color: white;
		border: none;
		padding: 20rpx 40rpx;
		border-radius: 8rpx;
		font-size: 26rpx;
	}

	.footer {
		text-align: center;
		margin-top: auto;
		padding-top: 60rpx;
	}

	.footer-text {
		font-size: 24rpx;
		color: #7f8c8d;
	}
</style>