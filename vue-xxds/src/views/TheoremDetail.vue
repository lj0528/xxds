<template>
  <div class="theorem-detail-container" v-if="theorem">
    <div class="back-nav">
      <button @click="goBack" class="back-btn">← 返回定理库</button>
      <span class="chapter-badge">{{ getChapterName(theorem.chapterId) }}</span>
    </div>

    <div class="theorem-card">
      <h1 class="theorem-title">{{ theorem.name }}</h1>

      <TheoremVideo :theorem-id="theorem.id" />
      
      <div class="content-section">
        <div class="section-header">
          <span class="section-icon">📜</span>
          <h2>定理内容</h2>
        </div>
        <div class="math-content" v-html="renderMath(theorem.content)"></div>
      </div>

      <div v-if="theorem.proof" class="content-section">
        <div class="section-header">
          <span class="section-icon">✍️</span>
          <h2>证明过程</h2>
        </div>
        <div class="math-content" v-html="renderMath(theorem.proof)"></div>
      </div>

      <div v-if="theorem.example" class="content-section">
        <div class="section-header">
          <span class="section-icon">📝</span>
          <h2>典型例题</h2>
        </div>
        <div class="math-content" v-html="renderMath(theorem.example)"></div>
      </div>

      <div class="meta-footer">
        <div class="difficulty-tag" :class="'diff-' + theorem.difficulty">
          {{ difficultyMap[theorem.difficulty] }}
        </div>
        <div class="chapter-info">
          📖 {{ getChapterName(theorem.chapterId) }}
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
  <div v-else class="loading-state">
    <div class="spinner"></div>
    <p>加载中...</p>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { renderMath } from '@/utils/mathRender'
import TheoremVideo from '@/components/TheoremVideo.vue'
import AiAssistant from '@/components/AiAssistant.vue'

interface Theorem {
  id: string
  chapterId: number
  name: string
  content: string
  proof?: string
  example?: string
  difficulty: number
}

const route = useRoute()
const router = useRouter()
const theorem = ref<Theorem | null>(null)
const showAi = ref(false) // 控制 AI 抽屉显示

const chapters = [
  { id: 1, name: '行列式' },
  { id: 2, name: '矩阵' },
  { id: 3, name: '向量' },
  { id: 4, name: '线性方程组' },
  { id: 5, name: '特征值与特征向量' },
  { id: 6, name: '二次型' }
]

const difficultyMap: Record<number, string> = { 1: '基础', 2: '进阶', 3: '综合' }

const getChapterName = (id: number) => chapters.find(c => c.id === id)?.name || '未知'

const goBack = () => {
  router.push('/theorems')
}

onMounted(async () => {
  const id = route.params.id as string
  try {
    const res = await axios.get(`/api/theorems/${id}`, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    theorem.value = res.data
  } catch (err) {
    console.error('加载定理详情失败', err)
  }
})
</script>

<style scoped>
/* 整体容器 */
.theorem-detail-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fc 0%, #eef2f8 100%);
  padding: 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 顶部导航 */
.back-nav {
  width: 100%;
  max-width: 1000px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.back-btn {
  background: rgba(255,255,255,0.9);
  border: none;
  padding: 0.5rem 1.2rem;
  border-radius: 40px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
  color: #1e293b;
  backdrop-filter: blur(4px);
}

.back-btn:hover {
  background: white;
  transform: translateX(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.chapter-badge {
  background: #e2e8f0;
  padding: 0.4rem 1rem;
  border-radius: 40px;
  font-size: 0.85rem;
  font-weight: 500;
  color: #2c3e50;
}

/* 定理卡片 */
.theorem-card {
  max-width: 1000px;
  width: 100%;
  background: white;
  border-radius: 32px;
  box-shadow: 0 20px 35px -12px rgba(0, 0, 0, 0.15);
  padding: 2rem 2.5rem;
  transition: transform 0.2s;
}

.theorem-title {
  font-size: 2.2rem;
  font-weight: 800;
  text-align: center;
  background: linear-gradient(120deg, #1e3c72, #2a5298);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  margin-top: 0;
  margin-bottom: 2rem;
  padding-bottom: 0.5rem;
  border-bottom: 3px solid #eef2ff;
}

/* 内容区块 */
.content-section {
  margin-bottom: 2rem;
  background: #fefefe;
  border-radius: 24px;
  padding: 0.2rem 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin-bottom: 1rem;
}

.section-icon {
  font-size: 1.6rem;
}

.section-header h2 {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0;
  color: #0f172a;
  position: relative;
}

.math-content {
  font-size: 1.05rem;
  line-height: 1.7;
  color: #1e293b;
  background: #fafcff;
  padding: 1.2rem 1.5rem;
  border-radius: 20px;
  border-left: 4px solid #3b82f6;
  overflow-x: auto;
}

.math-content :deep(.katex) {
  font-size: 1.05em;
}

.math-content :deep(.math) {
  overflow-x: auto;
}

/* 底部元信息 */
.meta-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 2.5rem;
  padding-top: 1.2rem;
  border-top: 2px dashed #e2e8f0;
  flex-wrap: wrap;
  gap: 1rem;
}

.difficulty-tag {
  padding: 0.3rem 1.2rem;
  border-radius: 40px;
  font-size: 0.85rem;
  font-weight: 600;
  background: #e2e8f0;
  color: #1e293b;
}

.diff-1 {
  background: #dcfce7;
  color: #15803d;
}

.diff-2 {
  background: #fef9c3;
  color: #854d0e;
}

.diff-3 {
  background: #fee2e2;
  color: #b91c1c;
}

.chapter-info {
  font-size: 0.9rem;
  color: #475569;
  background: #f1f5f9;
  padding: 0.3rem 1rem;
  border-radius: 40px;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: 1rem;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e2e8f0;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-state p {
  color: #475569;
  font-size: 1rem;
}

/* ========== AI 助手样式 ========== */
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

/* 响应式适配 */
@media (max-width: 700px) {
  .theorem-detail-container {
    padding: 1rem;
  }
  .theorem-card {
    padding: 1.2rem;
  }
  .theorem-title {
    font-size: 1.6rem;
  }
  .section-header h2 {
    font-size: 1.3rem;
  }
  .math-content {
    padding: 0.8rem 1rem;
    font-size: 0.95rem;
  }
  .back-nav {
    flex-direction: column;
    align-items: flex-start;
  }
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