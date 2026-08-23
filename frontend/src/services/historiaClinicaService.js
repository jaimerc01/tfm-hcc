import axios from 'axios'
const API_BASE_URL = process.env.VUE_APP_API_URL || 'http://localhost:8081'
const api = axios.create({ baseURL: API_BASE_URL, timeout: 10000 })
api.interceptors.request.use((cfg) => { const token = localStorage.getItem('authToken'); if (token) cfg.headers.Authorization = `Bearer ${token}`; return cfg })

export default {
  getMine() { return api.get('/historia') },
  // identification is sent as JSON
  updateIdentificacion(payload) { return api.put('/historia/identificacion', payload, { headers: { 'Content-Type': 'application/json; charset=utf-8' } }) },
  // antecedents and single-antecedent edits are plain text (multiline supported)
  updateAntecedentes(text) { return api.put('/historia/antecedentes', text, { headers: { 'Content-Type': 'text/plain; charset=utf-8' } }) },
  updateAntecedente(index, texto) { return api.put(`/historia/antecedentes/${index}`, texto, { headers: { 'Content-Type': 'text/plain; charset=utf-8' } }) },
  // alergias endpoint expects JSON: { alergias: "..." }
  updateAlergias(alergiasText) {
    // Unir todas las líneas en un solo string, separadas por salto de línea si hay varias
    const value = (alergiasText || '').split('\n').map(l => l.trim()).filter(l => l.length > 0).join('\n');
    const payload = JSON.stringify({ alergias: value });
    return api.post('/historia/alergias', payload, { headers: { 'Content-Type': 'application/json; charset=utf-8' } });
  },
  deleteDatoClinico(id) { return api.delete(`/historia/datos-clinicos/${id}`) },
  deleteAntecedente(index) { return api.delete(`/historia/antecedentes/${index}`) },
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
