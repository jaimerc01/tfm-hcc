<template>
  <div class="admin-medicos">
    <h2>{{$t('doctor_management')}}</h2>
    <button @click="startAdd">{{$t('add_doctor')}}</button>
    <table v-if="medicos.length" class="medicos-table">
      <thead>
        <tr>
          <th>{{$t('name')}}</th>
          <th>{{$t('surnames')}}</th>
          <th>{{$t('email')}}</th>
          <th>{{$t('nif')}}</th>
          <th>{{$t('specialty')}}</th>
          <th>{{$t('phone')}}</th>
          <th>{{$t('birth_date')}}</th>
          <th>{{$t('actions')}}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="m in medicos" :key="m.id">
          <td>{{ m.nombre }}</td>
          <td>{{ m.apellido1 }} {{ m.apellido2 }}</td>
          <td>{{ m.email }}</td>
          <td>{{ m.nif }}</td>
          <td>{{ m.especialidad }}</td>
          <td>{{ m.telefono }}</td>
          <td>{{ formatFecha(m.fechaNacimiento) }}</td>
          <td>
            <button @click="edit(m)">{{$t('edit')}}</button>
            <button @click="eliminar(m.id)">{{$t('delete')}}</button>
            <button @click="quitarPerfil(m.id)" :title="$t('remove_profile_title')">{{$t('remove_profile')}}</button>
          </td>
        </tr>
      </tbody>
    </table>
    <div v-else>{{$t('no_doctors')}}</div>

    <div v-if="showForm" class="medico-form-modal">
      <div class="medico-form">
        <h3>{{ editMedico ? $t('edit_doctor') : $t('add_doctor_title') }}</h3>
        <form @submit.prevent="guardar">
          <input v-model="form.nombre" :placeholder="$t('name')" required />
          <input v-model="form.apellido1" :placeholder="$t('first_surname')" required />
          <input v-model="form.apellido2" :placeholder="$t('second_surname')" />
          <input v-model="form.email" :placeholder="$t('email')" required type="email" />
          <input v-model="form.nif" :placeholder="$t('nif')" required />
          <input v-model="form.especialidad" :placeholder="$t('specialty')" />
          <input v-model="form.telefono" :placeholder="$t('phone')" />
          <input v-model="form.fechaNacimiento" :placeholder="$t('birth_date')" type="date" required />
          <input v-if="!editMedico" v-model="form.password" :placeholder="$t('password')" required type="password" />
          <div class="form-actions">
            <button type="submit">{{$t('save')}}</button>
            <button type="button" @click="cancelar">{{$t('cancel')}}</button>
          </div>
        </form>
        <div v-if="error" class="error-message">{{ error }}</div>
      </div>
    </div>

    <div v-if="showNifStep" class="medico-form-modal">
      <div class="medico-form">
        <h3>{{$t('search_user_nif')}}</h3>
        <form @submit.prevent="checkNif">
          <input v-model="form.nif" :placeholder="$t('nif')" required />
          <div class="form-actions">
            <button type="submit">{{$t('search')}}</button>
            <button type="button" @click="cancelar">{{$t('cancel')}}</button>
          </div>
        </form>
        <div v-if="error" class="error-message">{{ error }}</div>
      </div>
    </div>
  </div>
</template>

<script>

import medicoService from '@/services/medicoService';
import { validateNIF } from '@/utils/validateNIF';


export default {
  name: 'AdminMedicosView',
  data() {
    return {
      medicos: [],
    showForm: false,
    showNifStep: false,
      editMedico: null,
      form: {
        nombre: '', apellido1: '', apellido2: '', email: '', nif: '', especialidad: '', telefono: '', fechaNacimiento: '', password: '', estadoCuenta: 'ACTIVO'
      },
      error: ''
    };
  },
  async created() {
    await this.cargar();
  },
  methods: {
    formatFecha(fecha) {
      if (!fecha) return '';
      // Soporta Date, string ISO, o timestamp
      const d = typeof fecha === 'string' ? new Date(fecha) : fecha;
      if (isNaN(d)) return '';
      return d.toISOString().slice(0, 10);
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
    async eliminar(id) {
      if (!confirm(this.$t('confirm_delete_doctor'))) return;
      try {
        await medicoService.eliminar(id);
        await this.cargar();
      } catch (e) {
        this.error = this.$t('error_deleting_doctor');
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
        // Usuario existe: asignar perfil MEDICO
        if (res && res.data) {
          const user = res.data
          await medicoService.setPerfilMedico(user.id, true)
          await this.cargar()
          this.cancelar()
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
    async quitarPerfil(id) {
      if (!confirm(this.$t('confirm_remove_doctor_profile'))) return;
      try {
        await medicoService.setPerfilMedico(id, false);
        await this.cargar();
      } catch (e) {
        this.error = this.$t('error_removing_profile');
      }
    },
    cancelar() {
      this.showForm = false;
      this.showNifStep = false;
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
.admin-medicos { max-width: 900px; margin: 2rem auto; }
.medicos-table { width: 100%; border-collapse: collapse; margin-top: 1rem; }
.medicos-table th, .medicos-table td { border: 1px solid var(--border-medium); padding: 0.5rem; }
.medico-form-modal { position: fixed; top:0; left:0; right:0; bottom:0; background:var(--surface-overlay-soft); display:flex; align-items:center; justify-content:center; }
.medico-form { background:var(--card-bg); padding:2rem; border-radius:8px; min-width:300px; }
.form-actions { display:flex; gap:1rem; }
.error-message { color: var(--danger-color); margin-top: 1rem; }
</style>
