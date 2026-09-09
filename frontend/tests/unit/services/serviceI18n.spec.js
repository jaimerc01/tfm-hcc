import { describe, it, expect, vi, afterEach } from 'vitest'
import { tService } from '@/services/serviceI18n'
import es from '@/locales/es.json'
import gl from '@/locales/gl.json'

describe('tService', () => {
  afterEach(() => {
    localStorage.clear()
    vi.unstubAllGlobals()
  })

  it('usa el locale guardado en localStorage cuando es soportado', () => {
    const key = Object.keys(gl).find(k => gl[k] !== es[k])
    localStorage.setItem('app-locale', 'gl')
    expect(tService(key)).toBe(gl[key])
  })

  it('cae a español cuando el locale guardado no es soportado', () => {
    const key = Object.keys(es)[0]
    localStorage.setItem('app-locale', 'fr')
    expect(tService(key)).toBe(es[key])
  })

  it('usa navigator.language si no hay locale guardado', () => {
    vi.stubGlobal('navigator', { language: 'gl-ES' })
    const key = Object.keys(gl).find(k => gl[k] !== es[k])
    expect(tService(key)).toBe(gl[key])
  })

  it('devuelve el fallback proporcionado cuando la clave no existe', () => {
    expect(tService('clave_inexistente_xyz', 'por defecto')).toBe('por defecto')
  })

  it('devuelve la propia clave como fallback por defecto', () => {
    expect(tService('clave_inexistente_xyz')).toBe('clave_inexistente_xyz')
  })

  it('cae a la traducción española si la clave falta en el locale activo', () => {
    localStorage.setItem('app-locale', 'gl')
    // clave presente solo en es: buscamos una que exista en es pero no en gl
    const onlyEs = Object.keys(es).find(k => !(k in gl))
    if (onlyEs) {
      expect(tService(onlyEs)).toBe(es[onlyEs])
    }
  })
})
