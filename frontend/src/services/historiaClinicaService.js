import axios from 'axios'
const API_BASE_URL = process.env.VUE_APP_API_URL || 'http://localhost:8081'
const api = axios.create({ baseURL: API_BASE_URL, timeout: 10000 })
api.interceptors.request.use((cfg) => { const token = localStorage.getItem('authToken'); if (token) cfg.headers.Authorization = `Bearer ${token}`; return cfg })

export default {
  getMine() { return api.get('/historia') },
  // antecedentes clínicos: entradas individuales con categoria (PERSONAL/FAMILIAR) y descripcion
  crearAntecedente(antecedente) { return api.post('/historia/antecedentes', antecedente, { headers: { 'Content-Type': 'application/json; charset=utf-8' } }) },
  editarAntecedente(id, antecedente) { return api.put(`/historia/antecedentes/${id}`, antecedente, { headers: { 'Content-Type': 'application/json; charset=utf-8' } }) },
  deleteAntecedente(id) { return api.delete(`/historia/antecedentes/${id}`) },
  // alergias: entradas individuales con descripcion
  crearAlergia(alergia) { return api.post('/historia/alergias', alergia, { headers: { 'Content-Type': 'application/json; charset=utf-8' } }) },
  deleteAlergia(id) { return api.delete(`/historia/alergias/${id}`) },
  deleteDatoClinico(id) { return api.delete(`/historia/datos-clinicos/${id}`) },
  // Analisis de sangre: Replace all (PUT) or add new (POST)
  updateAnalisisSangre(text) { return api.put('/historia/analisis-sangre', text, { headers: { 'Content-Type': 'text/plain; charset=utf-8' } }) },
  añadirAnalisisSangre(text) { return api.post('/historia/analisis-sangre', text, { headers: { 'Content-Type': 'text/plain; charset=utf-8' } }) },
  // Signos vitales: Replace all (PUT) or add new (POST)
  updateSignosVitales(text) { return api.put('/historia/signos-vitales', text, { headers: { 'Content-Type': 'text/plain; charset=utf-8' } }) },
  añadirSignosVitales(text) { return api.post('/historia/signos-vitales', text, { headers: { 'Content-Type': 'text/plain; charset=utf-8' } }) },
  // Analisis de orina: Replace all (PUT) or add new (POST)
  updateAnalisisOrina(text) { return api.put('/historia/analisis-orina', text, { headers: { 'Content-Type': 'text/plain; charset=utf-8' } }) },
  añadirAnalisisOrina(text) { return api.post('/historia/analisis-orina', text, { headers: { 'Content-Type': 'text/plain; charset=utf-8' } }) },
  // getMine already returns the whole DTO, which may include analisisSangre, signosVitales and analisisOrina

  // Rangos de referencia
  getRangos() { return api.get('/rangos') }
}
