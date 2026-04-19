<template>
  <div class="ai-assistant">
    <div class="messages" ref="messageContainer">
      <div v-for="(msg, idx) in messages" :key="idx" :class="['message', msg.role]">
        <div class="role">{{ msg.role === 'user' ? '我' : 'AI 助手' }}</div>
        <div class="content" v-html="renderContent(msg.content)"></div>
      </div>
    </div>
    <div class="input-area">
      <textarea
        v-model="userInput"
        @keydown.ctrl.enter="sendMessage"
        placeholder="输入问题，按 Ctrl+Enter 发送"
        rows="3"
      ></textarea>
      <button @click="sendMessage" :disabled="loading">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { renderMath } from '@/utils/mathRender'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

const messages = ref([])
const userInput = ref('')
const loading = ref(false)
const messageContainer = ref(null)

// 格式化消息：支持 LaTeX、代码高亮、换行
const renderContent = (text) => {
  // 先处理代码块
  let html = text.replace(/```(\w*)\n([\s\S]*?)```/g, (match, lang, code) => {
    const highlighted = hljs.highlight(code.trim(), { language: lang || 'text' }).value
    return `<pre><code class="hljs language-${lang}">${highlighted}</code></pre>`
  })
  // 行内代码
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>')
  // LaTeX 公式
  html = renderMath(html)
  // 换行
  html = html.replace(/\n/g, '<br>')
  return html
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight
    }
  })
}

const sendMessage = async () => {
  if (!userInput.value.trim() || loading.value) return
  const question = userInput.value
  messages.value.push({ role: 'user', content: question })
  messages.value.push({ role: 'assistant', content: '' })
  userInput.value = ''
  scrollToBottom()
  loading.value = true

  try {
    const response = await fetch('/api/ai/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message: question })
    })
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const chunk = line.substring(5).trim()
          if (chunk) {
            const lastMsg = messages.value[messages.value.length - 1]
            lastMsg.content += chunk
            scrollToBottom()
          }
        }
      }
    }
  } catch (err) {
    console.error('AI 请求失败', err)
    messages.value[messages.value.length - 1].content = '抱歉，AI 服务暂时不可用。'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.ai-assistant {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: white;
  display: flex;
  flex-direction: column;
  height: 500px;
  max-height: 70vh;
}
.messages {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.message {
  display: flex;
  flex-direction: column;
  max-width: 80%;
}
.message.user {
  align-self: flex-end;
  background: #eef2ff;
  border-radius: 18px 18px 4px 18px;
  padding: 0.5rem 1rem;
}
.message.assistant {
  align-self: flex-start;
  background: #f1f5f9;
  border-radius: 18px 18px 18px 4px;
  padding: 0.5rem 1rem;
}
.role {
  font-size: 0.7rem;
  color: #64748b;
  margin-bottom: 0.2rem;
}
.content {
  font-size: 0.95rem;
  line-height: 1.5;
}
.input-area {
  padding: 1rem;
  border-top: 1px solid #e2e8f0;
  display: flex;
  gap: 0.5rem;
}
textarea {
  flex: 1;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  padding: 0.5rem;
  font-family: inherit;
  resize: vertical;
}
button {
  padding: 0 1.2rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
}
button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>