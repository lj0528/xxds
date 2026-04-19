<template>
  <div class="login-page">
    <div class="login-card">
      <div class="card-header">
        <h2>欢迎回来</h2>
        <p>登录以继续线性代数学习</p>
      </div>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>用户名</label>
          <div class="input-wrapper">
            <span class="input-icon">👤</span>
            <input 
              type="text" 
              v-model="form.username" 
              placeholder="请输入用户名"
              required 
            />
          </div>
        </div>

        <div class="form-group">
          <label>密码</label>
          <div class="input-wrapper">
            <span class="input-icon">🔒</span>
            <input 
              type="password" 
              v-model="form.password" 
              placeholder="请输入密码"
              required 
            />
          </div>
        </div>

        <button type="submit" :disabled="loading" class="login-btn">
          {{ loading ? '登录中...' : '登录' }}
        </button>

        <div v-if="errorMsg" class="message error">{{ errorMsg }}</div>

        <div class="register-link">
          还没有账号？ <router-link to="/register">立即注册</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const loading = ref(false)
const errorMsg = ref('')

const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.username || !form.password) {
    errorMsg.value = '用户名和密码不能为空'
    return
  }

  loading.value = true
  errorMsg.value = ''

  try {
    const response = await axios.post('/api/auth/login', {
      username: form.username,
      password: form.password
    })
    
    if (response.data.code === 200) {
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('username', response.data.username)
      localStorage.setItem('role', response.data.role || 'student')
      router.push('/profile')  
    } else {
      errorMsg.value = response.data.message || '登录失败'
    }
  } catch (err: any) {
    errorMsg.value = err.response?.data?.message || '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.login-card {
  background: white;
  border-radius: 32px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  width: 100%;
  max-width: 440px;
  padding: 2rem 2rem 2.5rem;
  transition: transform 0.2s;
}

.card-header {
  text-align: center;
  margin-bottom: 2rem;
}

.card-header h2 {
  font-size: 2rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 0.25rem;
}

.card-header p {
  color: #64748b;
  font-size: 0.9rem;
  margin: 0;
}

.form-group {
  margin-bottom: 1.25rem;
}

.form-group label {
  display: block;
  font-weight: 600;
  color: #334155;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.input-wrapper {
  display: flex;
  align-items: center;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 60px;
  transition: all 0.2s;
}

.input-wrapper:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
  background: white;
}

.input-icon {
  padding: 0 0 0 1rem;
  font-size: 1.1rem;
  color: #94a3b8;
}

.input-wrapper input {
  flex: 1;
  padding: 0.8rem 1rem;
  border: none;
  background: transparent;
  font-size: 0.95rem;
  outline: none;
}

.input-wrapper input::placeholder {
  color: #cbd5e1;
}

.login-btn {
  width: 100%;
  background: linear-gradient(100deg, #667eea, #764ba2);
  border: none;
  padding: 0.85rem;
  border-radius: 60px;
  font-size: 1rem;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
  margin-top: 0.5rem;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.message {
  text-align: center;
  padding: 0.75rem;
  border-radius: 60px;
  margin-top: 1rem;
  font-size: 0.85rem;
}

.message.error {
  background: #fee2e2;
  color: #b91c1c;
}

.register-link {
  text-align: center;
  margin-top: 1.5rem;
  font-size: 0.9rem;
  color: #475569;
}

.register-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>