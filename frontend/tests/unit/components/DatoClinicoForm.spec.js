import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import DatoClinicoForm from '@/components/DatoClinicoForm.vue'

const analytes = [
  { key: 'glucosa', labelKey: 'glucose', unit: 'mg/dL', validationMin: 0, validationMax: 1000, decimals: 0, recommendedMin: 70, recommendedMax: 110 },
  { key: 'hemoglobina', labelKey: 'hemoglobin', unit: 'g/dL', validationMin: 0, validationMax: 25, decimals: 1, recommendedMin: null, recommendedMax: null }
]

const factory = (props = {}) => mount(DatoClinicoForm, {
  props: { analytes, domainLabel: 'análisis de sangre', ...props }
})

describe('DatoClinicoForm', () => {
  it('preselecciona el primer analito y su unidad', () => {
    const w = factory()
    expect(w.vm.selected).toBe('glucosa')
    expect(w.vm.unitForSelected).toBe('mg/dL')
    expect(w.find('.unit-badge').text()).toBe('mg/dL')
  })

  it('inicializa inputDate con la fecha-hora local actual', () => {
    const w = factory()
    expect(w.vm.inputDate).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/)
  })

  it('rangeHintText usa los rangos recomendados si existen', () => {
    const w = factory()
    expect(w.vm.rangeHintText).toContain('70')
    expect(w.vm.rangeHintText).toContain('110')
  })

  it('rangeHintText cae a mensaje genérico sin rangos recomendados', async () => {
    const w = factory()
    w.vm.selected = 'hemoglobina'
    await w.vm.$nextTick()
    expect(w.vm.rangeHintText).toBeTruthy()
  })

  describe('validation', () => {
    it('vacío -> no válido', () => {
      expect(factory().vm.validation.isValid).toBe(false)
    })
    it('no numérico -> no válido', async () => {
      const w = factory()
      w.vm.value = 'abc'
      await w.vm.$nextTick()
      expect(w.vm.validation.isValid).toBe(false)
    })
    it('por debajo del mínimo -> no válido', async () => {
      const w = factory()
      w.vm.value = '-5'
      await w.vm.$nextTick()
      expect(w.vm.validation.isValid).toBe(false)
    })
    it('por encima del máximo -> no válido', async () => {
      const w = factory()
      w.vm.value = '5000'
      await w.vm.$nextTick()
      expect(w.vm.validation.isValid).toBe(false)
    })
    it('acepta coma decimal', async () => {
      const w = factory()
      w.vm.selected = 'hemoglobina'
      w.vm.value = '13,5'
      await w.vm.$nextTick()
      expect(w.vm.validation).toMatchObject({ isValid: true, value: 13.5 })
    })
  })

  it('canAdd es true solo con valor válido', async () => {
    const w = factory()
    expect(w.vm.canAdd).toBe(false)
    w.vm.value = '95'
    await w.vm.$nextTick()
    expect(w.vm.canAdd).toBe(true)
  })

  it('handleSubmit emite submit con key, valor formateado a decimals y createdAt ISO', async () => {
    const w = factory()
    w.vm.selected = 'hemoglobina'
    w.vm.value = '13,456'
    w.vm.inputDate = '2026-01-15T10:30'
    await w.vm.$nextTick()
    w.vm.handleSubmit()
    const payload = w.emitted('submit')[0][0]
    expect(payload.key).toBe('hemoglobina')
    expect(payload.value).toBe('13.5')
    expect(payload.unit).toBe('g/dL')
    expect(payload.createdAt).toBe(new Date('2026-01-15T10:30').toISOString())
  })

  it('handleSubmit no emite nada si el valor es inválido', async () => {
    const w = factory()
    w.vm.value = 'xx'
    await w.vm.$nextTick()
    w.vm.handleSubmit()
    expect(w.emitted('submit')).toBeFalsy()
  })

  it('resetFields limpia el valor y reinicia la fecha', async () => {
    const w = factory()
    w.vm.value = '99'
    w.vm.resetFields()
    expect(w.vm.value).toBe('')
    expect(w.vm.inputDate).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/)
  })

  it('deshabilita el botón cuando saving es true', async () => {
    const w = factory({ saving: true })
    w.vm.value = '95'
    await w.vm.$nextTick()
    expect(w.find('.btn-add').attributes('disabled')).toBeDefined()
  })
})
