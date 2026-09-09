import { setupServer } from 'msw/node'
import { http, HttpResponse } from 'msw'
import { afterAll, afterEach, beforeAll } from 'vitest'

// URL base que usan todos los services cuando VUE_APP_API_URL no está definida
// (ver p. ej. src/services/authService.js).
export const API = 'http://localhost:8081'

/**
 * "Backend" en memoria mínimo para los tests de integración: guarda el estado que
 * los flujos necesitan (token emitido, alergias del historial...) para que una
 * secuencia de peticiones se comporte como contra el servidor real.
 */
export function createBackend() {
  return {
    tokenValido: 'header.payload.signature',
    historial: { alergias: [], antecedentes: [], analisisSangre: [], signosVitales: [], analisisOrina: [] },
    rangos: [],
    solicitudesRecibidas: []
  }
}

export const backend = createBackend()

function autorizado(request) {
  const auth = request.headers.get('Authorization')
  return auth === `Bearer ${backend.tokenValido}`
}

export const handlers = [
  http.post(`${API}/authentication/login`, () =>
    HttpResponse.json({ token: backend.tokenValido, expirationTime: 3600000 })),

  http.post(`${API}/authentication/login/2fa`, async ({ request }) => {
    const body = await request.json()
    if (body.code === '123456') {
      return HttpResponse.json({ token: backend.tokenValido, expirationTime: 3600000 })
    }
    return new HttpResponse(null, { status: 401 })
  }),

  http.get(`${API}/usuario/nombre`, ({ request }) => {
    if (!autorizado(request)) return new HttpResponse(null, { status: 401 })
    return HttpResponse.json('Ana López')
  }),

  http.get(`${API}/usuario/me`, ({ request }) => {
    if (!autorizado(request)) return new HttpResponse(null, { status: 401 })
    return HttpResponse.json({ nombre: 'Ana', apellido1: 'López', nif: '12345678Z', email: 'ana@example.com' })
  }),

  http.get(`${API}/historia`, ({ request }) => {
    if (!autorizado(request)) return new HttpResponse(null, { status: 403 })
    return HttpResponse.json(backend.historial)
  }),

  http.get(`${API}/rangos`, () => HttpResponse.json(backend.rangos)),

  http.post(`${API}/historia/alergias`, async ({ request }) => {
    if (!autorizado(request)) return new HttpResponse(null, { status: 403 })
    const body = await request.json()
    backend.historial.alergias.push({
      id: backend.historial.alergias.length + 1,
      descripcion: body.descripcion,
      createdAt: '2026-01-01T00:00:00Z'
    })
    return HttpResponse.json(backend.historial)
  }),

  http.delete(`${API}/historia/alergias/:id`, ({ request, params }) => {
    if (!autorizado(request)) return new HttpResponse(null, { status: 403 })
    backend.historial.alergias = backend.historial.alergias.filter(a => String(a.id) !== params.id)
    return HttpResponse.json(backend.historial)
  }),

  http.get(`${API}/usuario/solicitudes`, ({ request }) => {
    if (!autorizado(request)) return new HttpResponse(null, { status: 401 })
    return HttpResponse.json(backend.solicitudesRecibidas)
  }),

  http.put(`${API}/usuario/solicitudes/:id`, async ({ request, params }) => {
    if (!autorizado(request)) return new HttpResponse(null, { status: 401 })
    const body = await request.json()
    const solicitud = backend.solicitudesRecibidas.find(s => String(s.id) === params.id)
    if (solicitud) solicitud.estado = body.estado
    return HttpResponse.json(solicitud ?? {})
  })
]

export const server = setupServer(...handlers)

/**
 * Registra el ciclo de vida de MSW y el reseteo del estado del backend en memoria.
 * Cada fichero de test de integración lo invoca una vez.
 */
export function useIntegrationServer() {
  beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
  afterEach(() => {
    server.resetHandlers()
    const limpio = createBackend()
    Object.assign(backend, limpio)
  })
  afterAll(() => server.close())
}
