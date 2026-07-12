import { defineStore } from 'pinia'

export const useUserStore = defineStore('userStore', {
	state: () => {
		return {
			userInfo: {}
		}
	},
	actions: {
		setUserInfo(userInfo) {
			this.userInfo = userInfo
		},
		getUserInfo() {
			return this.userInfo
		}
	}
})