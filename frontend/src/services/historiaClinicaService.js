import axios from 'axios'
const API_BASE_URL = process.env.VUE_APP_API_URL || 'http://localhost:8081'
const api = axios.create({ baseURL: API_BASE_URL, timeout: 10000 })
api.interceptors.request.use((cfg) => { const token = localStorage.getItem('authToken'); if (token) cfg.headers.Authorization = `Bearer ${token}`; return cfg })

export default {
  getMine() { return api.get('/historia/me') },
  updateIdentificacion(json) { return api.post('/historia/me/identificacion', json) },
  updateAntecedentes(text) { return api.post('/historia/me/antecedentes', text) },
  updateAlergias(json) { return api.post('/historia/me/alergias', json) }
}
