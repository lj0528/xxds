<template>
  <div class="test-container">
    <div class="test-header">
      <div class="test-info">
        <h2>{{ testTitle }}</h2>
        <p>📝 共 {{ questions.length }} 题 | 每题5分钟</p>
      </div>
      <div class="test-timer" :class="{ warning: remainingTime <= 60 }">
        ⏱️ 剩余时间: {{ formatTime(remainingTime) }}
      </div>
    </div>

    <div class="progress-bar">
      <div class="progress-fill" :style="{ width: ((currentQuestion + 1) / questions.length) * 100 + '%' }"></div>
    </div>

    <div class="question-area">
      <div class="question-card">
        <div class="question-header">
          <span class="question-num">第 {{ currentQuestion + 1 }} / {{ questions.length }} 题</span>
          <span class="question-type">{{ questions[currentQuestion]?.type === 'PROOF' ? '证明题' : '计算题' }}</span>
        </div>
        <div class="question-content" v-html="renderMath(questions[currentQuestion]?.content || '')"></div>
        <div class="answer-area">
          <textarea 
            v-model="answers[currentQuestion]" 
            :placeholder="questions[currentQuestion]?.type === 'PROOF' ? '✍️ 请输入证明过程...' : '🔢 请输入答案...'" 
            rows="8"
          ></textarea>
          <div v-if="questions[currentQuestion]?.hasMatrix" class="matrix-input-tool">
            <button @click="showMatrixEditorForIndex(currentQuestion)" class="matrix-btn">➕ 插入矩阵</button>
          </div>
        </div>
      </div>
    </div>

    <div class="navigation">
      <button @click="prevQuestion" :disabled="currentQuestion === 0" class="nav-btn prev">← 上一题</button>
      <div class="question-jump">
        <span>第</span>
        <input type="number" v-model.number="jumpIndex" min="1" :max="questions.length" @keyup.enter="jumpToQuestion" />
        <span>题</span>
        <button @click="jumpToQuestion" class="jump-btn">跳转</button>
      </div>
      <button @click="nextQuestion" :disabled="currentQuestion === questions.length - 1" class="nav-btn next">下一题 →</button>
    </div>

    <div class="submit-area">
      <button @click="submitTest" :disabled="isSubmitting" class="submit-btn">
        {{ isSubmitting ? '提交中...' : '📮 提交试卷' }}
      </button>
    </div>

    <MatrixEditor v-if="matrixEditorVisible" @close="matrixEditorVisible = false" @insert="insertMatrix" />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router'
import axios from 'axios'
import { renderMath } from '@/utils/mathRender'
import MatrixEditor from '@/components/MatrixEditor.vue'

const router = useRouter()
const route = useRoute()

const testId = ref('')
const testTitle = ref('线性代数练习')
const questions = ref([])
const answers = ref([])
const currentQuestion = ref(0)
const remainingTime = ref(3600)
let timer = null

const matrixEditorVisible = ref(false)
const isSubmitting = ref(false)
const submitted = ref(false)
const jumpIndex = ref(1)

const loadTestPaper = async () => {
  const difficulty = route.query.difficulty
  const recommend = route.query.recommend === 'true'
  let res
  try {
    if (recommend) {
      res = await axios.get('/api/test/recommend', { params: { count: 10 } })
    } else {
      const params = { count: 10 }
      if (difficulty && difficulty !== '') {
        params.difficulty = parseInt(difficulty)
      }
      res = await axios.get('/api/test/random', { params })
    }

    // 强制限制为最多10道题（如果后端返回超过10道，只取前10道）
    let questionsData = res.data.questions
    if (questionsData.length > 10) {
      questionsData = questionsData.slice(0, 10)
      console.warn('后端返回题目超过10道，已截取前10题')
    }
    //questions.value = res.data.questions
    questions.value = questionsData
    answers.value = new Array(questions.value.length).fill('')
    testId.value = res.data.testId
    remainingTime.value = questions.value.length * 300
    jumpIndex.value = 1
  } catch (error) {
    console.error('加载试题失败', error)
    alert('加载失败，请刷新重试')
  }
}

const startTimer = () => {
  timer = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--
    } else {
      clearInterval(timer)
      if (!isSubmitting.value) submitTest()
    }
  }, 1000)
}

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const prevQuestion = () => {
  if (currentQuestion.value > 0) currentQuestion.value--
  jumpIndex.value = currentQuestion.value + 1
}
const nextQuestion = () => {
  if (currentQuestion.value < questions.value.length - 1) currentQuestion.value++
  jumpIndex.value = currentQuestion.value + 1
}
const jumpToQuestion = () => {
  let idx = jumpIndex.value - 1
  if (idx < 0) idx = 0
  if (idx >= questions.value.length) idx = questions.value.length - 1
  currentQuestion.value = idx
  jumpIndex.value = idx + 1
}

const showMatrixEditorForIndex = () => {
  matrixEditorVisible.value = true
}
const insertMatrix = (matrixLatex) => {
  const current = answers.value[currentQuestion.value] || ''
  answers.value[currentQuestion.value] = current + matrixLatex
  matrixEditorVisible.value = false
}

const submitTest = async () => {
  if (isSubmitting.value) return
  const hasUnanswered = answers.value.some(ans => !ans || ans.trim() === '')
  if (hasUnanswered) {
    const ok = confirm('您有未作答的题目，确认提交吗？未答题目将计0分。')
    if (!ok) return
  }
  isSubmitting.value = true
  try {
    await axios.post('/api/test/submit', {
      testId: testId.value,
      answers: answers.value
    })
    alert('提交成功！')
    if (timer) clearInterval(timer)
    submitted.value = true
    await router.push('/profile')
  } catch (error) {
    console.error('提交失败', error)
    alert('提交失败，请稍后重试')
    isSubmitting.value = false
  }
}

onBeforeRouteLeave((to, from, next) => {
  if (submitted.value) {
    next()
    return
  }
  const hasUnanswered = answers.value.some(ans => !ans || ans.trim() === '')
  if (hasUnanswered && questions.value.length > 0) {
    const leave = confirm('您有未作答的题目，离开将丢失进度，确认离开吗？')
    if (leave) next()
    else next(false)
  } else {
    next()
  }
})

const handleBeforeUnload = (e) => {
  const hasUnanswered = answers.value.some(ans => !ans || ans.trim() === '')
  if (hasUnanswered && questions.value.length > 0) {
    e.preventDefault()
    e.returnValue = '您有未完成的题目，确定要离开吗？'
  }
}

onMounted(() => {
  loadTestPaper()
  startTimer()
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
  window.removeEventListener('beforeunload', handleBeforeUnload)
})
</script>

<style scoped>
.test-container {
  max-width: 1100px;
  margin: 2rem auto;
  padding: 0 1.5rem 2rem;
  background: #f8fafc;
  min-height: 100vh;
}

.test-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
  background: white;
  padding: 1rem 2rem;
  border-radius: 24px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  margin-bottom: 1.5rem;
}

.test-info h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  background: linear-gradient(120deg, #1e3c72, #2a5298);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
}

.test-info p {
  margin: 0.2rem 0 0;
  color: #475569;
  font-size: 0.85rem;
}

.test-timer {
  font-size: 1.4rem;
  font-weight: 600;
  color: #2c3e50;
  background: #f1f5f9;
  padding: 0.3rem 1.2rem;
  border-radius: 40px;
  letter-spacing: 1px;
}

.test-timer.warning {
  color: #e53e3e;
  background: #fff5f5;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.7; }
  100% { opacity: 1; }
}

.progress-bar {
  height: 6px;
  background: #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 2rem;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #2563eb);
  border-radius: 10px;
  transition: width 0.3s ease;
}

.question-area {
  margin-bottom: 2rem;
}

.question-card {
  background: white;
  border-radius: 32px;
  box-shadow: 0 20px 30px -12px rgba(0,0,0,0.1);
  padding: 2rem;
  transition: all 0.2s;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 1.2rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #eef2ff;
}

.question-num {
  font-size: 1.1rem;
  font-weight: 600;
  color: #3b82f6;
}

.question-type {
  background: #eef2ff;
  padding: 0.2rem 0.8rem;
  border-radius: 40px;
  font-size: 0.75rem;
  font-weight: 500;
  color: #1e40af;
}

.question-content {
  font-size: 1.05rem;
  line-height: 1.6;
  color: #1e293b;
  background: #fafcff;
  padding: 1.2rem;
  border-radius: 20px;
  margin-bottom: 1.5rem;
  border-left: 4px solid #3b82f6;
}

.answer-area textarea {
  width: 100%;
  padding: 1rem;
  border: 1px solid #cbd5e1;
  border-radius: 20px;
  font-family: 'Fira Code', monospace;
  font-size: 0.95rem;
  line-height: 1.5;
  resize: vertical;
  transition: all 0.2s;
}

.answer-area textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59,130,246,0.2);
}

.matrix-input-tool {
  margin-top: 1rem;
  text-align: right;
}

.matrix-btn {
  background: #f1f5f9;
  border: none;
  padding: 0.4rem 1rem;
  border-radius: 40px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
}

.matrix-btn:hover {
  background: #e2e8f0;
  transform: translateY(-1px);
}

.navigation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.nav-btn {
  background: white;
  border: 1px solid #cbd5e1;
  padding: 0.6rem 1.2rem;
  border-radius: 40px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.nav-btn:hover:not(:disabled) {
  background: #f1f5f9;
  transform: translateY(-1px);
  border-color: #94a3b8;
}

.nav-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.question-jump {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: white;
  padding: 0.2rem 1rem;
  border-radius: 60px;
  border: 1px solid #e2e8f0;
}

.question-jump input {
  width: 50px;
  text-align: center;
  padding: 0.3rem;
  border: 1px solid #cbd5e1;
  border-radius: 30px;
  font-size: 0.9rem;
}

.jump-btn {
  background: #3b82f6;
  border: none;
  color: white;
  padding: 0.2rem 0.8rem;
  border-radius: 30px;
  cursor: pointer;
  font-size: 0.8rem;
}

.submit-area {
  display: flex;
  justify-content: center;
}

.submit-btn {
  background: linear-gradient(100deg, #10b981, #059669);
  border: none;
  padding: 0.8rem 2rem;
  font-size: 1rem;
  font-weight: 600;
  color: white;
  border-radius: 60px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(16,185,129,0.3);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(16,185,129,0.4);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 700px) {
  .test-container {
    padding: 0 1rem 1rem;
  }
  .test-header {
    flex-direction: column;
    align-items: flex-start;
  }
  .question-card {
    padding: 1.2rem;
  }
  .navigation {
    flex-direction: column;
    align-items: stretch;
  }
  .nav-btn, .question-jump {
    justify-content: center;
  }
}
</style>