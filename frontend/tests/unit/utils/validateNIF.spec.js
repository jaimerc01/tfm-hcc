import { describe, it, expect } from 'vitest'
import { validateNIF } from '@/utils/validateNIF'

describe('validateNIF', () => {
  it('rechaza valores vacíos o no string', () => {
    expect(validateNIF('')).toBe(false)
    expect(validateNIF(null)).toBe(false)
    expect(validateNIF(undefined)).toBe(false)
    expect(validateNIF(12345678)).toBe(false)
  })

  it('valida un NIF correcto (con y sin espacios/guiones)', () => {
    expect(validateNIF('12345678Z')).toBe(true)
    expect(validateNIF('12345678-Z')).toBe(true)
    expect(validateNIF('12345678 z')).toBe(true)
  })

  it('rechaza un NIF con letra de control incorrecta', () => {
    expect(validateNIF('12345678A')).toBe(false)
  })

  it('valida un NIE correcto', () => {
    // X1234567L -> convierte X->0, calcula sobre 01234567 % 23 = 10 -> 'L'
    expect(validateNIF('X1234567L')).toBe(true)
  })

  it('rechaza un NIE con letra de control incorrecta', () => {
    expect(validateNIF('X1234567A')).toBe(false)
  })

  it('acepta un CIF con formato válido (validación simplificada)', () => {
    expect(validateNIF('A12345678')).toBe(true)
  })

  it('rechaza cadenas con formato no reconocido', () => {
    expect(validateNIF('ABCDEFGHI')).toBe(false)
    expect(validateNIF('1234567Z')).toBe(false)
  })
})
