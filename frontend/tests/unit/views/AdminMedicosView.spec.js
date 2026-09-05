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

  it('formatFecha normaliza a dd/mm/yyyy y tolera valores inválidos', async () => {
    const w = await factory()
    expect(w.vm.formatFecha('1980-05-09T12:00:00Z')).toBe('09/05/1980')
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

  it('checkNif con usuario existente pide confirmación en vez de asignar directamente', async () => {
    svc.checkByNif.mockResolvedValueOnce({ data: { id: 42, nombre: 'Luis', apellido1: 'Soto' } })
    const w = await factory()
    w.vm.startAdd()
    w.vm.form.nif = '12345678Z'
    await w.vm.checkNif()
    await flushPromises()

    expect(svc.setPerfilMedico).not.toHaveBeenCalled()
    expect(w.vm.foundUser).toEqual({ id: 42, nombre: 'Luis', apellido1: 'Soto' })
    expect(w.vm.showNifStep).toBe(false)
  })

  it('confirmAssignExisting asigna el perfil MEDICO al usuario encontrado y recarga', async () => {
    const w = await factory()
    w.vm.foundUser = { id: 42, nombre: 'Luis', apellido1: 'Soto' }
    await w.vm.confirmAssignExisting()
    await flushPromises()

    expect(svc.setPerfilMedico).toHaveBeenCalledWith(42, true)
    expect(w.vm.foundUser).toBeNull()
    expect(svc.listar).toHaveBeenCalledTimes(2)
  })

  it('confirmAssignExisting sin usuario encontrado no hace nada', async () => {
    const w = await factory()
    w.vm.foundUser = null
    await w.vm.confirmAssignExisting()
    expect(svc.setPerfilMedico).not.toHaveBeenCalled()
  })

  it('confirmAssignExisting muestra un error si falla la asignación', async () => {
    svc.setPerfilMedico.mockRejectedValueOnce(new Error('fallo'))
    const w = await factory()
    w.vm.foundUser = { id: 42, nombre: 'Luis' }
    await w.vm.confirmAssignExisting()
    expect(w.vm.error).toBeTruthy()
    expect(w.vm.foundUser).toBeNull()
  })

  it('cancelAssignExisting cierra el popup sin asignar el perfil', async () => {
    const w = await factory()
    w.vm.foundUser = { id: 42, nombre: 'Luis' }
    w.vm.cancelAssignExisting()
    expect(w.vm.foundUser).toBeNull()
    expect(svc.setPerfilMedico).not.toHaveBeenCalled()
  })

  it('el popup de asignar perfil abre un modal propio de la app, no el del navegador', async () => {
    svc.checkByNif.mockResolvedValueOnce({ data: { id: 42, nombre: 'Luis', apellido1: 'Soto' } })
    const confirmSpy = vi.spyOn(window, 'confirm')
    const w = await factory()
    w.vm.startAdd()
    w.vm.form.nif = '12345678Z'
    await w.vm.checkNif()
    await w.vm.$nextTick()

    expect(w.find('.modal-overlay').exists()).toBe(true)
    expect(w.text()).toContain('Luis Soto')
    expect(confirmSpy).not.toHaveBeenCalled()
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

  it('askDelete abre el popup propio de confirmación (no el del navegador)', async () => {
    svc.listar.mockResolvedValue([medico()])
    const confirmSpy = vi.spyOn(window, 'confirm')
    const w = await factory()
    await w.find('.actions-cell .btn-danger').trigger('click')
    await w.vm.$nextTick()

    expect(w.vm.deleteTarget).toEqual(medico())
    expect(w.find('.modal-overlay').exists()).toBe(true)
    expect(confirmSpy).not.toHaveBeenCalled()
  })

  it('confirmEliminar borra el médico objetivo, recarga y muestra un mensaje de éxito', async () => {
    const w = await factory()
    w.vm.deleteTarget = medico({ id: 5 })
    await w.vm.confirmEliminar()
    await flushPromises()
    expect(svc.eliminar).toHaveBeenCalledWith(5)
    expect(svc.listar).toHaveBeenCalledTimes(2)
    expect(w.vm.deleteTarget).toBeNull()
    expect(w.vm.resultMessage).toBe('Médico eliminado correctamente.')
    expect(w.vm.resultError).toBe(false)
    await w.vm.$nextTick()
    expect(w.find('.alert-success').exists()).toBe(true)
  })

  it('confirmEliminar muestra un mensaje de error si falla el borrado', async () => {
    svc.eliminar.mockRejectedValueOnce(new Error('fallo'))
    const w = await factory()
    w.vm.deleteTarget = medico({ id: 5 })
    await w.vm.confirmEliminar()
    await flushPromises()
    await w.vm.$nextTick()

    expect(w.vm.resultMessage).toBe('Error eliminando médico')
    expect(w.vm.resultError).toBe(true)
    expect(w.find('.alert-danger').exists()).toBe(true)
  })

  it('cancelDelete cierra el popup sin borrar nada', async () => {
    const w = await factory()
    w.vm.deleteTarget = medico({ id: 5 })
    w.vm.cancelDelete()
    expect(w.vm.deleteTarget).toBeNull()
    expect(svc.eliminar).not.toHaveBeenCalled()
  })

  it('askRemoveProfile abre el popup propio de confirmación (no el del navegador)', async () => {
    svc.listar.mockResolvedValue([medico()])
    const confirmSpy = vi.spyOn(window, 'confirm')
    const w = await factory()
    const removeBtn = w.findAll('.actions-cell button')[2]
    await removeBtn.trigger('click')
    await w.vm.$nextTick()

    expect(w.vm.removeProfileTarget).toEqual(medico())
    expect(w.find('.modal-overlay').exists()).toBe(true)
    expect(confirmSpy).not.toHaveBeenCalled()
  })

  it('confirmQuitarPerfil desasigna el perfil MEDICO del objetivo y muestra un mensaje de éxito', async () => {
    const w = await factory()
    w.vm.removeProfileTarget = medico({ id: 7 })
    await w.vm.confirmQuitarPerfil()
    await flushPromises()
    expect(svc.setPerfilMedico).toHaveBeenCalledWith(7, false)
    expect(w.vm.removeProfileTarget).toBeNull()
    expect(w.vm.resultMessage).toBe('Perfil de médico retirado correctamente.')
    expect(w.vm.resultError).toBe(false)
    await w.vm.$nextTick()
    expect(w.find('.alert-success').exists()).toBe(true)
  })

  it('confirmQuitarPerfil muestra un mensaje de error si falla la desasignación', async () => {
    svc.setPerfilMedico.mockRejectedValueOnce(new Error('fallo'))
    const w = await factory()
    w.vm.removeProfileTarget = medico({ id: 7 })
    await w.vm.confirmQuitarPerfil()
    await flushPromises()
    await w.vm.$nextTick()

    expect(w.vm.resultMessage).toBe('Error quitando perfil')
    expect(w.vm.resultError).toBe(true)
    expect(w.find('.alert-danger').exists()).toBe(true)
  })

  it('askDelete y askRemoveProfile limpian un mensaje de resultado anterior', async () => {
    const w = await factory()
    w.vm.resultMessage = 'mensaje previo'
    w.vm.askDelete(medico({ id: 9 }))
    expect(w.vm.resultMessage).toBe('')

    w.vm.resultMessage = 'otro mensaje previo'
    w.vm.askRemoveProfile(medico({ id: 9 }))
    expect(w.vm.resultMessage).toBe('')
  })

  it('cancelRemoveProfile cierra el popup sin llamar al servicio', async () => {
    const w = await factory()
    w.vm.removeProfileTarget = medico({ id: 7 })
    w.vm.cancelRemoveProfile()
    expect(w.vm.removeProfileTarget).toBeNull()
    expect(svc.setPerfilMedico).not.toHaveBeenCalled()
  })

  it('el formulario de añadir/editar médico usa campos con etiqueta, como el resto de la app', async () => {
    const w = await factory()
    w.vm.startAdd()
    await w.vm.$nextTick()
    w.vm.showNifStep = false
    w.vm.showForm = true
    await w.vm.$nextTick()

    expect(w.find('label[for="medico-nombre"]').exists()).toBe(true)
    expect(w.find('input#medico-nombre.form-input').exists()).toBe(true)
    expect(w.find('label[for="medico-apellido1"]').exists()).toBe(true)
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

  it('nombreCompleto e initials combinan nombre y apellidos', async () => {
    const w = await factory()
    expect(w.vm.nombreCompleto(medico())).toBe('Ana L')
    expect(w.vm.initials(medico())).toBe('AL')
  })

  it('los botones de acción son iconos con título y aria-label descriptivos', async () => {
    svc.listar.mockResolvedValue([medico()])
    const w = await factory()
    const [editBtn, deleteBtn, removeBtn] = w.findAll('.actions-cell button')

    expect(editBtn.find('svg').exists()).toBe(true)
    expect(editBtn.text()).toBe('')
    expect(editBtn.attributes('title')).toBe('Editar médico')
    expect(editBtn.attributes('aria-label')).toBe('Editar a Ana L')

    expect(deleteBtn.classes()).toContain('btn-danger')
    expect(deleteBtn.attributes('title')).toBe('Eliminar médico')
    expect(deleteBtn.attributes('aria-label')).toBe('Eliminar a Ana L')

    expect(removeBtn.attributes('title')).toBe('Quitar perfil')
    expect(removeBtn.attributes('aria-label')).toBe('Quitar el perfil de médico a Ana L')
  })

  it('muestra la sugerencia del estado vacío', async () => {
    const w = await factory()
    expect(w.find('.empty-state-small').text()).toContain('Añade el primero con el botón de arriba.')
  })
})
