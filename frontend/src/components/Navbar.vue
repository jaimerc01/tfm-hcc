<template>
  <nav class="nav">
    <div class="nav__content">
      <router-link class="nav__brand" :to="{ name: 'Dashboard' }">HCC</router-link>

      <button class="nav__toggle" @click="open = !open" :aria-label="$t('menu')">
        ☰
      </button>

      <div class="nav__links" :class="{ 'nav__links--open': open }">
      <notifications-dropdown />
          <router-link class="nav__link" :to="{ name: 'Dashboard' }" @click="close">{{ $t('home') }}</router-link>
        <router-link class="nav__link" :to="{ name: 'HistoriaClinica' }" @click="close">{{ $t('medical_history') }}</router-link>
        <router-link v-if="isMedico" class="nav__link" :to="{ name: 'Medico' }" @click="close">{{ $t('medical_zone') }}</router-link>
        <router-link v-if="isPaciente" class="nav__link" :to="{ name: 'MisSolicitudes' }" @click="close">{{ $t('my_requests') }}</router-link>
  <div v-if="isAdmin" class="nav__dropdown">
    <router-link class="nav__link" :to="{ name: 'Admin' }" @click="close">{{ $t('admin') }}</router-link>
    <div class="nav__dropdown-content">
      <router-link class="nav__link" :to="{ name: 'AdminMedicos' }" @click="close">{{ $t('manage_doctors') }}</router-link>
    </div>
  </div>
          <router-link class="nav__link" :to="{ name: 'UserData' }" @click="close">{{ $t('user_data') }}</router-link>
        <router-link class="nav__link" :to="{ name: 'PrivacyPolicy' }" @click="close">{{ $t('privacy') }}</router-link>

        <button class="nav__logout" @click="showLogoutModal = true">{{ $t('logout') }}</button>
      </div>
    </div>
  </nav>
  <div class="nav__spacer" />
  <div v-if="open" class="nav__backdrop" @click="close" />
  
  <!-- Modal de confirmación de logout -->
  <div v-if="showLogoutModal" class="logout-modal-overlay" @click="showLogoutModal = false">
    <div class="logout-modal" @click.stop>
      <div class="logout-modal-header">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="logout-icon">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
          <polyline points="16 17 21 12 16 7"></polyline>
          <line x1="21" y1="12" x2="9" y2="12"></line>
        </svg>
        <h3>{{ $t('logout') }}</h3>
      </div>
      <p class="logout-modal-text">{{ $t('logout_confirm') }}</p>
      <div class="logout-modal-actions">
        <button @click="confirmLogout" class="btn-confirm">{{ $t('logout_yes') }}</button>
        <button @click="showLogoutModal = false" class="btn-cancel">{{ $t('cancel') }}</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { useI18n } from 'vue-i18n'
import authService from '@/services/authService'
import NotificationsDropdown from '@/components/NotificationsDropdown.vue'

export default {
  name: 'AppNavbar',
  components: { NotificationsDropdown },
  setup() {
    const open = ref(false)
    const showLogoutModal = ref(false)
    const router = useRouter()
    const { logout } = useAuth()
    const isMedico = computed(() => {
      const claims = authService.getCurrentUser() || {}
      const roles = claims.authorities || claims.roles || []
      return Array.isArray(roles) && roles.some(r => String(r).toUpperCase().includes('MEDICO'))
    })
    const { t } = useI18n()
    const isAdmin = computed(() => {
      const claims = authService.getCurrentUser() || {}
      const roles = claims.authorities || claims.roles || []
      return Array.isArray(roles) && roles.some(r => String(r).toUpperCase().includes('ADMINISTRADOR'))
    })
    const isPaciente = computed(() => {
      const claims = authService.getCurrentUser() || {}
      const roles = claims.authorities || claims.roles || []
      return Array.isArray(roles) && roles.some(r => String(r).toUpperCase().includes('PACIENTE'))
    })

    const close = () => { open.value = false }
    
    const confirmLogout = async () => {
      showLogoutModal.value = false
      await logout()
      close()
      router.push({ name: 'Login' })
    }

  return { open, close, showLogoutModal, confirmLogout, isMedico, isAdmin, isPaciente, t }
  }
}
</script>

<style scoped>
.nav { 
  position: fixed; 
  top: 0; 
  left: 0; 
  right: 0; 
  background: var(--primary-color, #0284c7); 
  color: #fff; 
  z-index: 1000; 
  box-shadow: var(--shadow-sm, 0 1px 2px 0 rgb(0 0 0 / 0.05));
}

.nav__content { 
  display: flex; 
  align-items: center; 
  justify-content: space-between; 
  padding: 0.75rem 1rem; 
}

.nav__brand { 
  color: #fff; 
  text-decoration: none; 
  font-weight: 600; 
  font-size: 1.1rem; 
}

.nav__brand:hover {
  color: var(--primary-light, #bae6fd);
}

.nav__toggle { 
  display: none; 
  background: transparent; 
  border: none; 
  color: #fff; 
  font-size: 1.25rem; 
  cursor: pointer; 
  padding: 0.25rem;
  border-radius: 4px;
  transition: background-color 0.15s ease;
}

.nav__toggle:hover {
  background: var(--primary-hover, #0369a1);
}

.nav__links { 
  display: flex; 
  gap: 1rem; 
  align-items: center; 
  flex-wrap: wrap; 
}

.nav__link { 
  color: rgba(255, 255, 255, 0.9); 
  text-decoration: none; 
  padding: 0.5rem 0.75rem;
  border-radius: 4px;
  transition: all 0.15s ease;
  font-weight: 500;
}

.nav__link:hover {
  color: #fff;
  background: var(--primary-hover, #0369a1);
}

.nav__link.router-link-exact-active { 
  color: #fff;
  background: var(--primary-active, #075985);
}

.nav__logout { 
  margin-left: 0.5rem; 
  background: var(--danger-color, #dc2626); 
  color: #fff; 
  border: none; 
  padding: 0.5rem 0.75rem; 
  border-radius: 4px; 
  cursor: pointer; 
  font-weight: 500;
  transition: background-color 0.15s ease;
}

.nav__logout:hover { 
  background: var(--danger-hover, #b91c1c); 
}

.nav__spacer { 
  height: 56px; 
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
    background: var(--primary-color, #0284c7); 
    flex-direction: column; 
    padding: 0.75rem 1rem; 
    display: none; 
    border-top: 1px solid var(--primary-hover, #0369a1);
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
    background: rgba(0,0,0,0.25); 
  }
}

/* Dropdown styles */
.nav__dropdown { 
  position: relative; 
  display: inline-block; 
}

.nav__dropdown-content { 
  display: none; 
  position: absolute; 
  background: var(--primary-hover, #0369a1); 
  min-width: 180px; 
  z-index: 1; 
  border-radius: 4px;
  box-shadow: var(--shadow-md, 0 1px 3px 0 rgb(0 0 0 / 0.1));
  overflow: hidden;
}

.nav__dropdown:hover .nav__dropdown-content { 
  display: block; 
}

.nav__dropdown-content .nav__link {
  display: block;
  padding: 0.75rem 1rem;
  border-radius: 0;
}

.nav__dropdown-content .nav__link:hover {
  background: var(--primary-active, #075985);
}

/* Modal de logout */
.logout-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.logout-modal {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1);
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
  color: var(--danger-color, #dc2626);
}

.logout-modal-header h3 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary, #262626);
}

.logout-modal-text {
  text-align: center;
  color: var(--text-secondary, #737373);
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
  background: var(--danger-color, #dc2626);
  color: white;
}

.btn-confirm:hover {
  background: #b91c1c;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1);
}

.btn-cancel {
  background: var(--bg-light, #fafafa);
  color: var(--text-primary, #262626);
  border: 1.5px solid var(--border, #e5e5e5);
}

.btn-cancel:hover {
  background: var(--border, #e5e5e5);
}
</style>
.nav__dropdown-content .nav__link { display: block; padding: 0.5rem 1rem; color: #ecf0f1; }
.nav__dropdown-content .nav__link:hover { background: #22313a; color: #42b983; }
