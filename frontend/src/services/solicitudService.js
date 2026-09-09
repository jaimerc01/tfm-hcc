import axios from 'axios'

const API_BASE_URL = process.env.VUE_APP_API_URL || 'http://localhost:8081'

const api = axios.create({ baseURL: API_BASE_URL, timeout: 10000 })

api.interceptors.request.use((cfg) => {
  const token = localStorage.getItem('authToken')
  if (token) cfg.headers.Authorization = `Bearer ${token}`
  return cfg
})

// Solicitudes de asignación médico-paciente, desde el punto de vista del usuario:
// las recibidas (que el paciente acepta/rechaza) y, si además es médico, las enviadas.
export default {
  listarRecibidas() { return api.get('/usuario/solicitudes') },
  listarEnviadas() { return api.get('/medico/solicitudes-asignacion/enviadas') },
  actualizarEstado(id, estado) { return api.put(`/usuario/solicitudes/${id}`, { estado }) }
}
