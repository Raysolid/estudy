<template>
  <div class="user-container">
    <!-- 搜索和操作区域 -->
    <div class="filter-container">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="用户ID">
          <el-input
            v-model="searchForm.userId"
            placeholder="请输入用户ID"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input
            v-model="searchForm.email"
            placeholder="请输入邮箱"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input
            v-model="searchForm.nickname"
            placeholder="请输入昵称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <div class="operation-container">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column
          prop="userId"
          label="用户ID"
          width="120"
          align="center"
        />
        <el-table-column
          prop="email"
          label="邮箱"
          min-width="180"
          align="center"
        />
        <el-table-column
          prop="nickname"
          label="昵称"
          width="120"
          align="center"
        />
        <el-table-column prop="points" label="积分" width="100" align="center">
          <template #default="scope">
            <el-tag type="warning">{{ scope.row.points }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="preferenceNames"
          label="学习偏好"
          min-width="150"
          align="center"
          show-overflow-tooltip
        />
        <el-table-column
          prop="createTime"
          label="注册时间"
          width="180"
          align="center"
        >
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleEdit(scope.row)"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(scope.row)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNo"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 通用弹框 -->
    <Dialog
      v-model:visible="dialogVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      :form-config="formConfig"
      :form-data="currentRow"
      :rules="formRules"
      :loading="dialogLoading"
      :confirm-text="isEdit ? '更新' : '创建'"
      @confirm="handleDialogConfirm"
      @cancel="handleDialogCancel"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, getCurrentInstance } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import Dialog from '@/components/Dialog.vue'
import { md5 } from 'js-md5'
const { proxy } = getCurrentInstance()

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const isEdit = ref(false)
const currentRow = ref({})

// 分类选项（用于学习偏好选择）
const categoryOptions = ref([])

// 搜索表单
const searchForm = reactive({
  userId: '',
  email: '',
  nickname: ''
})

// 分页数据
const pagination = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0
})

// 表格数据
const tableData = ref([])

// 表单配置
const formConfig = computed(() => [
  {
    prop: 'userId',
    label: '用户ID',
    type: 'input',
    placeholder: '后台自动生成用户ID',
    maxlength: 12,
    showWordLimit: true,
    disabled: true
  },
  {
    prop: 'email',
    label: '邮箱',
    type: 'input',
    placeholder: '请输入邮箱',
    maxlength: 50,
    showWordLimit: true,
    required: true
  },
  {
    prop: 'nickname',
    label: '昵称',
    type: 'input',
    placeholder: '请输入昵称',
    maxlength: 20,
    showWordLimit: true,
    required: true
  },
  {
    prop: 'password',
    label: '密码',
    type: 'input',
    placeholder: isEdit.value ? '留空表示不修改密码' : '请输入密码',
    maxlength: 50,
    showWordLimit: true,
    required: !isEdit.value // 新增时必填，编辑时非必填
  },
  {
    prop: 'points',
    label: '积分',
    type: 'number',
    placeholder: '请输入积分',
    min: 0,
    max: 999999,
    required: false
  },
  {
    prop: 'preference',
    label: '学习偏好',
    type: 'select',
    placeholder: '请选择学习偏好',
    multiple: true,
    options: categoryOptions.value.map((cat) => ({
      label: cat.name,
      value: cat.categoryId
    })),
    required: false
  }
])

// 表单验证规则
const formRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 1, max: 20, message: '昵称长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    {
      required: !isEdit.value,
      message: '请输入密码',
      trigger: 'blur'
    },
    {
      min: 6,
      message: '密码长度不能少于6位',
      trigger: 'blur'
    }
  ],
  points: [
    {
      type: 'number',
      min: 0,
      max: 999999,
      message: '积分范围在 0-999999',
      trigger: 'blur'
    }
  ]
}

// 获取用户列表
const getUserList = async () => {
  loading.value = true
  try {
    let result = await proxy.Request({
      url: proxy.Api.getUserList,
      params: {
        ...searchForm,
        pageNo: pagination.pageNo,
        pageSize: pagination.pageSize
      }
    })
    if (!result) {
      return
    }
    loading.value = false
    tableData.value = result.data.list
    pagination.total = result.data.totalCount

    tableData.value.forEach((user) => {
      if (user.preference) {
        const prefer = user.preference.split(',')
        user.preferenceNames = prefer.map(
          (id) => categoryOptions.value[id]?.name
        )
      } else {
        user.preferenceNames = '-'
      }
    })
  } catch (error) {
    loading.value = false
    proxy.Message.error('获取用户列表失败')
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

// 搜索
const handleSearch = () => {
  pagination.pageNo = 1
  getUserList()
}

// 重置
const handleReset = () => {
  searchForm.userId = ''
  searchForm.email = ''
  searchForm.nickname = ''
  pagination.pageNo = 1
  getUserList()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  currentRow.value = {
    userId: '',
    email: '',
    nickname: '',
    password: '',
    points: 0,
    preference: []
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  // 将偏好字符串转换为数组
  const preferenceArray = row.preference
    ? row.preference.split(',').map((id) => id.trim())
    : []
  currentRow.value = {
    ...row,
    preference: preferenceArray,
    password: '' // 编辑时不显示原密码
  }
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户"${row.nickname}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    let result = await proxy.Request({
      url: proxy.Api.deleteUser,
      params: {
        userId: row.userId
      }
    })
    if (!result) {
      return
    }

    proxy.Message.success('删除成功')
    getUserList()
  } catch (error) {
    if (error !== 'cancel') {
      proxy.Message.error('删除失败')
    }
  }
}

// 分页大小改变
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.pageNo = 1
  getUserList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  pagination.pageNo = val
  getUserList()
}

// 弹框确认
const handleDialogConfirm = async (formData) => {
  debugger
  // 处理学习偏好数据（数组转字符串）
  const processedData = {
    ...formData,
    preference:
      formData.preference && formData.preference.length > 0
        ? formData.preference.join(',')
        : null
  }

  // 编辑时如果密码为空，则不更新密码
  if (isEdit.value && !processedData.password) {
    delete processedData.password
  }

  dialogLoading.value = true
  try {
    await editUser(processedData)
    proxy.Message.success(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false
    getUserList()
  } catch (error) {
    proxy.Message.error('操作失败')
  } finally {
    dialogLoading.value = false
  }
}

const editUser = async (formData) => {
  if (isEdit.value && formData.password) {
    formData.password = md5(formData.password)
  }
  let result = await proxy.Request({
    url: proxy.Api.editUser,
    params: {
      ...formData
    }
  })
  if (!result) {
    return
  }
}

// 弹框取消
const handleDialogCancel = () => {
  dialogVisible.value = false
}

onMounted(() => {
  const category = JSON.parse(sessionStorage.getItem('category'))
  categoryOptions.value = category || []
  getUserList()
})
</script>

<style lang="scss" scoped>
.user-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

  .filter-container {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 20px;

    .operation-container {
      margin-left: auto;
    }
  }

  .table-container {
    .pagination-container {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}

// 响应式布局
@media (max-width: 768px) {
  .user-container {
    .filter-container {
      flex-direction: column;

      .operation-container {
        margin-left: 0;
        margin-top: 15px;
        width: 100%;

        .el-button {
          width: 100%;
        }
      }
    }
  }
}
</style>
