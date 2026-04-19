<template>
  <div class="profile-page">
    <!-- 导航栏 -->
    <nav class="profile-nav">
      <div class="nav-container">
        <router-link to="/profile" class="nav-link" active-class="active">个人中心</router-link>
        <router-link to="/theorems" class="nav-link" active-class="active">定理库</router-link>
        <router-link to="/questions" class="nav-link" active-class="active">题库练习</router-link>
        <router-link to="/practice" class="nav-link" active-class="active">在线测试</router-link>
      </div>
    </nav>

    <div class="profile-content">
      <!-- 欢迎卡片 -->
      <div class="welcome-card">
        <div class="avatar">👤</div>
        <div class="welcome-info">
          <h2>欢迎回来，{{ username }}</h2>
          <p>继续线性代数学习之旅</p>
        </div>
      </div>

      <!-- 新增 AI 学习建议按钮 -->
      <div class="ai-advice-section">
        <button class="advice-btn" @click="getAiAdvice" :disabled="adviceLoading">
          📈 生成 AI 学习建议
        </button>
        <div v-if="aiAdvice" class="ai-advice-content">
          <h3>AI 学习建议</h3>
          <div v-html="renderMath(aiAdvice)"></div>
        </div>
      </div>

      <div class="stats-grid">
        <!-- 学习报告卡片 -->
        <div class="card report-card">
          <div class="card-header">
            <span class="card-icon">📊</span>
            <h3>学习报告</h3>
          </div>
          <div v-if="report" class="report-content">
            <div class="stat-item">
              <span class="stat-label">总测试次数</span>
              <span class="stat-value">{{ report.totalTests }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">平均分</span>
              <span class="stat-value">{{ report.avgScore }} / 100</span>
            </div>
            <div class="weak-chapters">
              <div class="stat-label">薄弱章节</div>
              <div class="chapter-tags">
                <span v-for="ch in report.weakChapters" :key="ch.chapterId" class="chapter-tag">
                  {{ getChapterName(ch.chapterId) }} ({{ ch.correctRate }}%)
                </span>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">暂无学习数据，开始测试吧～</div>
        </div>

        <!-- 错题本卡片 -->
        <div class="card wrong-card">
          <div class="card-header">
            <span class="card-icon">📝</span>
            <h3>错题本</h3>
          </div>
          <div v-if="wrongQuestions.length" class="wrong-list">
            <div v-for="(q, idx) in wrongQuestions" :key="q.id" class="wrong-item">
              <div class="wrong-header">
                <span class="wrong-index">错题 {{ idx + 1 }}</span>
                <button class="delete-btn" @click="deleteWrongQuestion(q.id)" title="删除错题">🗑️</button>
              </div>
              <div class="question-content" v-html="renderMath(q.content)"></div>
              <div class="answer wrong-ans">你的答案：{{ q.userAnswer || '未作答' }}</div>
              <div class="answer correct-ans">正确答案：{{ q.correctAnswer }}</div>
              <div class="solution" v-html="renderMath(q.solution)"></div>
            </div>
          </div>
          <div v-else class="empty-state">🎉 暂无错题，继续加油！</div>
        </div>
      </div>
    </div>

    <!-- 浮动 AI 按钮 -->
    <button class="ai-fab" @click="showAi = true">
      🤖 AI 助手
    </button>

    <!-- AI 助手抽屉 -->
    <div v-if="showAi" class="ai-drawer">
      <div class="drawer-header">
        <h3>AI 学习助手</h3>
        <button @click="showAi = false">✕</button>
      </div>
      <AiAssistant />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { renderMath } from '@/utils/mathRender'
import AiAssistant from '@/components/AiAssistant.vue'

const username = ref(localStorage.getItem('username') || '用户')
const report = ref<any>(null)
const wrongQuestions = ref<any[]>([])
const aiAdvice = ref('')       
const adviceLoading = ref(false) 
const showAi = ref(false)

const chapters = [
  { id: 1, name: '行列式' },
  { id: 2, name: '矩阵' },
  { id: 3, name: '向量' },
  { id: 4, name: '线性方程组' },
  { id: 5, name: '特征值与特征向量' },
  { id: 6, name: '二次型' }
]
const getChapterName = (id: number) => chapters.find(c => c.id === id)?.name || '未知'

// 获取学习报告（真实数据，失败时显示空状态）
const fetchReport = async () => {
  try {
    const res = await axios.get('/api/user/report', {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    report.value = res.data
  } catch (err) {
    console.error('获取学习报告失败', err)
    report.value = null
  }
}

// 获取错题本（真实数据）
const fetchWrongQuestions = async () => {
  try {
    const res = await axios.get('/api/user/wrong-questions', {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    wrongQuestions.value = res.data
  } catch (err) {
    console.error('获取错题本失败', err)
    wrongQuestions.value = []
  }
}

// 删除错题
const deleteWrongQuestion = async (questionId: string) => {
  if (confirm('确定要删除这道错题吗？删除后将无法恢复。')) {
    try {
      await axios.delete(`/api/user/wrong-questions/${questionId}`, {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
      })
      // 删除成功后刷新错题列表，同时刷新学习报告（因为删除错题可能影响正确率统计）
      await fetchWrongQuestions()
      await fetchReport()
    } catch (err) {
      console.error('删除错题失败', err)
      alert('删除失败，请稍后重试')
    }
  }
}

// 获取 AI 学习建议
const getAiAdvice = async () => {
  adviceLoading.value = true
  try {
    if (!report.value) {
      await fetchReport()
    }
    const reportData = report.value || { totalTests: 0, avgScore: 0, weakChapters: [] }
    const response = await axios.post('/api/ai/advice', { report: JSON.stringify(reportData) })
    aiAdvice.value = response.data
  } catch (err) {
    console.error(err)
    aiAdvice.value = '生成建议失败，请稍后重试。'
  } finally {
    adviceLoading.value = false
  }
}

onMounted(() => {
  fetchReport()
  fetchWrongQuestions()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f8fafc;
}

/* 导航栏 */
.profile-nav {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 10;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  gap: 0.5rem;
  padding: 0 1.5rem;
}

.nav-link {
  padding: 1rem 1.5rem;
  text-decoration: none;
  font-weight: 600;
  color: #475569;
  transition: all 0.2s;
  border-bottom: 3px solid transparent;
}

.nav-link:hover {
  color: #3b82f6;
}

.nav-link.active {
  color: #3b82f6;
  border-bottom-color: #3b82f6;
}

/* 主要内容区 */
.profile-content {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 0 1.5rem;
}

/* 欢迎卡片 */
.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 32px;
  padding: 2rem;
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  gap: 1.5rem;
  color: white;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1);
}

.avatar {
  width: 70px;
  height: 70px;
  background: rgba(255,255,255,0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.5rem;
  backdrop-filter: blur(4px);
}

.welcome-info h2 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 700;
}

.welcome-info p {
  margin: 0.25rem 0 0;
  opacity: 0.9;
  font-size: 0.95rem;
}

/* 双列网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 2rem;
}

/* 通用卡片样式 */
.card {
  background: white;
  border-radius: 28px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1.2rem 1.5rem;
  background: #fefefe;
  border-bottom: 1px solid #eef2ff;
}

.card-icon {
  font-size: 1.5rem;
}

.card-header h3 {
  margin: 0;
  font-size: 1.3rem;
  font-weight: 600;
  color: #1e293b;
}

/* 学习报告内容 */
.report-content {
  padding: 1.5rem;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  padding: 0.6rem 0;
  border-bottom: 1px dashed #e2e8f0;
}

.stat-label {
  color: #64748b;
  font-weight: 500;
}

.stat-value {
  font-weight: 700;
  font-size: 1.1rem;
  color: #3b82f6;
}

.weak-chapters {
  margin-top: 1rem;
}

.chapter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.chapter-tag {
  background: #fef3c7;
  color: #b45309;
  padding: 0.25rem 0.8rem;
  border-radius: 40px;
  font-size: 0.8rem;
  font-weight: 500;
}

/* 错题本样式 */
.wrong-list {
  max-height: 500px;
  overflow-y: auto;
  padding: 0.5rem 1rem 1rem;
}

.wrong-item {
  background: #fef2f2;
  border-radius: 20px;
  padding: 1rem;
  margin-bottom: 1rem;
  border-left: 4px solid #ef4444;
}

.wrong-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.wrong-index {
  font-size: 0.75rem;
  font-weight: 600;
  color: #b91c1c;
  background: #fee2e2;
  padding: 0.2rem 0.6rem;
  border-radius: 40px;
}

.delete-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.1rem;
  padding: 0 0.5rem;
  color: #ef4444;
  transition: transform 0.1s;
  border-radius: 50%;
}
.delete-btn:hover {
  transform: scale(1.15);
  background-color: #fee2e2;
}

.question-content {
  font-size: 0.95rem;
  color: #1e293b;
  margin-bottom: 0.8rem;
}

.answer {
  font-size: 0.85rem;
  margin: 0.3rem 0;
}

.wrong-ans {
  color: #dc2626;
}

.correct-ans {
  color: #10b981;
}

.solution {
  margin-top: 0.6rem;
  padding-top: 0.6rem;
  border-top: 1px solid #f1c5c5;
  font-size: 0.85rem;
  color: #475569;
}

.empty-state {
  padding: 3rem;
  text-align: center;
  color: #94a3b8;
  font-size: 0.95rem;
}

/* 响应式 */
@media (max-width: 800px) {
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  .nav-container {
    justify-content: space-between;
    padding: 0 0.5rem;
  }
  .nav-link {
    padding: 0.8rem 0.8rem;
    font-size: 0.85rem;
  }
  .welcome-card {
    flex-direction: column;
    text-align: center;
    padding: 1.5rem;
  }
}

/* AI 浮动按钮 */
.ai-fab {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #3b82f6;
  color: white;
  border: none;
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
  cursor: pointer;
  z-index: 100;
  font-size: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
}
.ai-fab:hover {
  transform: scale(1.05);
}

/* AI 抽屉 */
.ai-drawer {
  position: fixed;
  right: 0;
  top: 0;
  width: 420px;
  height: 100%;
  background: white;
  box-shadow: -4px 0 12px rgba(0,0,0,0.1);
  z-index: 200;
  display: flex;
  flex-direction: column;
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid #e2e8f0;
}
.drawer-header button {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
}

/* AI 学习建议区域样式（如果还没有） */
.ai-advice-section {
  margin-bottom: 2rem;
}
.advice-btn {
  background: #10b981;
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 40px;
  cursor: pointer;
  font-weight: 600;
}
.advice-btn:disabled {
  opacity: 0.6;
}
.ai-advice-content {
  background: #f0fdf4;
  border-radius: 24px;
  padding: 1rem;
  margin-top: 1rem;
}

/* 响应式适配 */
@media (max-width: 800px) {
  /* 原有响应式样式保持不变 */
  .ai-drawer {
    width: 100%;
  }
  .ai-fab {
    bottom: 1rem;
    right: 1rem;
    width: 48px;
    height: 48px;
    font-size: 1.2rem;
  }
}
</style>