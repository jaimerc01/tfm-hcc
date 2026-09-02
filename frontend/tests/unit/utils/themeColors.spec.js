import { describe, it, expect, afterEach } from 'vitest'
import { getThemeColor } from '@/utils/themeColors'

describe('getThemeColor', () => {
  afterEach(() => {
    document.documentElement.style.removeProperty('--color-test')
  })

  it('lee el valor de una variable CSS del :root', () => {
    document.documentElement.style.setProperty('--color-test', '#123456')
    expect(getThemeColor('--color-test', '#000')).toBe('#123456')
  })

  it('devuelve el fallback si la variable no está definida', () => {
    expect(getThemeColor('--no-existe', '#abcdef')).toBe('#abcdef')
  })
})
