<template>
  <div class="theorem-list-container">
    <div class="hero">
      <h1>📖 线性代数定理库</h1>
      <p>系统梳理行列式、矩阵、向量、方程组、特征值、二次型核心定理</p>
    </div>

    <div class="search-section">
      <div class="search-bar">
        <input 
          type="text" 
          v-model="keyword" 
          placeholder="🔍 输入定理名称或内容关键词"
          @keyup.enter="search"
        />
        <button class="btn-primary" @click="search">搜索</button>
        <button class="btn-secondary" @click="clearSearch">清空</button>
      </div>
    </div>

    <div class="chapter-tabs">
      <button 
        v-for="ch in chapters" 
        :key="ch.id"
        :class="{ active: currentChapter === ch.id }"
        @click="loadByChapter(ch.id)"
      >
        {{ ch.name }}
      </button>
    </div>

    <div class="theorem-list">
      <div v-for="thm in theorems" :key="thm.id" class="theorem-card">
        <div class="card-header">
          <h3>
            <router-link :to="'/theorem/' + thm.id">{{ thm.name }}</router-link>
          </h3>
          <span class="difficulty-badge" :class="'diff-' + thm.difficulty">
            {{ difficultyMap[thm.difficulty] }}
          </span>
        </div>
        <div class="content" v-html="renderMath(thm.content.substring(0, 150) + '...')"></div>
        <div class="meta">
          📚 章节：{{ getChapterName(thm.chapterId) }}
        </div>
      </div>
      <div v-if="theorems.length === 0" class="empty">
        <p>✨ 暂无定理数据，试试其他关键词或章节～</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { renderMath } from '@/utils/mathRender'

interface Theorem {
  id: string
  chapterId: number
  name: string
  content: string
  proof?: string
  example?: string
  difficulty: number
}

const keyword = ref('')
const theorems = ref<Theorem[]>([])
const currentChapter = ref<number | null>(null)

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

const search = async () => {
  if (!keyword.value.trim()) {
    await loadAll()
    return
  }
  try {
    const res = await axios.get('/api/theorems/search', { params: { keyword: keyword.value } })
    theorems.value = res.data
    currentChapter.value = null
  } catch (err) {
    console.error('搜索失败', err)
  }
}

const loadByChapter = async (chapterId: number) => {
  currentChapter.value = chapterId
  try {
    const res = await axios.get(`/api/theorems/chapter/${chapterId}`)
    theorems.value = res.data
    keyword.value = ''
  } catch (err) {
    console.error('加载章节失败', err)
  }
}

const loadAll = async () => {
  try {
    const res = await axios.get('/api/theorems/search')
    theorems.value = res.data
    currentChapter.value = null
  } catch (err) {
    console.error('加载全部定理失败', err)
  }
}

const clearSearch = () => {
  keyword.value = ''
  loadAll()
}

onMounted(() => {
  loadAll()
})
</script>

<style scoped>
/* 全局样式 */
.theorem-list-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem 2rem;
  background: #f8fafc;
  min-height: 100vh;
}

/* Hero 区域 */
.hero {
  text-align: center;
  margin-bottom: 2rem;
}

.hero h1 {
  font-size: 2.5rem;
  font-weight: 800;
  background: linear-gradient(135deg, #1e293b, #2d3e5f);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  margin-bottom: 0.5rem;
}

.hero p {
  color: #475569;
  font-size: 1.1rem;
}

/* 搜索栏区域 */
.search-section {
  background: white;
  border-radius: 60px;
  padding: 0.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.search-bar {
  display: flex;
  gap: 0.8rem;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
}

.search-bar input {
  flex: 1;
  min-width: 250px;
  padding: 0.8rem 1.2rem;
  border: 1px solid #e2e8f0;
  border-radius: 40px;
  font-size: 1rem;
  transition: all 0.2s;
  background: #f9f9ff;
}

.search-bar input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
}

.btn-primary, .btn-secondary {
  padding: 0.8rem 1.8rem;
  border-radius: 40px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  font-size: 0.9rem;
}

.btn-primary {
  background: linear-gradient(100deg, #3b82f6, #2563eb);
  color: white;
  box-shadow: 0 2px 6px rgba(37, 99, 235, 0.3);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.3);
}

.btn-secondary {
  background: #f1f5f9;
  color: #1e293b;
}

.btn-secondary:hover {
  background: #e2e8f0;
}

/* 章节标签 */
.chapter-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 0.8rem;
  margin-bottom: 2rem;
  justify-content: center;
}

.chapter-tabs button {
  padding: 0.6rem 1.4rem;
  border-radius: 40px;
  border: none;
  background: #f1f5f9;
  color: #334155;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.9rem;
}

.chapter-tabs button:hover {
  background: #e2e8f0;
  transform: translateY(-1px);
}

.chapter-tabs button.active {
  background: linear-gradient(100deg, #3b82f6, #2563eb);
  color: white;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

/* 定理卡片列表 */
.theorem-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 1.8rem;
}

.theorem-card {
  background: white;
  border-radius: 24px;
  padding: 1.5rem;
  transition: all 0.25s ease;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(226, 232, 240, 0.6);
}

.theorem-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 30px -12px rgba(0, 0, 0, 0.15);
  border-color: #cbd5e1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.card-header h3 {
  margin: 0;
  font-size: 1.3rem;
  font-weight: 600;
}

.card-header h3 a {
  text-decoration: none;
  color: #0f172a;
  transition: color 0.2s;
}

.card-header h3 a:hover {
  color: #3b82f6;
}

.difficulty-badge {
  font-size: 0.7rem;
  padding: 0.2rem 0.8rem;
  border-radius: 40px;
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

.content {
  color: #334155;
  line-height: 1.5;
  margin: 1rem 0;
  font-size: 0.95rem;
}

.meta {
  font-size: 0.8rem;
  color: #64748b;
  border-top: 1px solid #eef2ff;
  padding-top: 0.8rem;
  margin-top: 0.5rem;
}

.empty {
  grid-column: 1 / -1;
  text-align: center;
  padding: 3rem;
  background: white;
  border-radius: 32px;
  color: #64748b;
  font-size: 1.1rem;
}

/* 响应式 */
@media (max-width: 800px) {
  .theorem-list-container {
    padding: 1rem;
  }
  .hero h1 {
    font-size: 1.8rem;
  }
  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .search-bar input {
    width: auto;
  }
  .btn-primary, .btn-secondary {
    text-align: center;
  }
  .theorem-list {
    grid-template-columns: 1fr;
  }
}
</style>