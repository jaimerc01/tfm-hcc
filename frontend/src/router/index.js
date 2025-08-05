import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

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
  const { isAuthenticated } = useAuth()
  
  // Rutas que requieren autenticación
  if (to.meta.requiresAuth && !isAuthenticated.value) {
    next({ name: 'Login' })
    return
  }
  
  // Rutas solo para invitados (como login)
  if (to.meta.requiresGuest && isAuthenticated.value) {
    next({ name: 'Dashboard' })
    return
  }
  
  next()
})

export default router
