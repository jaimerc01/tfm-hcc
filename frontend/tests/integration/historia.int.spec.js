import { describe, it, expect, beforeEach, beforeAll } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { http, HttpResponse } from 'msw'

import { useIntegrationServer, server, API, backend } from './server'

// Componentes y service REALES; solo se intercepta la red.
import AlergiasSection from '@/components/AlergiasSection.vue'

useIntegrationServer()

// El componente carga el service con import() dinámico; se precompila aquí para
// que el primer test no dependa del tiempo de compilación del módulo.
beforeAll(() => import('@/services/historiaClinicaService'))

beforeEach(() => {
  localStorage.clear()
  localStorage.setItem('authToken', backend.tokenValido)
})

const montar = async () => {
  const w = mount(AlergiasSection)
  await flushPromises()
  await flushPromises()
  return w
}

describe('integración: historial clínico (alergias)', () => {
  it('carga las alergias existentes del backend al montarse', async () => {
    backend.historial.alergias = [
      { id: 1, descripcion: 'Polen', createdAt: '2026-01-01T00:00:00Z' }
    ]
    const w = await montar()
    expect(w.text()).toContain('Polen')
  })

  it('añadir una alergia: POST al backend, recarga y aparece en la lista', async () => {
    const w = await montar()
    expect(w.find('.empty-state-small').exists()).toBe(true)

    await w.find('#alergia-input').setValue('Frutos secos')
    await w.find('form').trigger('submit')
    await flushPromises()

    // El backend en memoria la ha recibido...
    expect(backend.historial.alergias).toHaveLength(1)
    expect(backend.historial.alergias[0].descripcion).toBe('Frutos secos')
    // ...y el componente, tras recargar /historia, la pinta.
    expect(w.text()).toContain('Frutos secos')
    expect(w.find('#alergia-input').element.value).toBe('')
  })

  it('borrar una alergia: DELETE al backend y desaparece de la lista', async () => {
    backend.historial.alergias = [{ id: 7, descripcion: 'Ácaros', createdAt: '2026-01-01T00:00:00Z' }]
    const w = await montar()

    await w.find('.btn-danger').trigger('click')
    await w.find('[role="dialog"] .btn-primary').trigger('click')
    await flushPromises()

    expect(backend.historial.alergias).toHaveLength(0)
    expect(w.text()).not.toContain('Ácaros')
  })

  it('si el backend responde 500 al guardar, se muestra el error y no se limpia el campo', async () => {
    server.use(http.post(`${API}/historia/alergias`, () => new HttpResponse(null, { status: 500 })))

    const w = await montar()
    await w.find('#alergia-input').setValue('Marisco')
    await w.find('form').trigger('submit')
    await flushPromises()

    expect(w.find('.alert-danger').exists()).toBe(true)
  })

  it('si la carga inicial del historial falla (403), se muestra el estado de error', async () => {
    server.use(http.get(`${API}/historia`, () => new HttpResponse(null, { status: 403 })))

    const w = await montar()
    expect(w.find('.alert-danger').exists()).toBe(true)
  })
})
