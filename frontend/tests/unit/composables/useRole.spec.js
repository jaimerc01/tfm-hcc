import { describe, it, expect, vi, beforeEach } from 'vitest'

const { getCurrentUser } = vi.hoisted(() => ({ getCurrentUser: vi.fn() }))
vi.mock('@/services/authService', () => ({ default: { getCurrentUser } }))

import { useRole } from '@/composables/useRole'

describe('useRole', () => {
  beforeEach(() => getCurrentUser.mockReset())

  it('marca isPaciente cuando el JWT tiene ROLE_PACIENTE', () => {
    getCurrentUser.mockReturnValue({ authorities: ['ROLE_PACIENTE'] })
    const { isPaciente, isMedico, isAdmin } = useRole()
    expect(isPaciente.value).toBe(true)
    expect(isMedico.value).toBe(false)
    expect(isAdmin.value).toBe(false)
  })

  it('marca isMedico e isAdmin según authorities', () => {
    getCurrentUser.mockReturnValue({ authorities: ['ROLE_MEDICO', 'ROLE_ADMINISTRADOR'] })
    const { isMedico, isAdmin } = useRole()
    expect(isMedico.value).toBe(true)
    expect(isAdmin.value).toBe(true)
  })

  it('todo false si no hay usuario', () => {
    getCurrentUser.mockReturnValue(null)
    const { isPaciente, isMedico, isAdmin } = useRole()
    expect([isPaciente.value, isMedico.value, isAdmin.value]).toEqual([false, false, false])
  })
})
