<template>
	<view class="change-password-container">
		<!-- 顶部标题 -->
		<view class="password-header">
			<text class="password-title">修改密码</text>
			<text class="password-subtitle">通过邮箱验证码设置新密码</text>
		</view>

		<!-- 表单区域 -->
		<view class="password-form">
			<!-- 邮箱显示 -->
			<view class="email-display">
				<view class="email-label">验证邮箱</view>
				<view class="email-value">{{ email }}</view>
				<text class="email-tip">验证码将发送到此邮箱</text>
			</view>

			<!-- 验证码输入 -->
			<view class="input-group">
				<view class="input-wrapper with-button">
					<uni-icons type="lock" size="20" color="#999" class="input-icon"></uni-icons>
					<input type="number" placeholder="请输入验证码" placeholder-class="placeholder"
						v-model="formData.emailCode" class="form-input" maxlength="6" />
					<button :class="['verify-btn', countdown > 0 ? 'disabled' : '']" :disabled="countdown > 0 || !email"
						@click="sendEmailCode">
						{{ countdown > 0 ? `${countdown}s后重新获取` : '获取验证码' }}
					</button>
				</view>
			</view>

			<!-- 新密码 -->
			<view class="input-group">
				<view class="input-wrapper">
					<uni-icons type="locked" size="20" color="#999" class="input-icon"></uni-icons>
					<input :type="showPassword ? 'text' : 'password'" placeholder="请输入新密码"
						placeholder-class="placeholder" v-model="formData.newPassword" class="form-input" />
					<uni-icons :type="showPassword ? 'eye' : 'eye-slash'" size="20" color="#999" class="password-toggle"
						@click="showPassword = !showPassword"></uni-icons>
				</view>
				<text v-if="passwordError" class="error-text">{{ passwordError }}</text>
			</view>

			<!-- 密码强度提示 -->
			<view class="password-strength" v-if="formData.newPassword">
				<view class="strength-bar">
					<view class="strength-segment" :class="getStrengthClass(1)"></view>
					<view class="strength-segment" :class="getStrengthClass(2)"></view>
					<view class="strength-segment" :class="getStrengthClass(3)"></view>
				</view>
				<text class="strength-text">{{ getStrengthText() }}</text>
			</view>

			<!-- 密码要求提示 -->
			<view class="password-requirements">
				<text class="requirements-title">密码要求：</text>
				<view class="requirement-item" :class="{ 'met': formData.newPassword.length >= 6 }">
					<uni-icons :type="formData.newPassword.length >= 6 ? 'checkmarkempty' : 'circle'" size="16"
						:color="formData.newPassword.length >= 6 ? '#5CD89E' : '#999'"></uni-icons>
					<text>至少6位字符</text>
				</view>
				<view class="requirement-item" :class="{ 'met': hasUpperCase && hasLowerCase }">
					<uni-icons :type="hasUpperCase && hasLowerCase ? 'checkmarkempty' : 'circle'" size="16"
						:color="hasUpperCase && hasLowerCase ? '#5CD89E' : '#999'"></uni-icons>
					<text>包含大小写字母</text>
				</view>
				<view class="requirement-item" :class="{ 'met': hasNumber }">
					<uni-icons :type="hasNumber ? 'checkmarkempty' : 'circle'" size="16"
						:color="hasNumber ? '#5CD89E' : '#999'"></uni-icons>
					<text>包含数字</text>
				</view>
			</view>

			<!-- 提交按钮 -->
			<button class="submit-btn" @click="handleSubmit">
				<text>确认修改</text>
			</button>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		reactive,
		computed,
		onMounted,
		inject
	} from 'vue'
	import {
		onLoad
	} from '@dcloudio/uni-app'
	import md5 from 'md5'
	import { LOCAL_STORAGE_KEY } from '../../utils/constants.js'
	import { useUserStore } from '../../stores/userStore.js'
	const userStore = useUserStore()
	const proxy = inject('utils')

	// 邮箱地址
	const email = ref('')

	// 表单数据
	const formData = reactive({
		emailCode: '',
		newPassword: ''
	})

	// UI状态
	const showPassword = ref(false)
	const passwordError = ref('')

	// 倒计时
	const countdown = ref(0)

	// 密码要求计算属性
	const hasUpperCase = computed(() => /[A-Z]/.test(formData.newPassword))
	const hasLowerCase = computed(() => /[a-z]/.test(formData.newPassword))
	const hasNumber = computed(() => /\d/.test(formData.newPassword))

	// 发送验证码
	const sendEmailCode = async () => {
		uni.showLoading({
			mask: true
		})
		let result = await proxy.Request({
			url: proxy.Api.emailCode,
			showLoading: false,
			params: {
				email: email.value
			}
		})
		if (!result) {
			return
		}
		uni.hideLoading()
		proxy.Message.success('验证码已发送')
		countdown.value = 60
		const timer = setInterval(() => {
			if (countdown.value > 0) {
				countdown.value--
			} else {
				clearInterval(timer)
			}
		}, 1000)
	}

	// 计算密码强度
	const passwordStrength = computed(() => {
		const password = formData.newPassword
		if (!password) return 0

		let strength = 0
		if (password.length >= 6) strength++
		if (hasUpperCase.value && hasLowerCase.value) strength++
		if (hasNumber.value) strength++
		if (/[^a-zA-Z0-9]/.test(password)) strength++

		return Math.min(strength, 3)
	})

	// 获取强度样式类
	const getStrengthClass = (segment) => {
		if (segment <= passwordStrength.value) {
			switch (passwordStrength.value) {
				case 1:
					return 'strength-weak'
				case 2:
					return 'strength-medium'
				case 3:
					return 'strength-strong'
				default:
					return ''
			}
		}
		return ''
	}

	// 获取强度文本
	const getStrengthText = () => {
		switch (passwordStrength.value) {
			case 0:
				return '密码强度'
			case 1:
				return '弱'
			case 2:
				return '中'
			case 3:
				return '强'
			default:
				return '密码强度'
		}
	}

	// 处理表单提交
	const handleSubmit = async () => {
		// 验证验证码
		if (!formData.emailCode) {
			uni.showToast({
				title: '请输入验证码',
				icon: 'none'
			})
			return
		}

		if (formData.emailCode.length !== 6) {
			uni.showToast({
				title: '验证码必须是6位数字',
				icon: 'none'
			})
			return
		}

		// 验证密码
		if (!formData.newPassword) {
			uni.showToast({
				title: '请输入新密码',
				icon: 'none'
			})
			return
		}

		if (formData.newPassword.length < 6) {
			passwordError.value = '密码长度不能少于6位'
			return
		}

		passwordError.value = ''
		
		// 修改密码
		let result = await proxy.Request({
			url: proxy.Api.changePassword,
			showLoading: false,
			params: {
				password: md5(formData.newPassword),
				emailCode: formData.emailCode
			}
		})
		if (!result) {
			return
		}
		logout()
	}
	
	// 退出登录
	const logout = async () => {
		let result = await proxy.Request({
			url: proxy.Api.logout,
			showLoading: false
		})
		if (!result) {
			return
		}
		uni.showModal({
			title: '提示',
			content: '修改密码成功，请重新登录',
			showCancel: false,
			success: () => {
				uni.removeStorageSync(LOCAL_STORAGE_KEY.token.key)
				uni.reLaunch({
					url: '/pages/login/login'
				})
			}
		})
	}
	
	onLoad(() => {
		email.value = userStore.getUserInfo().email
	})
</script>

<style lang="scss" scoped>
	.change-password-container {
		padding: 60rpx 50rpx;
		display: flex;
		flex-direction: column;
	}
	
	/* 头部样式 */
	.password-header {
		margin-bottom: 60rpx;
		text-align: center;
	}

	.password-title {
		font-size: 42rpx;
		font-weight: 700;
		color: #1a1a1a;
		display: block;
		margin-bottom: 16rpx;
	}

	.password-subtitle {
		font-size: 28rpx;
		color: #909399;
	}

	/* 表单样式 */
	.password-form {
		background-color: #fff;
		border-radius: 24rpx;
		padding: 40rpx;
		box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
		margin-bottom: 40rpx;
	}

	/* 邮箱显示 */
	.email-display {
		margin-bottom: 40rpx;
		padding: 24rpx;
		background-color: #f8f9fa;
		border-radius: 16rpx;
		border-left: 4rpx solid #6190E8;
	}

	.email-label {
		font-size: 26rpx;
		color: #666;
		margin-bottom: 8rpx;
	}

	.email-value {
		font-size: 30rpx;
		color: #333;
		font-weight: 500;
		margin-bottom: 8rpx;
	}

	.email-tip {
		font-size: 24rpx;
		color: #909399;
	}

	.input-group {
		margin-bottom: 40rpx;
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
			border-color: #6190E8;
			background-color: #fff;
		}

		&.with-button {
			padding-right: 200rpx;
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

	.password-toggle {
		margin-left: 16rpx;
		flex-shrink: 0;
	}

	.verify-btn {
		position: absolute;
		right: 10rpx;
		top: 50%;
		transform: translateY(-50%);
		background: #6190E8;
		color: #fff;
		font-size: 24rpx;
		padding: 12rpx 20rpx;
		border-radius: 12rpx;
		white-space: nowrap;

		&.disabled {
			background: #ccc;
			color: #999;
		}

		&::after {
			border: none;
		}
	}

	.error-text {
		font-size: 24rpx;
		color: #FF5E5E;
		margin-top: 8rpx;
		display: block;
		padding-left: 24rpx;
	}

	/* 密码强度指示器 */
	.password-strength {
		margin-bottom: 30rpx;
		padding: 0 10rpx;
	}

	.strength-bar {
		display: flex;
		gap: 8rpx;
		margin-bottom: 10rpx;
	}

	.strength-segment {
		flex: 1;
		height: 6rpx;
		border-radius: 3rpx;
		background-color: #f0f0f0;
		transition: all 0.3s;

		&.strength-weak {
			background-color: #FF5E5E;
		}

		&.strength-medium {
			background-color: #FFB800;
		}

		&.strength-strong {
			background-color: #5CD89E;
		}
	}

	.strength-text {
		font-size: 24rpx;
		color: #909399;
	}

	/* 密码要求 */
	.password-requirements {
		background-color: #f8f9fa;
		border-radius: 12rpx;
		padding: 24rpx;
		margin-bottom: 40rpx;
	}

	.requirements-title {
		font-size: 26rpx;
		color: #666;
		font-weight: 500;
		display: block;
		margin-bottom: 16rpx;
	}

	.requirement-item {
		display: flex;
		align-items: center;
		font-size: 24rpx;
		color: #999;
		margin-bottom: 12rpx;
		transition: all 0.3s;

		&.met {
			color: #5CD89E;
		}

		&:last-child {
			margin-bottom: 0;
		}

		text {
			margin-left: 12rpx;
		}
	}

	.submit-btn {
		background: linear-gradient(135deg, #6190E8 0%, #3A6FE8 100%);
		color: #fff;
		height: 88rpx;
		line-height: 88rpx;
		border-radius: 16rpx;
		font-size: 32rpx;
		font-weight: 500;
		transition: all 0.3s;

		&::after {
			border: none;
		}
	}
</style>