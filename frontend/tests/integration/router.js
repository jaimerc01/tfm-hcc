import { createRouter, createMemoryHistory } from 'vue-router'

const Blank = { template: '<div />' }

/**
 * Router de memoria para los tests de integración. Incluye solo las rutas que
 * necesita el flujo bajo prueba; las vistas destino que no son el objeto del test
 * se sustituyen por un placeholder.
 */
export function makeRouter(routes = [], initial = '/') {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', name: 'Home', component: Blank },
      { path: '/login', name: 'Login', component: Blank },
      { path: '/dashboard', name: 'Dashboard', component: Blank },
      { path: '/register', name: 'Register', component: Blank },
      { path: '/historia-clinica', name: 'HistoriaClinica', component: Blank },
      { path: '/privacidad', name: 'PoliticaPrivacidad', component: Blank },
      { path: '/cookies', name: 'PoliticaCookies', component: Blank },
      ...routes
    ]
  })
  router.push(initial)
  return router
}
