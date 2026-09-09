import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import FileDropZone from '@/components/FileDropZone.vue'

const file = (name = 'a.pdf') => new File(['x'], name, { type: 'application/pdf' })

const factory = (props = {}) => mount(FileDropZone, { props })

describe('FileDropZone', () => {
  it('sin archivo seleccionado muestra la invitación a subir', () => {
    const w = factory()
    expect(w.text()).toContain('Arrastra tu archivo aquí o haz clic para seleccionar')
    expect(w.find('.file-selected-name').exists()).toBe(false)
  })

  it('con un archivo seleccionado muestra su nombre', () => {
    const w = factory({ modelValue: file('informe.pdf') })
    expect(w.find('.file-selected-name').text()).toContain('informe.pdf')
  })

  it('seleccionar un archivo por el input emite update:modelValue', async () => {
    const w = factory()
    const input = w.find('input[type="file"]')
    Object.defineProperty(input.element, 'files', { value: [file('b.pdf')], configurable: true })
    await input.trigger('change')
    expect(w.emitted('update:modelValue')[0][0].name).toBe('b.pdf')
  })

  it('soltar un archivo (drag & drop) emite update:modelValue y desactiva el estado de arrastre', async () => {
    const w = factory()
    const zone = w.find('.file-drop-zone')
    await zone.trigger('dragover')
    expect(zone.classes()).toContain('drag-over')

    await zone.trigger('drop', { dataTransfer: { files: [file('c.pdf')] } })
    expect(w.emitted('update:modelValue')[0][0].name).toBe('c.pdf')
    expect(zone.classes()).not.toContain('drag-over')
  })

  it('dragleave desactiva el estado de arrastre sin emitir nada', async () => {
    const w = factory()
    const zone = w.find('.file-drop-zone')
    await zone.trigger('dragover')
    await zone.trigger('dragleave')
    expect(zone.classes()).not.toContain('drag-over')
    expect(w.emitted('update:modelValue')).toBeUndefined()
  })

  it('Enter/Espacio sobre la zona abren el selector nativo de archivos', async () => {
    const w = factory()
    const clickSpy = vi.spyOn(w.vm.$refs.inputRef, 'click').mockImplementation(() => {})
    await w.find('.file-drop-zone').trigger('keydown.enter')
    await w.find('.file-drop-zone').trigger('keydown.space')
    expect(clickSpy).toHaveBeenCalledTimes(2)
  })

  it('reset() vacía el input nativo para poder reseleccionar el mismo archivo', async () => {
    const w = factory()
    const input = w.find('input[type="file"]')
    Object.defineProperty(input.element, 'files', { value: [file('a.pdf')], configurable: true })
    await input.trigger('change')
    w.vm.reset()
    expect(w.vm.$refs.inputRef.value).toBe('')
  })

  it('usa la lista de extensiones por defecto si no se indica accept', () => {
    const w = factory()
    expect(w.find('input[type="file"]').attributes('accept')).toContain('.pdf')
    expect(w.find('input[type="file"]').attributes('accept')).toContain('.docx')
  })

  it('permite sobrescribir accept y el id del input', () => {
    const w = factory({ accept: '.pdf', inputId: 'mi-input' })
    const input = w.find('input[type="file"]')
    expect(input.attributes('accept')).toBe('.pdf')
    expect(input.attributes('id')).toBe('mi-input')
  })
})
