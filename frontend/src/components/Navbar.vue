<template>
  <nav class="nav" :aria-label="$t('main_navigation_aria')">
    <div class="nav__content">
      <span class="nav__brand" aria-label="HCC">HCC</span>

      <button
        type="button"
        class="nav__toggle"
        @click="toggleMenu"
        :aria-label="$t('menu')"
        :aria-expanded="open ? 'true' : 'false'"
        aria-controls="main-nav-links"
      >
        <AppIcon name="menu" size="xl" />
      </button>

      <div id="main-nav-links" class="nav__links" :class="{ 'nav__links--open': open }">
        <notifications-dropdown />
        <router-link class="nav__link" :to="{ name: 'Dashboard' }" @click="closeAll">{{ $t('home') }}</router-link>
        <router-link class="nav__link" :to="{ name: 'HistoriaClinica' }" @click="closeAll">{{ $t('medical_history') }}</router-link>
        <router-link v-if="isMedico" class="nav__link" :to="{ name: 'Medico' }" @click="closeAll">{{ $t('medical_zone') }}</router-link>
        <router-link v-if="isPaciente || isMedico" class="nav__link" :to="{ name: 'MisSolicitudes' }" @click="closeAll">{{ $t('my_requests') }}</router-link>

        <router-link v-if="isAdmin" class="nav__link" :to="{ name: 'Admin' }" @click="closeAll">{{ $t('admin') }}</router-link>

        <router-link class="nav__link" :to="{ name: 'DatosUsuario' }" @click="closeAll">{{ $t('user_data') }}</router-link>

        <button type="button" class="nav__logout" @click="openLogoutModal">{{ $t('logout') }}</button>

        <div class="nav__language-switch" role="group" :aria-label="$t('language_selector_aria')">
          <button
            type="button"
            class="nav__language-option"
            :class="{ 'is-active': currentLocale === 'es' }"
            :aria-pressed="currentLocale === 'es' ? 'true' : 'false'"
            @click="setLanguage('es')"
          >
            {{ $t('language_spanish') }}
          </button>
          <span class="nav__language-separator">|</span>
          <button
            type="button"
            class="nav__language-option"
            :class="{ 'is-active': currentLocale === 'gl' }"
            :aria-pressed="currentLocale === 'gl' ? 'true' : 'false'"
            @click="setLanguage('gl')"
          >
            {{ $t('language_galician') }}
          </button>
        </div>
      </div>
    </div>
  </nav>

  <div class="nav__spacer" />
  <div v-if="open" class="nav__backdrop" @click="closeAll" />

  <div v-if="showLogoutModal" class="modal-overlay" @click="closeLogoutModal">
    <div
      ref="logoutDialog"
      class="modal"
      role="dialog"
      aria-modal="true"
      aria-labelledby="logout-modal-title"
      @click.stop
      @keydown.esc.prevent="closeLogoutModal"
      tabindex="-1"
    >
      <div class="modal-header">
        <AppIcon name="log-out" size="3xl" class="logout-icon" />
        <h3 id="logout-modal-title">{{ $t('logout') }}</h3>
      </div>
      <p class="modal-text">{{ $t('logout_confirm') }}</p>
      <div class="modal-actions">
        <button type="button" @click="confirmLogout" class="modal-btn modal-btn--confirm">{{ $t('logout_yes') }}</button>
        <button type="button" @click="closeLogoutModal" class="modal-btn modal-btn--cancel">{{ $t('cancel') }}</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch, nextTick, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { useRole } from '@/composables/useRole'
import { useI18n } from 'vue-i18n'
import NotificationsDropdown from '@/components/NotificationsDropdown.vue'
import AppIcon from '@/components/AppIcon.vue'

export default {
  name: 'AppNavbar',
  components: { NotificationsDropdown, AppIcon },
  setup() {
    const open = ref(false)
    const showLogoutModal = ref(false)
    const logoutDialog = ref(null)

    const router = useRouter()
    const { logout } = useAuth()
    const { isMedico, isAdmin, isPaciente } = useRole()
    const { locale } = useI18n()

    const close = () => {
      open.value = false
    }

    const closeAll = () => close()

    const toggleMenu = () => {
      open.value = !open.value
    }

    const openLogoutModal = () => {
      showLogoutModal.value = true
    }

    const closeLogoutModal = () => {
      showLogoutModal.value = false
    }

    const currentLocale = computed(() => locale.value)

    const setLanguage = newLocale => {
      if (currentLocale.value === newLocale) {
        return
      }

      locale.value = newLocale
      document.documentElement.lang = newLocale
      localStorage.setItem('app-locale', newLocale)
      window.location.reload()
    }

    const confirmLogout = async () => {
      closeLogoutModal()
      await logout()
      close()
      router.push({ name: 'Login' })
    }

    watch(showLogoutModal, async isOpen => {
      document.body.style.overflow = isOpen ? 'hidden' : ''
      if (isOpen) {
        await nextTick()
        if (logoutDialog.value && typeof logoutDialog.value.focus === 'function') {
          logoutDialog.value.focus()
        }
      }
    })

    onBeforeUnmount(() => {
      document.body.style.overflow = ''
    })

    return {
      open,
      close,
      closeAll,
      toggleMenu,
      showLogoutModal,
      logoutDialog,
      openLogoutModal,
      closeLogoutModal,
      confirmLogout,
      isMedico,
      isAdmin,
      isPaciente,
      currentLocale,
      setLanguage
    }
  }
}
</script>

<style scoped>
.nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: var(--primary-color);
  color: var(--text-inverse);
  z-index: 1000;
  box-shadow: var(--shadow-sm);
}

.nav__content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1rem;
}

.nav__brand {
  color: var(--text-inverse);
  text-decoration: none;
  font-weight: 600;
  font-size: 1.1rem;
}

.nav__brand:hover {
  color: var(--primary-light);
}

.nav__toggle {
  display: none;
  background: transparent;
  border: none;
  color: var(--text-inverse);
  font-size: 1.25rem;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: background-color 0.15s ease;
}

.nav__toggle:hover {
  background: var(--primary-hover);
}

.nav__links {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex-wrap: wrap;
}

.nav__link {
  color: var(--white-alpha-90);
  text-decoration: none;
  padding: 0.5rem 0.75rem;
  border-radius: 4px;
  transition: all 0.15s ease;
  font-weight: 500;
}

.nav__link:hover {
  color: var(--text-inverse);
  background: var(--primary-hover);
}

.nav__link.router-link-exact-active {
  color: var(--text-inverse);
  background: var(--primary-active);
}

.nav__logout {
  margin-left: 0.5rem;
  background: var(--button-color);
  color: var(--on-button);
  border: none;
  padding: 0.5rem 0.75rem;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.15s ease;
}

.nav__logout:hover {
  background: var(--button-hover);
}

.nav__language-switch {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

.nav__language-option {
  border: none;
  background: transparent;
  color: var(--white-alpha-90);
  cursor: pointer;
  font-weight: 500;
  padding: 0.25rem;
  border-radius: 4px;
}

.nav__language-option:hover {
  color: var(--text-inverse);
  background: var(--primary-hover);
}

.nav__language-option.is-active {
  color: var(--text-inverse);
  text-decoration: underline;
}

.nav__language-separator {
  color: var(--white-alpha-90);
  font-size: 0.8rem;
}

.nav__spacer {
  height: 56px;
}

.nav__toggle:focus-visible,
.nav__link:focus-visible,
.nav__logout:focus-visible,
.nav__language-option:focus-visible {
  outline: 3px solid var(--focus-color);
  outline-offset: 2px;
}

@media (max-width: 768px) {
  .nav__toggle {
    display: inline-block;
  }

  .nav__links {
    position: absolute;
    top: 56px;
    left: 0;
    right: 0;
    background: var(--primary-color);
    flex-direction: column;
    padding: 0.75rem 1rem;
    display: none;
    border-top: 1px solid var(--primary-hover);
  }

  .nav__language-switch {
    width: fit-content;
    margin: 0.25rem 0;
  }

  .nav__links--open {
    display: flex;
  }

  .nav__spacer {
    height: 56px;
  }

  .nav__backdrop {
    position: fixed;
    inset: 56px 0 0 0;
    background: var(--surface-overlay-medium);
  }
}

.logout-icon {
  color: var(--danger-color);
}

@media (prefers-reduced-motion: reduce) {
  .nav__link,
  .nav__toggle,
  .nav__logout,
  .nav__language-option {
    animation: none;
    transition: none;
  }
}
</style>
