import axios from 'axios'
const API_BASE_URL = process.env.VUE_APP_API_URL || 'http://localhost:8081'
const api = axios.create({ baseURL: API_BASE_URL, timeout: 10000 })
api.interceptors.request.use((cfg) => { const token = localStorage.getItem('authToken'); if (token) cfg.headers.Authorization = `Bearer ${token}`; return cfg })

// Servicio para las operaciones del médico sobre sus pacientes: búsqueda, solicitudes
// de asignación y consulta del historial clínico de un paciente asignado.
export default {
  buscarPaciente(dni, fechaNacimiento) { return api.get('/medico/pacientes/buscar', { params: { dni, fechaNacimiento } }) },
  crearSolicitudAsignacion(nifPaciente) { return api.post('/medico/solicitudes-asignacion', null, { params: { nifPaciente } }) },
  listarSolicitudesPendientes() { return api.get('/medico/solicitudes-asignacion/pendientes') },
  listarSolicitudesEnviadas() { return api.get('/medico/solicitudes-asignacion/enviadas') },
  listarMisPacientes() { return api.get('/medico/pacientes') },
  obtenerHistorialPaciente(nif) { return api.get(`/medico/pacientes/${encodeURIComponent(nif)}/historial`) },
  crearAnotacion(nif, mensaje) { return api.post(`/medico/pacientes/${encodeURIComponent(nif)}/anotaciones`, { mensaje }) },
  listarAnotacionesPaciente(nif) { return api.get(`/medico/pacientes/${encodeURIComponent(nif)}/anotaciones`) },
  desasignarPaciente(nif) { return api.delete(`/relaciones/mis-pacientes/${encodeURIComponent(nif)}`) },
  // Documentos clínicos del paciente asignado (CU-16)
  listarArchivosPaciente(nif) { return api.get(`/medico/pacientes/${encodeURIComponent(nif)}/archivos`) },
  subirArchivoPaciente(nif, file) {
    const form = new FormData()
    form.append('file', file)
    return api.post(`/medico/pacientes/${encodeURIComponent(nif)}/archivos`, form, { headers: { 'Content-Type': 'multipart/form-data' } })
  },
  descargarArchivoPaciente(nif, id) {
    return api.get(`/medico/pacientes/${encodeURIComponent(nif)}/archivos/${encodeURIComponent(id)}`, { responseType: 'blob' })
  },
  // Propuestas de cambio sobre el historial de un paciente asignado (requieren confirmación del paciente)
  proponerCambioClinico(nif, propuesta) {
    return api.post(`/medico/pacientes/${encodeURIComponent(nif)}/propuestas-cambio`, propuesta, { headers: { 'Content-Type': 'application/json; charset=utf-8' } })
  },
  listarPropuestasCambio(nif) {
    return api.get(`/medico/pacientes/${encodeURIComponent(nif)}/propuestas-cambio`)
  }
}
