<template>
	<view class="auth-container">
		<!-- 背景装饰 -->
		<view class="background-decoration">
			<view class="decoration-circle circle-1"></view>
			<view class="decoration-circle circle-2"></view>
			<view class="decoration-circle circle-3"></view>
		</view>

		<!-- 顶部标题 -->
		<view class="auth-header">
			<text class="auth-title">{{ isLoginMode ? '欢迎回来' : '创建账户' }}</text>
			<text class="auth-subtitle">{{ isLoginMode ? '请登录您的账户' : '开启您的学习之旅' }}</text>
		</view>

		<!-- 表单区域 -->
		<uni-forms ref="formRef" :modelValue="formData" :rules="rules" validate-trigger="submit" err-show-type="toast">
			<view class="auth-form">
				<!-- 邮箱输入 -->
				<view class="input-group">
					<uni-forms-item name="email">
						<view class="input-wrapper">
							<uni-icons type="email" size="20" color="#999" class="input-icon"></uni-icons>
							<input type="text" placeholder="请输入邮箱地址" placeholder-class="placeholder"
								v-model="formData.email" class="form-input" />
						</view>
					</uni-forms-item>
				</view>

				<!-- 验证码输入 -->
				<view class="input-group" v-if="!isLoginMode || showEmailCode">
					<uni-forms-item name="emailCode">
						<view class="input-wrapper with-button">
							<uni-icons type="lock" size="20" color="#999" class="input-icon"></uni-icons>
							<input type="number" placeholder="请输入验证码" placeholder-class="placeholder"
								v-model="formData.emailCode" class="form-input" maxlength="6" />
							<button :class="['verify-btn', countdown > 0 ? 'disabled' : '']"
								:disabled="countdown > 0 || !formData.email" @click="sendEmailCode">
								{{ countdown > 0 ? `${countdown}s后重新获取` : '获取验证码' }}
							</button>
						</view>
					</uni-forms-item>
				</view>

				<!-- 密码输入（登录模式） -->
				<view class="input-group" v-if="isLoginMode && !showEmailCode">
					<uni-forms-item name="password">
						<view class="input-wrapper">
							<uni-icons type="locked" size="20" color="#999" class="input-icon"></uni-icons>
							<input :type="showPassword ? 'text' : 'password'" placeholder="请输入密码"
								placeholder-class="placeholder" v-model="formData.password" class="form-input" />
							<uni-icons :type="showPassword ? 'eye' : 'eye-slash'" size="20" color="#999"
								class="password-toggle" @click="showPassword = !showPassword"></uni-icons>
						</view>
					</uni-forms-item>
				</view>

				<!-- 密码输入（注册模式） -->
				<view class="input-group" v-if="!isLoginMode">
					<uni-forms-item name="password">
						<view class="input-wrapper">
							<uni-icons type="locked" size="20" color="#999" class="input-icon"></uni-icons>
							<input :type="showPassword ? 'text' : 'password'" placeholder="请设置密码"
								placeholder-class="placeholder" v-model="formData.password" class="form-input" />
							<uni-icons :type="showPassword ? 'eye' : 'eye-slash'" size="20" color="#999"
								class="password-toggle" @click="showPassword = !showPassword"></uni-icons>
						</view>
					</uni-forms-item>
				</view>

				<!-- 忘记密码（登录模式） -->
				<view class="forgot-password" v-if="isLoginMode">
					<text v-if="!showEmailCode" @click="toggleEmailCode">验证码登录</text>
					<text v-else @click="toggleEmailCode">密码登录</text>
				</view>

				<!-- 协议同意（注册模式） -->
				<view class="agreement" v-if="!isLoginMode">
					<uni-forms-item name="agreementChecked">
						<view class="agreement-checkbox"
							@click="formData.agreementChecked = !formData.agreementChecked">
							<uni-icons :type="formData.agreementChecked ? 'checkbox-filled' : 'circle'" size="16"
								:color="formData.agreementChecked ? '#6190E8' : '#999'"></uni-icons>
							<text>我已阅读并同意</text>
							<text class="agreement-link">《用户协议》</text>
							<text>和</text>
							<text class="agreement-link">《隐私政策》</text>
						</view>
					</uni-forms-item>
				</view>

				<!-- 提交按钮 -->
				<button class="submit-btn" @click="handleSubmit">
					<text >{{ isLoginMode ? '登录' : '注册' }}</text>
				</button>
			</view>
		</uni-forms>

		<!-- 切换登录/注册 -->
		<view class="auth-switch">
			<text>{{ isLoginMode ? '还没有账户？' : '已有账户？' }}</text>
			<text class="switch-btn" @click="toggleAuthMode">{{ isLoginMode ? '立即注册' : '立即登录' }}</text>
		</view>

		<!-- 第三方登录 -->
		<view class="social-login" v-if="isLoginMode">
			<view class="divider">
				<text class="divider-text">或使用其他方式登录</text>
			</view>
			<view class="social-buttons">
				<button class="social-btn wechat">
					<uni-icons type="weixin" size="32" color="#07C160"></uni-icons>
				</button>
				<button class="social-btn apple">
					<uni-icons type="qq" size="30" color="#7ca2e6"></uni-icons>
				</button>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		reactive,
		computed,
		inject
	} from 'vue'
	import md5 from 'md5'
	import { LOCAL_STORAGE_KEY } from '../../utils/constants'
	const proxy = inject('utils')
	
	const formRef = ref()
	const isLoginMode = ref(true)
	const formData = reactive({
		email: '',
		password: '',
		emailCode: '',
		agreementChecked: false
	})
	const rules = reactive({
		email: {
			rules: [{
				required: true,
				errorMessage: '邮箱不能为空'
			}, {
				format: 'email',
				errorMessage: '邮箱格式不正确'
			}]
		},
		password: {
			rules: [{
				required: true,
				errorMessage: '密码不能为空'
			}, {
				minLength: 6,
				errorMessage: '密码长度不能少于6位'
			}]
		},
		emailCode: {
			rules: [{
				required: true,
				errorMessage: '验证码不能为空'
			}, {
				length: 6,
				errorMessage: '验证码必须是6位数字'
			}]
		},
		agreementChecked: {
			rules: [{
				required: true,
				errorMessage: '请阅读并同意用户协议和隐私政策'
			}]
		}
	})

	// UI状态
	const showPassword = ref(false)
	const showEmailCode = ref(false)

	// 倒计时
	const countdown = ref(0)

	// 切换登录/注册模式
	const toggleAuthMode = () => {
		isLoginMode.value = !isLoginMode.value
		resetForm()
	}

	// 切换验证码登录
	const toggleEmailCode = () => {
		showEmailCode.value = !showEmailCode.value
		formData.password = ''
	}

	// 发送验证码
	const sendEmailCode = async () => {
		// 验证邮箱
		await formRef.value.validateField(['email'])
		uni.showLoading({
			mask: true
		})
		let result = await proxy.Request({
			url: proxy.Api.emailCode,
			showLoading: false,
			params: {
				email: formData.email
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

	// 处理表单提交
	const handleSubmit = async () => {
		try {
			// 验证表单
			let validateFields = ['email']

			if (!isLoginMode.value || showEmailCode.value) {
				validateFields.push('emailCode')
			} else {
				validateFields.push('password')
			}

			if (!isLoginMode.value) {
				validateFields.push('agreementChecked')
			}

			const isValid = await formRef.value.validate(validateFields)
			if (!isValid) {
				return
			}

			// 提交表单
			let url = isLoginMode.value ? proxy.Api.login : proxy.Api.register
			let params = {}
			Object.assign(params, formData)
			if (isLoginMode.value && !showEmailCode.value) {
				params.password = md5(params.password)
			}
			let result = await proxy.Request({
				url,
				showLoading: false,
				params
			})
			if (!result) {
				return
			}
			
			if (isLoginMode.value) {
				proxy.Message.success('登录成功')
				uni.setStorageSync(LOCAL_STORAGE_KEY.token.key, result.data.token)
				// 登录成功后跳转到首页
				uni.switchTab({
					url: '/pages/index/index'
				})
			} else {
				proxy.Message.success('注册成功')
				toggleAuthMode()
			}
			
		} catch (e) {
			console.log('表单验证失败', e)
		}
	}

	// 重置表单
	const resetForm = () => {
		formData.email = ''
		formData.password = ''
		formData.emailCode = ''
		formData.agreementChecked = false
		showEmailCode.value = false
	}
</script>

<style lang="scss" scoped>
	.auth-container {
		padding: 60rpx 50rpx;
		display: flex;
		flex-direction: column;
		position: relative;
		// background: linear-gradient(135deg, #f8faff 0%, #e6f0ff 100%);
	}

	/* 背景装饰 */
	.background-decoration {
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		pointer-events: none;
	}

	.decoration-circle {
		position: absolute;
		border-radius: 50%;
		opacity: 0.1;
	}

	.circle-1 {
		width: 300rpx;
		height: 300rpx;
		background: #6190E8;
		top: -150rpx;
		right: -150rpx;
	}

	.circle-2 {
		width: 200rpx;
		height: 200rpx;
		background: #FF9F43;
		bottom: 100rpx;
		left: -100rpx;
	}

	.circle-3 {
		width: 150rpx;
		height: 150rpx;
		background: #5CD89E;
		bottom: -50rpx;
		right: 100rpx;
	}

	/* 头部样式 */
	.auth-header {
		margin-bottom: 60rpx;
		text-align: center;
	}

	.auth-title {
		font-size: 42rpx;
		font-weight: 700;
		color: #1a1a1a;
		display: block;
		margin-bottom: 16rpx;
	}

	.auth-subtitle {
		font-size: 28rpx;
		color: #909399;
	}

	/* 表单样式 */
	.auth-form {
		background-color: #fff;
		border-radius: 24rpx;
		padding: 40rpx;
		box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
		margin-bottom: 40rpx;
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
		right: 0;
		top: 50%;
		transform: translateY(-50%);
		background: #6190E8;
		color: #fff;
		font-size: 24rpx;
		padding: 14rpx 20rpx;
		border-end-end-radius: 12rpx;
		white-space: nowrap;

		&.disabled {
			background: #ccc;
			color: #999;
		}

		&::after {
			border: none;
		}
	}

	.forgot-password {
		text-align: right;
		margin-bottom: 40rpx;

		text {
			font-size: 26rpx;
			color: #6190E8;
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

	.agreement {
		margin: 30rpx 0;
		text-align: center;
	}

	.agreement-checkbox {
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 24rpx;
		color: #666;

		text {
			margin: 0 4rpx;
		}
	}

	.agreement-link {
		color: #6190E8;
	}

	/* 切换登录/注册 */
	.auth-switch {
		text-align: center;
		font-size: 28rpx;
		color: #666;
		margin-bottom: 40rpx;
	}

	.switch-btn {
		color: #6190E8;
		margin-left: 10rpx;
	}

	/* 第三方登录 */
	.social-login {
		margin-top: auto;
	}

	.divider {
		position: relative;
		text-align: center;
		margin-bottom: 40rpx;

		&::before {
			content: '';
			position: absolute;
			left: 0;
			right: 0;
			top: 50%;
			height: 1rpx;
			background-color: #eee;
		}
	}

	.divider-text {
		position: relative;
		background-color: #f8faff;
		padding: 0 20rpx;
		font-size: 24rpx;
		color: #999;
	}

	.social-buttons {
		display: flex;
		justify-content: center;
		gap: 40rpx;
	}

	.social-btn {
		width: 80rpx;
		height: 80rpx;
		border-radius: 50%;
		background-color: #fff;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);

		&::after {
			border: none;
		}
	}

	/* 表单错误样式 */
	:deep(.uni-forms-item__error) {
		font-size: 24rpx;
		color: #FF5E5E;
		margin-top: 8rpx;
		padding-left: 24rpx;
	}
</style>