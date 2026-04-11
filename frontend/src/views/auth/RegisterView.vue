<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-card">
        <!-- Header -->
        <div class="register-header">
          <div class="icon-wrapper">
            <svg class="register-icon" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
              <circle cx="9" cy="7" r="4"></circle>
              <line x1="19" y1="8" x2="19" y2="14"></line>
              <line x1="22" y1="11" x2="16" y2="11"></line>
            </svg>
          </div>
          <h1>{{$t('register_title')}}</h1>
          <p class="subtitle">{{$t('register_subtitle')}}</p>
        </div>

        <!-- Alerts -->
        <div v-if="error" class="alert alert-danger" role="alert">
          <svg class="alert-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
          <span>{{ error }}</span>
        </div>

        <div v-if="success" class="alert alert-success" role="alert">
          <svg class="alert-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
            <polyline points="22 4 12 14.01 9 11.01"></polyline>
          </svg>
          <span>¡Cuenta creada exitosamente! Redirigiendo al login...</span>
        </div>

        <!-- Form -->
        <form @submit.prevent="handleSignup" class="register-form">
          <!-- Datos Personales -->
          <div class="form-section">
            <h3 class="section-title">{{$t('personal_data')}}</h3>
            <div class="form-grid">
              <div class="form-group">
                <label for="nombre" class="form-label">
                  {{$t('first_name')}} <span class="required">*</span>
                </label>
                <input 
                  id="nombre" 
                  v-model="form.nombre" 
                  type="text"
                  class="form-input"
                  :placeholder="$t('first_name_placeholder')"
                  required 
                  autocomplete="given-name"
                />
              </div>

              <div class="form-group">
                <label for="apellido1" class="form-label">
                  {{$t('first_surname')}} <span class="required">*</span>
                </label>
                <input 
                  id="apellido1" 
                  v-model="form.apellido1" 
                  type="text"
                  class="form-input"
                  :placeholder="$t('first_surname_placeholder')"
                  required 
                  autocomplete="family-name"
                />
              </div>

              <div class="form-group">
                <label for="apellido2" class="form-label">
                  {{$t('second_surname')}}
                </label>
                  <input
                  id="apellido2" 
                  v-model="form.apellido2" 
                  type="text"
                  class="form-input"
                    :placeholder="$t('second_surname_placeholder')"
                  autocomplete="additional-name"
                />
              </div>

              <div class="form-group">
                <label for="fechaNacimiento" class="form-label">
                  Fecha de Nacimiento <span class="required">*</span>
                </label>
                <input 
                  id="fechaNacimiento" 
                  v-model="form.fechaNacimiento" 
                  type="date"
                  class="form-input"
                  required 
                  autocomplete="bday"
                />
              </div>
            </div>
          </div>

          <!-- Datos de Identificación -->
          <div class="form-section">
            <h3 class="section-title">Datos de Identificación</h3>
            <div class="form-grid">
              <div class="form-group">
                <label for="nif" class="form-label">
                    {{$t('nif_label')}} <span class="required">*</span>
                </label>
                <input 
                  id="nif" 
                  v-model="form.nif" 
                  type="text"
                  class="form-input"
                    :placeholder="$t('nif_placeholder')"
                  required 
                  pattern="[0-9]{8}[A-Z]"
                  title="Formato: 8 dígitos seguidos de una letra mayúscula"
                  autocomplete="off"
                />
                  <small class="form-help">{{$t('nif_format_help')}}</small>
              </div>

              <div class="form-group">
                <label for="telefono" class="form-label">
                  Teléfono
                </label>
                <input 
                  id="telefono" 
                  v-model="form.telefono" 
                  type="tel"
                  class="form-input"
                    :placeholder="$t('phone_placeholder')"
                  autocomplete="tel"
                />
              </div>
            </div>
          </div>

          <!-- Datos de Acceso -->
          <div class="form-section">
            <h3 class="section-title">Datos de Acceso</h3>
            <div class="form-grid">
              <div class="form-group full-width">
                <label for="email" class="form-label">
                    {{$t('email_label')}} <span class="required">*</span>
                </label>
                <input 
                  id="email" 
                  v-model="form.email" 
                  type="email"
                  class="form-input"
                    :placeholder="$t('email_placeholder')"
                  required 
                  autocomplete="email"
                />
              </div>

              <div class="form-group">
                <label for="password" class="form-label">
                  Contraseña <span class="required">*</span>
                </label>
                <input 
                  id="password" 
                  v-model="form.password" 
                  type="password"
                  class="form-input"
                    :placeholder="$t('password_min_placeholder')"
                  required 
                  minlength="6"
                  autocomplete="new-password"
                />
                  <small class="form-help">{{$t('password_min_help')}}</small>
              </div>

              <div class="form-group">
                <label for="password2" class="form-label">
                    {{$t('confirm_password')}} <span class="required">*</span>
                </label>
                <input 
                  id="password2" 
                  v-model="form.password2" 
                  type="password"
                  class="form-input"
                    :placeholder="$t('repeat_password_placeholder')"
                  required 
                  minlength="6"
                  autocomplete="new-password"
                />
              </div>
            </div>
          </div>

          <!-- Actions -->
          <div class="form-actions">
            <button 
              type="submit" 
              class="btn-primary" 
              :disabled="loading"
            >
              <span v-if="!loading">
                <svg class="btn-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
                  <circle cx="9" cy="7" r="4"></circle>
                  <line x1="19" y1="8" x2="19" y2="14"></line>
                  <line x1="22" y1="11" x2="16" y2="11"></line>
                </svg>
                {{$t('register_title')}}
              </span>
              <span v-else class="loading-text">
                <svg class="spinner" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 12a9 9 0 1 1-6.219-8.56"></path>
                </svg>
                {{$t('register_loading')}}
              </span>
            </button>

            <button 
              type="button" 
              class="btn-secondary" 
              @click="goLogin"
              :disabled="loading"
            >
              <svg class="btn-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="19" y1="12" x2="5" y2="12"></line>
                <polyline points="12 19 5 12 12 5"></polyline>
              </svg>
              Volver al Login
            </button>
          </div>
        </form>

        <!-- Footer -->
        <div class="register-footer">
          <p>
            <svg class="info-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="16" x2="12" y2="12"></line>
              <line x1="12" y1="8" x2="12.01" y2="8"></line>
            </svg>
              {{$t('required_fields_note')}} <span class="required">*</span> {{$t('are_required')}}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import authService from '@/services/authService'
import { validateNIF } from '@/utils/validateNIF'

export default {
  name: 'RegisterView',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const error = ref('')
    const success = ref(false)

    const form = ref({
      nombre: '',
      apellido1: '',
      apellido2: '',
      email: '',
      password: '',
      password2: '',
      fechaNacimiento: '',
      nif: '',
      telefono: ''
    })

    const handleSignup = async () => {
      error.value = ''
      success.value = false
      if (!validateNIF(form.value.nif)) {
        error.value = 'El NIF introducido no es válido.'
        return
      }
      if (form.value.password !== form.value.password2) {
        error.value = 'Las contraseñas no coinciden.'
        return
      }
      loading.value = true
      try {
        // Preparar payload (convertir fecha a ISO si está rellena)
        const payload = { ...form.value }
        delete payload.password2
        if (!payload.fechaNacimiento) {
          throw new Error('La fecha de nacimiento es obligatoria')
        }
        payload.fechaNacimiento = new Date(payload.fechaNacimiento).toISOString()
        await authService.signup(payload)
        success.value = true
        setTimeout(() => router.push({ name: 'Login' }), 1200)
      } catch (e) {
        error.value = e.message || 'Error al crear usuario'
      } finally {
        loading.value = false
      }
    }

    const goLogin = () => router.push({ name: 'Login' })

    return { form, handleSignup, loading, error, success, goLogin }

  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--register-bg-start) 0%, var(--register-bg-end) 100%);
  padding: 2rem 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.register-container {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}

.register-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
  overflow: hidden;
}

/* Header */
.register-header {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-hover) 100%);
  color: white;
  padding: 2.5rem 2rem;
  text-align: center;
}

.icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  margin-bottom: 1rem;
}

.register-icon {
  color: white;
}

.register-header h1 {
  margin: 0 0 0.5rem 0;
  font-size: 2rem;
  font-weight: 700;
}

.subtitle {
  margin: 0;
  opacity: 0.95;
  font-size: 1rem;
}

/* Alerts */
.alert {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  margin: 1.5rem 2rem 0 2rem;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
}

.alert-icon {
  flex-shrink: 0;
}

.alert-danger {
  background: var(--alert-danger-bg);
  color: var(--alert-danger-text);
  border: 1px solid var(--alert-danger-border);
}

.alert-success {
  background: var(--alert-success-bg);
  color: var(--alert-success-text);
  border: 1px solid var(--alert-success-border);
}

/* Form */
.register-form {
  padding: 2rem;
}

.form-section {
  margin-bottom: 2rem;
}

.form-section:last-of-type {
  margin-bottom: 1.5rem;
}

.section-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 1rem 0;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid var(--primary-color);
}

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
  color: var(--danger-color);
}

.form-input {
  padding: 0.75rem 1rem;
  border: 1.5px solid var(--border);
  border-radius: 8px;
  font-size: 0.875rem;
  transition: all 0.2s ease;
  background: white;
}

.form-input:hover {
  border-color: var(--primary-color);
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(2, 132, 199, 0.1);
}

.form-input::placeholder {
  color: var(--text-placeholder);
}

.form-help {
  font-size: 0.75rem;
  color: var(--text-secondary);
  margin-top: -0.25rem;
}

/* Actions */
.form-actions {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border);
}

@media (min-width: 640px) {
  .form-actions {
    flex-direction: row;
  }
}

.btn-primary,
.btn-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.875rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
  box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05);
  flex: 1;
}

.btn-primary {
  background: var(--primary-color);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1);
}

.btn-primary:active:not(:disabled) {
  background: var(--primary-active);
  transform: translateY(0);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: white;
  color: var(--secondary-color);
  border: 1.5px solid var(--border);
}

.btn-secondary:hover:not(:disabled) {
  background: var(--bg-light);
  border-color: var(--secondary-color);
}

.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-icon {
  flex-shrink: 0;
}

.loading-text {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* Footer */
.register-footer {
  padding: 1.5rem 2rem;
  background: var(--bg-light);
  border-top: 1px solid var(--border);
  text-align: center;
}

.register-footer p {
  margin: 0;
  font-size: 0.875rem;
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.info-icon {
  flex-shrink: 0;
  color: var(--primary-color);
}

/* Responsive */
@media (max-width: 640px) {
  .register-page {
    padding: 1rem 0.5rem;
  }
  
  .register-header {
    padding: 2rem 1rem;
  }
  
  .register-header h1 {
    font-size: 1.5rem;
  }
  
  .icon-wrapper {
    width: 64px;
    height: 64px;
  }
  
  .register-icon {
    width: 40px;
    height: 40px;
  }
  
  .register-form {
    padding: 1.5rem 1rem;
  }
  
  .alert {
    margin: 1rem;
  }
  
  .register-footer {
    padding: 1rem;
  }
}
</style>
