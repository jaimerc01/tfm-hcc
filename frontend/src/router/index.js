import { createRouter, createWebHistory } from 'vue-router'
import authService from '@/services/authService'

// Vistas
import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/auth/LoginView.vue'
import RegisterView from '@/views/auth/RegisterView.vue'
import DashboardView from '@/views/dashboard/DashboardView.vue'
import HistoriaClinicaView from '@/views/HistoriaClinicaView.vue'
import PrivacyPolicyView from '@/views/PrivacyPolicyView.vue'
import UserDataView from '@/views/UserDataView.vue'
import MedicoView from '@/views/MedicoView.vue'
import AdminView from '@/views/AdminView.vue'
import AdminMedicosView from '@/views/AdminMedicosView.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomeView,
    meta: { requiresGuest: true } // Solo para usuarios no autenticados
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { requiresGuest: true } // Solo para usuarios no autenticados
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterView,
    meta: { requiresGuest: true }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: DashboardView,
    meta: { requiresAuth: true } // Requiere autenticación
  },
  {
    path: '/historia-clinica',
    name: 'HistoriaClinica',
    component: HistoriaClinicaView,
    meta: { requiresAuth: true }
  },
  {
    path: '/usuario',
    name: 'UserData',
    component: UserDataView,
    meta: { requiresAuth: true }
  },
  {
    path: '/medico',
    name: 'Medico',
    component: MedicoView,
    meta: { requiresAuth: true, requiresRole: 'MEDICO' }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: AdminView,
    meta: { requiresAuth: true, requiresRole: 'ADMINISTRADOR' }
  },
  {
    path: '/admin/medicos',
    name: 'AdminMedicos',
    component: AdminMedicosView,
    meta: { requiresAuth: true, requiresRole: 'ADMINISTRADOR' }
  },
  {
    path: '/privacidad',
    name: 'PrivacyPolicy',
    component: PrivacyPolicyView,
    meta: { requiresAuth: true }
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Guard de navegación
router.beforeEach((to, from, next) => {
  const isAuth = authService.isAuthenticated()
  const claims = authService.getCurrentUser() || {}
  const roles = (claims && (claims.authorities || claims.roles)) || []

  if (to.meta.requiresAuth && !isAuth) {
    return next({ name: 'Login' })
  }

  if (to.meta.requiresGuest && isAuth) {
    return next({ name: 'Dashboard' })
  }

  if (to.meta.requiresRole) {
    const required = String(to.meta.requiresRole)
    const has = Array.isArray(roles)
      ? roles.some(r => String(r).toUpperCase().includes(required.toUpperCase()))
      : false
    if (!has) {
      return next({ name: 'Dashboard' })
    }
  }

  next()
})

export default router
