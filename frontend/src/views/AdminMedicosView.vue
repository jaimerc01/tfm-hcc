<template>
  <div class="admin-medicos section-card">
    <div class="admin-medicos__header">
      <h2 class="admin-medicos__title">
        <AppIcon name="users" size="lg" />
        {{$t('doctor_management')}}
      </h2>
      <button type="button" class="btn-primary" @click="startAdd">
        <AppIcon name="user-plus" size="sm" />
        {{$t('add_doctor')}}
      </button>
    </div>

    <div
      v-if="resultMessage"
      :class="['alert', resultError ? 'alert-danger' : 'alert-success']"
      :role="resultError ? 'alert' : 'status'"
      :aria-live="resultError ? 'assertive' : 'polite'"
    >
      <AppIcon :name="resultError ? 'alert-circle' : 'check-circle'" size="lg" class="alert-icon" />
      <span>{{ resultMessage }}</span>
    </div>

    <div v-if="medicos.length" class="results-table-container">
      <table class="results-table medicos-table">
        <thead>
          <tr>
            <th>{{$t('name')}}</th>
            <th>{{$t('email')}}</th>
            <th>{{$t('nif')}}</th>
            <th>{{$t('specialty')}}</th>
            <th>{{$t('phone')}}</th>
            <th>{{$t('birth_date')}}</th>
            <th class="actions-col">{{$t('actions')}}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="m in medicos" :key="m.id">
            <td>
              <div class="doctor-cell">
                <span class="avatar-circle" aria-hidden="true">{{ initials(m) }}</span>
                <strong>{{ nombreCompleto(m) }}</strong>
              </div>
            </td>
            <td>{{ m.email }}</td>
            <td>{{ m.nif }}</td>
            <td>
              <span v-if="m.especialidad" class="badge badge-info">{{ m.especialidad }}</span>
              <span v-else class="text-muted">—</span>
            </td>
            <td>{{ m.telefono || '—' }}</td>
            <td>{{ formatFecha(m.fechaNacimiento) }}</td>
            <td class="actions-col">
              <div class="actions-cell">
                <button
                  type="button"
                  class="btn-icon"
                  @click="edit(m)"
                  :title="$t('edit_doctor')"
                  :aria-label="$t('edit_doctor_aria', { name: nombreCompleto(m) })">
                  <AppIcon name="edit" size="sm" />
                </button>
                <button
                  type="button"
                  class="btn-icon btn-danger"
                  @click="askDelete(m)"
                  :title="$t('delete_doctor')"
                  :aria-label="$t('delete_doctor_aria', { name: nombreCompleto(m) })">
                  <AppIcon name="trash" size="sm" />
                </button>
                <button
                  type="button"
                  class="btn-icon"
                  @click="askRemoveProfile(m)"
                  :title="$t('remove_profile')"
                  :aria-label="$t('remove_doctor_profile_aria', { name: nombreCompleto(m) })">
                  <AppIcon name="user-minus" size="sm" />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else class="empty-state-small">
      <AppIcon name="users" size="3xl" />
      <p>{{$t('no_doctors')}}</p>
      <span>{{$t('no_doctors_hint')}}</span>
    </div>

    <!-- Formulario de añadir/editar médico -->
    <AppModal
      v-if="showForm"
      :label="editMedico ? $t('edit_doctor') : $t('add_doctor_title')"
      @close="cancelar"
    >
      <template #header>
        <div class="modal-icon-header">
          <AppIcon name="user-plus" size="2xl" class="modal-icon modal-icon--primary" />
          <h3>{{ editMedico ? $t('edit_doctor') : $t('add_doctor_title') }}</h3>
        </div>
      </template>

      <form id="medico-form" novalidate @submit.prevent="guardar">
        <div class="form-grid">
          <div class="form-group">
            <label class="form-label" for="medico-nombre">{{$t('name')}} <span class="required" aria-hidden="true">*</span></label>
            <input id="medico-nombre" v-model.trim="form.nombre" class="form-input" required autocomplete="given-name" />
          </div>
          <div class="form-group">
            <label class="form-label" for="medico-apellido1">{{$t('first_surname')}} <span class="required" aria-hidden="true">*</span></label>
            <input id="medico-apellido1" v-model.trim="form.apellido1" class="form-input" required autocomplete="family-name" />
          </div>
          <div class="form-group">
            <label class="form-label" for="medico-apellido2">{{$t('second_surname')}}</label>
            <input id="medico-apellido2" v-model.trim="form.apellido2" class="form-input" autocomplete="additional-name" />
          </div>
          <div class="form-group">
            <label class="form-label" for="medico-email">{{$t('email')}} <span class="required" aria-hidden="true">*</span></label>
            <input id="medico-email" v-model.trim="form.email" class="form-input" required type="email" autocomplete="email" />
          </div>
          <div class="form-group">
            <label class="form-label" for="medico-nif">{{$t('nif')}} <span class="required" aria-hidden="true">*</span></label>
            <input id="medico-nif" v-model.trim="form.nif" class="form-input" required autocomplete="off" />
          </div>
          <div class="form-group">
            <label class="form-label" for="medico-especialidad">{{$t('specialty')}}</label>
            <input id="medico-especialidad" v-model.trim="form.especialidad" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label" for="medico-telefono">{{$t('phone')}}</label>
            <input id="medico-telefono" v-model.trim="form.telefono" class="form-input" autocomplete="tel" />
          </div>
          <div class="form-group">
            <label class="form-label" for="medico-fecha-nacimiento">{{$t('birth_date')}} <span class="required" aria-hidden="true">*</span></label>
            <input id="medico-fecha-nacimiento" v-model="form.fechaNacimiento" class="form-input" type="date" required autocomplete="bday" />
          </div>
          <div v-if="!editMedico" class="form-group full-width">
            <label class="form-label" for="medico-password">{{$t('password')}} <span class="required" aria-hidden="true">*</span></label>
            <input id="medico-password" v-model="form.password" class="form-input" required type="password" autocomplete="new-password" />
          </div>
        </div>
        <p v-if="error" class="modal-error" role="alert" aria-live="assertive">{{ error }}</p>
      </form>

      <template #footer>
        <div class="modal-footer-actions">
          <button type="submit" form="medico-form" class="btn-primary">{{$t('save')}}</button>
          <button type="button" class="btn-secondary" @click="cancelar">{{$t('cancel')}}</button>
        </div>
      </template>
    </AppModal>

    <!-- Paso previo de añadir médico: buscar si el usuario ya existe por NIF -->
    <AppModal v-if="showNifStep" :label="$t('search_user_nif')" @close="cancelar">
      <template #header>
        <div class="modal-icon-header">
          <AppIcon name="user" size="2xl" class="modal-icon modal-icon--primary" />
          <h3>{{$t('search_user_nif')}}</h3>
        </div>
      </template>

      <form id="nif-search-form" novalidate @submit.prevent="checkNif">
        <div class="form-group">
          <label class="form-label" for="nif-search-input">{{$t('nif')}} <span class="required" aria-hidden="true">*</span></label>
          <input id="nif-search-input" v-model.trim="form.nif" class="form-input" required autocomplete="off" />
        </div>
        <p v-if="error" class="modal-error" role="alert" aria-live="assertive">{{ error }}</p>
      </form>

      <template #footer>
        <div class="modal-footer-actions">
          <button type="submit" form="nif-search-form" class="btn-primary">{{$t('search')}}</button>
          <button type="button" class="btn-secondary" @click="cancelar">{{$t('cancel')}}</button>
        </div>
      </template>
    </AppModal>

    <!-- Confirmación de asignar el perfil de médico a un usuario ya existente -->
    <AppModal v-if="foundUser" :label="$t('assign_doctor_profile_title')" @close="cancelAssignExisting">
      <template #header>
        <div class="modal-icon-header">
          <AppIcon name="user-plus" size="2xl" class="modal-icon modal-icon--primary" />
          <h3>{{$t('assign_doctor_profile_title')}}</h3>
        </div>
      </template>
      <p class="modal-confirm-text">{{ $t('confirm_assign_doctor_profile', { nombre: nombreCompleto(foundUser) }) }}</p>
      <p v-if="error" class="modal-error" role="alert" aria-live="assertive">{{ error }}</p>
      <template #footer>
        <div class="modal-footer-actions">
          <button type="button" class="btn-primary" @click="confirmAssignExisting" :aria-label="$t('confirm')">{{$t('confirm')}}</button>
          <button type="button" class="btn-secondary" @click="cancelAssignExisting">{{$t('cancel')}}</button>
        </div>
      </template>
    </AppModal>

    <!-- Confirmación de eliminar médico -->
    <AppModal v-if="deleteTarget" :label="$t('delete_doctor')" @close="cancelDelete">
      <template #header>
        <div class="modal-icon-header">
          <AppIcon name="trash" size="2xl" class="modal-icon modal-icon--danger" />
          <h3>{{$t('delete_doctor')}}</h3>
        </div>
      </template>
      <p class="modal-confirm-text">{{ $t('confirm_delete_doctor') }}</p>
      <template #footer>
        <div class="modal-footer-actions">
          <button type="button" class="btn-primary" @click="confirmEliminar" :aria-label="$t('confirm')">{{$t('confirm')}}</button>
          <button type="button" class="btn-secondary" @click="cancelDelete">{{$t('cancel')}}</button>
        </div>
      </template>
    </AppModal>

    <!-- Confirmación de quitar el perfil de médico -->
    <AppModal v-if="removeProfileTarget" :label="$t('remove_profile')" @close="cancelRemoveProfile">
      <template #header>
        <div class="modal-icon-header">
          <AppIcon name="user-minus" size="2xl" class="modal-icon modal-icon--danger" />
          <h3>{{$t('remove_profile')}}</h3>
        </div>
      </template>
      <p class="modal-confirm-text">{{ $t('confirm_remove_doctor_profile') }}</p>
      <template #footer>
        <div class="modal-footer-actions">
          <button type="button" class="btn-primary" @click="confirmQuitarPerfil" :aria-label="$t('confirm')">{{$t('confirm')}}</button>
          <button type="button" class="btn-secondary" @click="cancelRemoveProfile">{{$t('cancel')}}</button>
        </div>
      </template>
    </AppModal>
  </div>
</template>

<script>

import medicoService from '@/services/medicoService';
import { validateNIF } from '@/utils/validateNIF';
import AppIcon from '@/components/AppIcon.vue';
import AppModal from '@/components/Modal.vue';


export default {
  name: 'AdminMedicosView',
  components: { AppIcon, AppModal },
  data() {
    return {
      medicos: [],
    showForm: false,
    showNifStep: false,
      editMedico: null,
      form: {
        nombre: '', apellido1: '', apellido2: '', email: '', nif: '', especialidad: '', telefono: '', fechaNacimiento: '', password: '', estadoCuenta: 'ACTIVO'
      },
      error: '',
      deleteTarget: null,
      removeProfileTarget: null,
      foundUser: null,
      resultMessage: '',
      resultError: false
    };
  },
  async created() {
    await this.cargar();
  },
  methods: {
    nombreCompleto(m) {
      return [m.nombre, m.apellido1, m.apellido2].filter(Boolean).join(' ');
    },
    initials(m) {
      const iniciales = [m.nombre, m.apellido1].filter(Boolean).map(s => s[0]);
      return iniciales.join('').toUpperCase();
    },
    formatFecha(fecha) {
      if (!fecha) return '';
      // Soporta Date, string ISO, o timestamp
      const d = typeof fecha === 'string' ? new Date(fecha) : fecha;
      if (isNaN(d)) return '';
      // Componentes en hora local: toISOString() convierte a UTC y puede
      // mostrar el día anterior si la hora local no es UTC+0.
      const dia = String(d.getDate()).padStart(2, '0');
      const mes = String(d.getMonth() + 1).padStart(2, '0');
      return `${dia}/${mes}/${d.getFullYear()}`;
    },
  async cargar() {
      try {
        this.medicos = await medicoService.listar();
      } catch (e) {
        this.error = this.$t('error_loading_doctors');
      }
    },
  edit(m) {
      this.editMedico = m;
      // Normalizar fechaNacimiento a yyyy-MM-dd para el input date
      let fecha = '';
      if (m.fechaNacimiento) {
        const d = typeof m.fechaNacimiento === 'string' ? new Date(m.fechaNacimiento) : m.fechaNacimiento;
        if (!isNaN(d)) fecha = d.toISOString().slice(0, 10);
      }
      this.form = { ...m, fechaNacimiento: fecha, password: '' };
      this.showForm = true;
      this.error = '';
    },
    askDelete(medico) {
      this.deleteTarget = medico;
      this.resultMessage = '';
    },
    cancelDelete() {
      this.deleteTarget = null;
    },
    async confirmEliminar() {
      if (!this.deleteTarget) return;
      const id = this.deleteTarget.id;
      try {
        await medicoService.eliminar(id);
        this.deleteTarget = null;
        await this.cargar();
        this.mostrarResultado(this.$t('doctor_deleted_success'), false);
      } catch (e) {
        this.deleteTarget = null;
        this.mostrarResultado(this.$t('error_deleting_doctor'), true);
      }
    },
    startAdd() {
      this.form = { nombre: '', apellido1: '', apellido2: '', email: '', nif: '', especialidad: '', telefono: '', fechaNacimiento: '', password: '', estadoCuenta: 'ACTIVO' }
      this.showNifStep = true;
      this.showForm = false;
      this.editMedico = null;
      this.error = '';
    },
    async checkNif() {
      if (!validateNIF(this.form.nif)) {
        this.error = this.$t('invalid_nif');
        return;
      }
      try {
        const res = await medicoService.checkByNif(this.form.nif)
        // Usuario existe: pedir confirmación antes de asignarle el perfil MEDICO
        if (res && res.data) {
          this.foundUser = res.data
          this.showNifStep = false
          this.error = ''
          return
        }
      } catch (e) {
        // 404 -> no existe usuario: continuar con formulario completo
        if (e.response && e.response.status === 404) {
          this.showNifStep = false
          this.showForm = true
          this.error = ''
          return
        }
        this.error = this.$t('error_checking_nif')
      }
    },
    cancelAssignExisting() {
      this.foundUser = null;
    },
    async confirmAssignExisting() {
      if (!this.foundUser) return;
      const id = this.foundUser.id;
      try {
        await medicoService.setPerfilMedico(id, true);
        this.foundUser = null;
        await this.cargar();
        this.cancelar();
      } catch (e) {
        this.foundUser = null;
        this.error = this.$t('error_assigning_profile');
      }
    },
    askRemoveProfile(medico) {
      this.removeProfileTarget = medico;
      this.resultMessage = '';
    },
    cancelRemoveProfile() {
      this.removeProfileTarget = null;
    },
    async confirmQuitarPerfil() {
      if (!this.removeProfileTarget) return;
      const id = this.removeProfileTarget.id;
      try {
        await medicoService.setPerfilMedico(id, false);
        this.removeProfileTarget = null;
        await this.cargar();
        this.mostrarResultado(this.$t('doctor_profile_removed_success'), false);
      } catch (e) {
        this.removeProfileTarget = null;
        this.mostrarResultado(this.$t('error_removing_profile'), true);
      }
    },
    mostrarResultado(mensaje, esError) {
      this.resultMessage = mensaje;
      this.resultError = esError;
    },
    cancelar() {
      this.showForm = false;
      this.showNifStep = false;
      this.foundUser = null;
      this.editMedico = null;
      this.error = '';
    },
    async guardar() {
      // Validar NIF antes de enviar
      if (!validateNIF(this.form.nif)) {
        this.error = this.$t('invalid_nif');
        return;
      }

      try {
        // El <input type="date"> da solo la parte de fecha (YYYY-MM-DD); el backend
        // espera un LocalDateTime, así que se completa con la hora (igual que en
        // RegisterView y DatosUsuarioView).
        const payload = {
          ...this.form,
          fechaNacimiento: this.form.fechaNacimiento ? `${this.form.fechaNacimiento}T00:00:00` : null
        };
        if (this.editMedico) {
          await medicoService.actualizar(this.editMedico.id, payload);
        } else {
          // Forzar estadoCuenta ACTIVO al crear
          await medicoService.crear({ ...payload, estadoCuenta: 'ACTIVO' });
        }
        await this.cargar();
        this.cancelar();
      } catch (e) {
        this.error = this.$t('error_saving_doctor');
      }
    }
  }
};
</script>

<style scoped>
.admin-medicos {
  margin-top: 2rem;
}

.admin-medicos__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  margin-bottom: 1.5rem;
}

.admin-medicos__title {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin: 0;
  font-size: 1.25rem;
  color: var(--text-primary);
}

.results-table-container {
  overflow-x: auto;
}

.medicos-table tbody tr {
  transition: background-color var(--transition-fast);
}

.medicos-table tbody tr:hover {
  background: var(--bg-light);
}

.doctor-cell {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.avatar-circle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2.25rem;
  height: 2.25rem;
  flex-shrink: 0;
  border-radius: 50%;
  background: var(--primary-100);
  color: var(--primary-color);
  font-size: 0.8rem;
  font-weight: 700;
}

.text-muted {
  color: var(--text-secondary);
}

.actions-col {
  text-align: right;
  white-space: nowrap;
}

.actions-cell {
  display: inline-flex;
  gap: 0.5rem;
}

/* ---------- Modal header (formularios y confirmaciones) ---------- */
/* Mismo patrón que AlergiasSection/AntecedentesSection: icono grande
   centrado sobre el título, dentro del slot #header de AppModal. */
.modal-icon-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  width: 100%;
}

.modal-icon-header h3 {
  margin: 0;
  text-align: center;
}

.modal-icon--primary {
  color: var(--primary-color);
}

.modal-icon--danger {
  color: var(--danger-color);
}

.modal-confirm-text {
  text-align: center;
  color: var(--text-secondary);
}

.modal-footer-actions {
  display: flex;
  justify-content: center;
  gap: 0.75rem;
}

/* ---------- Formulario de médico ---------- */
/* Mismos tokens que el formulario de registro (RegisterView) y el de
   edición de datos de usuario (DatosUsuarioView), para que se vea como
   el resto de formularios de la aplicación. */
.form-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.25rem;
}

@media (min-width: 640px) {
  .form-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .form-group.full-width {
    grid-column: 1 / -1;
  }
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
}

.required {
  color: var(--danger-active);
  font-size: 1.2em;
  font-weight: 800;
}

.form-input {
  padding: 0.75rem 1rem;
  border: 1.5px solid var(--border);
  border-radius: 8px;
  font-size: 0.875rem;
  transition: all 0.2s ease;
  background: var(--card-bg);
}

.form-input:hover {
  border-color: var(--primary-color);
}

.form-input:focus-visible {
  outline: var(--focus-outline);
  outline-offset: var(--focus-outline-offset);
}

@media (prefers-reduced-motion: reduce) {
  .medicos-table tbody tr,
  .form-input {
    transition: none;
  }
}
</style>
