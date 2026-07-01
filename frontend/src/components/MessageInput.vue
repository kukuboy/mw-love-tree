<template>
  <div class="message-input-wrapper">
    <textarea
      ref="textareaRef"
      v-model="text"
      class="input-field"
      :disabled="disabled"
      placeholder="说点什么吧..."
      rows="1"
      @input="autoResize"
      @keydown="handleKeydown"
    />
    <button
      class="send-btn"
      :disabled="disabled || !text.trim()"
      @click="handleSend"
      title="发送 (Enter)"
    >
      💌
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineProps<{
  disabled?: boolean
}>()

const emit = defineEmits<{
  (e: 'send', content: string): void
}>()

const text = ref('')
const textareaRef = ref<HTMLTextAreaElement | null>(null)

function autoResize() {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = el.scrollHeight + 'px'
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

function handleSend() {
  const content = text.value.trim()
  if (!content) return
  emit('send', content)
  text.value = ''
  // Reset textarea height to default (one row)
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
  }
}
</script>

<style scoped>
.message-input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  background: var(--color-bg-glass-strong);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-top: 1px solid var(--color-border);
  flex-shrink: 0;
}

.input-field {
  flex: 1;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 10px 14px;
  font-size: 15px;
  font-family: var(--font-body);
  color: var(--color-text);
  background: var(--color-bg-glass);
  resize: none;
  outline: none;
  line-height: 1.5;
  max-height: 120px;
  transition: border-color 0.2s;
  overflow-y: auto;
}

.input-field:focus {
  border-color: var(--color-primary);
}

.input-field::placeholder {
  color: var(--color-text);
  opacity: 0.35;
}

.input-field:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn {
  flex-shrink: 0;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: var(--color-bg);
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  box-shadow: 0 2px 12px rgba(201, 168, 108, 0.3);
}

.send-btn:hover:not(:disabled) {
  opacity: 0.85;
  transform: scale(1.05);
}

.send-btn:active:not(:disabled) {
  transform: scale(0.95);
}

.send-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}
</style>
