import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import ProponerCambioClinicoModal from '@/components/ProponerCambioClinicoModal.vue'

const factory = (props) => mount(ProponerCambioClinicoModal, { props })

describe('ProponerCambioClinicoModal', () => {
  it('no permite enviar sin motivo', async () => {
    const w = factory({ dominio: 'ALERGIA', operacion: 'CREATE' })
    // descripción rellena, motivo vacío
    await w.find('#proponer-alergia').setValue('Polen')
    const enviar = w.findAll('button').find(b => b.text().includes('Enviar propuesta'))
    expect(enviar.attributes('disabled')).toBeDefined()
  })

  it('al enviar una propuesta de alta de medición emite el payload con el motivo', async () => {
    const w = factory({ dominio: 'ANALISIS_SANGRE', operacion: 'CREATE' })
    await w.find('#proponer-parametro').setValue('Glucosa')
    await w.find('#proponer-valor').setValue('95')
    await w.find('#proponer-unidad').setValue('mg/dL')
    await w.find('#proponer-motivo').setValue('Corrección tras consulta')
    const enviar = w.findAll('button').find(b => b.text().includes('Enviar propuesta'))
    await enviar.trigger('click')

    const payload = w.emitted('submit')[0][0]
    expect(payload).toMatchObject({
      dominio: 'ANALISIS_SANGRE',
      operacion: 'CREATE',
      idRecursoObjetivo: null,
      motivo: 'Corrección tras consulta',
      medicion: { label: 'Glucosa', value: '95', unit: 'mg/dL' }
    })
  })

  it('en un borrado muestra el valor actual y no exige datos nuevos', async () => {
    const entrada = { id: 'x1', tipo: 'Glucosa', valor: '250', unidad: 'mg/dL' }
    const w = factory({ dominio: 'ANALISIS_SANGRE', operacion: 'DELETE', entrada })
    expect(w.text()).toContain('Glucosa: 250 mg/dL')
    await w.find('#proponer-motivo').setValue('Valor introducido por error')
    const enviar = w.findAll('button').find(b => b.text().includes('Enviar propuesta'))
    await enviar.trigger('click')
    const payload = w.emitted('submit')[0][0]
    expect(payload.operacion).toBe('DELETE')
    expect(payload.idRecursoObjetivo).toBe('x1')
    expect(payload.medicion).toBeUndefined()
  })
})
