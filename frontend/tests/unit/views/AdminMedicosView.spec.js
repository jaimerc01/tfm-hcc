import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const svc = vi.hoisted(() => ({
  listar: vi.fn(),
  checkByNif: vi.fn(),
  crear: vi.fn(() => Promise.resolve()),
  actualizar: vi.fn(() => Promise.resolve()),
  eliminar: vi.fn(() => Promise.resolve()),
  setPerfilMedico: vi.fn(() => Promise.resolve())
}))
vi.mock('@/services/medicoService', () => ({ default: svc }))

import AdminMedicosView from '@/views/AdminMedicosView.vue'

beforeEach(() => {
  vi.clearAllMocks()
  svc.listar.mockResolvedValue([])
  vi.spyOn(window, 'confirm').mockReturnValue(true)
})

const factory = async () => {
  const w = mount(AdminMedicosView)
  await flushPromises()
  return w
}

const medico = (over = {}) => ({ id: 1, nombre: 'Ana', apellido1: 'L', email: 'a@x.com', nif: '12345678Z', fechaNacimiento: '1980-05-09T00:00:00Z', ...over })

describe('AdminMedicosView', () => {
  it('carga la lista de médicos al crearse', async () => {
    svc.listar.mockResolvedValue([medico()])
    const w = await factory()
    expect(w.find('.medicos-table').exists()).toBe(true)
    expect(w.text()).toContain('Ana')
  })

  it('muestra el estado vacío si no hay médicos', async () => {
    const w = await factory()
    expect(w.find('.medicos-table').exists()).toBe(false)
  })

  it('formatFecha normaliza a yyyy-MM-dd y tolera valores inválidos', async () => {
    const w = await factory()
    expect(w.vm.formatFecha('1980-05-09T00:00:00Z')).toBe('1980-05-09')
    expect(w.vm.formatFecha('')).toBe('')
    expect(w.vm.formatFecha('nope')).toBe('')
  })

  it('startAdd abre el paso de comprobación de NIF', async () => {
    const w = await factory()
    w.vm.startAdd()
    expect(w.vm.showNifStep).toBe(true)
    expect(w.vm.showForm).toBe(false)
  })

  it('checkNif rechaza NIF inválido', async () => {
    const w = await factory()
    w.vm.startAdd()
    w.vm.form.nif = 'malo'
    await w.vm.checkNif()
    expect(w.vm.error).toBeTruthy()
    expect(svc.checkByNif).not.toHaveBeenCalled()
  })

  it('checkNif con usuario existente le asigna el perfil MEDICO', async () => {
    svc.checkByNif.mockResolvedValueOnce({ data: { id: 42 } })
    const w = await factory()
    w.vm.startAdd()
    w.vm.form.nif = '12345678Z'
    await w.vm.checkNif()
    await flushPromises()
    expect(svc.setPerfilMedico).toHaveBeenCalledWith(42, true)
  })

  it('checkNif con 404 pasa al formulario completo', async () => {
    svc.checkByNif.mockRejectedValueOnce({ response: { status: 404 } })
    const w = await factory()
    w.vm.startAdd()
    w.vm.form.nif = '12345678Z'
    await w.vm.checkNif()
    expect(w.vm.showForm).toBe(true)
    expect(w.vm.showNifStep).toBe(false)
  })

  it('edit precarga el formulario con la fecha normalizada', async () => {
    const w = await factory()
    w.vm.edit(medico())
    expect(w.vm.editMedico).toBeTruthy()
    expect(w.vm.form.fechaNacimiento).toBe('1980-05-09')
    expect(w.vm.form.password).toBe('')
  })

  it('guardar (nuevo) valida el NIF y llama a crear con estadoCuenta ACTIVO', async () => {
    const w = await factory()
    w.vm.form = { ...medico(), fechaNacimiento: '1980-05-09' }
    w.vm.editMedico = null
    await w.vm.guardar()
    await flushPromises()
    expect(svc.crear).toHaveBeenCalledWith(expect.objectContaining({ estadoCuenta: 'ACTIVO' }))
  })

  it('guardar (edición) llama a actualizar con el id', async () => {
    const w = await factory()
    w.vm.form = { ...medico(), fechaNacimiento: '1980-05-09' }
    w.vm.editMedico = medico()
    await w.vm.guardar()
    await flushPromises()
    expect(svc.actualizar).toHaveBeenCalledWith(1, expect.any(Object))
  })

  it('guardar rechaza NIF inválido', async () => {
    const w = await factory()
    w.vm.form = { ...medico(), nif: 'malo' }
    await w.vm.guardar()
    expect(w.vm.error).toBeTruthy()
    expect(svc.crear).not.toHaveBeenCalled()
  })

  it('eliminar pide confirmación y recarga', async () => {
    const w = await factory()
    await w.vm.eliminar(5)
    await flushPromises()
    expect(svc.eliminar).toHaveBeenCalledWith(5)
    expect(svc.listar).toHaveBeenCalledTimes(2)
  })

  it('eliminar no hace nada si se cancela la confirmación', async () => {
    window.confirm.mockReturnValue(false)
    const w = await factory()
    await w.vm.eliminar(5)
    expect(svc.eliminar).not.toHaveBeenCalled()
  })

  it('quitarPerfil desasigna el perfil MEDICO', async () => {
    const w = await factory()
    await w.vm.quitarPerfil(7)
    await flushPromises()
    expect(svc.setPerfilMedico).toHaveBeenCalledWith(7, false)
  })

  it('cancelar cierra formularios y limpia el error', async () => {
    const w = await factory()
    w.vm.showForm = true
    w.vm.cancelar()
    expect(w.vm.showForm).toBe(false)
    expect(w.vm.editMedico).toBeNull()
  })

  it('muestra un error si la carga falla', async () => {
    svc.listar.mockRejectedValueOnce(new Error('boom'))
    const w = await factory()
    expect(w.vm.error).toBeTruthy()
  })
})
