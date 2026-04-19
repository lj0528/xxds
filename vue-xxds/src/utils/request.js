// src/utils/request.js
/*
import axios from 'axios';

const request = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api', // 基础路径，开发时可配置代理
    timeout: 10000,
});

export default request;

*/

import axios from 'axios'

axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})