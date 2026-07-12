<template>
	<view class="container">
		<!-- 用户信息区域 -->
		<view class="user-info">
			<image class="avatar" src="/static/images/avatar.png"></image>
			<view class="user-meta">
				<text class="nickname">{{ userInfo.nickname }}</text>
				<text class="member-info">会员积分: {{ userInfo.points }}</text>
			</view>
		</view>

		<!-- 选项卡片 -->
		<view class="function-card">
			<view class="function-row">
				<!-- 打卡功能 -->
				<view class="function-item" @click="sign">
					<view class="function-icon checkin-icon">
						<uni-icons type="calendar" size="28" color="#fff"></uni-icons>
					</view>
					<text class="function-label">打卡</text>
				</view>

				<!-- 编辑资料功能 -->
				<view class="function-item" @click="goEdit">
					<view class="function-icon edit-icon">
						<uni-icons type="person" size="24" color="#fff"></uni-icons>
					</view>
					<text class="function-label">编辑资料</text>
				</view>

				<!-- 修改密码功能 -->
				<view class="function-item" @click="changePassword">
					<view class="function-icon password-icon">
						<uni-icons type="locked" size="24" color="#fff"></uni-icons>
					</view>
					<text class="function-label">修改密码</text>
				</view>

				<!-- 退出登录功能 -->
				<view class="function-item" @click="logout">
					<view class="function-icon logout-icon">
						<uni-icons type="undo" size="24" color="#fff"></uni-icons>
					</view>
					<text class="function-label">退出登录</text>
				</view>
			</view>
		</view>

		<!-- 最近学习记录 -->
		<view class="section">
			<view class="section-header">
				<text class="section-title">最近学习记录</text>
				<text class="section-more" @click="viewAllStudyRecords">查看全部 ></text>
			</view>
			<view class="study-records">
				<view v-for="(record, index) in recentStudyRecords" :key="record.recordId" class="record-item"
					@click="viewStudyDetail(record.recordId)">
					<view class="record-main">
						<text class="record-title">{{ record.name }}</text>
						<view class="record-meta">
							<view class="record-type" :class="getRecordResult(record)[0]">
								{{ getRecordResult(record)[1] }}
							</view>
							<text class="record-duration">{{ record.duration }}分钟</text>
						</view>
					</view>
					<view class="record-time">
						<text>{{ formatStudyTime(record.startTime) }}</text>
					</view>
				</view>
				<view v-if="recentStudyRecords.length === 0" class="empty-tip">
					<text>暂无学习记录</text>
				</view>
			</view>
		</view>

		<!-- 最近错题 -->
		<view class="section">
			<view class="section-header">
				<text class="section-title">最近错题</text>
				<text class="section-more" @click="viewWrongQuestions">查看全部 ></text>
			</view>
			<view class="wrong-questions">
				<view v-for="(item, index) in recentWrongQuestions" :key="index" class="wrong-item"
					@click="viewWrongDetail(item.questionId)">
					<text class="wrong-title">{{ index+1 }}. {{ item.content }}</text>
					<view class="wrong-meta">
						<text class="wrong-category">{{ item.name }}</text>
						<text class="wrong-time">{{ item.wrongTime }}</text>
					</view>
				</view>
				<view v-if="recentWrongQuestions.length === 0" class="empty-tip">
					<text>暂无错题记录</text>
				</view>
			</view>
		</view>

		<!-- 学习日历 -->
		<view class="section">
			<view class="section-header">
				<text class="section-title">学习日历</text>
				<text class="section-more">{{ currentMonth }}</text>
			</view>
			<view class="calendar">
				<view class="calendar-header">
					<text v-for="(day, index) in weekDays" :key="index" class="week-day">{{ day }}</text>
				</view>
				<view class="calendar-days">
					<view v-for="(day, index) in calendarDays" :key="index"
						:class="['day', day.hasData ? 'active' : '', day.isToday ? 'today' : '']">
						<text class="day-number">{{ day.day }}</text>
						<view :class="['day-dot', day.hasData ? 'active' : '']"></view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		onLoad,
		onShow
	} from '@dcloudio/uni-app'
	import {
		ref,
		reactive,
		inject
	} from 'vue'
	import {
		LOCAL_STORAGE_KEY
	} from '../../utils/constants'
	import {
		useUserStore
	} from '../../stores/userStore.js'
	const userStore = useUserStore()
	const proxy = inject('utils')

	// 用户信息
	const userInfo = ref({})

	// 最近学习记录
	const recentStudyRecords = ref([])

	// 最近错题
	const recentWrongQuestions = ref([])

	// 日历相关数据
	const weekDays = ref(['日', '一', '二', '三', '四', '五', '六'])
	const calendarDays = ref([])
	const currentMonth = ref('')

	// 加载用户数据
	const loadUserInfo = async () => {
		let result = await proxy.Request({
			url: proxy.Api.getUserInfo,
			showLoading: false
		})
		if (!result) {
			return
		}
		userInfo.value = result.data
		userStore.setUserInfo(result.data)
	}

	// 打卡
	const sign = async () => {
		const now = new Date().getDate()
		const nowIndex = calendarDays.value.findIndex(item => item.day === now)
		if (calendarDays.value[nowIndex].hasData) {
			proxy.Message.warning('今日已打卡')
			return
		}
		let result = await proxy.Request({
			url: proxy.Api.sign,
			showLoading: false
		})
		if (result.code === 200) {
			proxy.Message.success('打卡成功，积分+10')
			initCalendar()
		}
	}

	// 编辑资料
	const goEdit = () => {
		uni.navigateTo({
			url: '/pages/my/edit'
		})
	}

	// 修改密码
	const changePassword = () => {
		uni.navigateTo({
			url: '/pages/my/changePassword'
		})
	}

	// 退出登录
	const logout = () => {
		uni.showModal({
			title: '提示',
			content: '确定要退出登录吗？',
			success: async () => {
				let result = await proxy.Request({
					url: proxy.Api.logout,
					showLoading: false
				})
				if (!result) {
					return
				}
				proxy.Message.success('退出成功')
				uni.removeStorageSync(LOCAL_STORAGE_KEY.token.key)
				uni.reLaunch({
					url: '/pages/login/login'
				})
			}
		})
	}

	// 加载最近学习记录
	const loadRecentStudyRecords = async () => {
		let result = await proxy.Request({
			url: proxy.Api.getRecentExam,
			showLoading: false
		})
		if (!result) {
			return
		}
		recentStudyRecords.value = result.data
	}
	
	const getRecordResult = (record) => {
		const accuracy = record.score / record.totalScore
		return accuracy > 0.6 ? ['pass', '通过'] : ['fail', '未通过']
	}
	
	// 格式化学习时间
	const formatStudyTime = (timeString) => {
	    const date = new Date(timeString)
	    const now = new Date()
	    const diffTime = now.getTime() - date.getTime()
	    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
	    
	    if (diffDays === 0) {
	        const hours = date.getHours()
	        const minutes = String(date.getMinutes()).padStart(2, '0')
	        return `今天 ${hours}:${minutes}`
	    } else if (diffDays === 1) {
	        const hours = date.getHours()
	        const minutes = String(date.getMinutes()).padStart(2, '0')
	        return `昨天 ${hours}:${minutes}`
	    } else if (diffDays < 7) {
	        return `${diffDays}天前`
	    } else {
	        const month = date.getMonth() + 1
	        const day = date.getDate()
	        return `${month}月${day}日`
	    }
	}

	// 加载最近错题
	const loadRecentWrongQuestions = async () => {
		let result = await proxy.Request({
			url: proxy.Api.getRecentWrongQuestions,
			showLoading: false
		})
		if (!result) {
			return
		}
		recentWrongQuestions.value = result.data
	}

	// 初始化日历
	const initCalendar = async () => {
		const now = new Date()
		const year = now.getFullYear()
		const month = now.getMonth()
		const firstDay = new Date(year, month, 1)
		const lastDay = new Date(year, month + 1, 0)
		const daysInMonth = lastDay.getDate()

		// 设置当前月份
		currentMonth.value = `${year}年${month + 1}月`

		// 生成日历数组
		let days = []
		const startDay = firstDay.getDay() // 本月第一天是周几

		// 上个月的最后几天
		for (let i = 0; i < startDay; i++) {
			days.push({
				day: '',
				hasData: false
			})
		}

		// 本月的日期
		const studyRecords = await getStudyRecords()
		for (let i = 1; i <= daysInMonth; i++) {
			const dateStr = `${year}-${month + 1}-${i}`
			const isToday = i === now.getDate() && month === now.getMonth()
			days.push({
				day: i,
				date: dateStr,
				hasData: studyRecords[dateStr],
				isToday: isToday
			})
		}

		calendarDays.value = days
	}

	// 获取学习记录
	const getStudyRecords = async () => {
		let result = await proxy.Request({
			url: proxy.Api.getMonthSignInfo,
			showLoading: false
		})
		if (!result) {
			return
		}
		return result.data
	}

	// 查看学习记录
	const viewAllStudyRecords = () => {
		uni.navigateTo({
			url: '/pages/report/report'
		})
	}

	const viewStudyDetail = (id) => {
		uni.navigateTo({
			url: `/pages/report/report-detail?id=${id}`
		})
	}

	// 查看错题
	const viewWrongQuestions = () => {
		uni.navigateTo({
			url: '/pages/wrong/wrong'
		})
	}

	const viewWrongDetail = (id) => {
		uni.navigateTo({
			url: `/pages/wrong/wrong-detail?id=${id}&s`
		})
	}

	// 生命周期
	onLoad(() => {
		initCalendar()
	})

	onShow(() => {
		loadUserInfo()
		loadRecentWrongQuestions()
		loadRecentStudyRecords()
	})
</script>

<style lang="scss" scoped>
	.container {
		padding: 20rpx;
	}

	/* 用户信息区域 */
	.user-info {
		display: flex;
		align-items: center;
		padding: 30rpx;
		background: linear-gradient(135deg, #6190E8, #A7BFE8);
		border-radius: 16rpx;
		margin-bottom: 30rpx;
		box-shadow: 0 8rpx 24rpx rgba(97, 144, 232, 0.15);
	}

	.avatar {
		width: 120rpx;
		height: 120rpx;
		border-radius: 50%;
		border: 4rpx solid rgba(255, 255, 255, 0.3);
		margin-right: 24rpx;
	}

	.user-meta {
		flex: 1;
		display: flex;
		flex-direction: column;
	}

	.nickname {
		font-size: 36rpx;
		font-weight: 600;
		color: #fff;
		margin-bottom: 12rpx;
	}

	.member-info {
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.8);
		background: rgba(0, 0, 0, 0.1);
		padding: 6rpx 12rpx;
		border-radius: 20rpx;
		align-self: flex-start;
	}

	/* 选项卡片 */
	.function-card {
		background-color: #fff;
		border-radius: 20rpx;
		padding: 30rpx 20rpx;
		margin-bottom: 30rpx;
		box-shadow: 0 6rpx 18rpx rgba(0, 0, 0, 0.06);
	}

	.function-row {
		display: flex;
		justify-content: space-between;
	}

	.function-item {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 15rpx 5rpx;
		border-radius: 16rpx;
		transition: all 0.3s ease;

		&:active {
			background-color: #f8f9fa;
			transform: translateY(4rpx);
		}
	}

	.function-icon {
		width: 70rpx;
		height: 70rpx;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-bottom: 12rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
	}

	.checkin-icon {
		background: linear-gradient(135deg, #6190E8 0%, #3A6FE8 100%);
	}

	.edit-icon {
		background: linear-gradient(135deg, #67C23A 0%, #5BAF2E 100%);
	}

	.password-icon {
		background: linear-gradient(135deg, #9C27B0 0%, #7B1FA2 100%);
	}

	.logout-icon {
		background: linear-gradient(135deg, #E6A23C 0%, #D9901A 100%);
	}

	.function-label {
		font-size: 24rpx;
		color: #333;
		font-weight: 500;
		text-align: center;
	}

	/* 分区标题样式 */
	.section {
		background-color: #fff;
		border-radius: 16rpx;
		padding: 0 30rpx 30rpx;
		margin-bottom: 30rpx;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
	}

	.section-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 30rpx 0;
		border-bottom: 1rpx solid #f1f3f5;
	}

	.section-title {
		font-size: 32rpx;
		font-weight: 600;
		color: #333;
		position: relative;
		padding-left: 16rpx;

		&::before {
			content: "";
			position: absolute;
			left: 0;
			top: 8rpx;
			bottom: 8rpx;
			width: 6rpx;
			background-color: #6190E8;
			border-radius: 3rpx;
		}
	}

	.section-more {
		font-size: 24rpx;
		color: #909399;
	}

	/* 最近学习记录样式 */
	.study-records {
		margin-top: 20rpx;
	}

	.record-item {
		padding: 24rpx;
		background-color: #f8f9fa;
		border-radius: 12rpx;
		margin-bottom: 20rpx;
		transition: all 0.3s;
		display: flex;
		justify-content: space-between;
		align-items: flex-start;

		&:active {
			transform: translateY(4rpx);
			box-shadow: none;
		}
	}

	.record-main {
		flex: 1;
		margin-right: 20rpx;
	}

	.record-title {
		font-size: 28rpx;
		font-weight: 600;
		color: #333;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
		margin-bottom: 16rpx;
		line-height: 1.4;
	}

	.record-meta {
		display: flex;
		align-items: center;
		gap: 16rpx;
	}

	.record-type {
		font-size: 24rpx;
		padding: 4rpx 12rpx;
		border-radius: 12rpx;
		font-weight: 500;

		&.pass {
			background-color: #e8f4ff;
			color: #5cd89e;
		}

		&.fail {
			background-color: #fff0e8;
			color: #ff3908;
		}
	}

	.record-duration {
		font-size: 24rpx;
		color: #909399;
	}

	.record-time {
		font-size: 24rpx;
		color: #909399;
		flex-shrink: 0;
		text-align: right;
	}

	/* 最近错题样式 */
	.wrong-questions {
		margin-top: 20rpx;
	}

	.wrong-item {
		padding: 24rpx;
		background-color: #f8f9fa;
		border-radius: 12rpx;
		margin-bottom: 20rpx;
		transition: all 0.3s;

		&:active {
			transform: translateY(4rpx);
			box-shadow: none;
		}
	}

	.wrong-title {
		font-size: 28rpx;
		color: #333;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
		margin-bottom: 16rpx;
	}

	.wrong-meta {
		display: flex;
		justify-content: space-between;
		font-size: 24rpx;
		color: #909399;
	}

	.wrong-category {
		background-color: #e8f4ff;
		color: #6190E8;
		padding: 4rpx 12rpx;
		border-radius: 12rpx;
	}

	/* 空状态样式 */
	.empty-tip {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 60rpx 0;
		color: #909399;
	}

	/* 学习日历样式 */
	.calendar {
		margin-top: 20rpx;
	}

	.calendar-header {
		display: flex;
		justify-content: space-between;
		margin-bottom: 20rpx;
	}

	.week-day {
		flex: 1;
		text-align: center;
		font-size: 24rpx;
		color: #909399;
	}

	.calendar-days {
		display: flex;
		flex-wrap: wrap;
	}

	.day {
		width: 14.28%;
		aspect-ratio: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		position: relative;
	}

	.day-number {
		font-size: 28rpx;
		color: #333;
	}

	.day.active .day-number {
		font-weight: 600;
		color: #6190E8;
	}

	.day.today .day-number {
		color: #fff;
		background-color: #6190E8;
		width: 50rpx;
		height: 50rpx;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.day-dot {
		width: 8rpx;
		height: 8rpx;
		border-radius: 50%;
		margin-top: 6rpx;

		&.active {
			background-color: #6190E8;
		}
	}

	/* 动画效果 */
	@keyframes fadeIn {
		from {
			opacity: 0;
			transform: translateY(20rpx);
		}

		to {
			opacity: 1;
			transform: translateY(0);
		}
	}

	.user-info,
	.function-card,
	.section {
		animation: fadeIn 0.5s ease-out forwards;
	}

	.section:nth-child(1) {
		animation-delay: 0.1s;
	}

	.section:nth-child(2) {
		animation-delay: 0.2s;
	}

	.section:nth-child(3) {
		animation-delay: 0.3s;
	}
</style>