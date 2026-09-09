<template>
  <main class="register-page">
    <div class="register-container">
      <section class="register-card" aria-labelledby="register-title">
        <header class="register-header">
          <div class="icon-wrapper" aria-hidden="true">
            <AppIcon class="register-icon" name="user-plus" size="3xl" />
          </div>
          <h1 id="register-title">{{$t('register_title')}}</h1>
          <p class="subtitle">{{$t('register_subtitle')}}</p>
        </header>

        <div
          v-if="error"
          id="form-error"
          ref="formErrorRef"
          class="alert alert-danger"
          role="alert"
          aria-live="assertive"
          tabindex="-1"
        >
          <AppIcon class="alert-icon" name="alert-circle" size="lg" />
          <span>{{ error }}</span>
        </div>

        <div v-if="success" class="alert alert-success" role="status" aria-live="polite">
          <AppIcon class="alert-icon" name="check-circle" size="lg" />
          <span>{{ $t('register_success') }}</span>
        </div>

        <form
          ref="formRef"
          @submit.prevent="handleSignup"
          class="register-form"
          novalidate
          :aria-busy="loading ? 'true' : 'false'"
        >
          <fieldset class="form-section">
            <legend class="section-title">{{$t('personal_data')}}</legend>
            <div class="form-grid">
              <div class="form-group">
                <label for="nombre" class="form-label">
                  {{$t('first_name')}} <span class="required" aria-hidden="true">*</span>
                </label>
                <input
                  id="nombre"
                  ref="nombreInput"
                  v-model.trim="form.nombre"
                  type="text"
                  class="form-input"
                  :placeholder="$t('first_name_placeholder')"
                  required
                  autocomplete="given-name"
                  :aria-invalid="fieldErrors.nombre ? 'true' : 'false'"
                  :aria-describedby="buildDescribedBy('nombre')"
                  @blur="validateNombreField"
                />
                <p v-if="fieldErrors.nombre" id="nombre-error" class="field-error">{{ fieldErrors.nombre }}</p>
              </div>

              <div class="form-group">
                <label for="apellido1" class="form-label">
                  {{$t('first_surname')}} <span class="required" aria-hidden="true">*</span>
                </label>
                <input
                  id="apellido1"
                  ref="apellido1Input"
                  v-model.trim="form.apellido1"
                  type="text"
                  class="form-input"
                  :placeholder="$t('first_surname_placeholder')"
                  required
                  autocomplete="family-name"
                  :aria-invalid="fieldErrors.apellido1 ? 'true' : 'false'"
                  :aria-describedby="buildDescribedBy('apellido1')"
                  @blur="validateApellido1Field"
                />
                <p v-if="fieldErrors.apellido1" id="apellido1-error" class="field-error">{{ fieldErrors.apellido1 }}</p>
              </div>

              <div class="form-group">
                <label for="apellido2" class="form-label">
                  {{$t('second_surname')}}
                </label>
                <input
                  id="apellido2"
                  v-model.trim="form.apellido2"
                  type="text"
                  class="form-input"
                  :placeholder="$t('second_surname_placeholder')"
                  autocomplete="additional-name"
                />
              </div>

              <div class="form-group">
                <label for="fechaNacimiento" class="form-label">
                  {{ $t('birth_date_label') }} <span class="required" aria-hidden="true">*</span>
                </label>
                <input
                  id="fechaNacimiento"
                  ref="fechaNacimientoInput"
                  v-model="form.fechaNacimiento"
                  type="date"
                  class="form-input"
                  required
                  autocomplete="bday"
                  :aria-invalid="fieldErrors.fechaNacimiento ? 'true' : 'false'"
                  :aria-describedby="buildDescribedBy('fechaNacimiento')"
                  @blur="validateFechaNacimientoField"
                />
                <p v-if="fieldErrors.fechaNacimiento" id="fechaNacimiento-error" class="field-error">{{ fieldErrors.fechaNacimiento }}</p>
              </div>
            </div>
          </fieldset>

          <fieldset class="form-section">
            <legend class="section-title">{{ $t('identification_data') }}</legend>
            <div class="form-grid">
              <div class="form-group">
                <label for="email" class="form-label">
                  {{$t('email_label')}} <span class="required" aria-hidden="true">*</span>
                </label>
                <input
                  id="email"
                  ref="emailInput"
                  v-model.trim="form.email"
                  type="email"
                  class="form-input"
                  :placeholder="$t('email_placeholder')"
                  required
                  autocomplete="email"
                  :aria-invalid="fieldErrors.email ? 'true' : 'false'"
                  :aria-describedby="buildDescribedBy('email')"
                  @blur="validateEmailField"
                />
                <p v-if="fieldErrors.email" id="email-error" class="field-error">{{ fieldErrors.email }}</p>
              </div>

              <div class="form-group">
                <label for="telefono" class="form-label">
                  {{ $t('phone') }}
                </label>
                <input
                  id="telefono"
                  v-model.trim="form.telefono"
                  type="tel"
                  class="form-input"
                  :placeholder="$t('phone_placeholder')"
                  autocomplete="tel"
                />
              </div>
            </div>
          </fieldset>

          <fieldset class="form-section">
            <legend class="section-title">{{ $t('access_data') }}</legend>
            <div class="form-grid">
              <div class="form-group full-width">
                <label for="nif" class="form-label">
                  {{$t('nif_label')}} <span class="required" aria-hidden="true">*</span>
                </label>
                <input
                  id="nif"
                  ref="nifInput"
                  v-model.trim="form.nif"
                  type="text"
                  class="form-input"
                  :placeholder="$t('nif_placeholder')"
                  required
                  maxlength="9"
                  autocomplete="off"
                  :aria-invalid="fieldErrors.nif ? 'true' : 'false'"
                  :aria-describedby="buildDescribedBy('nif', true)"
                  @input="normalizeNif"
                  @blur="validateNifField"
                />
                <small id="nif-help" class="form-help">{{$t('nif_format_help')}}</small>
                <p v-if="fieldErrors.nif" id="nif-error" class="field-error">{{ fieldErrors.nif }}</p>
              </div>

              <div class="form-group">
                <label for="password" class="form-label">
                  {{ $t('password') }} <span class="required" aria-hidden="true">*</span>
                </label>
                <input
                  id="password"
                  ref="passwordInput"
                  v-model="form.password"
                  type="password"
                  class="form-input"
                  :placeholder="$t('password_min_placeholder')"
                  required
                  minlength="8"
                  autocomplete="new-password"
                  :aria-invalid="fieldErrors.password ? 'true' : 'false'"
                  :aria-describedby="buildDescribedBy('password', true)"
                  @blur="validatePasswordField"
                />
                <small id="password-help" class="form-help">{{$t('password_min_help')}}</small>
                <p v-if="fieldErrors.password" id="password-error" class="field-error">{{ fieldErrors.password }}</p>
              </div>

              <div class="form-group">
                <label for="password2" class="form-label">
                  {{$t('confirm_password')}} <span class="required" aria-hidden="true">*</span>
                </label>
                <input
                  id="password2"
                  ref="password2Input"
                  v-model="form.password2"
                  type="password"
                  class="form-input"
                  :placeholder="$t('repeat_password_placeholder')"
                  required
                  minlength="8"
                  autocomplete="new-password"
                  :aria-invalid="fieldErrors.password2 ? 'true' : 'false'"
                  :aria-describedby="buildDescribedBy('password2')"
                  @blur="validatePassword2Field"
                />
                <p v-if="fieldErrors.password2" id="password2-error" class="field-error">{{ fieldErrors.password2 }}</p>
              </div>
            </div>
          </fieldset>

          <div class="consent-group">
            <label class="consent-check" for="aceptaTratamientoDatos">
              <input
                id="aceptaTratamientoDatos"
                ref="consentInput"
                v-model="form.aceptaTratamientoDatos"
                type="checkbox"
                :aria-invalid="fieldErrors.aceptaTratamientoDatos ? 'true' : 'false'"
                :aria-describedby="buildDescribedBy('aceptaTratamientoDatos')"
              />
              <span>
                <i18n-t keypath="register_consent_label" tag="span">
                  <template #link>
                    <router-link :to="{ name: 'PoliticaPrivacidad' }" target="_blank">{{ $t('privacy') }}</router-link>
                  </template>
                </i18n-t>
              </span>
            </label>
            <p v-if="fieldErrors.aceptaTratamientoDatos" id="aceptaTratamientoDatos-error" class="field-error">
              {{ fieldErrors.aceptaTratamientoDatos }}
            </p>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn-primary" :disabled="loading" :aria-disabled="loading ? 'true' : 'false'">
              <span v-if="!loading">
                <AppIcon name="user-plus" size="lg" />
                {{$t('register_title')}}
              </span>
              <span v-else class="loading-text">
                <AppIcon name="spinner" size="lg" spin />
                {{$t('register_loading')}}
              </span>
            </button>

            <button type="button" class="btn-secondary" @click="goLogin" :disabled="loading" :aria-disabled="loading ? 'true' : 'false'">
              <AppIcon name="arrow-left" size="lg" />
              {{ $t('back_to_login') }}
            </button>
          </div>
        </form>

        <footer class="register-footer">
          <p>
            <AppIcon class="info-icon" name="info" size="sm" />
            {{$t('required_fields_note')}} <span class="required" aria-hidden="true">*</span> {{$t('are_required')}}
          </p>
        </footer>
      </section>
    </div>
  </main>
</template>

<script>
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import authService from '@/services/authService'
import { validateNIF } from '@/utils/validateNIF'
import AppIcon from '@/components/AppIcon.vue'

const MIN_PASSWORD_LENGTH = 8

export default {
  name: 'RegisterView',
  components: { AppIcon },
  setup() {
    const router = useRouter()
    const { t } = useI18n()
    const loading = ref(false)
    const error = ref('')
    const success = ref(false)

    const formRef = ref(null)
    const formErrorRef = ref(null)
    const nombreInput = ref(null)
    const apellido1Input = ref(null)
    const fechaNacimientoInput = ref(null)
    const nifInput = ref(null)
    const emailInput = ref(null)
    const passwordInput = ref(null)
    const password2Input = ref(null)
    const consentInput = ref(null)

    const form = ref({
      nombre: '',
      apellido1: '',
      apellido2: '',
      email: '',
      password: '',
      password2: '',
      fechaNacimiento: '',
      nif: '',
      telefono: '',
      aceptaTratamientoDatos: false
    })

    const fieldErrors = ref({
      nombre: '',
      apellido1: '',
      fechaNacimiento: '',
      nif: '',
      email: '',
      password: '',
      password2: '',
      aceptaTratamientoDatos: ''
    })

    const resetFieldErrors = () => {
      fieldErrors.value = {
        nombre: '',
        apellido1: '',
        fechaNacimiento: '',
        nif: '',
        email: '',
        password: '',
        password2: '',
        aceptaTratamientoDatos: ''
      }
    }

    // Edad mínima para registrarse (LOPDGDD art. 7).
    const MIN_EDAD_REGISTRO = 14

    const normalizeNif = () => {
      form.value.nif = form.value.nif.toUpperCase().replace(/\s+/g, '')
    }

    const validateNombreField = () => {
      if (!form.value.nombre) {
        fieldErrors.value.nombre = t('first_name_required')
        return false
      }
      fieldErrors.value.nombre = ''
      return true
    }

    const validateApellido1Field = () => {
      if (!form.value.apellido1) {
        fieldErrors.value.apellido1 = t('first_surname_required')
        return false
      }
      fieldErrors.value.apellido1 = ''
      return true
    }

    const calcularEdad = (fechaIso) => {
      const nacimiento = new Date(fechaIso)
      const hoy = new Date()
      let edad = hoy.getFullYear() - nacimiento.getFullYear()
      const m = hoy.getMonth() - nacimiento.getMonth()
      if (m < 0 || (m === 0 && hoy.getDate() < nacimiento.getDate())) edad -= 1
      return edad
    }

    const validateFechaNacimientoField = () => {
      if (!form.value.fechaNacimiento) {
        fieldErrors.value.fechaNacimiento = t('birth_date_required')
        return false
      }
      if (calcularEdad(form.value.fechaNacimiento) < MIN_EDAD_REGISTRO) {
        fieldErrors.value.fechaNacimiento = t('register_min_age_error', { age: MIN_EDAD_REGISTRO })
        return false
      }
      fieldErrors.value.fechaNacimiento = ''
      return true
    }

    const validateConsentimientoField = () => {
      if (!form.value.aceptaTratamientoDatos) {
        fieldErrors.value.aceptaTratamientoDatos = t('register_consent_required')
        return false
      }
      fieldErrors.value.aceptaTratamientoDatos = ''
      return true
    }

    const validateNifField = () => {
      if (!form.value.nif) {
        fieldErrors.value.nif = t('nif_required')
        return false
      }
      if (!validateNIF(form.value.nif)) {
        fieldErrors.value.nif = t('invalid_nif')
        return false
      }
      fieldErrors.value.nif = ''
      return true
    }

    const validateEmailField = () => {
      if (!form.value.email) {
        fieldErrors.value.email = t('email_required')
        return false
      }
      fieldErrors.value.email = ''
      return true
    }

    const validatePasswordField = () => {
      if (!form.value.password) {
        fieldErrors.value.password = t('password_required')
        return false
      }
      if (form.value.password.length < MIN_PASSWORD_LENGTH) {
        fieldErrors.value.password = t('password_min_length')
        return false
      }
      fieldErrors.value.password = ''
      return true
    }

    const validatePassword2Field = () => {
      if (!form.value.password2) {
        fieldErrors.value.password2 = t('confirm_password_required')
        return false
      }
      if (form.value.password !== form.value.password2) {
        fieldErrors.value.password2 = t('passwords_no_match')
        return false
      }
      fieldErrors.value.password2 = ''
      return true
    }

    const buildDescribedBy = (field, hasHelp = false) => {
      const ids = []
      if (hasHelp) ids.push(field + '-help')
      if (fieldErrors.value[field]) ids.push(field + '-error')
      if (error.value) ids.push('form-error')
      return ids.length ? ids.join(' ') : undefined
    }

    const focusFirstError = async () => {
      await nextTick()

      if (fieldErrors.value.nombre && nombreInput.value) return nombreInput.value.focus()
      if (fieldErrors.value.apellido1 && apellido1Input.value) return apellido1Input.value.focus()
      if (fieldErrors.value.fechaNacimiento && fechaNacimientoInput.value) return fechaNacimientoInput.value.focus()
      if (fieldErrors.value.email && emailInput.value) return emailInput.value.focus()
      if (fieldErrors.value.nif && nifInput.value) return nifInput.value.focus()
      if (fieldErrors.value.password && passwordInput.value) return passwordInput.value.focus()
      if (fieldErrors.value.password2 && password2Input.value) return password2Input.value.focus()
      if (fieldErrors.value.aceptaTratamientoDatos && consentInput.value) return consentInput.value.focus()

      if (error.value && formErrorRef.value) formErrorRef.value.focus()
    }

    const validateForm = () => {
      const checks = [
        validateNombreField(),
        validateApellido1Field(),
        validateFechaNacimientoField(),
        validateEmailField(),
        validateNifField(),
        validatePasswordField(),
        validatePassword2Field(),
        validateConsentimientoField()
      ]
      return checks.every(Boolean)
    }

    const handleSignup = async () => {
      error.value = ''
      success.value = false
      resetFieldErrors()
      normalizeNif()

      if (!validateForm()) {
        await focusFirstError()
        return
      }

      loading.value = true
      try {
        const payload = { ...form.value }
        delete payload.password2
        payload.fechaNacimiento = new Date(payload.fechaNacimiento).toISOString()

        await authService.signup(payload)
        success.value = true
        setTimeout(() => router.push({ name: 'Login' }), 1200)
      } catch (e) {
        error.value = e.message || t('register_error')
        await focusFirstError()
      } finally {
        loading.value = false
      }
    }

    const goLogin = () => router.push({ name: 'Login' })

    return {
      form,
      formRef,
      formErrorRef,
      nombreInput,
      apellido1Input,
      fechaNacimientoInput,
      nifInput,
      emailInput,
      passwordInput,
      password2Input,
      consentInput,
      fieldErrors,
      loading,
      error,
      success,
      handleSignup,
      goLogin,
      normalizeNif,
      validateNombreField,
      validateApellido1Field,
      validateFechaNacimientoField,
      validateNifField,
      validateEmailField,
      validatePasswordField,
      validatePassword2Field,
      validateConsentimientoField,
      buildDescribedBy
    }
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
  background: var(--card-bg);
  border-radius: 12px;
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

.register-header {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-hover) 100%);
  color: var(--text-inverse);
  padding: 2.5rem 2rem;
  text-align: center;
}

.icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: var(--white-alpha-20);
  border-radius: 50%;
  margin-bottom: 1rem;
}

.register-icon {
  color: var(--text-inverse);
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
  color: white;
}

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

.register-form {
  padding: 2rem;
}

.form-section {
  margin-bottom: 2rem;
  border: 0;
  padding: 0;
  min-width: 0;
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
  width: 100%;
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

.form-input::placeholder {
  color: var(--text-placeholder);
}

.form-help {
  font-size: 0.75rem;
  color: var(--text-secondary);
  margin-top: -0.25rem;
}

.field-error {
  margin: 0;
  font-size: 0.8rem;
  color: var(--danger-active);
  font-weight: 600;
}

.privacy-notice {
  margin: 0;
  font-size: 0.85rem;
  color: var(--text-secondary);
  text-align: center;
}

.consent-group {
  margin: 0.5rem 0;
}

.consent-check {
  display: flex;
  align-items: flex-start;
  gap: 0.625rem;
  font-size: 0.85rem;
  color: var(--text-secondary);
  cursor: pointer;
}

.consent-check input[type='checkbox'] {
  margin-top: 0.15rem;
  flex-shrink: 0;
  width: 1rem;
  height: 1rem;
  cursor: pointer;
}

.privacy-notice a {
  color: var(--primary-color);
  font-weight: 600;
}

.form-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--border);
}

@media (min-width: 640px) {
  .form-actions {
    flex-direction: row;
  }
}

/* Layout only -- colour, hover and disabled come from the shared button
   system in styles/shared.css (every button in the app is green). */
.btn-primary,
.btn-secondary {
  padding: 0.875rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  flex: 1;
}

.loading-text {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

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

.form-input:focus-visible,
.btn-primary:focus-visible,
.btn-secondary:focus-visible,
.alert:focus-visible {
  outline: var(--focus-outline);
  outline-offset: var(--focus-outline-offset);
}

@media (prefers-reduced-motion: reduce) {
  .btn-primary,
  .btn-secondary,
  .form-input {
    transition: none;
  }
}

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
}

</style>
