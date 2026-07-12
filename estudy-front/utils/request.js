import message from './message.js'
import api from './api.js'
import { LOCAL_STORAGE_KEY } from './constants.js'

const contentTypeForm = 'application/x-www-form-urlencoded;charset=UTF-8'
const contentTypeJson = 'application/json'
const responseTypeJson = 'json'

let port = uni.getSystemInfoSync().uniPlatform
let baseUrl = port === 'web' ? '/api' : `${api.domain}/api`

const request = (config) => {
	const { url, params, dataType, showLoading = true, showError = true, errorCallback, responseType = responseTypeJson} = config
	let contentType = dataType === 'json' ? contentTypeJson : contentTypeForm
	let formData = params
	if (dataType !== 'json') {
		formData = new FormData()
		for (let key in params) {
			formData.append(key, params[key] ?? '')
		}
	}
	let headers = {
		'Content-Type': contentType,
		'X-Requested-With': 'XMLHttpRequest',
		'token': uni.getStorageSync(LOCAL_STORAGE_KEY.token.key)
	}
	if (params) {
		for (let item in params) {
			params[item] ??= '' // undefined则赋值为''
		}
	}
	return new Promise((resolve, reject) => {
		if (showLoading) {
			uni.showLoading()
		}
		uni.request({
			url: `${baseUrl}${url}`,
			data: params,
			header: headers,
			responseType: responseType,
			method: 'POST'
		}).then(res => {
			if (showLoading) {
				uni.hideLoading()
			}
			uni.stopPullDownRefresh()
			if (res.statusCode !== 200) {
				return Promise.reject('网络连接错误')
			}
			const responseData = res.data
			if (responseType === 'arraybuffer' || responseType === 'blob') {
				resolve(responseData)
				return
			}
			if (responseData.code === 200) {
				resolve(responseData)
				return
			} else if (responseData.code === 401) {
				uni.navigateTo({
					url: '/pages/login/login'
				})
				return Promise.reject()
			} else {
				errorCallback && errorCallback(responseData.info)
				return Promise.reject(responseData.info)
			}
		}).catch(err => {
			if (err && showError) {
				message.error(err)
			}
			return null
		})
	})
}

export default request