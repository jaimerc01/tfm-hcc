import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import AppModal from '@/components/Modal.vue'

const mountModal = (props = {}, slots = {}) =>
  mount(AppModal, { props, slots, attachTo: document.body })

describe('AppModal', () => {
  it('muestra el label por defecto en el encabezado', () => {
    const w = mountModal({ label: 'Borrar alergia' })
    expect(w.find('.modal-header h3').text()).toBe('Borrar alergia')
    expect(w.find('[role="dialog"]').attributes('aria-label')).toBe('Borrar alergia')
    w.unmount()
  })

  it('renderiza los slots header, default y footer', () => {
    const w = mountModal({}, {
      header: '<span class="h">cabecera</span>',
      default: '<p class="b">cuerpo</p>',
      footer: '<button class="f">ok</button>'
    })
    expect(w.find('.h').exists()).toBe(true)
    expect(w.find('.b').text()).toBe('cuerpo')
    expect(w.find('.f').exists()).toBe(true)
    w.unmount()
  })

  it('emite close al hacer click en el overlay (self)', async () => {
    const w = mountModal()
    await w.find('.modal-overlay').trigger('click')
    expect(w.emitted('close')).toBeTruthy()
    w.unmount()
  })

  it('NO emite close al hacer click dentro del diálogo', async () => {
    const w = mountModal()
    await w.find('.modal').trigger('click')
    expect(w.emitted('close')).toBeFalsy()
    w.unmount()
  })

  it('emite close al pulsar Escape', async () => {
    const w = mountModal()
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }))
    expect(w.emitted('close')).toBeTruthy()
    w.unmount()
  })

  it('quita los listeners de teclado al desmontar', () => {
    const removeSpy = vi.spyOn(document, 'removeEventListener')
    const w = mountModal()
    w.unmount()
    expect(removeSpy).toHaveBeenCalledWith('keydown', expect.any(Function))
    removeSpy.mockRestore()
  })

  it('enfoca el primer elemento focusable al abrir', async () => {
    const w = mountModal({}, { footer: '<button id="primero">A</button><button id="segundo">B</button>' })
    await w.vm.$nextTick()
    expect(document.activeElement?.id).toBe('primero')
    w.unmount()
  })
})
