import axios from 'axios'
const API_BASE_URL = process.env.VUE_APP_API_URL || 'http://localhost:8081'
const api = axios.create({ baseURL: API_BASE_URL, timeout: 10000 })
api.interceptors.request.use((cfg) => { const token = localStorage.getItem('authToken'); if (token) cfg.headers.Authorization = `Bearer ${token}`; return cfg })

export default {
  getMine() { return api.get('/historia/me') },
  // identification is sent as JSON
  updateIdentificacion(payload) { return api.post('/historia/me/identificacion', payload, { headers: { 'Content-Type': 'application/json; charset=utf-8' } }) },
  // antecedents and single-antecedent edits are plain text (multiline supported)
  updateAntecedentes(text) { return api.post('/historia/me/antecedentes', text, { headers: { 'Content-Type': 'text/plain; charset=utf-8' } }) },
  updateAntecedente(index, texto) { return api.put(`/historia/me/antecedentes/${index}`, texto, { headers: { 'Content-Type': 'text/plain; charset=utf-8' } }) },
  // alergias endpoint expects plain multiline text; parameter named text for clarity
  updateAlergias(text) { return api.post('/historia/me/alergias', text, { headers: { 'Content-Type': 'text/plain; charset=utf-8' } }) },
  deleteDatoClinico(id) { return api.delete(`/historia/me/datos/${id}`) },
  deleteAntecedente(index) { return api.delete(`/historia/me/antecedentes/${index}`) }
  ,
  // Analisis de sangre: simple text field for now
  updateAnalisisSangre(text) { return api.post('/historia/me/analisis-sangre', text, { headers: { 'Content-Type': 'text/plain; charset=utf-8' } }) },
  // getMine already returns the whole DTO, which may include analisisSangre
}
