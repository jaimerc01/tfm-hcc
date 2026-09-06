import { describe, it, expect, vi, beforeEach } from 'vitest'

const { client, create } = vi.hoisted(() => {
  const client = {
    get: vi.fn(() => Promise.resolve({ data: {} })),
    post: vi.fn(() => Promise.resolve({ data: {} })),
    put: vi.fn(), delete: vi.fn(),
    interceptors: { request: { use: vi.fn() } }
  }
  return { client, create: vi.fn(() => client) }
})
vi.mock('axios', () => ({ default: { create } }))

import svc from '@/services/medicoPacienteService'

describe('medicoPacienteService', () => {
  beforeEach(() => { client.get.mockClear(); client.post.mockClear() })

  it('buscarPaciente pasa dni y fechaNacimiento como params', () => {
    svc.buscarPaciente('12345678Z', '1990-01-01')
    expect(client.get).toHaveBeenCalledWith('/medico/pacientes/buscar', { params: { dni: '12345678Z', fechaNacimiento: '1990-01-01' } })
  })

  it('crearSolicitudAsignacion hace POST con nifPaciente como param', () => {
    svc.crearSolicitudAsignacion('11111111H')
    expect(client.post).toHaveBeenCalledWith('/medico/solicitudes-asignacion', null, { params: { nifPaciente: '11111111H' } })
  })

  it('listarSolicitudesPendientes / enviadas / misPacientes', () => {
    svc.listarSolicitudesPendientes()
    expect(client.get).toHaveBeenCalledWith('/medico/solicitudes-asignacion/pendientes')
    svc.listarSolicitudesEnviadas()
    expect(client.get).toHaveBeenCalledWith('/medico/solicitudes-asignacion/enviadas')
    svc.listarMisPacientes()
    expect(client.get).toHaveBeenCalledWith('/medico/pacientes')
  })

  it('obtenerHistorialPaciente codifica el nif en la URL', () => {
    svc.obtenerHistorialPaciente('11111111/H')
    expect(client.get).toHaveBeenCalledWith('/medico/pacientes/11111111%2FH/historial')
  })

  it('crearAnotacion hace POST con el mensaje en el cuerpo y el nif codificado', () => {
    svc.crearAnotacion('11111111/H', 'Revisar tensión')
    expect(client.post).toHaveBeenCalledWith('/medico/pacientes/11111111%2FH/anotaciones', { mensaje: 'Revisar tensión' })
  })

  it('listarAnotacionesPaciente hace GET sobre /medico/pacientes/{nif}/anotaciones', () => {
    svc.listarAnotacionesPaciente('11111111/H')
    expect(client.get).toHaveBeenCalledWith('/medico/pacientes/11111111%2FH/anotaciones')
  })

  it('desasignarPaciente hace DELETE sobre /relaciones/mis-pacientes con el nif codificado', () => {
    svc.desasignarPaciente('11111111/H')
    expect(client.delete).toHaveBeenCalledWith('/relaciones/mis-pacientes/11111111%2FH')
  })

  it('listarArchivosPaciente hace GET sobre /medico/pacientes/{nif}/archivos', () => {
    svc.listarArchivosPaciente('11111111H')
    expect(client.get).toHaveBeenCalledWith('/medico/pacientes/11111111H/archivos')
  })

  it('subirArchivoPaciente hace POST multipart con el fichero', () => {
    const file = new File(['x'], 'a.pdf')
    svc.subirArchivoPaciente('11111111H', file)
    const [url, form, cfg] = client.post.mock.calls.at(-1)
    expect(url).toBe('/medico/pacientes/11111111H/archivos')
    expect(form).toBeInstanceOf(FormData)
    expect(cfg.headers['Content-Type']).toBe('multipart/form-data')
  })

  it('descargarArchivoPaciente hace GET con responseType blob', () => {
    svc.descargarArchivoPaciente('11111111H', 'f1')
    expect(client.get).toHaveBeenCalledWith('/medico/pacientes/11111111H/archivos/f1', { responseType: 'blob' })
  })

  it('proponerCambioClinico hace POST al endpoint del paciente con el cuerpo de la propuesta', () => {
    const propuesta = { dominio: 'ANALISIS_SANGRE', operacion: 'CREATE', motivo: 'm' }
    svc.proponerCambioClinico('11111111H', propuesta)
    const [url, body] = client.post.mock.calls.at(-1)
    expect(url).toBe('/medico/pacientes/11111111H/propuestas-cambio')
    expect(body).toEqual(propuesta)
  })

  it('listarPropuestasCambio hace GET al endpoint del paciente', () => {
    svc.listarPropuestasCambio('11111111H')
    expect(client.get).toHaveBeenCalledWith('/medico/pacientes/11111111H/propuestas-cambio')
  })
})
