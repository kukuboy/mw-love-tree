<template>
  <VueDatePicker
    v-model="pickerValue"
    model-type="yyyy-MM-dd"
    :formats="{ input: 'yyyy-MM-dd' }"
    :placeholder="placeholder"
    :auto-apply="true"
    :teleport="true"
    :week-start="1"
    :max-date="maxDate"
    :min-date="minDate"
    :input-attrs="{
      id: inputId,
      name,
      required,
      autocomplete: 'off',
      inputmode: 'numeric',
    }"
    class="date-picker"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { VueDatePicker } from '@vuepic/vue-datepicker'

const props = withDefaults(defineProps<{
  modelValue?: string
  placeholder?: string
  minDate?: string
  maxDate?: string
  required?: boolean
  inputId?: string
  name?: string
}>(), {
  modelValue: '',
  placeholder: '请选择日期',
  minDate: undefined,
  maxDate: undefined,
  required: false,
  inputId: undefined,
  name: undefined,
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const pickerValue = computed({
  get: () => props.modelValue || null,
  set: (value: string | Date | null) => {
    if (typeof value === 'string') {
      emit('update:modelValue', value)
      return
    }

    if (value instanceof Date && !Number.isNaN(value.getTime())) {
      const year = value.getFullYear()
      const month = String(value.getMonth() + 1).padStart(2, '0')
      const day = String(value.getDate()).padStart(2, '0')
      emit('update:modelValue', `${year}-${month}-${day}`)
      return
    }

    emit('update:modelValue', '')
  },
})
</script>

<style scoped>
.date-picker {
  width: 100%;
}

.date-picker :deep(.dp__theme_light) {
  --dp-border-color: rgba(232, 160, 191, 0.2);
  --dp-border-color-hover: rgba(197, 137, 142, 0.45);
  --dp-primary-color: var(--color-primary);
  --dp-primary-text-color: #fff;
  --dp-hover-color: rgba(232, 160, 191, 0.12);
  --dp-icon-color: rgba(58, 46, 51, 0.45);
  --dp-text-color: var(--color-text);
  --dp-background-color: #fff;
  --dp-menu-border-color: rgba(232, 160, 191, 0.16);
}

.date-picker :deep(.dp__input) {
  width: 100%;
  min-height: 48px;
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.6);
  font-size: 16px;
  font-family: var(--font-family);
  color: var(--color-text);
  transition: border-color 0.2s, box-shadow 0.2s;
}

.date-picker :deep(.dp__input:focus),
.date-picker :deep(.dp__input_focus) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(232, 160, 191, 0.12);
}

.date-picker :deep(.dp__menu) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 18px 40px rgba(197, 137, 142, 0.18);
}

.date-picker :deep(.dp__calendar_header_separator) {
  background: rgba(232, 160, 191, 0.12);
}

.date-picker :deep(.dp__cell_inner) {
  border-radius: 10px;
}

.date-picker :deep(.dp__today) {
  border-color: rgba(197, 137, 142, 0.4);
}

@media (max-width: 768px) {
  .date-picker :deep(.dp__input) {
    min-height: 50px;
    padding: 14px 16px;
  }

  .date-picker :deep(.dp__menu) {
    border-radius: 18px 18px 0 0;
  }
}
</style>
