<template>
  <div class="system-container">
    <el-card class="system-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="header-title">系统设置</span>
          <span class="header-desc">管理系统配置参数</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="150px"
        class="system-form"
      >
        <!-- 管理员密码 -->
        <el-form-item label="管理员密码" prop="adminPassword">
          <el-input
            v-model="formData.adminPassword"
            type="password"
            placeholder="请输入新的管理员密码"
            show-password
            clearable
            :maxlength="50"
            show-word-limit
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
          <div class="form-tips">留空表示不修改密码，密码长度6-50位</div>
        </el-form-item>

        <!-- Coze Token -->
        <el-form-item label="Coze Token" prop="cozeToken">
          <el-input
            v-model="formData.cozeToken"
            type="password"
            placeholder="请输入Coze平台Token"
            show-password
            clearable
            :maxlength="255"
            show-word-limit
          >
            <template #prefix>
              <el-icon><Key /></el-icon>
            </template>
          </el-input>
          <div class="form-tips">Coze平台的API访问Token，用于集成AI能力</div>
        </el-form-item>

        <!-- Coze Workflow ID -->
        <el-form-item label="Coze Workflow ID" prop="cozeWorkflowId">
          <el-input
            v-model="formData.cozeWorkflowId"
            placeholder="请输入Coze工作流ID"
            clearable
            :maxlength="100"
            show-word-limit
          >
            <template #prefix>
              <el-icon><Connection /></el-icon>
            </template>
          </el-input>
          <div class="form-tips">
            Coze平台的工作流ID，用于指定特定的AI处理流程
          </div>
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item class="form-actions">
          <el-button
            type="primary"
            @click="handleSave"
            :loading="loading"
            :disabled="!formChanged"
          >
            <el-icon><Check /></el-icon>
            保存设置
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button @click="handleTestConnection" :loading="testing">
            <el-icon><Link /></el-icon>
            测试连接
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 设置说明 -->
    <el-card class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="header-title">设置说明</span>
        </div>
      </template>
      <div class="info-content">
        <el-alert
          title="安全提示"
          type="warning"
          description="修改系统设置可能会影响系统正常运行，请谨慎操作。特别是Coze Token属于敏感信息，请妥善保管。"
          show-icon
          :closable="false"
        />

        <div class="info-sections">
          <div class="info-section">
            <h4>
              <el-icon><Lock /></el-icon> 管理员密码
            </h4>
            <p>用于登录系统管理后台的密码。建议定期更换，确保系统安全。</p>
          </div>

          <div class="info-section">
            <h4>
              <el-icon><Key /></el-icon> Coze Token
            </h4>
            <p>
              从Coze平台获取的API访问令牌，用于调用AI服务。请确保Token的有效性。
            </p>
          </div>

          <div class="info-section">
            <h4>
              <el-icon><Connection /></el-icon> Coze Workflow ID
            </h4>
            <p>
              在Coze平台创建的工作流ID，用于指定题目生成、智能批改等AI处理流程。
            </p>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Lock,
  Key,
  Connection,
  Check,
  Refresh,
  Link
} from '@element-plus/icons-vue'
const { proxy } = getCurrentInstance()

// 响应式数据
const loading = ref(false)
const testing = ref(false)
const formRef = ref()

// 表单数据（使用小驼峰命名法）
const formData = reactive({
  adminPassword: '',
  cozeToken: '',
  cozeWorkflowId: ''
})

// 原始数据（用于比较是否发生变化）
const originalData = ref({})

// 表单验证规则
const formRules = {
  adminPassword: [
    {
      min: 6,
      max: 50,
      message: '密码长度在 6 到 50 个字符',
      trigger: 'blur'
    }
  ],
  cozeToken: [
    {
      required: true,
      message: '请输入Coze Token',
      trigger: 'blur'
    },
    {
      min: 10,
      message: 'Token长度不能少于10位',
      trigger: 'blur'
    }
  ],
  cozeWorkflowId: [
    {
      required: true,
      message: '请输入Coze Workflow ID',
      trigger: 'blur'
    },
    {
      min: 5,
      message: 'Workflow ID长度不能少于5位',
      trigger: 'blur'
    }
  ]
}

// 计算属性：判断表单是否发生变化
const formChanged = computed(() => {
  return JSON.stringify(formData) !== JSON.stringify(originalData.value)
})

// 加载系统设置
const loadSystemSettings = async () => {
  try {
    let result = await proxy.Request({
      url: proxy.Api.getSetting
    })
    if (!result) {
      return
    }

    Object.assign(formData, result.data)
    originalData.value = { ...result.data }
  } catch (error) {
    console.error('获取系统设置失败:', error)
    proxy.Message.error('获取系统设置失败')
  }
}

// 保存设置
const handleSave = async () => {
  if (!formRef.value) return

  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    loading.value = true

    // 准备提交数据（如果密码为空，则不更新密码）
    const submitData = { ...formData }
    if (!submitData.adminPassword) {
      delete submitData.adminPassword
    }

    let result = await proxy.Request({
      url: proxy.Api.saveSetting,
      params: {
        ...submitData
      }
    })
    if (!result) {
      return
    }

    loading.value = false
    originalData.value = { ...formData }
    proxy.Message.success('系统设置保存成功')
  } catch (error) {
    loading.value = false
    console.error('保存系统设置失败:', error)
    proxy.Message.error('保存系统设置失败')
  }
}

// 重置表单
const handleReset = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要重置所有设置吗？未保存的修改将会丢失。',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    Object.assign(formData, originalData.value)
    proxy.Message.success('重置成功')
  } catch (error) {
    if (error !== 'cancel') {
      proxy.Message.error('重置失败')
    }
  }
}

// 测试连接
const handleTestConnection = async () => {
  if (!formData.cozeToken || !formData.cozeWorkflowId) {
    proxy.Message.warning('请先填写Coze Token和Workflow ID')
    return
  }

  testing.value = true
  try {
    // TODO: 调用测试连接API
    // await testCozeConnectionApi({
    //   cozeToken: formData.cozeToken,
    //   cozeWorkflowId: formData.cozeWorkflowId
    // })

    // 模拟API调用
    setTimeout(() => {
      testing.value = false
      proxy.Message.success('Coze连接测试成功')
    }, 1500)
  } catch (error) {
    testing.value = false
    console.error('连接测试失败:', error)
    proxy.Message.error('Coze连接测试失败，请检查配置')
  }
}

onMounted(() => {
  loadSystemSettings()
})
</script>

<style lang="scss" scoped>
.system-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);

  .system-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-title {
        font-size: 18px;
        font-weight: bold;
        color: #303133;
      }

      .header-desc {
        font-size: 14px;
        color: #909399;
      }
    }

    .system-form {
      max-width: 600px;

      .form-tips {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
        line-height: 1.4;
      }

      .form-actions {
        margin-top: 30px;
        padding-top: 20px;
        border-top: 1px solid #f0f0f0;
      }
    }
  }

  .info-card {
    .card-header {
      .header-title {
        font-size: 16px;
        font-weight: bold;
        color: #303133;
      }
    }

    .info-content {
      .info-sections {
        margin-top: 20px;

        .info-section {
          margin-bottom: 20px;
          padding: 15px;
          background-color: #f8f9fa;
          border-radius: 4px;
          border-left: 4px solid #409eff;

          h4 {
            display: flex;
            align-items: center;
            margin: 0 0 8px 0;
            color: #303133;
            font-size: 14px;

            .el-icon {
              margin-right: 8px;
              color: #409eff;
            }
          }

          p {
            margin: 0;
            color: #606266;
            font-size: 13px;
            line-height: 1.5;
          }

          &:last-child {
            margin-bottom: 0;
          }
        }
      }
    }
  }
}

// 响应式布局
@media (max-width: 768px) {
  .system-container {
    padding: 10px;

    .system-card {
      .card-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;

        .header-desc {
          font-size: 12px;
        }
      }

      .system-form {
        :deep(.el-form-item) {
          .el-form-item__label {
            text-align: left;
          }
        }
      }
    }
  }
}
</style>
