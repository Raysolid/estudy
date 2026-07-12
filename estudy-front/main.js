import { createSSRApp } from 'vue'
import App from './App'

import * as Pinia from 'pinia'
import message from './utils/message.js'
import request from './utils/request.js'
import api from './utils/api.js'

import setupRouterInterceptor from './utils/permission.js'
setupRouterInterceptor()

export function createApp() {
  const app = createSSRApp(App)
  
  app.use(Pinia.createPinia())
  app.provide('utils', {
	  Message: message,
	  Request: request,
	  Api: api
  })
  app.config.globalProperties.Message = message
  app.config.globalProperties.Request = request
  app.config.globalProperties.Api = api

  return {
    app
  }
}
