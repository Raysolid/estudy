<template>
	<view class="profile-edit-container">
		<!-- 顶部标题 -->
		<view class="profile-header">
			<text class="profile-subtitle">完善您的个人资料</text>
		</view>

		<!-- 表单区域 -->
		<view class="profile-form">
			<!-- 基本信息 -->
			<view class="form-section">
				<view class="section-title">基本信息</view>

				<view class="input-group">
					<view class="input-wrapper">
						<uni-icons type="person" size="20" color="#999" class="input-icon"></uni-icons>
						<input type="text" placeholder="请输入昵称" placeholder-class="placeholder"
							v-model="formData.nickname" class="form-input" maxlength="20" />
					</view>
					<text class="char-count">{{ formData.nickname.length }}/20</text>
				</view>

				<view class="input-group">
					<view class="input-wrapper">
						<uni-icons type="email" size="20" color="#999" class="input-icon"></uni-icons>
						<input type="text" placeholder="请输入邮箱" placeholder-class="placeholder" v-model="formData.email"
							class="form-input" style="color: #999" disabled />
					</view>
					<text class="input-tip">邮箱为登录账号，不可修改</text>
				</view>
			</view>

			<!-- 学习偏好 -->
			<view class="form-section">
				<view class="section-title">学习偏好</view>

				<view class="preference-group">
					<text class="preference-label">擅长领域</text>
					<view class="tag-list">
						<view v-for="(tag, index) in skillTags" :key="index" :class="[
                'tag-item',
                formData.preference.includes(tag.categoryId) ? 'active' : ''
              ]" @click="toggleSkill(tag.categoryId)">
							{{ tag.name }}
						</view>
					</view>
				</view>

				<view class="preference-group">
					<text class="preference-label">每日学习目标</text>
					<view class="goal-selector">
						<view v-for="goal in studyGoals" :key="goal.value" :class="[
                'goal-item',
                formData.dailyGoal === goal.value ? 'active' : ''
              ]" @click="formData.dailyGoal = goal.value">
							{{ goal.label }}
						</view>
					</view>
				</view>
			</view>

			<!-- 按钮组 -->
			<view class="button-group">
				<button class="cancel-btn" @click="handleCancel">取消</button>
				<button class="save-btn" @click="handleSave">
					<text>保存</text>
				</button>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		reactive,
		inject
	} from 'vue'
	import {
		onLoad
	} from '@dcloudio/uni-app'
	import {
		useQuestionStore
	} from '../../stores/questionStore.js'
	const questionStore = useQuestionStore()
	const proxy = inject('utils')

	// 表单数据
	const formData = reactive({
		nickname: '',
		email: '',
		preference: [],
		dailyGoal: 30
	})

	// 技能标签
	const skillTags = ref([])

	// 学习目标选项
	const studyGoals = ref([{
			label: '15分钟',
			value: 15
		},
		{
			label: '30分钟',
			value: 30
		},
		{
			label: '60分钟',
			value: 60
		},
		{
			label: '90分钟',
			value: 90
		}
	])

	// 加载用户数据
	const loadUserData = async () => {
		let result = await proxy.Request({
			url: proxy.Api.getUserInfo,
			showLoading: false
		})
		if (!result) {
			return
		}
		result.data.preference = result.data.preference?.split(',').map(Number) || []
		Object.assign(formData, result.data)
		skillTags.value = questionStore.getCategoryList()
	}

	// 切换技能标签
	const toggleSkill = (id) => {
		const index = formData.preference.findIndex((item) => item === id)
		if (index > -1) {
			// 存在，取消选中
			formData.preference.splice(index, 1)
			return
		}
		if (formData.preference.length >= 5) {
			proxy.Message.warning('最多选择5个标签')
			return
		}
		if (!formData.preference.includes(id)) {
			formData.preference.push(id)
		}
	}

	// 处理保存
	const handleSave = async () => {
		if (!formData.nickname.trim()) {
			proxy.Message.warning('请输入昵称')
			return
		}

		let result = await proxy.Request({
			url: proxy.Api.updateUserInfo,
			showLoading: false,
			params: {
				nickname: formData.nickname,
				preference: formData.preference.join(',')
			}
		})
		if (!result) {
			return
		}
		proxy.Message.success('修改成功')
		setTimeout(() => {
			uni.navigateBack()
		}, 1000)
	}

	// 处理取消
	const handleCancel = () => {
		uni.navigateBack()
	}

	onLoad(() => {
		loadUserData()
	})
</script>

<style lang="scss" scoped>
	.profile-edit-container {
		padding: 40rpx 30rpx;
	}

	/* 头部样式 */
	.profile-header {
		margin-bottom: 40rpx;
		text-align: center;
	}

	.profile-subtitle {
		font-size: 28rpx;
		color: #909399;
	}

	/* 表单样式 */
	.profile-form {
		background-color: #fff;
		border-radius: 24rpx;
		padding: 40rpx;
		box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
	}

	/* 表单分区 */
	.form-section {
		margin-bottom: 50rpx;
	}

	.section-title {
		font-size: 32rpx;
		font-weight: 600;
		color: #1a1a1a;
		margin-bottom: 30rpx;
		padding-left: 16rpx;
		border-left: 4rpx solid #6190e8;
	}

	/* 输入组样式 */
	.input-group {
		margin-bottom: 40rpx;
		position: relative;
	}

	.input-wrapper {
		display: flex;
		align-items: center;
		background-color: #f8f9fa;
		border-radius: 16rpx;
		padding: 24rpx;
		position: relative;
		border: 2rpx solid transparent;
		transition: all 0.3s;

		&:focus-within {
			border-color: #6190e8;
			background-color: #fff;
		}
	}

	.input-icon {
		margin-right: 16rpx;
		flex-shrink: 0;
	}

	.form-input {
		flex: 1;
		font-size: 28rpx;
		color: #333;
		height: 40rpx;
		line-height: 40rpx;
	}

	.placeholder {
		color: #999;
	}

	.char-count {
		position: absolute;
		right: 0;
		bottom: -30rpx;
		font-size: 24rpx;
		color: #909399;
	}

	.input-tip {
		font-size: 24rpx;
		color: #909399;
		margin-top: 8rpx;
		padding-left: 24rpx;
	}

	/* 偏好设置 */
	.preference-group {
		margin-bottom: 40rpx;
	}

	.preference-label {
		display: block;
		font-size: 28rpx;
		color: #333;
		margin-bottom: 20rpx;
		font-weight: 500;
	}

	.tag-list {
		display: flex;
		flex-wrap: wrap;
		gap: 20rpx;
	}

	.tag-item {
		padding: 12rpx 24rpx;
		background-color: #f8f9fa;
		border-radius: 40rpx;
		font-size: 26rpx;
		color: #666;
		transition: all 0.3s;

		&.active {
			background-color: rgba(97, 144, 232, 0.1);
			color: #6190e8;
			font-weight: 500;
		}
	}

	.goal-selector {
		display: flex;
		gap: 20rpx;
	}

	.goal-item {
		flex: 1;
		padding: 20rpx;
		background-color: #f8f9fa;
		border-radius: 12rpx;
		font-size: 28rpx;
		color: #666;
		text-align: center;
		transition: all 0.3s;

		&.active {
			background-color: #6190e8;
			color: #fff;
			font-weight: 500;
		}
	}

	/* 按钮组 */
	.button-group {
		display: flex;
		gap: 20rpx;
		margin-top: 60rpx;
	}

	.cancel-btn {
		flex: 1;
		height: 88rpx;
		line-height: 88rpx;
		border-radius: 16rpx;
		font-size: 32rpx;
		font-weight: 500;
		background-color: #f8f9fa;
		color: #666;
		transition: all 0.3s;

		&::after {
			border: none;
		}

		&:active {
			background-color: #e8e8e8;
		}
	}

	.save-btn {
		flex: 1;
		height: 88rpx;
		line-height: 88rpx;
		border-radius: 16rpx;
		font-size: 32rpx;
		font-weight: 500;
		background: linear-gradient(135deg, #6190e8 0%, #3a6fe8 100%);
		color: #fff;
		transition: all 0.3s;

		&::after {
			border: none;
		}
	}
</style>