import { defineStore } from 'pinia'

export const useTaskStore = defineStore('task', {
  state: () => ({
    taskId: '',
    taskResult: ''
  }),
  
  actions: {
    setTaskId(taskId) {
      this.taskId = taskId
      // 可选：持久化存储到本地
      if (taskId) {
        uni.setStorageSync('current_task_id', taskId)
      } else {
        uni.removeStorageSync('current_task_id')
      }
    },
    
    getTaskId() {
      // 优先从内存读取，如果没有则从本地存储读取
      if (this.taskId) {
        return this.taskId
      }
      return uni.getStorageSync('current_task_id') || ''
    },
    
    setTaskResult(result) {
      this.taskResult = result
      // 可选：持久化存储到本地
      if (result) {
        uni.setStorageSync('current_task_result', result)
      } else {
        uni.removeStorageSync('current_task_result')
      }
    },
    
    getTaskResult() {
      // 优先从内存读取，如果没有则从本地存储读取
      if (this.taskResult) {
        return this.taskResult
      }
      return uni.getStorageSync('current_task_result') || ''
    },
    
    clearTask() {
      this.taskId = ''
      this.taskResult = ''
      uni.removeStorageSync('current_task_id')
      uni.removeStorageSync('current_task_result')
    }
  }
})