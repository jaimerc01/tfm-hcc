import axios from 'axios';
const API_BASE_URL = process.env.VUE_APP_API_URL || 'http://localhost:8081';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export default {
  async listar() {
    const { data } = await api.get('/admin/medicos');
    return data;
  },
  async crear(medico) {
    const { data } = await api.post('/admin/medicos', medico);
    return data;
  },
  async actualizar(id, medico) {
    const { data } = await api.put(`/admin/medicos/${id}`, medico);
    return data;
  },
  async eliminar(id) {
    await api.delete(`/admin/medicos/${id}`);
  }
};
