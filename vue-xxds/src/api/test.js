import axios from 'axios'

const request = axios.create({ baseURL: '/api' })

export const getTestPaper = (testId) => {
  return request.get(`/test/${testId}`)
}

export const submitTestAnswers = (data) => {
  return request.post('/test/submit', data)
}