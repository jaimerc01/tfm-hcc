import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

// Contrato común de las tres secciones de datos clínicos (sangre, orina, signos vitales):
// todas componen el mismo trío chart/form/tabla sobre un composable useXxx equivalente.
export function runSectionContract({ name, loadComponent, composable }) {
  describe(name, () => {
    beforeEach(() => {
      vi.clearAllMocks()
      composable.loadRangos.mockResolvedValue()
      composable.load.mockResolvedValue()
      composable.addEntry.mockResolvedValue()
      composable.deleteEntryById.mockResolvedValue()
      composable.clearAllEntries.mockResolvedValue()
      composable.entries.value = []
    })

    const factory = async () => {
      const Component = await loadComponent()
      const w = mount(Component, {
        global: { stubs: { DatoClinicoChart: true } }
      })
      await flushPromises()
      return w
    }

    it('al crearse carga rangos e historial', async () => {
      await factory()
      expect(composable.loadRangos).toHaveBeenCalled()
      expect(composable.load).toHaveBeenCalled()
    })

    it('muestra error si falla la carga del historial', async () => {
      composable.load.mockRejectedValueOnce(new Error('boom'))
      const spy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const w = await factory()
      expect(w.find('.alert-danger').exists()).toBe(true)
      spy.mockRestore()
    })

    it('no rompe si falla la carga de rangos', async () => {
      composable.loadRangos.mockRejectedValueOnce(new Error('rangos'))
      const spy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const w = await factory()
      expect(w.find('.analisis-section').exists()).toBe(true)
      spy.mockRestore()
    })

    it('handleFormSubmit añade la entrada y muestra mensaje de éxito', async () => {
      vi.useFakeTimers()
      const w = await factory()
      await w.vm.handleFormSubmit({ key: 'x', value: '1' })
      expect(composable.addEntry).toHaveBeenCalledWith({ key: 'x', value: '1' })
      expect(w.vm.msg).toBeTruthy()
      vi.useRealTimers()
    })

    it('handleFormSubmit muestra error si addEntry falla', async () => {
      composable.addEntry.mockRejectedValueOnce(new Error('500'))
      const spy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const w = await factory()
      await w.vm.handleFormSubmit({})
      expect(w.vm.error).toBeTruthy()
      spy.mockRestore()
    })

    it('removeEntry abre el modal y confirmDelete borra por id', async () => {
      composable.entries.value = [{ id: 42, key: 'x', value: '1' }]
      const w = await factory()
      w.vm.removeEntry(0)
      expect(w.vm.showDeleteModal).toBe(true)
      await w.vm.confirmDelete()
      expect(composable.deleteEntryById).toHaveBeenCalledWith(42)
      expect(w.vm.showDeleteModal).toBe(false)
    })

    it('confirmDelete no hace nada si no hay índice', async () => {
      const w = await factory()
      w.vm.deleteIndex = null
      await w.vm.confirmDelete()
      expect(composable.deleteEntryById).not.toHaveBeenCalled()
    })

    it('confirmClearAll vacía todas las entradas', async () => {
      const w = await factory()
      w.vm.clearAll()
      expect(w.vm.showClearAllModal).toBe(true)
      await w.vm.confirmClearAll()
      expect(composable.clearAllEntries).toHaveBeenCalled()
      expect(w.vm.showClearAllModal).toBe(false)
    })

    it('confirmClearAll muestra error si clearAllEntries falla', async () => {
      composable.clearAllEntries.mockRejectedValueOnce(new Error('nope'))
      const spy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.useFakeTimers()
      const w = await factory()
      await w.vm.confirmClearAll()
      expect(w.vm.error).toContain('nope')
      vi.useRealTimers()
      spy.mockRestore()
    })

    it('showTemporaryToast activa y desactiva el toast', async () => {
      vi.useFakeTimers()
      const w = await factory()
      w.vm.showTemporaryToast('hola')
      expect(w.vm.showToast).toBe(true)
      vi.advanceTimersByTime(2000)
      expect(w.vm.showToast).toBe(false)
      vi.useRealTimers()
    })
  })
}
