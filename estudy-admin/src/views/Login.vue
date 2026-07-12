<template>
  <div class="login-body">
    <div class="login-panel">
      <el-form
        class="login-form"
        :model="formData"
        :rules="rules"
        ref="formDataRef"
        @submit.prevent
      >
        <div class="form-title">Estudy管理后台</div>
        <el-form-item prop="username">
          <el-input
            clearable
            size="large"
            placeholder="请输入账号"
            v-model.trim="formData.username"
          >
            <template #prefix>
              <span class="iconfont icon-phone"></span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            size="large"
            type="password"
            clearable
            show-password
            placeholder="请输入密码"
            v-model.trim="formData.password"
          >
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="checkCode">
          <div class="verify-panel">
            <el-input
              clearable
              size="large"
              placeholder="请输入验证码"
              v-model.trim="formData.checkCode"
            >
              <template #prefix>
                <span class="iconfont icon-verify"></span>
              </template>
            </el-input>
            <img
              class="verify-code"
              :src="checkCode"
              @click="changeCheckCode"
            />
          </div>
        </el-form-item>
        <el-form-item>
          <div class="remember-panel">
            <el-checkbox v-model="formData.remember">记住账号</el-checkbox>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="op-btn" size="large" @click="login">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import md5 from 'js-md5'
import { ref, getCurrentInstance, nextTick, onMounted } from 'vue'
const { proxy } = getCurrentInstance()
import { useRoute, useRouter } from 'vue-router'
const route = useRoute()
const router = useRouter()

const formData = ref({})
const formDataRef = ref()
const rules = {
  username: [{ required: true, message: '请输入账号' }],
  password: [{ required: true, message: '请输入密码' }],
  checkCode: [{ required: true, message: '请输入验证码' }]
}

// 验证码
const checkCode = ref(null)
const changeCheckCode = async () => {
  let result = await proxy.Request({
    url: proxy.Api.checkCode
  })
  if (!result) {
    return
  }
  checkCode.value = result.data
}

// 登录
const login = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    let params = {}
    Object.assign(params, formData.value)

    const cookiesPwd = proxy.VueCookies.get('loginInfo')?.password
    if (params.password !== cookiesPwd) {
      params.password = md5(params.password)
    }

    let result = await proxy.Request({
      url: proxy.Api.login,
      params,
      errorCallback: () => {
        changeCheckCode()
      }
    })
    if (!result) {
      return
    }
    if (params.remember) {
      const loginInfo = {
        username: params.username,
        password: params.password,
        remember: params.remember
      }
      proxy.VueCookies.set('loginInfo', loginInfo, '7d')
    } else {
      proxy.VueCookies.remove('loginInfo')
    }
    proxy.Message.success('登录成功')
    proxy.VueCookies.set('token', result.data)
    sessionStorage.setItem('token', result.data)
    router.push('/')
  })
}

const init = () => {
  nextTick(() => {
    changeCheckCode()
    formDataRef.value.resetFields()
    formData.value = {}
    const loginInfo = proxy.VueCookies.get('loginInfo')
    if (loginInfo) {
      formData.value = loginInfo
    }
  })
}

onMounted(() => {
  init()
})
</script>

<style lang="scss" scoped>
.login-body {
  display: flex;
  height: calc(100vh);
  background: url('../assets/login-bg.png');
  background-size: cover;
  background-position: center center;
  background-repeat: no-repeat;

  .login-panel {
    margin: 26vh auto;
    width: 430px;

    .login-form {
      padding: 25px;
      background: #fff;
      border-radius: 5px;

      .form-title {
        margin-bottom: 20px;
        text-align: center;
        font-size: 18px;
        font-weight: bold;
      }

      .remember-panel {
        width: 100%;
      }

      .op-btn {
        width: 100%;
      }
    }
  }

  .verify-panel {
    display: flex;
    width: 100%;

    .verify-code {
      margin-left: 5px;
      cursor: pointer;
    }
  }
}
</style>
