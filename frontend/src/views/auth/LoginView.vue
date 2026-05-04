<template>
  <main class="login-container">
    <section class="login-card" aria-labelledby="login-title">
      <h1 id="login-title">{{ $t('login_title') }}</h1>

      <form ref="formRef" @submit.prevent="handleLogin" class="login-form" novalidate :aria-busy="isLoading ? 'true' : 'false'">
        <div class="form-group">
          <label for="nif">{{ $t('nif_label') }}</label>
          <input
            id="nif"
            ref="nifInput"
            v-model.trim="credentials.nif"
            type="text"
            required
            aria-required="true"
            maxlength="9"
            autocomplete="username"
            inputmode="text"
            :placeholder="$t('nif_placeholder')"
            :aria-invalid="fieldErrors.nif ? 'true' : 'false'"
            :aria-describedby="buildDescribedBy('nif', !!fieldErrors.nif)"
            @input="normalizeNif"
            @blur="validateNifField"
          />
          <p id="nif-help" class="input-help sr-only">{{ $t('nif_placeholder') }}</p>
          <p v-if="fieldErrors.nif" id="nif-error" class="field-error">{{ fieldErrors.nif }}</p>
        </div>

        <div class="form-group">
          <label for="password">{{ $t('password_label') }}</label>
          <input
            id="password"
            ref="passwordInput"
            v-model="credentials.password"
            type="password"
            required
            aria-required="true"
            autocomplete="current-password"
            :placeholder="$t('password_placeholder')"
            :aria-invalid="fieldErrors.password ? 'true' : 'false'"
            :aria-describedby="buildDescribedBy('password', !!fieldErrors.password)"
            @blur="validatePasswordField"
          />
          <p v-if="fieldErrors.password" id="password-error" class="field-error">{{ fieldErrors.password }}</p>
        </div>

        <button type="submit" :disabled="isLoading" :aria-disabled="isLoading ? 'true' : 'false'" class="login-btn">
          {{ isLoading ? $t('logging_in') : $t('login_button') }}
        </button>

        <p v-if="isLoading" class="sr-only" role="status" aria-live="polite">
          {{ $t('logging_in') }}
        </p>

        <div
          v-if="error"
          id="form-error"
          ref="formErrorRef"
          class="error-message"
          role="alert"
          aria-live="assertive"
          tabindex="-1"
        >
          {{ error }}
        </div>
      </form>

      <div class="alt-actions">
        <router-link to="/register"><strong>{{ $t('no_account') }}</strong></router-link>
      </div>
    </section>
  </main>
</template>

<script>
  import { ref, nextTick } from 'vue'
  import { useRouter } from 'vue-router'
  import { useI18n } from 'vue-i18n'

  import { useAuth } from '@/composables/useAuth'
  import { validateNIF } from '@/utils/validateNIF'

  export default {
    name: 'LoginView',
    setup() {
      const router = useRouter()
      const { t } = useI18n()
      const { login } = useAuth()

      const credentials = ref({ nif: '', password: '' })
      const fieldErrors = ref({ nif: '', password: '' })
      const isLoading = ref(false)
      const error = ref('')

      const formRef = ref(null)
      const nifInput = ref(null)
      const passwordInput = ref(null)
      const formErrorRef = ref(null)

      const normalizeNif = () => {
        credentials.value.nif = credentials.value.nif.toUpperCase().replace(/\s+/g, '')
      }

      const validateNifField = () => {
        if (!credentials.value.nif) {
          fieldErrors.value.nif = ''
          return true
        }

        if (!validateNIF(credentials.value.nif)) {
          fieldErrors.value.nif = t('invalid_nif')
          return false
        }

        fieldErrors.value.nif = ''
        return true
      }

      const validatePasswordField = () => {
        if (!credentials.value.password) {
          fieldErrors.value.password = t('password_required')
          return false
        }

        fieldErrors.value.password = ''
        return true
      }

      const buildDescribedBy = (field, hasFieldError) => {
        const ids = []

        if (field === 'nif') ids.push('nif-help')
        if (hasFieldError) ids.push(`${field}-error`)
        if (error.value) ids.push('form-error')

        return ids.length ? ids.join(' ') : undefined
      }

      const focusFirstError = async () => {
        await nextTick()

        if (fieldErrors.value.nif && nifInput.value) {
          nifInput.value.focus()
          return
        }

        if (fieldErrors.value.password && passwordInput.value) {
          passwordInput.value.focus()
          return
        }

        if (error.value && formErrorRef.value) {
          formErrorRef.value.focus()
        }
      }

      const handleLogin = async () => {
        normalizeNif()
        fieldErrors.value = { nif: '', password: '' }
        error.value = ''

        const isNifValid = validateNifField()
        const isPasswordValid = validatePasswordField()

        if (!isNifValid || !isPasswordValid) {
          await focusFirstError()
          return
        }

        try {
          isLoading.value = true
          await login(credentials.value)
          router.push('/dashboard')
        } catch (err) {
          error.value = err.message || t('login_error')
          await focusFirstError()
        } finally {
          isLoading.value = false
        }
      }

      return {
        credentials,
        fieldErrors,
        isLoading,
        error,
        formRef,
        nifInput,
        passwordInput,
        formErrorRef,
        normalizeNif,
        validateNifField,
        validatePasswordField,
        buildDescribedBy,
        handleLogin
      }
    }
  }
</script>

<style scoped>
  .login-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    background-color: var(--bg-medium);
  }

  .login-card {
    background: var(--card-bg);
    padding: 2rem;
    border-radius: 8px;
    box-shadow: var(--shadow-soft);
    width: 100%;
    max-width: 400px;
  }

  .login-form {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .form-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .form-group label {
    font-weight: 500;
    color: var(--text-primary);
  }

  .form-group input {
    padding: 0.75rem;
    border: 1px solid var(--neutral-300);
    border-radius: 4px;
    font-size: 1rem;
  }

  .input-help {
    margin: 0;
    font-size: 0.875rem;
    color: var(--text-secondary);
  }

  .field-error {
    margin: 0;
    color: var(--danger-color);
    font-size: 0.9rem;
  }

  .login-btn {
    background-color: var(--brand-accent);
    color: var(--text-inverse);
    padding: 0.75rem;
    border: none;
    border-radius: 4px;
    font-size: 1rem;
    cursor: pointer;
    transition: background-color 0.3s;
  }

  .login-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  .error-message {
    color: var(--danger-color);
    text-align: left;
    padding: 0.5rem;
    background-color: var(--danger-soft-bg);
    border-radius: 4px;
    font-weight: 500;
  }

  .alt-actions {
    margin-top: 1rem;
    text-align: center;
  }

  .alt-actions a {
    color: var(--primary-color);
    text-decoration: none;
  }

  .alt-actions a:hover {
    text-decoration: underline;
  }

  .form-group input:focus-visible,
  .login-btn:focus-visible,
  .alt-actions a:focus-visible,
  .error-message:focus-visible {
    outline: 3px solid var(--focus-color, #005fcc);
    outline-offset: 2px;
  }

  .sr-only {
    position: absolute;
    width: 1px;
    height: 1px;
    padding: 0;
    margin: -1px;
    border: 0;
    overflow: hidden;
    clip: rect(0 0 0 0);
    white-space: nowrap;
  }
</style>
