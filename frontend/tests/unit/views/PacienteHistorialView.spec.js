import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const svc = vi.hoisted(() => ({
  obtenerHistorialPaciente: vi.fn(),
  crearAnotacion: vi.fn(),
  listarAnotacionesPaciente: vi.fn(),
  listarArchivosPaciente: vi.fn(),
  subirArchivoPaciente: vi.fn(),
  descargarArchivoPaciente: vi.fn()
}))
vi.mock('@/services/medicoPacienteService', () => ({ default: svc }))
vi.mock('@/components/PatientClinicalCharts.vue', () => ({ default: { name: 'PatientClinicalCharts', template: '<div class="charts-stub" />' } }))
vi.mock('@/components/ClinicalTimelineCard.vue', () => ({ default: { name: 'ClinicalTimelineCard', template: '<div class="timeline-stub" />' } }))

import PacienteHistorialView from '@/views/medico/PacienteHistorialView.vue'

const stubs = { RouterLink: { template: '<a><slot /></a>', props: ['to'] } }
const route = { params: { nif: '12345678Z' }, query: { nombre: 'Ana López' } }

beforeEach(() => {
  vi.clearAllMocks()
  svc.obtenerHistorialPaciente.mockResolvedValue({ data: {} })
  svc.listarArchivosPaciente.mockResolvedValue({ data: [] })
  svc.listarAnotacionesPaciente.mockResolvedValue({ data: [] })
})

const factory = async (r = route) => {
  const w = mount(PacienteHistorialView, { global: { stubs, mocks: { $route: r } } })
  await flushPromises()
  return w
}

describe('PacienteHistorialView', () => {
  it('carga el historial del paciente por NIF de la ruta', async () => {
    await factory()
    expect(svc.obtenerHistorialPaciente).toHaveBeenCalledWith('12345678Z')
  })

  it('usa el nombre de la query, con fallback al NIF', async () => {
    const w = await factory()
    expect(w.vm.nombrePaciente).toBe('Ana López')
    const w2 = await factory({ params: { nif: '99Z' }, query: {} })
    expect(w2.vm.nombrePaciente).toBe('99Z')
  })

  it('agrupa antecedentes por categoría', async () => {
    svc.obtenerHistorialPaciente.mockResolvedValue({ data: { antecedentes: [
      { id: 1, categoria: 'PERSONAL', descripcion: 'A' },
      { id: 2, categoria: 'FAMILIAR', descripcion: 'B' }
    ] } })
    const w = await factory()
    expect(w.vm.groupedAntecedentes).toHaveLength(2)
  })

  it('muestra las alergias y análisis del historial', async () => {
    svc.obtenerHistorialPaciente.mockResolvedValue({ data: {
      alergias: [{ id: 1, descripcion: 'Polen', createdAt: '2026-01-01T00:00:00Z' }],
      analisisSangre: [{ id: 1, tipo: 'Glucosa', valor: '95', unidad: 'mg/dL' }]
    } })
    const w = await factory()
    expect(w.text()).toContain('Polen')
    expect(w.text()).toContain('Glucosa')
  })

  it('muestra un error si la carga falla', async () => {
    svc.obtenerHistorialPaciente.mockRejectedValueOnce(new Error('boom'))
    const w = await factory()
    expect(w.find('.alert-danger').exists()).toBe(true)
  })

  it('formatDateTime tolera nulos y fechas inválidas', async () => {
    const w = await factory()
    expect(w.vm.formatDateTime(null)).toBe('')
    expect(w.vm.formatDateTime('nope')).toBe('nope')
  })

  it('enviarAnotacion envía el mensaje recortado y marca éxito', async () => {
    svc.crearAnotacion.mockResolvedValueOnce({ data: {} })
    const w = await factory()
    w.vm.anotacionMensaje = '  Revisar tensión  '
    await w.vm.enviarAnotacion()
    expect(svc.crearAnotacion).toHaveBeenCalledWith('12345678Z', 'Revisar tensión')
    expect(w.vm.anotacionSuccess).toBe(true)
    expect(w.vm.anotacionMensaje).toBe('')
  })

  it('carga las anotaciones que el médico ha escrito a este paciente al crearse', async () => {
    svc.listarAnotacionesPaciente.mockResolvedValueOnce({ data: [
      { id: 'a1', mensaje: 'Revisar analítica', createdAt: '2026-02-01T09:00:00Z' }
    ] })
    const w = await factory()
    expect(svc.listarAnotacionesPaciente).toHaveBeenCalledWith('12345678Z')
    expect(w.text()).toContain('Revisar analítica')
  })

  it('recarga las anotaciones tras enviar una nueva', async () => {
    svc.crearAnotacion.mockResolvedValueOnce({ data: {} })
    const w = await factory()
    svc.listarAnotacionesPaciente.mockClear()
    w.vm.anotacionMensaje = 'Nueva observación'
    await w.vm.enviarAnotacion()
    expect(svc.listarAnotacionesPaciente).toHaveBeenCalledWith('12345678Z')
  })

  it('un fallo al cargar las anotaciones previas no rompe la vista', async () => {
    svc.listarAnotacionesPaciente.mockRejectedValueOnce(new Error('boom'))
    const w = await factory()
    expect(w.vm.anotaciones).toEqual([])
  })

  it('enviarAnotacion no llama al servicio si el mensaje está vacío', async () => {
    const w = await factory()
    w.vm.anotacionMensaje = '   '
    await w.vm.enviarAnotacion()
    expect(svc.crearAnotacion).not.toHaveBeenCalled()
  })

  it('enviarAnotacion muestra el error adecuado según el código de estado', async () => {
    svc.crearAnotacion.mockRejectedValueOnce({ response: { status: 403 } })
    const w = await factory()
    w.vm.anotacionMensaje = 'texto'
    await w.vm.enviarAnotacion()
    expect(w.vm.anotacionError).toBeTruthy()
    expect(w.vm.anotacionSuccess).toBe(false)
  })

  it('carga los documentos del paciente al crearse', async () => {
    svc.listarArchivosPaciente.mockResolvedValueOnce({ data: [{ id: 'f1', nombreOriginal: 'analitica.pdf' }] })
    const w = await factory()
    expect(svc.listarArchivosPaciente).toHaveBeenCalledWith('12345678Z')
    expect(w.text()).toContain('analitica.pdf')
  })

  it('seleccionar un archivo en el FileDropZone lo deja listo para subir', async () => {
    const w = await factory()
    const dropZone = w.findComponent({ name: 'FileDropZone' })
    expect(dropZone.exists()).toBe(true)
    const nuevoFile = new File(['x'], 'informe.pdf', { type: 'application/pdf' })
    w.vm.archivoError = 'error previo'
    w.vm.archivoSuccess = true
    await dropZone.vm.$emit('update:modelValue', nuevoFile)
    expect(w.vm.archivoSeleccionado).toBe(nuevoFile)
    expect(w.vm.archivoError).toBeNull()
    expect(w.vm.archivoSuccess).toBe(false)
  })

  it('subirArchivo envía el fichero seleccionado y recarga la lista', async () => {
    svc.subirArchivoPaciente.mockResolvedValueOnce({ data: {} })
    const w = await factory()
    w.vm.archivoSeleccionado = new File(['x'], 'nuevo.pdf', { type: 'application/pdf' })
    await w.vm.subirArchivo()
    expect(svc.subirArchivoPaciente).toHaveBeenCalledWith('12345678Z', expect.any(File))
    expect(w.vm.archivoSuccess).toBe(true)
    expect(w.vm.archivoSeleccionado).toBeNull()
  })

  it('subirArchivo traduce el 403 al mensaje de acceso denegado', async () => {
    svc.subirArchivoPaciente.mockRejectedValueOnce({ response: { status: 403 } })
    const w = await factory()
    w.vm.archivoSeleccionado = new File(['x'], 'nuevo.pdf')
    await w.vm.subirArchivo()
    expect(w.vm.archivoError).toBeTruthy()
    expect(w.vm.archivoSuccess).toBe(false)
  })

  it('un fallo al cargar documentos deja la lista vacía sin romper la vista', async () => {
    svc.listarArchivosPaciente.mockRejectedValueOnce(new Error('boom'))
    const w = await factory()
    expect(w.vm.archivos).toEqual([])
  })
})
