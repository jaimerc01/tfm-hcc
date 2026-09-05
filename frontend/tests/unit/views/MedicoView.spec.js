import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { ref } from 'vue'

const svc = vi.hoisted(() => ({
  buscarPaciente: vi.fn(),
  crearSolicitudAsignacion: vi.fn(() => Promise.resolve()),
  listarSolicitudesPendientes: vi.fn(),
  listarMisPacientes: vi.fn(),
  desasignarPaciente: vi.fn(() => Promise.resolve())
}))
vi.mock('@/services/medicoPacienteService', () => ({ default: svc }))

const role = vi.hoisted(() => ({ isMedico: { value: true } }))
vi.mock('@/composables/useRole', () => ({ useRole: () => role }))

import MedicoView from '@/views/MedicoView.vue'

const stubs = { RouterLink: { template: '<a><slot /></a>', props: ['to'] } }

beforeEach(() => {
  vi.clearAllMocks()
  role.isMedico = ref(true)
  svc.listarSolicitudesPendientes.mockResolvedValue({ data: [] })
  svc.listarMisPacientes.mockResolvedValue({ data: [] })
})

const factory = async () => {
  const w = mount(MedicoView, { global: { stubs } })
  await flushPromises()
  return w
}

describe('MedicoView', () => {
  it('como médico carga solicitudes y pacientes al crearse', async () => {
    await factory()
    expect(svc.listarSolicitudesPendientes).toHaveBeenCalled()
    expect(svc.listarMisPacientes).toHaveBeenCalled()
  })

  it('no carga nada si no es médico', async () => {
    role.isMedico = ref(false)
    await factory()
    expect(svc.listarMisPacientes).not.toHaveBeenCalled()
  })

  it('buscarPaciente valida el NIF', async () => {
    const w = await factory()
    w.vm.dni = 'malo'
    w.vm.fechaNacimiento = '1990-01-01'
    await w.vm.buscarPaciente()
    expect(w.vm.searchError).toBeTruthy()
    expect(svc.buscarPaciente).not.toHaveBeenCalled()
  })

  it('buscarPaciente exige fecha de nacimiento', async () => {
    const w = await factory()
    w.vm.dni = '12345678Z'
    w.vm.fechaNacimiento = ''
    await w.vm.buscarPaciente()
    expect(w.vm.searchError).toBeTruthy()
  })

  it('buscarPaciente con datos válidos asigna el paciente encontrado', async () => {
    svc.buscarPaciente.mockResolvedValueOnce({ data: { nif: '12345678Z', nombre: 'Ana' } })
    const w = await factory()
    w.vm.dni = '12345678Z'
    w.vm.fechaNacimiento = '1990-01-01'
    await w.vm.buscarPaciente()
    expect(w.vm.paciente).toMatchObject({ nif: '12345678Z' })
  })

  it('buscarPaciente muestra "no encontrado" si la respuesta no tiene nif', async () => {
    svc.buscarPaciente.mockResolvedValueOnce({ data: {} })
    const w = await factory()
    w.vm.dni = '12345678Z'
    w.vm.fechaNacimiento = '1990-01-01'
    await w.vm.buscarPaciente()
    expect(w.vm.paciente).toBeNull()
    expect(w.vm.searchError).toBeTruthy()
  })

  it('buscarPaciente captura el error de red', async () => {
    svc.buscarPaciente.mockRejectedValueOnce(new Error('500'))
    const w = await factory()
    w.vm.dni = '12345678Z'
    w.vm.fechaNacimiento = '1990-01-01'
    await w.vm.buscarPaciente()
    expect(w.vm.searchError).toBeTruthy()
  })

  it('solicitarAsignacion envía la solicitud y recarga pendientes', async () => {
    const w = await factory()
    w.vm.paciente = { nif: '12345678Z' }
    await w.vm.solicitarAsignacion()
    expect(svc.crearSolicitudAsignacion).toHaveBeenCalledWith('12345678Z')
    expect(w.vm.asignacionError).toBe(false)
  })

  it('solicitarAsignacion muestra el mensaje del backend en caso de error', async () => {
    svc.crearSolicitudAsignacion.mockRejectedValueOnce({ response: { data: { message: 'Ya existe' } } })
    const w = await factory()
    w.vm.paciente = { nif: '12345678Z' }
    await w.vm.solicitarAsignacion()
    expect(w.vm.asignacionMsg).toBe('Ya existe')
    expect(w.vm.asignacionError).toBe(true)
  })

  it('cargarMisPacientes gestiona el error', async () => {
    svc.listarMisPacientes.mockRejectedValueOnce(new Error('boom'))
    const w = await factory()
    expect(w.vm.error).toBeTruthy()
  })

  it('nombreCompleto une los apellidos existentes', async () => {
    const w = await factory()
    expect(w.vm.nombreCompleto({ nombre: 'Ana', apellido1: 'López', apellido2: null })).toBe('Ana López')
  })

  it('formatDate convierte ISO a dd/mm/yyyy y tolera valores raros', async () => {
    const w = await factory()
    expect(w.vm.formatDate('1990-05-09')).toBe('09/05/1990')
    expect(w.vm.formatDate('')).toBe('')
    expect(w.vm.formatDate('xx')).toBe('xx')
  })

  it('renderiza la tarjeta de paciente asignado con enlace al historial', async () => {
    svc.listarMisPacientes.mockResolvedValue({ data: [{ nif: '1Z', nombre: 'Ana', apellido1: 'L' }] })
    const w = await factory()
    expect(w.find('.solicitud-card').exists()).toBe(true)
    expect(w.find('.btn-accept').text()).toContain('Ver historial')
    expect(w.find('.btn-reject').text()).toContain('Finalizar relación')
  })

  it('muestra las solicitudes pendientes como pastillas con estado', async () => {
    svc.listarSolicitudesPendientes.mockResolvedValue({ data: [{ id: 1, paciente: { nif: '1Z' }, estado: 'PENDIENTE', fechaCreacion: '2024-01-01' }] })
    const w = await factory()
    expect(w.find('.solicitudes-grid .status-badge').text()).toBe('Pendiente')
    expect(w.text()).toContain('1Z')
  })

  it('muestra el estado vacío de mis pacientes cuando no hay ninguno', async () => {
    const w = await factory()
    expect(w.find('.empty-state').exists()).toBe(true)
    expect(w.text()).toContain('No tienes pacientes asignados todavía')
  })

  describe('filtro de mis pacientes', () => {
    const dosPacientes = [
      { nif: '12345678Z', nombre: 'Ana', apellido1: 'García', apellido2: 'López' },
      { nif: '87654321X', nombre: 'Luis', apellido1: 'Fernández', apellido2: '' }
    ]

    it('sin filtro muestra todos los pacientes', async () => {
      svc.listarMisPacientes.mockResolvedValue({ data: dosPacientes })
      const w = await factory()
      expect(w.vm.pacientesFiltrados).toHaveLength(2)
      expect(w.findAll('.solicitudes-grid .solicitud-card')).toHaveLength(2)
    })

    it('filtra por nombre (sin distinguir mayúsculas)', async () => {
      svc.listarMisPacientes.mockResolvedValue({ data: dosPacientes })
      const w = await factory()
      w.vm.filtroPacientes = 'ana garcía'
      await w.vm.$nextTick()
      expect(w.vm.pacientesFiltrados.map(p => p.nif)).toEqual(['12345678Z'])
    })

    it('filtra por DNI', async () => {
      svc.listarMisPacientes.mockResolvedValue({ data: dosPacientes })
      const w = await factory()
      w.vm.filtroPacientes = '87654321'
      await w.vm.$nextTick()
      expect(w.vm.pacientesFiltrados.map(p => p.nif)).toEqual(['87654321X'])
    })

    it('sin coincidencias muestra un estado vacío distinto al de "sin pacientes"', async () => {
      svc.listarMisPacientes.mockResolvedValue({ data: dosPacientes })
      const w = await factory()
      w.vm.filtroPacientes = 'no-existe'
      await w.vm.$nextTick()
      expect(w.vm.pacientesFiltrados).toHaveLength(0)
      expect(w.text()).toContain('Ningún paciente coincide con el filtro')
      expect(w.text()).not.toContain('No tienes pacientes asignados todavía')
    })

    it('no muestra el filtro cuando no hay pacientes asignados', async () => {
      const w = await factory()
      expect(w.find('#filtro-pacientes').exists()).toBe(false)
    })
  })

  it('pedirDesasignar abre el modal con el paciente objetivo', async () => {
    const w = await factory()
    w.vm.pedirDesasignar({ nif: '1Z', nombre: 'Ana' })
    expect(w.vm.desasignarTarget).toMatchObject({ nif: '1Z' })
  })

  it('confirmarDesasignar llama al servicio, cierra el modal y recarga', async () => {
    svc.listarMisPacientes.mockResolvedValue({ data: [{ nif: '1Z', nombre: 'Ana' }] })
    const w = await factory()
    w.vm.pedirDesasignar({ nif: '1Z', nombre: 'Ana' })
    await w.vm.confirmarDesasignar()
    await flushPromises()
    expect(svc.desasignarPaciente).toHaveBeenCalledWith('1Z')
    expect(w.vm.desasignarTarget).toBeNull()
    expect(w.vm.desasignarError).toBe(false)
  })

  it('confirmarDesasignar muestra el error 404 traducido', async () => {
    svc.desasignarPaciente.mockRejectedValueOnce({ response: { status: 404 } })
    const w = await factory()
    w.vm.pedirDesasignar({ nif: '1Z', nombre: 'Ana' })
    await w.vm.confirmarDesasignar()
    expect(w.vm.desasignarError).toBe(true)
    expect(w.vm.desasignarMsg).toBeTruthy()
  })
})
