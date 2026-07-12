const message = {
	success: (msg, callback) => {
		uni.showToast({
			title: msg,
			icon: 'success',
			success: callback || null
		})
	},
	error: (msg, callback) => {
		uni.showToast({
			title: msg,
			icon: 'error',
			success: callback || null
		})
	},
	warning: (msg, callback) => {
		uni.showToast({
			title: msg,
			icon: 'none',
			success: callback || null
		})
	}
}

export default message