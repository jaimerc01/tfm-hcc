import axios from 'axios'

const API_BASE_URL = process.env.VUE_APP_API_URL || 'http://localhost:8081'

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export default {
  async list() {
    const { data } = await api.get('/historia/archivos')
    return data
  },
  async upload(file) {
    const form = new FormData()
    form.append('file', file)
    const { data } = await api.post('/historia/archivos', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    return data
  },
  async download(id) {
    const res = await api.get(`/historia/archivos/${id}` , { responseType: 'blob' })
    // Try to infer filename from header (filename* or filename)
    const cd = res.headers['content-disposition'] || ''
    let filename = null
    let m = /filename\*=UTF-8''([^;]+)/i.exec(cd)
    if (m && m[1]) {
      try { filename = decodeURIComponent(m[1]) } catch (_) { filename = m[1] }
    }
    if (!filename) {
      m = /filename="?([^";]+)"?/i.exec(cd)
      if (m && m[1]) filename = m[1]
    }
    if (!filename) filename = `archivo_${id}`
    return { blob: res.data, filename }
  },
  async remove(id) {
    await api.delete(`/historia/archivos/${id}`)
  }
}
