<template>
  <main class="auth-viewport login-container">
    <section class="login-card" aria-labelledby="forgot-title">
      <h1 id="forgot-title">{{ $t('forgot_password_title') }}</h1>

      <p v-if="!sent" class="forgot-help">{{ $t('forgot_password_help') }}</p>

      <form v-if="!sent" @submit.prevent="handleSubmit" class="login-form" novalidate :aria-busy="loading ? 'true' : 'false'">
        <div class="form-group">
          <label for="email">{{ $t('email_label') }}</label>
          <input
            id="email"
            ref="emailInput"
            v-model.trim="email"
            type="email"
            required
            aria-required="true"
            autocomplete="email"
            :placeholder="$t('email_placeholder')"
            :aria-invalid="fieldError ? 'true' : 'false'"
            :aria-describedby="fieldError ? 'email-error' : undefined"
            @blur="validateEmail"
          />
          <p v-if="fieldError" id="email-error" class="field-error">{{ fieldError }}</p>
        </div>

        <button type="submit" :disabled="loading" :aria-disabled="loading ? 'true' : 'false'" class="login-btn">
          {{ loading ? $t('forgot_password_sending') : $t('forgot_password_submit') }}
        </button>
      </form>

      <div v-else class="info-message" role="status" aria-live="polite">
        {{ $t('forgot_password_sent') }}
      </div>

      <div class="alt-actions">
        <router-link to="/login"><strong>{{ $t('back_to_login') }}</strong></router-link>
      </div>
    </section>
  </main>
</template>

<script>
  import { ref, nextTick, onMounted } from 'vue'
  import { useI18n } from 'vue-i18n'

  import authService from '@/services/authService'

  export default {
    name: 'ForgotPasswordView',
    setup() {
      const { t } = useI18n()

      const email = ref('')
      const fieldError = ref('')
      const loading = ref(false)
      const sent = ref(false)
      const emailInput = ref(null)

      const validateEmail = () => {
        if (!email.value || !email.value.includes('@')) {
          fieldError.value = t('email_required')
          return false
        }
        fieldError.value = ''
        return true
      }

      const handleSubmit = async () => {
        if (!validateEmail()) {
          await nextTick()
          emailInput.value?.focus()
          return
        }

        loading.value = true
        try {
          await authService.solicitarResetPassword(email.value)
          // Respuesta deliberadamente neutra: no se confirma si el correo existe.
          sent.value = true
        } catch (err) {
          fieldError.value = err.message || t('auth_generic_retry')
        } finally {
          loading.value = false
        }
      }

      onMounted(() => emailInput.value?.focus())

      return { email, fieldError, loading, sent, emailInput, validateEmail, handleSubmit }
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

  .forgot-help {
    margin: 0 0 1rem 0;
    color: var(--text-secondary);
    font-size: 0.9rem;
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
