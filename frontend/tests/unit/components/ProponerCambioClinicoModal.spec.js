import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import ProponerCambioClinicoModal from '@/components/ProponerCambioClinicoModal.vue'

const factory = (props) => mount(ProponerCambioClinicoModal, { props })

describe('ProponerCambioClinicoModal', () => {
  it('en un alta el título y la etiqueta hablan de alta, no de cambio', () => {
    const w = factory({ dominio: 'ANTECEDENTE', operacion: 'CREATE' })
    expect(w.find('h3').text()).toBe('Proponer un alta en el historial')
    expect(w.text()).not.toContain('Valor propuesto')
    expect(w.find('label[for="proponer-descripcion"]').text()).toBe('Descripción')
  })

  it('en una modificación el título habla de modificación', () => {
    const entrada = { id: 'a1', categoria: 'PERSONAL', descripcion: 'Hipertensión' }
    const w = factory({ dominio: 'ANTECEDENTE', operacion: 'UPDATE', entrada })
    expect(w.find('h3').text()).toBe('Proponer una modificación en el historial')
    expect(w.text()).toContain('Valor actual')
    expect(w.find('label[for="proponer-descripcion"]').text()).toBe('Valor propuesto')
  })

  it('no permite enviar sin motivo', async () => {
    const w = factory({ dominio: 'ALERGIA', operacion: 'CREATE' })
    // descripción rellena, motivo vacío
    await w.find('#proponer-alergia').setValue('Polen')
    const enviar = w.findAll('button').find(b => b.text().includes('Enviar propuesta'))
    expect(enviar.attributes('disabled')).toBeDefined()
  })

  it('en un alta de medición el parámetro es un desplegable de los de la app y autocompleta la unidad', async () => {
    const w = factory({ dominio: 'ANALISIS_SANGRE', operacion: 'CREATE' })
    const select = w.find('select#proponer-parametro')
    expect(select.exists()).toBe(true)
    const opciones = select.findAll('option').map(o => o.element.value).filter(Boolean)
    expect(opciones).toContain('glucosa')
    expect(opciones).toContain('Colesterol LDL')
    // La unidad se rellena sola al elegir el parámetro y no se puede editar.
    await select.setValue('glucosa')
    expect(w.find('#proponer-unidad').element.value).toBe('mg/dL')
    expect(w.find('#proponer-unidad').attributes('readonly')).toBeDefined()
  })

  it('al enviar una propuesta de alta de medición emite el payload con la key del parámetro', async () => {
    const w = factory({ dominio: 'ANALISIS_SANGRE', operacion: 'CREATE' })
    await w.find('select#proponer-parametro').setValue('glucosa')
    await w.find('#proponer-valor').setValue('95')
    await w.find('#proponer-motivo').setValue('Corrección tras consulta')
    const enviar = w.findAll('button').find(b => b.text().includes('Enviar propuesta'))
    await enviar.trigger('click')

    const payload = w.emitted('submit')[0][0]
    expect(payload).toMatchObject({
      dominio: 'ANALISIS_SANGRE',
      operacion: 'CREATE',
      idRecursoObjetivo: null,
      motivo: 'Corrección tras consulta',
      medicion: { label: 'glucosa', value: '95', unit: 'mg/dL' }
    })
  })

  it('en una modificación de medición el parámetro es fijo y no se puede cambiar', async () => {
    const entrada = { id: 'm1', tipo: 'Glucosa', valor: '250', unidad: 'mg/dL' }
    const w = factory({ dominio: 'ANALISIS_SANGRE', operacion: 'UPDATE', entrada })
    expect(w.find('select#proponer-parametro').exists()).toBe(false)
    const campo = w.find('input#proponer-parametro')
    expect(campo.attributes('readonly')).toBeDefined()
    expect(campo.element.value).toBe('Glucosa')

    await w.find('#proponer-valor').setValue('180')
    await w.find('#proponer-motivo').setValue('Repetición de la analítica')
    await w.findAll('button').find(b => b.text().includes('Enviar propuesta')).trigger('click')

    const payload = w.emitted('submit')[0][0]
    expect(payload.medicion).toMatchObject({ label: 'Glucosa', value: '180', unit: 'mg/dL' })
  })

  it('en un alta de medición la fecha se precarga con la actual y es editable', async () => {
    const w = factory({ dominio: 'SIGNOS_VITALES', operacion: 'CREATE' })
    const fecha = w.find('#proponer-fecha')
    expect(fecha.attributes('readonly')).toBeUndefined()
    expect(fecha.element.value).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/)
    const desfase = Date.now() - new Date(fecha.element.value).getTime()
    expect(desfase).toBeGreaterThanOrEqual(0)
    expect(desfase).toBeLessThan(2 * 60 * 1000)
  })

  it('en una modificación de medición la fecha se precarga con la de la fila y es editable', async () => {
    const cuando = new Date('2026-01-15T09:30:00Z')
    const entrada = { id: 'm2', tipo: 'IMC', valor: '27', unidad: 'kg/m²', createdAt: cuando.toISOString() }
    const w = factory({ dominio: 'SIGNOS_VITALES', operacion: 'UPDATE', entrada })
    const fecha = w.find('#proponer-fecha')
    expect(fecha.attributes('readonly')).toBeUndefined()
    expect(new Date(fecha.element.value).getTime()).toBe(cuando.getTime())

    await fecha.setValue('2026-02-20T14:00')
    await w.find('#proponer-valor').setValue('26')
    await w.find('#proponer-motivo').setValue('Nueva medición')
    await w.findAll('button').find(b => b.text().includes('Enviar propuesta')).trigger('click')

    const payload = w.emitted('submit')[0][0]
    expect(payload.medicion.createdAt).toBe(new Date('2026-02-20T14:00').toISOString())
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
