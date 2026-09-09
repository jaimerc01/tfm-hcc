import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { ref } from 'vue'

const logout = vi.hoisted(() => vi.fn(() => Promise.resolve()))
const roles = vi.hoisted(() => ({ isMedico: { value: false }, isAdmin: { value: false }, isPaciente: { value: false } }))
const push = vi.hoisted(() => vi.fn())

vi.mock('@/composables/useAuth', () => ({ useAuth: () => ({ logout }) }))
vi.mock('@/composables/useRole', () => ({ useRole: () => roles }))
vi.mock('vue-router', () => ({ useRouter: () => ({ push }) }))
vi.mock('@/components/NotificationsDropdown.vue', () => ({ default: { name: 'NotificationsDropdown', template: '<div class="notif-stub" />' } }))

import Navbar from '@/components/Navbar.vue'
import { i18n } from '../../setup'

const stubs = { RouterLink: { template: '<a><slot /></a>', props: ['to'] } }
const mountNav = () => mount(Navbar, { global: { stubs } })

beforeEach(() => {
  vi.clearAllMocks()
  roles.isMedico = ref(false)
  roles.isAdmin = ref(false)
  roles.isPaciente = ref(false)
  localStorage.clear()
  i18n.global.locale.value = 'es'
})

describe('Navbar', () => {
  it('no muestra enlaces de médico/paciente/admin por defecto', () => {
    const w = mountNav()
    expect(w.text()).not.toContain('Zona médica')
    expect(w.findAll('a').some(a => a.text() === 'Admin')).toBe(false)
  })

  it('muestra el enlace de zona médica si isMedico', () => {
    roles.isMedico = ref(true)
    const w = mountNav()
    expect(w.text().toLowerCase()).toContain('médic')
  })

  it('muestra "Mis solicitudes" si isPaciente', () => {
    roles.isPaciente = ref(true)
    const w = mountNav()
    expect(w.text().toLowerCase()).toContain('solicitud')
  })

  it('muestra "Mis solicitudes" también si isMedico (solicitudes enviadas)', () => {
    roles.isMedico = ref(true)
    const w = mountNav()
    expect(w.text().toLowerCase()).toContain('solicitud')
  })

  it('no muestra "Mis solicitudes" si no es ni paciente ni médico', () => {
    const w = mountNav()
    expect(w.text().toLowerCase()).not.toContain('solicitud')
  })

  it('toggle del menú alterna la clase abierta', async () => {
    const w = mountNav()
    expect(w.find('.nav__links--open').exists()).toBe(false)
    await w.find('.nav__toggle').trigger('click')
    expect(w.find('.nav__links--open').exists()).toBe(true)
  })

  it('admin: muestra un único enlace directo a Administración, sin desplegable', () => {
    roles.isAdmin = ref(true)
    const w = mountNav()
    expect(w.findAll('a').some(a => a.text() === 'Administración')).toBe(true)
    expect(w.find('.nav__dropdown-toggle').exists()).toBe(false)
  })

  it('abre el modal de logout y confirma: logout + redirección a Login', async () => {
    const w = mountNav()
    await w.find('.nav__logout').trigger('click')
    expect(w.find('[role="dialog"]').exists()).toBe(true)
    await w.find('.modal-btn--confirm').trigger('click')
    await flushPromises()
    expect(logout).toHaveBeenCalled()
    expect(push).toHaveBeenCalledWith({ name: 'Login' })
  })

  it('cancelar cierra el modal de logout', async () => {
    const w = mountNav()
    await w.find('.nav__logout').trigger('click')
    await w.find('.modal-btn--cancel').trigger('click')
    expect(w.find('[role="dialog"]').exists()).toBe(false)
  })

  it('cambiar de idioma persiste el locale y recarga', async () => {
    const reload = vi.fn()
    vi.stubGlobal('location', { ...window.location, reload })
    const w = mountNav()
    const glBtn = w.findAll('.nav__language-option').find(b => b.text().toLowerCase().includes('galeg') || b.text().toLowerCase().includes('gallego'))
    await glBtn.trigger('click')
    expect(localStorage.getItem('app-locale')).toBe('gl')
    expect(reload).toHaveBeenCalled()
    vi.unstubAllGlobals()
  })

  it('seleccionar el idioma ya activo no hace nada', async () => {
    const reload = vi.fn()
    vi.stubGlobal('location', { ...window.location, reload })
    const w = mountNav()
    const esBtn = w.findAll('.nav__language-option').find(b => b.text().toLowerCase().includes('espa'))
    await esBtn.trigger('click')
    expect(reload).not.toHaveBeenCalled()
    vi.unstubAllGlobals()
  })
})
