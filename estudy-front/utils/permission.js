// 路由拦截器配置
import { LOCAL_STORAGE_KEY } from "./constants";

const setupRouterInterceptor = () => {
	const whiteList = [
		'/pages/login/login',
		'/',
		'/pages/category/category'
	]
	const list = ['navigateTo', 'redirectTo', 'reLaunch', 'switchTab'];
	// 为uni.navigateTo/uni.redirectTo/uni.reLaunch/uni.switchTab这4个路由方法添加拦截器
	list.forEach((item) => {
		uni.addInterceptor(item, {
			invoke(e) {
				const url = e.url.split('?')[0]
				// 判断当前用户是否登录
				let token = uni.getStorageSync(LOCAL_STORAGE_KEY.token.key)
				if (!token && !whiteList.includes(url)) {
					uni.reLaunch({
						url: '/pages/login/login',
					})
					return false
				}
				return true
			},
			fail(err) {
				// 失败回调拦截
				console.log(err)
			},
		})
	})
}

export default setupRouterInterceptor