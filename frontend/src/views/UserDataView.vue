<template>
  <div class="page-container-narrow">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-icon">
        <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"></path>
          <circle cx="12" cy="7" r="4"></circle>
        </svg>
      </div>
      <div>
        <h1>Mis Datos Personales</h1>
        <p class="subtitle">Gestiona tu información personal y configuración de cuenta</p>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Cargando información...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="alert alert-danger" role="alert">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="8" x2="12" y2="12"></line>
        <line x1="12" y1="16" x2="12.01" y2="16"></line>
      </svg>
      {{ error }}
    </div>

    <!-- No Data State -->
    <div v-else-if="!user" class="empty-state">
      <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
        <circle cx="12" cy="7" r="4"></circle>
      </svg>
      <h3>No se pudieron cargar los datos</h3>
      <p>Por favor, intenta recargar la página</p>
    </div>

    <!-- Data Content -->
    <div v-else class="user-data-content">
      <!-- View Mode -->
      <template v-if="!editMode">
        <div class="panel-card">
          <div class="panel-header">
            <h3>Información Personal</h3>
            <p class="panel-subtitle">Revisa y actualiza tus datos personales</p>
          </div>

          <div class="data-grid">
            <div class="data-item">
              <div class="data-label">Nombre</div>
              <div class="data-value">{{ user.nombre }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">Primer Apellido</div>
              <div class="data-value">{{ user.apellido1 }}</div>
            </div>
            <div class="data-item" v-if="user.apellido2">
              <div class="data-label">Segundo Apellido</div>
              <div class="data-value">{{ user.apellido2 }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">NIF</div>
              <div class="data-value">{{ user.nif }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">Email</div>
              <div class="data-value">{{ user.email }}</div>
            </div>
            <div class="data-item" v-if="user.telefono">
              <div class="data-label">Teléfono</div>
              <div class="data-value">{{ user.telefono }}</div>
            </div>
            <div class="data-item" v-if="user.fechaNacimiento">
              <div class="data-label">Fecha de Nacimiento</div>
              <div class="data-value">{{ formatDate(user.fechaNacimiento) }}</div>
            </div>
            <div class="data-item" v-if="user.fechaCreacion">
              <div class="data-label">Fecha de Alta</div>
              <div class="data-value">{{ formatDateTime(user.fechaCreacion) }}</div>
            </div>
            <div class="data-item" v-if="user.fechaUltimaModificacion">
              <div class="data-label">Última Modificación</div>
              <div class="data-value">{{ formatDateTime(user.fechaUltimaModificacion) }}</div>
            </div>
          </div>

          <div class="form-actions">
            <button 
              type="button"
              class="btn-primary" 
              @click="startEdit"
              aria-label="Editar datos personales">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
              </svg>
              Editar datos
            </button>
            <button 
              type="button"
              class="btn-secondary" 
              @click="exportData" 
              :disabled="exportLoading"
              :aria-label="exportLoading ? 'Exportando datos personales...' : 'Descargar todos mis datos personales en formato JSON'">
              <svg v-if="!exportLoading" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="7 10 12 15 17 10"></polyline>
                <line x1="12" y1="15" x2="12" y2="3"></line>
              </svg>
              <div v-else class="spinner-small"></div>
              {{ exportLoading ? 'Exportando...' : 'Descargar mis datos' }}
            </button>
          </div>
        </div>
      </template>
      <!-- Edit Mode -->
      <template v-else>
        <div class="panel-card">
          <div class="panel-header">
            <h3>Editar Información Personal</h3>
            <p class="panel-subtitle">Actualiza tus datos personales</p>
          </div>

          <form @submit.prevent="submitEdit">
            <div class="form-grid">
              <div class="form-group">
                <label class="form-label">Nombre <span class="required">*</span></label>
                <input type="text" v-model="form.nombre" class="form-input" required />
              </div>
              <div class="form-group">
                <label class="form-label">Primer Apellido <span class="required">*</span></label>
                <input type="text" v-model="form.apellido1" class="form-input" required />
              </div>
              <div class="form-group">
                <label class="form-label">Segundo Apellido</label>
                <input type="text" v-model="form.apellido2" class="form-input" />
              </div>
              <div class="form-group">
                <label class="form-label">NIF <span class="required">*</span></label>
                <input type="text" v-model="form.nif" class="form-input" required />
              </div>
              <div class="form-group">
                <label class="form-label">Email <span class="required">*</span></label>
                <input type="email" v-model="form.email" class="form-input" required />
              </div>
              <div class="form-group">
                <label class="form-label">Teléfono</label>
                <input type="text" v-model="form.telefono" class="form-input" />
              </div>
              <div class="form-group full-width">
                <label class="form-label">Fecha de Nacimiento</label>
                <input type="date" v-model="form.fechaNacimiento" class="form-input" />
              </div>
            </div>

            <div v-if="editError" class="alert alert-danger" role="alert">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
              {{ editError }}
            </div>

            <div v-if="editSuccess" class="alert alert-success" role="status" aria-live="polite">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
              Datos actualizados correctamente
            </div>

            <div v-if="nifChanged" class="alert alert-warning" role="alert">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
                <line x1="12" y1="9" x2="12" y2="13"></line>
                <line x1="12" y1="17" x2="12.01" y2="17"></line>
              </svg>
              Has cambiado el NIF. Necesitarás volver a iniciar sesión.
            </div>

            <div class="form-actions form-actions--right">
              <button 
                type="button" 
                class="btn-secondary"
                @click="cancelEdit" 
                :disabled="editLoading"
                aria-label="Cancelar edición y descartar cambios">
                Cancelar
              </button>
              <button 
                type="submit" 
                class="btn-primary"
                :disabled="editLoading"
                :aria-label="editLoading ? 'Guardando cambios en datos personales...' : 'Guardar cambios en datos personales'">
                <div v-if="editLoading" class="spinner-small"></div>
                <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
                {{ editLoading ? 'Guardando...' : 'Guardar cambios' }}
              </button>
            </div>
          </form>
        </div>
      </template>
      <!-- Password Section -->
      <div class="section">
        <div class="panel-card">
          <div class="panel-header">
            <h3>Cambiar Contraseña</h3>
            <p class="panel-subtitle">Actualiza tu contraseña de acceso</p>
          </div>

          <button 
            v-if="!showPwForm" 
            type="button"
            class="btn-secondary" 
            @click="showPwForm=true"
            aria-label="Mostrar formulario para cambiar contraseña">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect width="18" height="11" x="3" y="11" rx="2" ry="2"></rect>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
            </svg>
            Cambiar contraseña
          </button>

          <form v-else @submit.prevent="submitPw">
            <div class="info-box">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <path d="M12 16v-4"></path>
                <path d="M12 8h.01"></path>
              </svg>
              <span>Introduce tu contraseña actual y elige una nueva contraseña segura (mínimo 6 caracteres).</span>
            </div>

            <div class="form-grid">
              <div class="form-group full-width">
                <label class="form-label">Contraseña Actual <span class="required">*</span></label>
                <input type="password" v-model="pw.current" class="form-input" required />
              </div>
              <div class="form-group">
                <label class="form-label">Nueva Contraseña <span class="required">*</span></label>
                <input type="password" v-model="pw.new1" class="form-input" minlength="6" required />
              </div>
              <div class="form-group">
                <label class="form-label">Repetir Nueva Contraseña <span class="required">*</span></label>
                <input type="password" v-model="pw.new2" class="form-input" minlength="6" required />
              </div>
            </div>

            <div v-if="pwError" class="alert alert-danger" role="alert">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
              {{ pwError }}
            </div>

            <div v-if="pwSuccess" class="alert alert-success" role="status" aria-live="polite">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
              Contraseña cambiada correctamente. Redirigiendo...
            </div>

            <div class="form-actions form-actions--right">
              <button 
                type="button" 
                class="btn-secondary"
                @click="cancelPw" 
                :disabled="pwLoading"
                aria-label="Cancelar cambio de contraseña">
                Cancelar
              </button>
              <button 
                type="submit" 
                class="btn-primary"
                :disabled="pwLoading"
                :aria-label="pwLoading ? 'Guardando nueva contraseña...' : 'Guardar nueva contraseña'">
                <div v-if="pwLoading" class="spinner-small"></div>
                <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
                {{ pwLoading ? 'Guardando...' : 'Guardar nueva contraseña' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Delete Account Modal -->
      <div v-if="showDelete" class="modal-overlay" role="dialog" aria-modal="true" @click.self="closeDelete">
        <div class="modal">
          <div class="modal-header">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: var(--danger-color)">
              <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>
              <line x1="12" y1="9" x2="12" y2="13"></line>
              <line x1="12" y1="17" x2="12.01" y2="17"></line>
            </svg>
            <h3>Confirmar eliminación</h3>
          </div>
          <p class="modal-text">¿Estás seguro que quieres eliminar tu cuenta? Esta acción es irreversible y perderás todos tus datos.</p>
          
          <div v-if="deleteError" class="alert alert-danger" role="alert">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            {{ deleteError }}
          </div>

          <div class="modal-actions">
            <button 
              type="button"
              class="btn-secondary"
              :disabled="deleteLoading" 
              @click="closeDelete" 
              aria-label="Cancelar eliminación de cuenta">
              Cancelar
            </button>
            <button 
              type="button"
              class="btn-danger"
              :disabled="deleteLoading" 
              @click="confirmDelete"
              :aria-label="deleteLoading ? 'Eliminando cuenta...' : 'Confirmar eliminación de cuenta definitivamente'">
              <div v-if="deleteLoading" class="spinner-small"></div>
              <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="3 6 5 6 21 6"></polyline>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
              </svg>
              {{ deleteLoading ? 'Eliminando...' : 'Sí, eliminar mi cuenta' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import authService from '@/services/authService'
import { validateNIF } from '@/utils/validateNIF'


export default {
  name: 'UserDataView',
  setup() {
  const router = useRouter()
  const user = ref(null)
  const editMode = ref(false)
  const showDelete = ref(false)
  const deleteLoading = ref(false)
  const deleteError = ref('')
    const form = ref({ nombre:'', apellido1:'', apellido2:'', nif:'', email:'', telefono:'', fechaNacimiento:'' })
    const editLoading = ref(false)
    const editError = ref('')
    const editSuccess = ref(false)
    const nifChanged = ref(false)
    const loading = ref(true)
    const error = ref('')
  const showPwForm = ref(false)
  const pw = ref({ current:'', new1:'', new2:'' })
  const pwLoading = ref(false)
  const pwError = ref('')
  const pwSuccess = ref(false)
  const exportLoading = ref(false)
  const reauth = ref({ password: '' })

    const load = async () => {
      loading.value = true
      error.value = ''
      try {
        user.value = await authService.fetchMyData()
      } catch (e) {
        error.value = e.message || 'Error al cargar datos'
      } finally {
        loading.value = false
      }
    }

    const formatDate = (d) => {
      if (!d) return ''
      const date = new Date(d)
      return date.toLocaleDateString()
    }
    const formatDateTime = (d) => {
      if (!d) return ''
      const date = new Date(d)
      return date.toLocaleString()
    }

    const submitPw = async () => {
      pwError.value = ''
      pwSuccess.value = false
      if (pw.value.new1 !== pw.value.new2) { pwError.value = 'Las contraseñas no coinciden'; return }
      pwLoading.value = true
      try {
  await authService.changePassword(pw.value.current, pw.value.new1)
  pwSuccess.value = true
  pw.value = { current:'', new1:'', new2:'' }
  // Redirigir a login tras breve notificación
  setTimeout(() => { router.push({ name: 'Login' }) }, 800)
      } catch (e) {
        pwError.value = e.message || 'Error al cambiar contraseña'
      } finally {
        pwLoading.value = false
      }
    }
    const cancelPw = () => { showPwForm.value = false; pw.value = { current:'', new1:'', new2:'' }; pwError.value=''; pwSuccess.value=false }

    const exportData = async () => {
      exportLoading.value = true
      try {
        const token = authService.getToken()
        const headers = { 'Authorization': 'Bearer ' + token }
        if (reauth.value.password) headers['X-Current-Password'] = reauth.value.password
        const resp = await fetch((process.env.VUE_APP_API_URL || 'http://localhost:8081') + '/usuario/export', { headers })
        if (resp.status === 401 && resp.headers.get('X-Reauth-Required') === 'true') {
          throw new Error('Debes introducir tu contraseña para exportar')
        }
        if (!resp.ok) throw new Error('Error al exportar')
        const json = await resp.json()
        const blob = new Blob([JSON.stringify(json, null, 2)], { type: 'application/json' })
        const a = document.createElement('a')
        a.href = URL.createObjectURL(blob)
        a.download = 'export_usuario_' + new Date().toISOString().slice(0,10) + '.json'
        a.click()
        URL.revokeObjectURL(a.href)
      } catch (e) {
        alert(e.message || 'Error al exportar')
      } finally {
        exportLoading.value = false
      }
    }

    const openDelete = () => { deleteError.value=''; showDelete.value = true }
    const closeDelete = () => { if (!deleteLoading.value) showDelete.value = false }
    const confirmDelete = async () => {
      deleteError.value=''
      deleteLoading.value = true
      try {
        const token = authService.getToken()
        const resp = await fetch((process.env.VUE_APP_API_URL || 'http://localhost:8081') + '/usuario/me', { method:'DELETE', headers: { 'Authorization':'Bearer '+token, 'X-Current-Password': reauth.value.password || '' }})
        if (resp.status === 401 && resp.headers.get('X-Reauth-Required') === 'true') {
            throw new Error('Debes introducir tu contraseña para eliminar la cuenta')
        }
        if (!resp.ok && resp.status !== 204) throw new Error('Error al eliminar')
        authService._clearAuth?.()
        router.push({ name: 'Login' })
      } catch (e) {
        deleteError.value = e.message || 'Error al eliminar'
      } finally {
        deleteLoading.value = false
      }
    }

    const startEdit = () => {
      if (!user.value) return
      editMode.value = true
      editError.value = ''
      editSuccess.value = false
      nifChanged.value = false
      form.value = {
        nombre: user.value.nombre || '',
        apellido1: user.value.apellido1 || '',
        apellido2: user.value.apellido2 || '',
        nif: user.value.nif || '',
        email: user.value.email || '',
        telefono: user.value.telefono || '',
        fechaNacimiento: user.value.fechaNacimiento ? new Date(user.value.fechaNacimiento).toISOString().slice(0,10) : ''
      }
    }
    const cancelEdit = () => {
      editMode.value = false
      editLoading.value = false
      editError.value = ''
      editSuccess.value = false
    }
    const submitEdit = async () => {
      editError.value = ''
      editSuccess.value = false
      nifChanged.value = false
      editLoading.value = true
      try {
        // Validaciones frontend
        if (!form.value.nombre?.trim()) throw new Error('El nombre es obligatorio')
        if (!form.value.apellido1?.trim()) throw new Error('El primer apellido es obligatorio')
  if (!validateNIF(form.value.nif)) throw new Error('El NIF introducido no es válido.')
        if (!/^([^@\n]+)@([^@\n]+)\.[^@\n]+$/.test(form.value.email)) throw new Error('Formato de email inválido')
        if (form.value.telefono && !/^[0-9+\-() ]{0,20}$/.test(form.value.telefono)) throw new Error('Formato de teléfono inválido')
        const payload = { ...form.value }
        if (!payload.fechaNacimiento) payload.fechaNacimiento = null
        const updated = await authService.updateMyData(payload)
        user.value = updated
        editSuccess.value = true
        const currentTokenUser = authService.getCurrentUser()?.sub
        if (updated && updated.nif && updated.nif !== currentTokenUser) {
          nifChanged.value = true
          // Forzar logout y redirigir
          authService._clearAuth?.()
          setTimeout(() => { router.push({ name: 'Login' }) }, 900)
        } else {
          setTimeout(() => { editMode.value = false; editSuccess.value = false }, 1200)
        }
      } catch (e) {
        editError.value = e.message || 'Error al actualizar'
      } finally {
        editLoading.value = false
      }
    }

    onMounted(load)
  return { user, loading, error, formatDate, formatDateTime, showPwForm, pw, pwLoading, pwError, pwSuccess, submitPw, cancelPw, editMode, form, startEdit, cancelEdit, submitEdit, editLoading, editError, editSuccess, nifChanged, showDelete, openDelete, closeDelete, confirmDelete, deleteLoading, deleteError, exportData, exportLoading, reauth }
  }
}
</script>

<style scoped>
/* Data Grid - specific to this view */
.data-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

@media (min-width: 640px) {
  .data-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.data-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 1rem;
  background: var(--bg-light, #fafafa);
  border-radius: 8px;
  border: 1px solid var(--border, #e5e5e5);
}

.data-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-secondary, #737373);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.data-value {
  font-size: 0.9375rem;
  font-weight: 500;
  color: var(--text-primary, #262626);
  word-break: break-word;
}

/* Responsive adjustments */
@media (max-width: 640px) {
  .data-grid {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .form-actions button {
    width: 100%;
  }
}
</style>
