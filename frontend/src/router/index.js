import { createRouter, createWebHistory } from 'vue-router'
import authService from '@/services/authService'

// Vistas
import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/auth/LoginView.vue'
import DashboardView from '@/views/dashboard/DashboardView.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomeView
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { requiresGuest: true } // Solo para usuarios no autenticados
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: DashboardView,
    meta: { requiresAuth: true } // Requiere autenticación
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Guard de navegación
router.beforeEach((to, from, next) => {
  const isAuth = authService.isAuthenticated()

  if (to.meta.requiresAuth && !isAuth) {
    return next({ name: 'Login' })
  }

  if (to.meta.requiresGuest && isAuth) {
    return next({ name: 'Dashboard' })
  }

  next()
})

export default router
