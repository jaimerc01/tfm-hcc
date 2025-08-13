<template>
  <div class="user-data-page">
    <h1>Mis Datos</h1>
    <div v-if="loading" class="loading">Cargando...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="!user">No se pudieron cargar los datos.</div>
    <div v-else class="data-card">
      <template v-if="!editMode">
        <dl>
          <div class="row"><dt>Nombre</dt><dd>{{ user.nombre }}</dd></div>
          <div class="row"><dt>Primer Apellido</dt><dd>{{ user.apellido1 }}</dd></div>
          <div class="row" v-if="user.apellido2"><dt>Segundo Apellido</dt><dd>{{ user.apellido2 }}</dd></div>
          <div class="row"><dt>NIF</dt><dd>{{ user.nif }}</dd></div>
          <div class="row"><dt>Email</dt><dd>{{ user.email }}</dd></div>
          <div class="row" v-if="user.telefono"><dt>Teléfono</dt><dd>{{ user.telefono }}</dd></div>
          <div class="row" v-if="user.fechaNacimiento"><dt>Fecha Nacimiento</dt><dd>{{ formatDate(user.fechaNacimiento) }}</dd></div>
          <div class="row" v-if="user.fechaCreacion"><dt>Alta</dt><dd>{{ formatDateTime(user.fechaCreacion) }}</dd></div>
          <div class="row" v-if="user.fechaUltimaModificacion"><dt>Última Modificación</dt><dd>{{ formatDateTime(user.fechaUltimaModificacion) }}</dd></div>
        </dl>
        <div class="top-actions">
          <button class="edit-btn" @click="startEdit">Editar datos</button>
        </div>
      </template>
      <template v-else>
        <form class="edit-form" @submit.prevent="submitEdit">
          <div class="edit-grid">
            <label>Nombre<input type="text" v-model="form.nombre" required /></label>
            <label>Primer Apellido<input type="text" v-model="form.apellido1" required /></label>
            <label>Segundo Apellido<input type="text" v-model="form.apellido2" /></label>
            <label>NIF<input type="text" v-model="form.nif" required /></label>
            <label>Email<input type="email" v-model="form.email" required /></label>
            <label>Teléfono<input type="text" v-model="form.telefono" /></label>
            <label>Fecha Nacimiento<input type="date" v-model="form.fechaNacimiento" /></label>
          </div>
          <div class="edit-actions">
            <button type="submit" :disabled="editLoading">{{ editLoading ? 'Guardando...' : 'Guardar cambios' }}</button>
            <button type="button" @click="cancelEdit" :disabled="editLoading">Cancelar</button>
          </div>
          <p v-if="editError" class="edit-error">{{ editError }}</p>
          <p v-if="editSuccess" class="edit-success">Datos actualizados</p>
          <p v-if="nifChanged" class="nif-warning">Has cambiado el NIF. Necesitarás volver a iniciar sesión.</p>
        </form>
      </template>
      <div class="pw-section">
        <button v-if="!showPwForm" class="pw-btn" @click="showPwForm=true">Cambiar contraseña</button>
        <form v-else class="pw-form" @submit.prevent="submitPw">
          <div class="pw-grid">
            <label>
              Actual
              <input type="password" v-model="pw.current" required />
            </label>
            <label>
              Nueva
              <input type="password" v-model="pw.new1" minlength="6" required />
            </label>
            <label>
              Repetir Nueva
              <input type="password" v-model="pw.new2" minlength="6" required />
            </label>
          </div>
          <div class="pw-actions">
            <button type="submit" :disabled="pwLoading">{{ pwLoading ? 'Guardando...' : 'Guardar' }}</button>
            <button type="button" @click="cancelPw" :disabled="pwLoading">Cancelar</button>
          </div>
          <p v-if="pwError" class="pw-error">{{ pwError }}</p>
          <p v-if="pwSuccess" class="pw-success">Contraseña actualizada</p>
        </form>
      </div>
      <div class="danger-zone">
        <h3>Zona peligrosa</h3>
        <button class="delete-btn" @click="openDelete">Borrar mi cuenta</button>
      </div>
    </div>
  </div>
  <div v-if="showDelete" class="modal-backdrop">
    <div class="modal">
      <h3>Confirmar eliminación</h3>
      <p>Esta acción es permanente. Se eliminarán tus datos y no podrás recuperarlos. ¿Deseas continuar?</p>
      <div class="modal-actions">
        <button :disabled="deleteLoading" @click="confirmDelete">{{ deleteLoading ? 'Eliminando...' : 'Sí, borrar' }}</button>
        <button :disabled="deleteLoading" @click="closeDelete" class="secondary">Cancelar</button>
      </div>
      <p v-if="deleteError" class="delete-error">{{ deleteError }}</p>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import authService from '@/services/authService'

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

    const openDelete = () => { deleteError.value=''; showDelete.value = true }
    const closeDelete = () => { if (!deleteLoading.value) showDelete.value = false }
    const confirmDelete = async () => {
      deleteError.value=''
      deleteLoading.value = true
      try {
        await authService.deleteAccount()
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
        if (!/^[0-9A-Za-z]{6,15}$/.test(form.value.nif)) throw new Error('Formato de NIF inválido')
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
  return { user, loading, error, formatDate, formatDateTime, showPwForm, pw, pwLoading, pwError, pwSuccess, submitPw, cancelPw, editMode, form, startEdit, cancelEdit, submitEdit, editLoading, editError, editSuccess, nifChanged, showDelete, openDelete, closeDelete, confirmDelete, deleteLoading, deleteError }
  }
}
</script>

<style scoped>
.user-data-page { padding:1.5rem; }
.data-card { background:#fff; border:1px solid #e2e2e2; border-radius:8px; padding:1rem 1.25rem; max-width:640px; }
 .top-actions { margin-top:1rem; }
 .edit-btn { background:#2563eb; color:#fff; border:none; padding:.55rem .9rem; border-radius:4px; cursor:pointer; }
 .edit-form { margin-top:.75rem; background:#f8fafc; border:1px solid #dbe2e8; padding:1rem; border-radius:6px; }
 .edit-grid { display:grid; gap:.75rem; grid-template-columns:repeat(auto-fit,minmax(180px,1fr)); }
 .edit-grid label { display:flex; flex-direction:column; font-size:.85rem; font-weight:600; color:#444; }
 .edit-grid input { margin-top:.25rem; padding:.5rem .6rem; border:1px solid #cbd5e1; border-radius:4px; }
 .edit-actions { margin-top:1rem; display:flex; gap:.6rem; }
 .edit-actions button { background:#42b983; color:#fff; border:none; padding:.55rem .95rem; border-radius:4px; cursor:pointer; }
 .edit-actions button[type="button"] { background:#999; }
 .edit-error { color:#b91c1c; margin-top:.5rem; }
 .edit-success { color:#166534; margin-top:.5rem; }
 .nif-warning { color:#92400e; margin-top:.5rem; font-size:.85rem; }
dl { margin:0; }
.row { display:flex; padding:.5rem 0; border-bottom:1px solid #f0f0f0; }
.row:last-child { border-bottom:none; }
dt { width:200px; font-weight:600; color:#444; }
dd { margin:0; flex:1; color:#222; }
.loading { color:#555; }
.error { color:#c0392b; background:#fdecea; padding:.75rem; border-radius:4px; max-width:640px; }
.pw-section { margin-top:1.5rem; }
.pw-btn { background:#2563eb; color:#fff; border:none; padding:.6rem 1rem; border-radius:4px; cursor:pointer; }
.pw-btn:hover { background:#1d4ed8; }
.pw-form { margin-top:.75rem; background:#fafafa; border:1px solid #e0e0e0; padding:1rem; border-radius:6px; }
.pw-grid { display:grid; gap:.75rem; grid-template-columns:repeat(auto-fit,minmax(160px,1fr)); }
.pw-grid label { display:flex; flex-direction:column; font-size:.9rem; font-weight:600; color:#444; }
.pw-grid input { margin-top:.25rem; padding:.5rem .6rem; border:1px solid #ccc; border-radius:4px; }
.pw-actions { margin-top:1rem; display:flex; gap:.6rem; }
.pw-actions button { background:#42b983; color:#fff; border:none; padding:.55rem .95rem; border-radius:4px; cursor:pointer; }
.pw-actions button[type="button"] { background:#999; }
.pw-actions button:hover:not(:disabled) { opacity:.9; }
.pw-error { color:#b91c1c; margin-top:.5rem; }
.pw-success { color:#166534; margin-top:.5rem; }
 .danger-zone { margin-top:2rem; border-top:1px solid #f3f3f3; padding-top:1rem; }
 .danger-zone h3 { font-size:1rem; margin:0 0 .6rem; color:#b91c1c; }
 .delete-btn { background:#dc2626; color:#fff; border:none; padding:.55rem .9rem; border-radius:4px; cursor:pointer; }
 .delete-btn:hover { background:#b91c1c; }
 .modal-backdrop { position:fixed; inset:0; background:rgba(0,0,0,.5); display:flex; align-items:center; justify-content:center; z-index:1000; }
 .modal { background:#fff; padding:1.25rem 1.5rem; border-radius:8px; width:100%; max-width:420px; box-shadow:0 4px 18px rgba(0,0,0,.15); }
 .modal h3 { margin:0 0 .75rem; }
 .modal-actions { display:flex; gap:.6rem; margin-top:1rem; }
 .modal-actions button { background:#dc2626; color:#fff; border:none; padding:.6rem 1rem; border-radius:4px; cursor:pointer; }
 .modal-actions button.secondary { background:#888; }
 .delete-error { color:#b91c1c; margin-top:.75rem; }
</style>
