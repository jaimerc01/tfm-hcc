import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const service = vi.hoisted(() => ({
  listarPropuestasCambio: vi.fn(),
  responderPropuestaCambio: vi.fn()
}))
vi.mock('@/services/historiaClinicaService', () => ({ default: service }))

import PropuestasCambioClinicoSection from '@/components/PropuestasCambioClinicoSection.vue'

const propuesta = (over = {}) => ({
  id: 'p1',
  dominio: 'ANALISIS_SANGRE',
  operacion: 'UPDATE',
  estado: 'PENDIENTE',
  motivo: 'Corrección del valor',
  descripcionActual: 'Glucosa: 250 mg/dL',
  medicion: { label: 'Glucosa', value: '95', unit: 'mg/dL' },
  medico: { nombre: 'Ana', apellido1: 'López' },
  fechaCreacion: '2026-09-01T10:00:00Z',
  ...over
})

beforeEach(() => {
  vi.clearAllMocks()
  service.listarPropuestasCambio.mockResolvedValue({ data: [] })
})

describe('PropuestasCambioClinicoSection', () => {
  it('muestra un estado vacío y emite un contador de 0 cuando no hay propuestas', async () => {
    const w = mount(PropuestasCambioClinicoSection)
    await flushPromises()
    expect(w.find('.propuestas-section').exists()).toBe(true)
    expect(w.text()).toContain('No hay ninguna propuesta de cambio sobre tu historial.')
    expect(w.emitted('count-changed')[0]).toEqual([0])
  })

  it('muestra las propuestas pendientes con su motivo y el valor propuesto', async () => {
    service.listarPropuestasCambio.mockResolvedValue({ data: [propuesta()] })
    const w = mount(PropuestasCambioClinicoSection)
    await flushPromises()
    expect(w.text()).toContain('Corrección del valor')
    expect(w.text()).toContain('Glucosa: 250 mg/dL')
    expect(w.text()).toContain('Glucosa: 95 mg/dL')
    expect(w.text()).toContain('Ana López')
    expect(w.emitted('count-changed')[0]).toEqual([1])
  })

  it('separa el histórico resuelto y solo cuenta las pendientes', async () => {
    service.listarPropuestasCambio.mockResolvedValue({
      data: [
        propuesta({ id: 'pend' }),
        propuesta({ id: 'acc', estado: 'ACEPTADA', fechaResolucion: '2026-09-02T09:00:00Z' })
      ]
    })
    const w = mount(PropuestasCambioClinicoSection)
    await flushPromises()

    expect(w.text()).toContain('Propuestas resueltas')
    expect(w.text()).toContain('Aceptada')
    expect(w.text()).toContain('Resuelta el')
    // Solo la pendiente tiene botones de acción.
    expect(w.findAll('button').filter(b => b.text().includes('Aceptar y aplicar'))).toHaveLength(1)
    expect(w.emitted('count-changed')[0]).toEqual([1])
  })

  it('al aceptar una propuesta llama al servicio y emite applied', async () => {
    service.listarPropuestasCambio.mockResolvedValueOnce({ data: [propuesta()] }).mockResolvedValueOnce({ data: [] })
    service.responderPropuestaCambio.mockResolvedValue({ data: {} })
    const w = mount(PropuestasCambioClinicoSection)
    await flushPromises()

    await w.findAll('button').find(b => b.text().includes('Aceptar y aplicar')).trigger('click')
    // modal de confirmación
    await w.findAll('button').filter(b => b.text().includes('Aceptar y aplicar')).at(-1).trigger('click')
    await flushPromises()

    expect(service.responderPropuestaCambio).toHaveBeenCalledWith('p1', true)
    expect(w.emitted('applied')).toBeTruthy()
  })
})
