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
  // Edición individual de un dato clínico ya guardado (parámetro, valor, unidad, fecha)
  editarDatoClinico(id, medicion) { return api.put(`/historia/datos-clinicos/${id}`, medicion, { headers: { 'Content-Type': 'application/json; charset=utf-8' } }) },
  // Propuestas de cambio que un médico ha enviado sobre el historial y que el paciente debe confirmar
  listarPropuestasCambio() { return api.get('/historia/propuestas-cambio') },
  responderPropuestaCambio(id, aceptar) { return api.post(`/historia/propuestas-cambio/${id}`, { aceptar }, { headers: { 'Content-Type': 'application/json; charset=utf-8' } }) },
  // Analisis de sangre: Replace all (PUT) or add new (POST). El cuerpo es un array de mediciones (application/json).
  updateAnalisisSangre(datos) { return api.put('/historia/analisis-sangre', datos) },
  añadirAnalisisSangre(datos) { return api.post('/historia/analisis-sangre', datos) },
  // Signos vitales: Replace all (PUT) or add new (POST)
  updateSignosVitales(datos) { return api.put('/historia/signos-vitales', datos) },
  añadirSignosVitales(datos) { return api.post('/historia/signos-vitales', datos) },
  // Analisis de orina: Replace all (PUT) or add new (POST)
  updateAnalisisOrina(datos) { return api.put('/historia/analisis-orina', datos) },
  añadirAnalisisOrina(datos) { return api.post('/historia/analisis-orina', datos) },
  // getMine already returns the whole DTO, which may include analisisSangre, signosVitales and analisisOrina

  // Rangos de referencia
  getRangos() { return api.get('/rangos') }
}
