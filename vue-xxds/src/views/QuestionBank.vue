<template>
  <div class="question-bank">
    <div class="hero">
      <h1>📚 题库练习</h1>
      <p>精选线性代数题目，巩固知识，提升能力</p>
    </div>

    <div class="card">
      <div class="filters">
        <div class="filter-row">
          <div class="filter-item">
            <label>⭐ 难度等级</label>
            <div class="options">
              <label class="option" :class="{ active: difficulty === '' }">
                <input type="radio" value="" v-model="difficulty" /> 全部
              </label>
              <label class="option" :class="{ active: difficulty === '1' }">
                <input type="radio" value="1" v-model="difficulty" /> 基础
              </label>
              <label class="option" :class="{ active: difficulty === '2' }">
                <input type="radio" value="2" v-model="difficulty" /> 进阶
              </label>
              <label class="option" :class="{ active: difficulty === '3' }">
                <input type="radio" value="3" v-model="difficulty" /> 综合
              </label>
            </div>
          </div>

          <div class="filter-item recommend-item">
            <label class="checkbox-label">
              <input type="checkbox" v-model="useRecommend" />
              <span>✨ 智能推荐</span>
              <small>根据你的历史表现推荐题目</small>
            </label>
          </div>
        </div>

        <div class="action">
          <button @click="startPractice" class="btn-primary">
            🚀 开始练习
          </button>
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AiAssistant from '@/components/AiAssistant.vue'

const router = useRouter()
const difficulty = ref('')
const useRecommend = ref(false)
const showAi = ref(false)

const startPractice = () => {
  router.push({
    path: '/practice',
    query: {
      difficulty: difficulty.value,
      recommend: useRecommend.value ? 'true' : 'false'
    }
  })
}
</script>

<style scoped>
/* 笔记本屏幕适配：卡片占满宽度，留白极少 */
.question-bank {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f4fa 0%, #e2e8f0 100%);
  padding: 1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.hero {
  text-align: center;
  margin-bottom: 1rem;
  width: 100%;
  max-width: 1200px;
}

.hero h1 {
  font-size: 2.2rem;
  font-weight: 700;
  background: linear-gradient(120deg, #1e3c72, #2a5298);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  margin-bottom: 0.2rem;
}

.hero p {
  font-size: 1rem;
  color: #2d3e5f;
  margin-top: 0;
}

.card {
  background: white;
  border-radius: 24px;
  box-shadow: 0 12px 24px -8px rgba(0, 0, 0, 0.15);
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.filters {
  padding: 1.5rem 2rem;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
  align-items: flex-end;
  margin-bottom: 1.5rem;
}

.filter-item {
  flex: 1;
  min-width: 240px;
}

.filter-item label {
  font-weight: 600;
  color: #1e293b;
  display: block;
  margin-bottom: 0.5rem;
  font-size: 1rem;
}

.options {
  display: flex;
  flex-wrap: wrap;
  gap: 0.8rem;
}

.option {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: #f1f5f9;
  padding: 0.5rem 1.2rem;
  border-radius: 40px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
  color: #334155;
  font-size: 0.9rem;
}

.option input {
  margin: 0;
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.option.active {
  background: #3b82f6;
  color: white;
  box-shadow: 0 4px 10px rgba(59, 130, 246, 0.25);
}

.recommend-item {
  flex: 0 1 auto;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  cursor: pointer;
  background: #f8fafc;
  padding: 0.5rem 1.2rem;
  border-radius: 40px;
  transition: all 0.2s;
  white-space: nowrap;
}

.checkbox-label input {
  width: 18px;
  height: 18px;
  accent-color: #3b82f6;
}

.checkbox-label span {
  font-weight: 600;
  font-size: 0.9rem;
  color: #0f172a;
}

.checkbox-label small {
  color: #64748b;
  font-size: 0.75rem;
}

.checkbox-label:hover {
  background: #eef2ff;
}

.action {
  display: flex;
  justify-content: center;
  margin-top: 0.5rem;
}

.btn-primary {
  background: linear-gradient(100deg, #3b82f6, #2563eb);
  border: none;
  padding: 0.7rem 2rem;
  font-size: 1rem;
  font-weight: 600;
  color: white;
  border-radius: 40px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.25);
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  min-width: 160px;
  justify-content: center;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.3);
}

.btn-primary:active {
  transform: translateY(1px);
}

/* ========== 新增：AI 助手样式 ========== */
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

/* 小屏幕适配 */
@media (max-width: 800px) {
  .question-bank {
    padding: 0.5rem;
  }
  .filters {
    padding: 1rem;
  }
  .filter-row {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  .recommend-item {
    align-self: flex-start;
  }
  .checkbox-label {
    white-space: normal;
  }
  /* AI 抽屉在小屏幕占满宽度 */
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