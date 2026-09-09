<template>
  <footer class="app-footer">
    <div class="app-footer__content">
      <router-link class="app-footer__link" :to="{ name: 'PoliticaPrivacidad' }">{{ $t('privacy') }}</router-link>
      <button
        type="button"
        class="app-footer__button"
        @click="$router.push({ name: 'PoliticaCookies' })"
      >
        {{ $t('cookies_policy') }}
      </button>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  margin-top: 2.5rem;
  width: 100%;
  border-top: 1px solid var(--footer-border);
  background: var(--footer-bg);
  box-shadow: var(--shadow-sm);
}

.app-footer__content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0.95rem 1.25rem;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 1rem;
}

.app-footer__link {
  color: var(--white-alpha-90);
  font-size: 0.875rem;
  text-decoration: none;
  font-weight: 600;
  border-radius: var(--radius-sm);
  padding: 0.35rem 0.45rem;
  transition: color var(--transition-fast), background-color var(--transition-fast);
}

.app-footer__button {
  border: 0;
  background: transparent;
  color: var(--white-alpha-90);
  font-size: 0.875rem;
  font-weight: 600;
  border-radius: var(--radius-sm);
  padding: 0.35rem 0.45rem;
  cursor: pointer;
  transition: color var(--transition-fast), background-color var(--transition-fast);
}

.app-footer__link:hover,
.app-footer__link:focus-visible,
.app-footer__button:hover,
.app-footer__button:focus-visible {
  color: var(--text-inverse);
  background: var(--primary-hover);
  text-decoration: none;
}

.app-footer__link:focus-visible,
.app-footer__button:focus-visible {
  outline: 3px solid var(--focus-color);
  outline-offset: 2px;
}

@media (prefers-reduced-motion: reduce) {
  .app-footer__link,
  .app-footer__button {
    transition: none;
  }
}

@media (max-width: 640px) {
  .app-footer__content {
    padding: 0.85rem 1rem;
    justify-content: center;
  }
}
</style>
