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
        ☰
      </button>

      <div id="main-nav-links" class="nav__links" :class="{ 'nav__links--open': open }">
        <notifications-dropdown />
        <router-link class="nav__link" :to="{ name: 'Dashboard' }" @click="closeAll">{{ $t('home') }}</router-link>
        <router-link class="nav__link" :to="{ name: 'HistoriaClinica' }" @click="closeAll">{{ $t('medical_history') }}</router-link>
        <router-link v-if="isMedico" class="nav__link" :to="{ name: 'Medico' }" @click="closeAll">{{ $t('medical_zone') }}</router-link>
        <router-link v-if="isPaciente" class="nav__link" :to="{ name: 'MisSolicitudes' }" @click="closeAll">{{ $t('my_requests') }}</router-link>

        <div v-if="isAdmin" class="nav__dropdown">
          <router-link class="nav__link" :to="{ name: 'Admin' }" @click="closeAll">{{ $t('admin') }}</router-link>
          <button
            type="button"
            class="nav__dropdown-toggle"
            @click="toggleAdminMenu"
            :aria-expanded="openAdminMenu ? 'true' : 'false'"
            aria-controls="admin-submenu"
          >
            {{ $t('manage_doctors') }}
          </button>
          <div v-if="openAdminMenu" id="admin-submenu" class="nav__dropdown-content" role="menu">
            <router-link class="nav__link" role="menuitem" :to="{ name: 'AdminMedicos' }" @click="closeAll">{{ $t('manage_doctors') }}</router-link>
          </div>
        </div>

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

  <div v-if="showLogoutModal" class="logout-modal-overlay" @click="closeLogoutModal">
    <div
      ref="logoutDialog"
      class="logout-modal"
      role="dialog"
      aria-modal="true"
      aria-labelledby="logout-modal-title"
      @click.stop
      @keydown.esc.prevent="closeLogoutModal"
      tabindex="-1"
    >
      <div class="logout-modal-header">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="logout-icon" aria-hidden="true" focusable="false">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
          <polyline points="16 17 21 12 16 7"></polyline>
          <line x1="21" y1="12" x2="9" y2="12"></line>
        </svg>
        <h3 id="logout-modal-title">{{ $t('logout') }}</h3>
      </div>
      <p class="logout-modal-text">{{ $t('logout_confirm') }}</p>
      <div class="logout-modal-actions">
        <button type="button" @click="confirmLogout" class="btn-confirm">{{ $t('logout_yes') }}</button>
        <button type="button" @click="closeLogoutModal" class="btn-cancel">{{ $t('cancel') }}</button>
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

export default {
  name: 'AppNavbar',
  components: { NotificationsDropdown },
  setup() {
    const open = ref(false)
    const openAdminMenu = ref(false)
    const showLogoutModal = ref(false)
    const logoutDialog = ref(null)

    const router = useRouter()
    const { logout } = useAuth()
    const { isMedico, isAdmin, isPaciente } = useRole()
    const { locale } = useI18n()

    const close = () => {
      open.value = false
      openAdminMenu.value = false
    }

    const closeAll = () => close()

    const toggleMenu = () => {
      open.value = !open.value
      if (!open.value) openAdminMenu.value = false
    }

    const toggleAdminMenu = () => {
      openAdminMenu.value = !openAdminMenu.value
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
      openAdminMenu,
      close,
      closeAll,
      toggleMenu,
      toggleAdminMenu,
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
  background: var(--danger-color);
  color: var(--text-inverse);
  border: none;
  padding: 0.5rem 0.75rem;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.15s ease;
}

.nav__logout:hover {
  background: var(--danger-hover);
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

.nav__dropdown {
  position: relative;
  display: inline-block;
}

.nav__dropdown-toggle {
  background: transparent;
  color: var(--white-alpha-90);
  border: 1px solid transparent;
  border-radius: 4px;
  padding: 0.5rem 0.75rem;
  cursor: pointer;
  font-weight: 500;
}

.nav__dropdown-toggle:hover {
  color: var(--text-inverse);
  background: var(--primary-hover);
}

.nav__dropdown-content {
  position: absolute;
  background: var(--primary-hover);
  min-width: 180px;
  z-index: 1;
  border-radius: 4px;
  box-shadow: var(--shadow-md);
  overflow: hidden;
}

.nav__dropdown-content .nav__link {
  display: block;
  padding: 0.75rem 1rem;
  border-radius: 0;
}

.nav__dropdown-content .nav__link:hover {
  background: var(--primary-active);
}

.nav__toggle:focus-visible,
.nav__link:focus-visible,
.nav__logout:focus-visible,
.nav__language-option:focus-visible,
.nav__dropdown-toggle:focus-visible,
.btn-confirm:focus-visible,
.btn-cancel:focus-visible {
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

  .nav__dropdown-content {
    position: static;
    min-width: 100%;
    box-shadow: none;
    margin-top: 0.25rem;
  }
}

.logout-modal-overlay {
  position: fixed;
  inset: 0;
  background: var(--surface-overlay);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

.logout-modal {
  background: var(--card-bg);
  border-radius: 12px;
  padding: 2rem;
  max-width: 400px;
  width: 90%;
  box-shadow: var(--shadow-xl);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.logout-modal-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.logout-icon {
  color: var(--danger-color);
}

.logout-modal-header h3 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
}

.logout-modal-text {
  text-align: center;
  color: var(--text-secondary);
  margin-bottom: 2rem;
  font-size: 1rem;
}

.logout-modal-actions {
  display: flex;
  gap: 0.75rem;
  flex-direction: column;
}

@media (min-width: 400px) {
  .logout-modal-actions {
    flex-direction: row;
  }
}

.btn-confirm,
.btn-cancel {
  flex: 1;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-confirm {
  background: var(--danger-color);
  color: var(--text-inverse);
}

.btn-confirm:hover {
  background: var(--danger-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-lg);
}

.btn-cancel {
  background: var(--bg-light);
  color: var(--text-primary);
  border: 1.5px solid var(--border);
}

.btn-cancel:hover {
  background: var(--border);
}

@media (prefers-reduced-motion: reduce) {
  .logout-modal-overlay,
  .logout-modal,
  .btn-confirm,
  .btn-cancel,
  .nav__link,
  .nav__toggle,
  .nav__logout,
  .nav__dropdown-toggle,
  .nav__language-option {
    animation: none;
    transition: none;
  }
}
</style>
