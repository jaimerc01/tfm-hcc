import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const svc = vi.hoisted(() => ({
  obtenerHistorialPaciente: vi.fn(),
  crearAnotacion: vi.fn(),
  listarAnotacionesPaciente: vi.fn(),
  listarArchivosPaciente: vi.fn(),
  subirArchivoPaciente: vi.fn(),
  descargarArchivoPaciente: vi.fn(),
  listarPropuestasCambio: vi.fn(),
  proponerCambioClinico: vi.fn()
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
  svc.listarPropuestasCambio.mockResolvedValue({ data: [] })
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

  it('enviarAnotacion con éxito limpia el mensaje y recarga', async () => {
    svc.crearAnotacion.mockResolvedValueOnce({ data: {} })
    const w = await factory()
    w.vm.anotacionMensaje = 'Revisar tensión'
    await w.vm.enviarAnotacion()
    expect(svc.crearAnotacion).toHaveBeenCalledWith('12345678Z', 'Revisar tensión')
    expect(w.vm.anotacionMensaje).toBe('')
    expect(w.vm.anotacionSuccess).toBe(true)
  })

  it('enviarAnotacion traduce el 404 al mensaje de paciente no encontrado', async () => {
    svc.crearAnotacion.mockRejectedValueOnce({ response: { status: 404 } })
    const w = await factory()
    w.vm.anotacionMensaje = 'texto'
    await w.vm.enviarAnotacion()
    expect(w.vm.anotacionError).toBeTruthy()
  })

  describe('propuestas de cambio clínico', () => {
    it('carga las propuestas enviadas al crearse', async () => {
      svc.listarPropuestasCambio.mockResolvedValueOnce({
        data: [{ id: 'p1', dominio: 'ALERGIA', operacion: 'CREATE', estado: 'PENDIENTE', motivo: 'm', fechaCreacion: '2026-01-01T00:00:00Z' }]
      })
      const w = await factory()
      expect(svc.listarPropuestasCambio).toHaveBeenCalledWith('12345678Z')
      expect(w.vm.propuestasEnviadas).toHaveLength(1)
    })

    it('un fallo al cargar propuestas deja la lista vacía', async () => {
      svc.listarPropuestasCambio.mockRejectedValueOnce(new Error('boom'))
      const w = await factory()
      expect(w.vm.propuestasEnviadas).toEqual([])
    })

    it('abrirProponer y cerrarProponer controlan la visibilidad del modal', async () => {
      const w = await factory()
      w.vm.abrirProponer('ALERGIA', 'CREATE')
      expect(w.vm.proponer).toMatchObject({ visible: true, dominio: 'ALERGIA', operacion: 'CREATE' })

      w.vm.cerrarProponer()
      expect(w.vm.proponer.visible).toBe(false)
    })

    it('cerrarProponer no cierra mientras se está guardando', async () => {
      const w = await factory()
      w.vm.abrirProponer('ALERGIA', 'CREATE')
      w.vm.proponerSaving = true
      w.vm.cerrarProponer()
      expect(w.vm.proponer.visible).toBe(true)
    })

    it('enviarPropuesta con éxito cierra el modal y recarga las propuestas', async () => {
      svc.proponerCambioClinico.mockResolvedValueOnce({ data: {} })
      const w = await factory()
      w.vm.abrirProponer('ALERGIA', 'CREATE')
      await w.vm.enviarPropuesta({ dominio: 'ALERGIA', operacion: 'CREATE', motivo: 'm', alergia: { descripcion: 'Polen' } })
      expect(svc.proponerCambioClinico).toHaveBeenCalledWith('12345678Z', expect.objectContaining({ dominio: 'ALERGIA' }))
      expect(w.vm.proponer.visible).toBe(false)
      expect(w.vm.proponerError).toBe(false)
      expect(svc.listarPropuestasCambio).toHaveBeenCalledTimes(2)
    })

    it('enviarPropuesta traduce el 403 al mensaje de acceso denegado', async () => {
      svc.proponerCambioClinico.mockRejectedValueOnce({ response: { status: 403 } })
      const w = await factory()
      await w.vm.enviarPropuesta({ dominio: 'ALERGIA', operacion: 'CREATE', motivo: 'm' })
      expect(w.vm.proponerError).toBe(true)
      expect(w.vm.proponerMsg).toBeTruthy()
    })

    it('enviarPropuesta muestra un error genérico ante otros fallos', async () => {
      svc.proponerCambioClinico.mockRejectedValueOnce({ response: { status: 500 } })
      const w = await factory()
      await w.vm.enviarPropuesta({ dominio: 'ALERGIA', operacion: 'CREATE', motivo: 'm' })
      expect(w.vm.proponerError).toBe(true)
      expect(w.vm.proponerSaving).toBe(false)
    })
  })

  describe('paginación', () => {
    it('avanza y retrocede páginas dentro de los límites', async () => {
      svc.obtenerHistorialPaciente.mockResolvedValue({
        data: { alergias: Array.from({ length: 25 }, (_, i) => ({ id: i, descripcion: 'A' + i })) }
      })
      const w = await factory()
      expect(w.vm.alergiasTotalPages).toBe(3)

      w.vm.goToPrevAlergiasPage()
      expect(w.vm.alergiasPage).toBe(1)

      w.vm.goToNextAlergiasPage()
      w.vm.goToNextAlergiasPage()
      expect(w.vm.alergiasPage).toBe(3)

      w.vm.goToNextAlergiasPage()
      expect(w.vm.alergiasPage).toBe(3)

      w.vm.goToPrevAlergiasPage()
      expect(w.vm.alergiasPage).toBe(2)
    })

    it('pagina también documentos y anotaciones', async () => {
      svc.listarArchivosPaciente.mockResolvedValue({ data: Array.from({ length: 12 }, (_, i) => ({ id: 'f' + i, nombreOriginal: 'd' + i })) })
      svc.listarAnotacionesPaciente.mockResolvedValue({ data: Array.from({ length: 12 }, (_, i) => ({ id: 'a' + i, mensaje: 'm' + i })) })
      const w = await factory()

      w.vm.goToNextArchivosPage()
      expect(w.vm.archivosPage).toBe(2)
      w.vm.goToPrevArchivosPage()
      expect(w.vm.archivosPage).toBe(1)

      w.vm.goToNextAnotacionesPage()
      expect(w.vm.anotacionesPage).toBe(2)
      w.vm.goToPrevAnotacionesPage()
      expect(w.vm.anotacionesPage).toBe(1)
    })
  })

  describe('descarga de documentos', () => {
    it('descarga el fichero como blob y dispara la descarga en el navegador', async () => {
      svc.descargarArchivoPaciente.mockResolvedValueOnce({ data: new Blob(['x']), headers: { 'content-type': 'application/pdf' } })
      const click = vi.fn()
      const createEl = document.createElement.bind(document)
      vi.spyOn(document, 'createElement').mockImplementation(tag => {
        const el = createEl(tag)
        if (tag === 'a') el.click = click
        return el
      })
      const w = await factory()
      await w.vm.descargarArchivo({ id: 'f1', nombreOriginal: 'informe.pdf' })
      expect(svc.descargarArchivoPaciente).toHaveBeenCalledWith('12345678Z', 'f1')
      expect(click).toHaveBeenCalled()
      document.createElement.mockRestore()
    })

    it('muestra un error si la descarga falla', async () => {
      svc.descargarArchivoPaciente.mockRejectedValueOnce(new Error('boom'))
      const w = await factory()
      await w.vm.descargarArchivo({ id: 'f1' })
      expect(w.vm.archivoError).toBeTruthy()
    })
  })
})
