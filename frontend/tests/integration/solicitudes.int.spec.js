import { describe, it, expect, beforeEach, beforeAll } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

import { useIntegrationServer, backend } from './server'

// Vista + solicitudService + axios REALES; solo se intercepta la red.
import MisSolicitudesView from '@/views/MisSolicitudesView.vue'

useIntegrationServer()
beforeAll(() => import('@/services/solicitudService'))

beforeEach(() => {
  localStorage.clear()
  localStorage.setItem('authToken', backend.tokenValido)
})

const montar = async () => {
  const w = mount(MisSolicitudesView)
  await flushPromises()
  await flushPromises()
  return w
}

describe('integración: mis solicitudes', () => {
  it('lista las solicitudes recibidas del backend', async () => {
    backend.solicitudesRecibidas = [
      { id: 1, estado: 'PENDIENTE', medico: { nombre: 'Dr. House' }, paciente: { nombre: 'Ana' }, fechaCreacion: '2026-01-01T10:00:00Z' }
    ]
    const w = await montar()

    expect(w.text()).toContain('Dr. House')
    expect(w.text().toLowerCase()).toContain('pendiente')
  })

  it('aceptar una solicitud: PUT al backend y el estado se actualiza tras recargar', async () => {
    backend.solicitudesRecibidas = [
      { id: 5, estado: 'PENDIENTE', medico: { nombre: 'Dra. Grey' }, paciente: { nombre: 'Ana' }, fechaCreacion: '2026-01-01T10:00:00Z' }
    ]
    const w = await montar()

    await w.find('.btn-accept').trigger('click')          // abre el modal de confirmación
    await w.find('.modal-actions button').trigger('click') // confirma
    await flushPromises()
    await flushPromises()

    expect(backend.solicitudesRecibidas[0].estado).toBe('ACEPTADA')
    expect(w.text().toLowerCase()).toContain('aceptada')
    expect(w.find('.btn-accept').exists()).toBe(false) // ya no está pendiente
  })

  it('muestra el estado vacío cuando no hay solicitudes', async () => {
    const w = await montar()
    expect(w.find('.empty-state').exists()).toBe(true)
  })
})
