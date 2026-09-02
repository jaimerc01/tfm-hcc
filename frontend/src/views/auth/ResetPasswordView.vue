<template>
  <main class="auth-viewport login-container">
    <section class="login-card" aria-labelledby="reset-title">
      <h1 id="reset-title">{{ $t('reset_password_title') }}</h1>

      <div v-if="!token" class="error-message" role="alert">
        {{ $t('reset_password_link_invalid') }}
        <div class="alt-actions">
          <router-link to="/recuperar-password"><strong>{{ $t('forgot_password_request_new') }}</strong></router-link>
        </div>
      </div>

      <form
        v-else-if="!done"
        @submit.prevent="handleSubmit"
        class="login-form"
        novalidate
        :aria-busy="loading ? 'true' : 'false'"
      >
        <div class="form-group">
          <label for="password">{{ $t('reset_password_new_label') }}</label>
          <input
            id="password"
            ref="passwordInput"
            v-model="password"
            type="password"
            required
            aria-required="true"
            minlength="8"
            autocomplete="new-password"
            :placeholder="$t('reset_password_min_placeholder')"
            :aria-invalid="fieldErrors.password ? 'true' : 'false'"
            :aria-describedby="fieldErrors.password ? 'password-error' : 'password-help'"
            @blur="validatePassword"
          />
          <small id="password-help" class="form-help">{{ $t('reset_password_min_help') }}</small>
          <p v-if="fieldErrors.password" id="password-error" class="field-error">{{ fieldErrors.password }}</p>
        </div>

        <div class="form-group">
          <label for="password2">{{ $t('confirm_password') }}</label>
          <input
            id="password2"
            v-model="password2"
            type="password"
            required
            aria-required="true"
            minlength="8"
            autocomplete="new-password"
            :placeholder="$t('repeat_password_placeholder')"
            :aria-invalid="fieldErrors.password2 ? 'true' : 'false'"
            :aria-describedby="fieldErrors.password2 ? 'password2-error' : undefined"
            @blur="validatePassword2"
          />
          <p v-if="fieldErrors.password2" id="password2-error" class="field-error">{{ fieldErrors.password2 }}</p>
        </div>

        <button type="submit" :disabled="loading" :aria-disabled="loading ? 'true' : 'false'" class="login-btn">
          {{ loading ? $t('reset_password_saving') : $t('reset_password_submit') }}
        </button>

        <div v-if="error" class="error-message" role="alert" aria-live="assertive">
          {{ error }}
          <div class="alt-actions">
            <router-link to="/recuperar-password"><strong>{{ $t('forgot_password_request_new') }}</strong></router-link>
          </div>
        </div>
      </form>

      <div v-else class="info-message" role="status" aria-live="polite">
        {{ $t('reset_password_done') }}
      </div>

      <div v-if="token && !done" class="alt-actions">
        <router-link to="/login"><strong>{{ $t('back_to_login') }}</strong></router-link>
      </div>
    </section>
  </main>
</template>

<script>
  import { ref, nextTick, onMounted } from 'vue'
  import { useRouter, useRoute } from 'vue-router'
  import { useI18n } from 'vue-i18n'

  import authService from '@/services/authService'

  const MIN_PASSWORD_LENGTH = 8

  export default {
    name: 'ResetPasswordView',
    setup() {
      const router = useRouter()
      const route = useRoute()
      const { t } = useI18n()

      const token = ref(route.query.token || '')
      const password = ref('')
      const password2 = ref('')
      const fieldErrors = ref({ password: '', password2: '' })
      const loading = ref(false)
      const done = ref(false)
      const error = ref('')
      const passwordInput = ref(null)

      const validatePassword = () => {
        if (!password.value || password.value.length < MIN_PASSWORD_LENGTH) {
          fieldErrors.value.password = t('reset_password_min_help')
          return false
        }
        fieldErrors.value.password = ''
        return true
      }

      const validatePassword2 = () => {
        if (password.value !== password2.value) {
          fieldErrors.value.password2 = t('passwords_no_match')
          return false
        }
        fieldErrors.value.password2 = ''
        return true
      }

      const handleSubmit = async () => {
        error.value = ''
        const ok = [validatePassword(), validatePassword2()].every(Boolean)
        if (!ok) {
          await nextTick()
          passwordInput.value?.focus()
          return
        }

        loading.value = true
        try {
          await authService.confirmarResetPassword(token.value, password.value)
          done.value = true
          setTimeout(() => router.push({ name: 'Login' }), 1500)
        } catch (err) {
          error.value = err.message || t('auth_generic_retry')
        } finally {
          loading.value = false
        }
      }

      onMounted(() => passwordInput.value?.focus())

      return {
        token,
        password,
        password2,
        fieldErrors,
        loading,
        done,
        error,
        passwordInput,
        validatePassword,
        validatePassword2,
        handleSubmit
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

  .form-help {
    font-size: 0.75rem;
    color: var(--text-secondary);
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

  .info-message {
    color: var(--alert-success-text);
    text-align: left;
    padding: 0.75rem;
    background-color: var(--alert-success-bg);
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
  .alt-actions a:focus-visible {
    outline: 3px solid var(--focus-color);
    outline-offset: 2px;
  }
</style>
