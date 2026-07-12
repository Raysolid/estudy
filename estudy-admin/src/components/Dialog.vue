<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :before-close="handleClose"
    :close-on-click-modal="false"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-width="labelWidth"
    >
      <el-form-item
        v-for="item in formConfig"
        :key="item.prop"
        :label="item.label"
        :prop="item.prop"
        :required="item.required"
      >
        <!-- 输入框 -->
        <el-input
          v-if="item.type === 'input'"
          v-model="formData[item.prop]"
          :placeholder="item.placeholder"
          :maxlength="item.maxlength"
          :show-word-limit="item.showWordLimit"
          :disabled="item.disabled"
        />

        <!-- 文本域 -->
        <el-input
          v-else-if="item.type === 'textarea'"
          v-model="formData[item.prop]"
          type="textarea"
          :rows="item.rows || 4"
          :placeholder="item.placeholder"
          :maxlength="item.maxlength"
          :show-word-limit="item.showWordLimit"
          :disabled="item.disabled"
        />

        <!-- 数字输入框 -->
        <el-input-number
          v-else-if="item.type === 'number'"
          v-model="formData[item.prop]"
          :min="item.min"
          :max="item.max"
          :step="item.step"
          :placeholder="item.placeholder"
          :disabled="item.disabled"
          controls-position="right"
        />

        <!-- 选择器 -->
        <el-select
          v-else-if="item.type === 'select'"
          v-model="formData[item.prop]"
          :placeholder="item.placeholder"
          :multiple="item.multiple"
          :disabled="item.disabled"
          style="width: 100%"
        >
          <el-option
            v-for="option in item.options"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>

        <!-- 日期选择器 -->
        <el-date-picker
          v-else-if="item.type === 'date'"
          v-model="formData[item.prop]"
          :type="item.dateType || 'date'"
          :placeholder="item.placeholder"
          :format="item.format"
          :value-format="item.valueFormat"
          :disabled="item.disabled"
          style="width: 100%"
        />

        <!-- 开关 -->
        <el-switch
          v-else-if="item.type === 'switch'"
          v-model="formData[item.prop]"
          :active-text="item.activeText"
          :inactive-text="item.inactiveText"
          :disabled="item.disabled"
        />

        <!-- 自定义插槽 -->
        <slot
          v-else-if="item.type === 'slot'"
          :name="item.slotName"
          :form-data="formData"
          :item="item"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer" v-if="showButton">
        <el-button @click="handleCancel" :disabled="loading">取消</el-button>
        <el-button
          type="primary"
          @click="handleConfirm"
          :loading="loading"
          :disabled="confirmDisabled"
        >
          {{ confirmText }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed, getCurrentInstance } from 'vue'
const { proxy } = getCurrentInstance()

// 定义props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '表单'
  },
  width: {
    type: String,
    default: '500px'
  },
  labelWidth: {
    type: String,
    default: '80px'
  },
  formConfig: {
    type: Array,
    default: () => []
  },
  formData: {
    type: Object,
    default: () => ({})
  },
  rules: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  },
  confirmText: {
    type: String,
    default: '确定'
  },
  confirmDisabled: {
    type: Boolean,
    default: false
  },
  showButton: {
    type: Boolean,
    default: true
  }
})

// 定义emits
const emit = defineEmits(['update:visible', 'confirm', 'cancel'])

// 响应式数据
const formRef = ref()
const localFormData = ref({})

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 监听props变化
watch(
  () => props.formData,
  (newVal) => {
    localFormData.value = { ...newVal }
  },
  { immediate: true, deep: true }
)

// 方法
const handleClose = () => {
  if (props.loading) return // 加载中不允许关闭

  visible.value = false
  emit('cancel')
  resetForm()
}

const handleCancel = () => {
  handleClose()
}

const handleConfirm = async () => {
  if (!formRef.value) return

  const valid = await formRef.value.validate()
  if (valid) {
    emit('confirm', localFormData.value)
  }
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 暴露方法给父组件
defineExpose({
  resetForm
})
</script>

<style lang="scss" scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
