import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const svc = vi.hoisted(() => ({
  list: vi.fn(),
  upload: vi.fn(() => Promise.resolve()),
  remove: vi.fn(() => Promise.resolve()),
  download: vi.fn()
}))
vi.mock('@/services/archivoClinicoService', () => ({ default: svc }))

// Las secciones tienen sus propios tests; aquí solo interesa la vista contenedora.
vi.mock('@/components/AntecedentesSection.vue', () => ({ default: { template: '<div class="section-stub" />' } }))
vi.mock('@/components/AlergiasSection.vue', () => ({ default: { template: '<div class="section-stub" />' } }))
vi.mock('@/components/AnalisisSangreSection.vue', () => ({ default: { template: '<div class="section-stub" />' } }))
vi.mock('@/components/SignosVitalesSection.vue', () => ({ default: { template: '<div class="section-stub" />' } }))
vi.mock('@/components/AnalisisOrinaSection.vue', () => ({ default: { template: '<div class="section-stub" />' } }))
vi.mock('@/components/AnotacionesMedicasSection.vue', () => ({ default: { template: '<div class="section-stub" />' } }))
vi.mock('@/components/PropuestasCambioClinicoSection.vue', () => ({
  default: { name: 'PropuestasCambioClinicoSection', template: '<div class="section-stub propuestas-stub" />', emits: ['applied', 'count-changed'] }
}))

import HistoriaClinicaView from '@/views/HistoriaClinicaView.vue'

const file = (name = 'a.pdf') => new File(['x'], name, { type: 'application/pdf' })

beforeEach(() => {
  vi.clearAllMocks()
  svc.list.mockResolvedValue([])
})

const factory = async () => {
  const w = mount(HistoriaClinicaView, { attachTo: document.body })
  await flushPromises()
  return w
}

describe('HistoriaClinicaView', () => {
  it('carga los archivos al crearse', async () => {
    svc.list.mockResolvedValue([{ id: 1, nombreOriginal: 'informe.pdf', sizeBytes: 2048 }])
    const w = await factory()
    w.vm.activeSection = 'archivos'
    await w.vm.$nextTick()
    expect(w.text()).toContain('informe.pdf')
    w.unmount()
  })

  it('muestra error si la carga de archivos falla', async () => {
    svc.list.mockRejectedValueOnce(new Error('boom'))
    const w = await factory()
    expect(w.vm.error).toBeTruthy()
    w.unmount()
  })

  it('onTabKeydown navega entre pestañas con las flechas', async () => {
    const w = await factory()
    w.vm.onTabKeydown({ key: 'ArrowRight', preventDefault: vi.fn() }, 'antecedentes')
    expect(w.vm.activeSection).toBe('alergias')
    w.vm.onTabKeydown({ key: 'ArrowLeft', preventDefault: vi.fn() }, 'alergias')
    expect(w.vm.activeSection).toBe('antecedentes')
    w.vm.onTabKeydown({ key: 'End', preventDefault: vi.fn() }, 'antecedentes')
    expect(w.vm.activeSection).toBe('propuestas')
    w.vm.onTabKeydown({ key: 'Home', preventDefault: vi.fn() }, 'propuestas')
    expect(w.vm.activeSection).toBe('antecedentes')
    w.unmount()
  })

  it('la pestaña de cambios propuestos muestra un contador con las propuestas pendientes', async () => {
    const w = await factory()
    expect(w.find('.tab-badge').exists()).toBe(false)

    w.findComponent({ name: 'PropuestasCambioClinicoSection' }).vm.$emit('count-changed', 2)
    await w.vm.$nextTick()

    expect(w.find('#propuestas-tab .tab-badge').text()).toBe('2')
    w.unmount()
  })

  it('seleccionar un archivo en el FileDropZone actualiza el fichero a subir', async () => {
    const w = await factory()
    w.vm.activeSection = 'archivos'
    await w.vm.$nextTick()
    const dropZone = w.findComponent({ name: 'FileDropZone' })
    expect(dropZone.exists()).toBe(true)
    await dropZone.vm.$emit('update:modelValue', file())
    expect(w.vm.file.name).toBe('a.pdf')
    w.unmount()
  })

  it('onUpload sube el fichero y recarga la lista', async () => {
    const w = await factory()
    w.vm.file = file()
    await w.vm.onUpload()
    await flushPromises()
    expect(svc.upload).toHaveBeenCalled()
    expect(w.vm.file).toBeNull()
    expect(svc.list).toHaveBeenCalledTimes(2)
    w.unmount()
  })

  it('onUpload muestra error si falla', async () => {
    svc.upload.mockRejectedValueOnce(new Error('500'))
    const w = await factory()
    w.vm.file = file()
    await w.vm.onUpload()
    await flushPromises()
    expect(w.vm.error).toBeTruthy()
    w.unmount()
  })

  it('askDelete abre la confirmación sin borrar todavía', async () => {
    svc.list.mockResolvedValue([{ id: 1, nombreOriginal: 'a', sizeBytes: 1 }])
    const w = await factory()
    w.vm.askDelete({ id: 1, nombreOriginal: 'a' })
    expect(w.vm.deleteTarget).toMatchObject({ id: 1 })
    expect(svc.remove).not.toHaveBeenCalled()
    w.unmount()
  })

  it('cancelDelete cierra la confirmación sin borrar', async () => {
    const w = await factory()
    w.vm.askDelete({ id: 1, nombreOriginal: 'a' })
    w.vm.cancelDelete()
    expect(w.vm.deleteTarget).toBeNull()
    expect(svc.remove).not.toHaveBeenCalled()
    w.unmount()
  })

  it('confirmDelete elimina el archivo de la lista y cierra la confirmación', async () => {
    svc.list.mockResolvedValue([{ id: 1, nombreOriginal: 'a', sizeBytes: 1 }, { id: 2, nombreOriginal: 'b', sizeBytes: 1 }])
    const w = await factory()
    w.vm.askDelete({ id: 1, nombreOriginal: 'a' })
    await w.vm.confirmDelete()
    await flushPromises()
    expect(svc.remove).toHaveBeenCalledWith(1)
    expect(w.vm.items).toHaveLength(1)
    expect(w.vm.deleteTarget).toBeNull()
    w.unmount()
  })

  it('confirmDelete muestra error si falla y cierra la confirmación', async () => {
    svc.list.mockResolvedValue([{ id: 1, nombreOriginal: 'a', sizeBytes: 1 }])
    svc.remove.mockRejectedValueOnce(new Error('500'))
    const w = await factory()
    w.vm.askDelete({ id: 1, nombreOriginal: 'a' })
    await w.vm.confirmDelete()
    await flushPromises()
    expect(w.vm.error).toBeTruthy()
    expect(w.vm.deleteTarget).toBeNull()
    w.unmount()
  })

  it('el botón de borrar abre el modal de confirmación con el nombre del archivo', async () => {
    svc.list.mockResolvedValue([{ id: 1, nombreOriginal: 'informe.pdf', sizeBytes: 1 }])
    const w = await factory()
    w.vm.activeSection = 'archivos'
    await w.vm.$nextTick()
    await w.find('.btn-icon.btn-danger').trigger('click')
    expect(w.vm.deleteTarget).toMatchObject({ id: 1 })
    expect(w.text()).toContain('informe.pdf')
    w.unmount()
  })

  it('download crea un enlace temporal con el blob', async () => {
    svc.download.mockResolvedValue({ blob: new Blob(['x']), filename: 'informe.pdf' })
    const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {})
    const w = await factory()
    await w.vm.download({ id: 5 })
    await flushPromises()
    expect(svc.download).toHaveBeenCalledWith(5)
    expect(clickSpy).toHaveBeenCalled()
    clickSpy.mockRestore()
    w.unmount()
  })

  it('download muestra error si falla', async () => {
    svc.download.mockRejectedValueOnce(new Error('boom'))
    const w = await factory()
    await w.vm.download({ id: 5 })
    await flushPromises()
    expect(w.vm.error).toBeTruthy()
    w.unmount()
  })

  it('formatSize formatea bytes a unidades legibles', async () => {
    const w = await factory()
    expect(w.vm.formatSize(0)).toBe('0.0 B')
    expect(w.vm.formatSize(2048)).toBe('2.0 KB')
    expect(w.vm.formatSize(5 * 1024 * 1024)).toBe('5.0 MB')
    expect(w.vm.formatSize(null)).toBe('')
    w.unmount()
  })

  it('cambiar de sección mueve el foco al panel', async () => {
    const w = await factory()
    w.vm.activeSection = 'alergias'
    await w.vm.$nextTick()
    await flushPromises()
    expect(w.find('#alergias-panel').exists()).toBe(true)
    w.unmount()
  })
})
