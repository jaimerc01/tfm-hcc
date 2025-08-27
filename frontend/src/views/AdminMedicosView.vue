<template>
  <div class="admin-medicos">
  <h2>Gestión de Médicos</h2>
  <button @click="startAdd">Añadir médico</button>
    <table v-if="medicos.length" class="medicos-table">
      <thead>
        <tr>
          <th>Nombre</th>
          <th>Apellidos</th>
          <th>Email</th>
          <th>NIF</th>
          <th>Especialidad</th>
          <th>Teléfono</th>
          <th>Fecha nacimiento</th>
          <th>Acciones</th>
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
            <button @click="edit(m)">Editar</button>
            <button @click="eliminar(m.id)">Eliminar</button>
            <button @click="quitarPerfil(m.id)" title="Quitar perfil MEDICO">Quitar perfil</button>
          </td>
        </tr>
      </tbody>
    </table>
    <div v-else>No hay médicos registrados.</div>

    <div v-if="showForm" class="medico-form-modal">
      <div class="medico-form">
        <h3>{{ editMedico ? 'Editar médico' : 'Añadir médico' }}</h3>
        <form @submit.prevent="guardar">
          <input v-model="form.nombre" placeholder="Nombre" required />
          <input v-model="form.apellido1" placeholder="Primer apellido" required />
          <input v-model="form.apellido2" placeholder="Segundo apellido" />
          <input v-model="form.email" placeholder="Email" required type="email" />
          <input v-model="form.nif" placeholder="NIF" required />
          <input v-model="form.especialidad" placeholder="Especialidad" />
          <input v-model="form.telefono" placeholder="Teléfono" />
          <input v-model="form.fechaNacimiento" placeholder="Fecha de nacimiento" type="date" required />
          <input v-if="!editMedico" v-model="form.password" placeholder="Contraseña" required type="password" />
          <div class="form-actions">
            <button type="submit">Guardar</button>
            <button type="button" @click="cancelar">Cancelar</button>
          </div>
        </form>
        <div v-if="error" class="error-message">{{ error }}</div>
      </div>
    </div>

    <div v-if="showNifStep" class="medico-form-modal">
      <div class="medico-form">
        <h3>Buscar usuario por NIF</h3>
        <form @submit.prevent="checkNif">
          <input v-model="form.nif" placeholder="NIF" required />
          <div class="form-actions">
            <button type="submit">Buscar</button>
            <button type="button" @click="cancelar">Cancelar</button>
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
        this.error = 'Error cargando médicos';
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
      if (!confirm('¿Eliminar este médico?')) return;
      try {
        await medicoService.eliminar(id);
        await this.cargar();
      } catch (e) {
        this.error = 'Error eliminando médico';
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
        this.error = 'El NIF introducido no es válido';
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
        this.error = 'Error comprobando NIF'
      }
    },
    async quitarPerfil(id) {
      if (!confirm('¿Quitar el perfil MEDICO de este usuario?')) return;
      try {
        await medicoService.setPerfilMedico(id, false);
        await this.cargar();
      } catch (e) {
        this.error = 'Error quitando perfil';
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
        this.error = 'El NIF introducido no es válido';
        return;
      }

      try {
  if (this.editMedico) {
          await medicoService.actualizar(this.editMedico.id, this.form);
        } else {
          // Forzar estadoCuenta ACTIVO al crear
          const medico = { ...this.form, estadoCuenta: 'ACTIVO' };
          await medicoService.crear(medico);
        }
        await this.cargar();
        this.cancelar();
      } catch (e) {
        this.error = 'Error guardando médico';
      }
    }
  }
};
</script>

<style scoped>
.admin-medicos { max-width: 900px; margin: 2rem auto; }
.medicos-table { width: 100%; border-collapse: collapse; margin-top: 1rem; }
.medicos-table th, .medicos-table td { border: 1px solid #ccc; padding: 0.5rem; }
.medico-form-modal { position: fixed; top:0; left:0; right:0; bottom:0; background:rgba(0,0,0,0.2); display:flex; align-items:center; justify-content:center; }
.medico-form { background:#fff; padding:2rem; border-radius:8px; min-width:300px; }
.form-actions { margin-top:1rem; display:flex; gap:1rem; }
.error-message { color: #c00; margin-top: 1rem; }
</style>
