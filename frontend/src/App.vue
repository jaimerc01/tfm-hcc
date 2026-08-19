<template>
  <div id="app">
    <div v-if="!showNavbar" class="language-switch" :aria-label="$t('language_selector_aria')">
      <button
        type="button"
        class="language-switch__option"
        :class="{ 'is-active': currentLocale === 'es' }"
        @click="setLanguage('es')"
      >
        {{ $t('language_spanish') }}
      </button>
      <span class="language-switch__separator">|</span>
      <button
        type="button"
        class="language-switch__option"
        :class="{ 'is-active': currentLocale === 'gl' }"
        @click="setLanguage('gl')"
      >
        {{ $t('language_galician') }}
      </button>
    </div>
    <AppNavbar v-if="showNavbar" />
    <router-view />
    <AppFooter v-if="showNavbar" />
  </div>
</template>

<script>
import AppNavbar from '@/components/Navbar.vue'
import AppFooter from '@/components/AppFooter.vue'
export default {
  name: 'App',
  components: { AppNavbar, AppFooter },
  computed: {
    showNavbar() {
      const meta = this.$route && this.$route.meta ? this.$route.meta : {}
      // Ocultar en rutas de invitado (p. ej., login)
      return !meta.requiresGuest
    },
    currentLocale() {
      const locale = this.$i18n.locale
      return typeof locale === 'object' && locale !== null && 'value' in locale ? locale.value : locale
    }
  },
  methods: {
    setLanguage(locale) {
      if (this.currentLocale === locale) {
        return
      }

      const currentLocale = this.$i18n.locale
      if (typeof currentLocale === 'object' && currentLocale !== null && 'value' in currentLocale) {
        currentLocale.value = locale
      } else {
        this.$i18n.locale = locale
      }
      document.documentElement.lang = locale
      localStorage.setItem('app-locale', locale)
      window.location.reload()
    }
  }
}
</script>

<style>
/* Import global styles */
@import './styles/variables.css';
@import './styles/components.css';
@import './styles/layout.css';
@import './styles/forms.css';
@import './styles/shared.css';

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: var(--text-primary);
}

.language-switch {
  position: fixed;
  top: 0.75rem;
  right: 1rem;
  z-index: 1200;

  display: inline-flex;
  align-items: center;
  gap: 0.15rem;

  padding: 0.25rem;

  background: var(--bg-light);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.language-switch__option {
  border: none;
  border-radius: var(--radius-sm);

  background: transparent;
  color: var(--text-secondary);

  font-size: 0.85rem;
  font-weight: 600;

  cursor: pointer;

  padding: 0.4rem 0.65rem;

  transition:
    background-color var(--transition-fast),
    color var(--transition-fast),
    box-shadow var(--transition-fast);
}

.language-switch__option:hover {
  background-color: var(--primary-light);
  color: var(--primary-color);
}

.language-switch__option.is-active {
  background-color: var(--primary-color);
  color: var(--on-primary);
  box-shadow: var(--button-shadow);
  text-decoration: none;
}

.language-switch__option:focus-visible {
  outline: var(--focus-outline);
  outline-offset: var(--focus-outline-offset);
}

.language-switch__separator {
  display: none;
}

@media (max-width: 640px) {
  .language-switch {
    right: 0.5rem;
    top: 0.5rem;
  }

}
</style>
