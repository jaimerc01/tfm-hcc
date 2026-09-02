<template>
  <main class="auth-viewport login-container">
    <section class="login-card" aria-labelledby="login-title">
      <h1 id="login-title">{{ step === 'twoFactor' ? $t('two_factor_title') : $t('login_title') }}</h1>

      <form
        v-if="step === 'twoFactor'"
        ref="twoFactorFormRef"
        @submit.prevent="handleTwoFactorSubmit"
        class="login-form"
        novalidate
        :aria-busy="twoFactorLoading ? 'true' : 'false'"
      >
        <p class="two-factor-help">{{ $t('two_factor_help') }}</p>

        <div class="form-group">
          <label for="two-factor-code">{{ $t('two_factor_code_label') }}</label>
          <input
            id="two-factor-code"
            ref="twoFactorInput"
            v-model="twoFactorCode"
            type="text"
            inputmode="numeric"
            autocomplete="one-time-code"
            required
            aria-required="true"
            maxlength="6"
            :placeholder="$t('two_factor_code_placeholder')"
            :aria-invalid="twoFactorError ? 'true' : 'false'"
            :aria-describedby="twoFactorError ? 'two-factor-error' : undefined"
          />
        </div>

        <button type="submit" :disabled="twoFactorLoading" class="login-btn">
          {{ twoFactorLoading ? $t('logging_in') : $t('two_factor_verify_button') }}
        </button>

        <button type="button" class="link-btn" @click="backToCredentials">
          {{ $t('two_factor_back') }}
        </button>

        <div
          v-if="twoFactorError"
          id="two-factor-error"
          class="error-message"
          role="alert"
          aria-live="assertive"
        >
          {{ twoFactorError }}
        </div>
      </form>

      <form v-else ref="formRef" @submit.prevent="handleLogin" class="login-form" novalidate :aria-busy="isLoading ? 'true' : 'false'">
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

        <div class="google-login">
          <div class="separator" aria-hidden="true">
            <span>{{ $t('or') }}</span>
          </div>

          <button
            type="button"
            class="google-btn"
            @click="handleGoogleLogin"
            :aria-label="$t('login_with_google')"
          >
            <svg
              class="google-icon"
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <path
                fill="currentColor"
                d="M21.35 12.23c0-.79-.07-1.55-.23-2.27H12v4.3h5.23a4.47 4.47 0 0 1-1.94 2.93v2.39h3.14c1.84-1.69 2.92-4.18 2.92-7.35z"
              />
              <path
                fill="currentColor"
                d="M12 21.99c2.63 0 4.84-.87 6.45-2.41l-3.14-2.39c-.87.58-1.98.92-3.31.92-2.54 0-4.69-1.72-5.46-4.03H3.3v2.46A9.74 9.74 0 0 0 12 21.99z"
              />
              <path
                fill="currentColor"
                d="M6.54 14.08A5.86 5.86 0 0 1 6.23 12c0-.72.12-1.42.31-2.08V7.46H3.3A9.99 9.99 0 0 0 2.01 12c0 1.63.39 3.17 1.29 4.54l3.24-2.46z"
              />
              <path
                fill="currentColor"
                d="M12 5.89c1.43 0 2.71.49 3.72 1.45l2.79-2.79C16.83 2.92 14.63 2 12 2a9.74 9.74 0 0 0-8.7 5.46l3.24 2.46C6.31 7.61 8.46 5.89 12 5.89z"
              />
            </svg>

            <span>{{ $t('login_with_google') }}</span>
          </button>
        </div>

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

      <div v-if="step === 'credentials'" class="alt-actions">
        <router-link to="/recuperar-password">{{ $t('forgot_password_link') }}</router-link>
        <router-link to="/register"><strong>{{ $t('no_account') }}</strong></router-link>
      </div>
    </section>
  </main>
</template>

<script>
  import { ref, nextTick, onMounted } from 'vue'
  import { useRouter, useRoute } from 'vue-router'
  import { useI18n } from 'vue-i18n'

  import { useAuth } from '@/composables/useAuth'
  import authService from '@/services/authService'
  import { validateNIF } from '@/utils/validateNIF'

  const GOOGLE_ERROR_KEYS = {
    google_account_not_found: 'google_account_not_found_error',
    google_email_not_verified: 'google_login_error',
    google_code_invalid: 'google_login_error',
    google_auth_failed: 'google_login_error'
  }

  export default {
    name: 'LoginView',
    setup() {
      const router = useRouter()
      const route = useRoute()
      const { t } = useI18n()
      const { login, loginTwoFactor } = useAuth()

      const credentials = ref({ nif: '', password: '' })
      const fieldErrors = ref({ nif: '', password: '' })
      const isLoading = ref(false)
      const error = ref('')

      const formRef = ref(null)
      const nifInput = ref(null)
      const passwordInput = ref(null)
      const formErrorRef = ref(null)

      // Segundo factor (TOTP): 'credentials' es el paso normal de NIF/contraseña;
      // 'twoFactor' aparece solo si el usuario tiene activado el segundo factor.
      const step = ref('credentials')
      const challengeId = ref('')
      const twoFactorCode = ref('')
      const twoFactorError = ref('')
      const twoFactorLoading = ref(false)
      const twoFactorFormRef = ref(null)
      const twoFactorInput = ref(null)

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
          const result = await login(credentials.value)

          if (result.requiresTwoFactor) {
            challengeId.value = result.challengeId
            twoFactorCode.value = ''
            twoFactorError.value = ''
            step.value = 'twoFactor'
            await nextTick()
            twoFactorInput.value?.focus()
            return
          }

          router.push('/dashboard')
        } catch (err) {
          error.value = err.message || t('login_error')
          await focusFirstError()
        } finally {
          isLoading.value = false
        }
      }

      const handleGoogleLogin = () => {
        error.value = ''
        // Navegación completa: el navegador sale de la SPA hacia el backend y, desde ahí,
        // hacia Google. La vuelta la gestiona GoogleCallbackView.
        authService.redirectToGoogleLogin()
      }

      const handleTwoFactorSubmit = async () => {
        twoFactorError.value = ''

        if (!twoFactorCode.value || !/^\d{6}$/.test(twoFactorCode.value)) {
          twoFactorError.value = t('two_factor_code_invalid')
          return
        }

        try {
          twoFactorLoading.value = true
          await loginTwoFactor(challengeId.value, twoFactorCode.value)
          router.push('/dashboard')
        } catch (err) {
          twoFactorError.value = err.message || t('two_factor_code_invalid')
          twoFactorCode.value = ''
          await nextTick()
          twoFactorInput.value?.focus()
        } finally {
          twoFactorLoading.value = false
        }
      }

      const backToCredentials = () => {
        step.value = 'credentials'
        challengeId.value = ''
        twoFactorCode.value = ''
        twoFactorError.value = ''
      }

      onMounted(async () => {
        const googleError = route.query.error
        if (googleError) {
          error.value = t(GOOGLE_ERROR_KEYS[googleError] || 'google_login_error')
          router.replace({ name: 'Login' })
          await focusFirstError()
          return
        }

        // Login con Google, pero el usuario tiene activado el segundo factor:
        // GoogleCallbackView reenvía aquí con el challengeId pendiente de resolver.
        const googleChallengeId = route.query.challengeId
        if (googleChallengeId) {
          challengeId.value = googleChallengeId
          twoFactorCode.value = ''
          twoFactorError.value = ''
          step.value = 'twoFactor'
          router.replace({ name: 'Login' })
          await nextTick()
          twoFactorInput.value?.focus()
        }
      })

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
        handleLogin,
        handleGoogleLogin,
        step,
        twoFactorCode,
        twoFactorError,
        twoFactorLoading,
        twoFactorFormRef,
        twoFactorInput,
        handleTwoFactorSubmit,
        backToCredentials
      }
    }
  }
</script>

<style scoped>
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
    color: var(--primary-color);
  }

  .field-error {
    margin: 0;
    color: var(--danger-color);
    font-size: 0.9rem;
  }

  .login-btn {
    background-color: var(--button-color);
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
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
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
    outline: 3px solid var(--focus-color);
    outline-offset: 2px;
  }

  .google-login {
    display: flex;
    flex-direction: column;
    gap: var(--spacing-md);
  }

  .separator {
    display: flex;
    align-items: center;
    gap: var(--spacing-md);
    color: var(--primary-color);
    font-size: 0.875rem;
  }

  .separator::before,
  .separator::after {
    content: '';
    flex: 1;
    height: 1px;
    background-color: var(--border);
  }

  .separator span {
    flex-shrink: 0;
  }

  .google-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: var(--spacing-sm);
    width: 100%;
    padding: 0.75rem;

    background-color: var(--bg-light);
    color: var(--text-primary);

    border: 1px solid var(--border-medium);
    border-radius: var(--radius-sm);

    font-size: 1rem;
    font-weight: 500;

    cursor: pointer;

    box-shadow: var(--button-shadow);

    transition:
      background-color var(--transition-fast),
      box-shadow var(--transition-fast);
  }

  .google-btn:hover {
    background-color: var(--neutral-50);
    box-shadow: var(--button-shadow-hover);
  }

  .google-btn:active {
    box-shadow: var(--button-shadow-active);
  }

  .google-btn:focus-visible {
    outline: var(--focus-outline);
    outline-offset: var(--focus-outline-offset);
  }

  .google-icon {
    width: 18px;
    height: 18px;
    flex-shrink: 0;
    color: inherit;
  }

  .two-factor-help {
    margin: 0;
    color: var(--text-secondary);
    font-size: 0.9rem;
  }

  .link-btn {
    background: none;
    border: none;
    padding: 0;
    color: var(--primary-color);
    font-size: 0.9rem;
    text-decoration: underline;
    cursor: pointer;
    align-self: center;
  }

  .link-btn:focus-visible {
    outline: 3px solid var(--focus-color);
    outline-offset: 2px;
  }
</style>
