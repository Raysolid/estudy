import { ElMessage } from 'element-plus'

const showMessage = (msg, callback, type) => {
  ElMessage({
    type: type,
    message: msg,
    duration: 2000,
    offset: 100,
    onClose: () => {
      if (callback) {
        callback()
      }
    }
  })
}

const message = {
  error: (msg, callback) => {
    showMessage(msg, callback, 'error')
  },
  success: (msg, callback) => {
    showMessage(msg, callback, 'success')
  },
  info: (msg, callback) => {
    showMessage(msg, callback, 'info')
  },
  warning: (msg, callback) => {
    showMessage(msg, callback, 'warning')
  }
}

export default message
