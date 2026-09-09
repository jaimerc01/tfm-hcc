<template>
  <main class="auth-viewport callback-container">
    <section class="callback-card" aria-labelledby="callback-title">
      <h1 id="callback-title" class="sr-only">{{ $t('login_with_google') }}</h1>
      <div class="spinner" aria-hidden="true"></div>
      <p role="status" aria-live="polite">{{ $t('completing_google_login') }}</p>
    </section>
  </main>
</template>

<script>
  import { onMounted } from 'vue'
  import { useRouter, useRoute } from 'vue-router'

  import authService from '@/services/authService'

  export default {
    name: 'GoogleCallbackView',
    setup() {
      const router = useRouter()
      const route = useRoute()

      onMounted(async () => {
        const code = route.query.code

        if (!code) {
          router.replace({ name: 'Login', query: { error: 'google_auth_failed' } })
          return
        }

        try {
          const result = await authService.exchangeGoogleCode(code)

          if (result?.requiresTwoFactor) {
            router.replace({ name: 'Login', query: { challengeId: result.challengeId } })
            return
          }

          router.replace({ name: 'Dashboard' })
        } catch (_err) {
          router.replace({ name: 'Login', query: { error: 'google_auth_failed' } })
        }
      })

      return {}
    }
  }
</script>

<style scoped>
  .callback-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: var(--spacing-md);
    background: var(--card-bg);
    padding: 2rem;
    border-radius: 8px;
    box-shadow: var(--shadow-soft);
    color: var(--text-primary);
  }

  .spinner {
    width: 32px;
    height: 32px;
    border: 3px solid var(--border);
    border-top-color: var(--primary-color);
    border-radius: 50%;
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

  @media (prefers-reduced-motion: reduce) {
    .spinner {
      animation: none;
    }
  }
</style>
