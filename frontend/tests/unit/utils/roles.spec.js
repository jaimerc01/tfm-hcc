import { describe, it, expect } from 'vitest'
import { ROLES, extractRoles, hasRole } from '@/utils/roles'

describe('roles', () => {
  it('ROLES es un objeto congelado con los tres roles de negocio', () => {
    expect(ROLES).toEqual({ PACIENTE: 'PACIENTE', MEDICO: 'MEDICO', ADMINISTRADOR: 'ADMINISTRADOR' })
    expect(Object.isFrozen(ROLES)).toBe(true)
  })

  describe('extractRoles', () => {
    it('devuelve [] cuando no hay claims o no hay authorities', () => {
      expect(extractRoles(null)).toEqual([])
      expect(extractRoles(undefined)).toEqual([])
      expect(extractRoles({})).toEqual([])
    })

    it('normaliza authorities como array de strings quitando ROLE_ y en mayúsculas', () => {
      expect(extractRoles({ authorities: ['ROLE_MEDICO', 'role_paciente'] })).toEqual(['MEDICO', 'PACIENTE'])
    })

    it('acepta authorities como valor único (no array)', () => {
      expect(extractRoles({ authorities: 'ROLE_ADMINISTRADOR' })).toEqual(['ADMINISTRADOR'])
    })

    it('acepta authorities como objetos con authority/role/name', () => {
      expect(extractRoles({ authorities: [{ authority: 'ROLE_MEDICO' }, { role: 'paciente' }, { name: 'ADMIN' }] }))
        .toEqual(['MEDICO', 'PACIENTE', 'ADMIN'])
    })

    it('filtra entradas vacías', () => {
      expect(extractRoles({ authorities: ['', null, 'ROLE_MEDICO'] })).toEqual(['MEDICO'])
    })
  })

  describe('hasRole', () => {
    it('detecta un rol presente sin importar mayúsculas', () => {
      expect(hasRole({ authorities: ['ROLE_MEDICO'] }, 'medico')).toBe(true)
      expect(hasRole({ authorities: ['ROLE_MEDICO'] }, ROLES.MEDICO)).toBe(true)
    })

    it('devuelve false si el rol no está', () => {
      expect(hasRole({ authorities: ['ROLE_PACIENTE'] }, ROLES.ADMINISTRADOR)).toBe(false)
    })
  })
})
