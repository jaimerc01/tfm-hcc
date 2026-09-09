import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import PasswordInput from '@/components/PasswordInput.vue'

const factory = (props = {}) => mount(PasswordInput, {
  props: { id: 'pw-test', label: 'Contraseña', ...props }
})

describe('PasswordInput', () => {
  it('empieza oculto (type password) con el icono de "mostrar"', () => {
    const w = factory()
    const input = w.find('input')
    expect(input.attributes('type')).toBe('password')
    expect(w.find('button').attributes('aria-label')).toBe('Mostrar contraseña')
    expect(w.find('button').attributes('title')).toBe('Mostrar contraseña')
  })

  it('el botón alterna entre mostrar y ocultar la contraseña', async () => {
    const w = factory()
    const button = w.find('button')
    await button.trigger('click')
    expect(w.find('input').attributes('type')).toBe('text')
    expect(button.attributes('aria-label')).toBe('Ocultar contraseña')
    expect(button.attributes('aria-pressed')).toBe('true')

    await button.trigger('click')
    expect(w.find('input').attributes('type')).toBe('password')
    expect(button.attributes('aria-pressed')).toBe('false')
  })

  it('el botón no tiene texto visible, solo el icono', () => {
    const w = factory()
    const button = w.find('button')
    expect(button.find('svg').exists()).toBe(true)
    expect(button.text()).toBe('')
  })

  it('emite update:modelValue al escribir', async () => {
    const w = factory({ modelValue: '' })
    await w.find('input').setValue('secreto123')
    expect(w.emitted('update:modelValue')[0][0]).toBe('secreto123')
  })

  it('propaga label, required, minlength y autocomplete al input', () => {
    const w = factory({ label: 'Nueva contraseña', required: true, minlength: 8, autocomplete: 'new-password' })
    expect(w.find('label').text()).toContain('Nueva contraseña')
    const input = w.find('input')
    expect(input.attributes('required')).toBeDefined()
    expect(input.attributes('minlength')).toBe('8')
    expect(input.attributes('autocomplete')).toBe('new-password')
  })

  it('muestra la pista solo si se indica', () => {
    const sinHint = factory()
    expect(sinHint.find('.field-hint').exists()).toBe(false)
    const conHint = factory({ hint: 'La necesitarás para confirmar la acción.' })
    expect(conHint.find('.field-hint').text()).toBe('La necesitarás para confirmar la acción.')
  })

  it('reset() vuelve a ocultar la contraseña', async () => {
    const w = factory()
    await w.find('button').trigger('click')
    expect(w.find('input').attributes('type')).toBe('text')
    w.vm.reset()
    await w.vm.$nextTick()
    expect(w.find('input').attributes('type')).toBe('password')
  })
})
