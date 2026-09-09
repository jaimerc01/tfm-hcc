import { createRouter, createWebHistory } from 'vue-router'
import authService from '@/services/authService'
import { ROLES, hasRole } from '@/utils/roles'

// Vistas
import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/auth/LoginView.vue'
import RegisterView from '@/views/auth/RegisterView.vue'
import GoogleCallbackView from '@/views/auth/GoogleCallbackView.vue'
import ForgotPasswordView from '@/views/auth/ForgotPasswordView.vue'
import ResetPasswordView from '@/views/auth/ResetPasswordView.vue'
import DashboardView from '@/views/dashboard/DashboardView.vue'
import HistoriaClinicaView from '@/views/HistoriaClinicaView.vue'
import PoliticaPrivacidadView from '@/views/PoliticaPrivacidadView.vue'
import PoliticaCookiesView from '@/views/PoliticaCookiesView.vue'
import DatosUsuarioView from '@/views/DatosUsuarioView.vue'
import MedicoView from '@/views/MedicoView.vue'
import PacienteHistorialView from '@/views/medico/PacienteHistorialView.vue'
import AdminView from '@/views/AdminView.vue'
import AdminMedicosView from '@/views/AdminMedicosView.vue'
import MisSolicitudesView from '@/views/MisSolicitudesView.vue'

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
    path: '/auth/google/callback',
    name: 'GoogleCallback',
    component: GoogleCallbackView
  },
  {
    path: '/recuperar-password',
    name: 'RecuperarPassword',
    component: ForgotPasswordView,
    meta: { requiresGuest: true }
  },
  {
    path: '/restablecer-password',
    name: 'RestablecerPassword',
    component: ResetPasswordView,
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
    name: 'DatosUsuario',
    component: DatosUsuarioView,
    meta: { requiresAuth: true }
  },
  {
    path: '/mis-solicitudes',
    name: 'MisSolicitudes',
    component: MisSolicitudesView,
    meta: { requiresAuth: true }
  },
  {
    path: '/medico',
    name: 'Medico',
    component: MedicoView,
    meta: { requiresAuth: true, requiresRole: ROLES.MEDICO }
  },
  {
    path: '/medico/pacientes/:nif/historial',
    name: 'MedicoPacienteHistorial',
    component: PacienteHistorialView,
    meta: { requiresAuth: true, requiresRole: ROLES.MEDICO }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: AdminView,
    meta: { requiresAuth: true, requiresRole: ROLES.ADMINISTRADOR }
  },
  {
    path: '/admin/medicos',
    name: 'AdminMedicos',
    component: AdminMedicosView,
    meta: { requiresAuth: true, requiresRole: ROLES.ADMINISTRADOR }
  },
  {
    path: '/privacidad',
    name: 'PoliticaPrivacidad',
    component: PoliticaPrivacidadView
  },
  {
    path: '/cookies',
    name: 'PoliticaCookies',
    component: PoliticaCookiesView
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

  if (to.meta.requiresAuth && !isAuth) {
    return next({ name: 'Login' })
  }

  if (to.meta.requiresGuest && isAuth) {
    return next({ name: 'Dashboard' })
  }

  if (to.meta.requiresRole && !hasRole(claims, to.meta.requiresRole)) {
    return next({ name: 'Dashboard' })
  }

  next()
})

export default router
