<template>
  <div class="dashboard">
    <h1>{{$t('dashboard_title')}}</h1>
    <p v-if="welcomeName">{{$t('hello')}} {{ welcomeName }}</p>
    <p v-else>{{$t('welcome_login_success')}}</p>
    <div class="user-info">
      <p>{{$t('authenticated_user')}}</p>
      <button @click="showLogoutModal = true" class="logout-btn">
        {{$t('logout_button')}}
      </button>
    </div>
  </div>

  <!-- Modal de confirmación de logout -->
  <div v-if="showLogoutModal" class="logout-modal-overlay" @click="showLogoutModal = false">
    <div class="logout-modal" @click.stop>
      <div class="logout-modal-header">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="logout-icon">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
          <polyline points="16 17 21 12 16 7"></polyline>
          <line x1="21" y1="12" x2="9" y2="12"></line>
        </svg>
        <h3>{{$t('logout_confirm_title')}}</h3>
      </div>
      <p class="logout-modal-text">{{$t('logout_confirm_text')}}</p>
      <div class="logout-modal-actions">
        <button @click="confirmLogout" class="btn-confirm">{{$t('logout_confirm_button')}}</button>
        <button @click="showLogoutModal = false" class="btn-cancel">{{$t('logout_cancel_button')}}</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useAuth } from '@/composables/useAuth'
import { useRouter } from 'vue-router'
import authService from '@/services/authService'

export default {
  name: 'DashboardView',
  setup() {
    const { logout } = useAuth()
    const router = useRouter()
    const welcomeName = ref('')
    const loading = ref(false)
    const loadError = ref('')
    const showLogoutModal = ref(false)
    
    const confirmLogout = async () => {
      showLogoutModal.value = false
      await logout()
      router.push('/login')
    }

    const loadWelcome = async () => {
      try {
        loading.value = true
        // 1) Fallback inmediato desde el JWT (sub = nif, o nombre/name si existiera)
        const claims = authService.getCurrentUser()
        if (claims) {
          const maybeName = claims.nombre || claims.name || claims.sub || ''
          if (maybeName && !welcomeName.value) welcomeName.value = maybeName
        }
        // 2) Intentar cargar el nombre real desde el backend si existe el endpoint
        const name = await authService.fetchMyName()
        if (name) welcomeName.value = name
      } catch (e) {
        loadError.value = 'No se pudo cargar el nombre'
      } finally {
        loading.value = false
      }
    }

    onMounted(loadWelcome)
    
    return {
      showLogoutModal,
      confirmLogout,
      welcomeName,
      loading,
      loadError
    }
  }
}
</script>

<style scoped>
.logout-btn { background-color:#e74c3c; color:white; padding:.75rem 1.5rem; border:none; border-radius:4px; cursor:pointer; font-size:1rem }
.logout-btn:hover { background-color:#c0392b }

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
