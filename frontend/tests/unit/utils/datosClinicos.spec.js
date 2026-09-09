import { describe, it, expect, vi, afterEach } from 'vitest'
import { normalizarNombre, mapTipoToKey, getAnalyteLabel, formatDate, applyRangos, mapHistorialEntries } from '@/utils/datosClinicos'

describe('datosClinicos', () => {
  afterEach(() => vi.useRealTimers())

  describe('normalizarNombre', () => {
    it('devuelve cadena vacía para valores falsy', () => {
      expect(normalizarNombre('')).toBe('')
      expect(normalizarNombre(null)).toBe('')
      expect(normalizarNombre(undefined)).toBe('')
    })

    it('quita acentos, pasa a minúsculas y elimina espacios', () => {
      expect(normalizarNombre('  Colesterol Total ')).toBe('colesteroltotal')
      expect(normalizarNombre('Presión Arterial Sistólica')).toBe('presionarterialsistolica')
    })
  })

  describe('mapTipoToKey', () => {
    const analytes = [
      { key: 'glucosa', aliases: ['glucosa', 'glucose', 'glicosa'] },
      { key: 'Colesterol LDL', aliases: ['colesterol ldl', 'ldl'] }
    ]

    it('devuelve null si no hay tipo', () => {
      expect(mapTipoToKey('', analytes)).toBe(null)
      expect(mapTipoToKey(null, analytes)).toBe(null)
    })

    it('mapea por key normalizada', () => {
      expect(mapTipoToKey('Glucosa', analytes)).toBe('glucosa')
    })

    it('mapea por alias normalizado', () => {
      expect(mapTipoToKey('LDL', analytes)).toBe('Colesterol LDL')
      expect(mapTipoToKey('glicosa', analytes)).toBe('glucosa')
    })

    it('devuelve el nombre normalizado si no encuentra analito', () => {
      expect(mapTipoToKey('Desconocido', analytes)).toBe('desconocido')
    })

    it('tolera lista de analitos ausente', () => {
      expect(mapTipoToKey('Glucosa')).toBe('glucosa')
    })
  })

  describe('getAnalyteLabel', () => {
    const t = (k) => `T:${k}`
    it('devuelve cadena vacía sin analito', () => {
      expect(getAnalyteLabel(null, t)).toBe('')
    })
    it('usa labelKey si existe, si no la key', () => {
      expect(getAnalyteLabel({ labelKey: 'glucose', key: 'glucosa' }, t)).toBe('T:glucose')
      expect(getAnalyteLabel({ key: 'glucosa' }, t)).toBe('T:glucosa')
    })
  })

  describe('applyRangos', () => {
    it('rellena recommendedMin/Max del analito por coincidencia de nombre o alias', () => {
      const analytes = [
        { key: 'glucosa', aliases: ['glucose'], recommendedMin: null, recommendedMax: null },
        { key: 'colesterol', aliases: [], recommendedMin: null, recommendedMax: null }
      ]
      applyRangos(analytes, [
        { nombre: 'Glucose', valorInferiorNumerico: 70, valorSuperiorNumerico: 110 },
        { nombre: 'sin-analito', valorInferiorNumerico: 1, valorSuperiorNumerico: 2 }
      ])
      expect(analytes[0].recommendedMin).toBe(70)
      expect(analytes[0].recommendedMax).toBe(110)
      expect(analytes[1].recommendedMin).toBeNull()
    })

    it('tolera entradas no-array', () => {
      expect(applyRangos(null, [])).toBeNull()
      expect(applyRangos([{ key: 'x' }], null)).toEqual([{ key: 'x' }])
    })
  })

  describe('mapHistorialEntries', () => {
    const analytes = [{ key: 'glucosa', aliases: ['glucosa'], unit: 'mg/dL' }]

    it('normaliza el array crudo a la forma de las gráficas', () => {
      const out = mapHistorialEntries([
        { id: '1', tipo: 'Glucosa', valor: 95, fechaCreacion: '2026-01-01T00:00:00Z' }
      ], analytes)
      expect(out[0]).toMatchObject({ id: '1', key: 'glucosa', value: '95', unit: 'mg/dL', createdAt: '2026-01-01T00:00:00Z' })
    })

    it('parsea un string JSON', () => {
      const out = mapHistorialEntries('[{"key":"glucosa","value":80}]', analytes)
      expect(out).toHaveLength(1)
      expect(out[0].value).toBe('80')
    })

    it('devuelve [] ante JSON inválido o valores no-array', () => {
      expect(mapHistorialEntries('no-json', analytes)).toEqual([])
      expect(mapHistorialEntries({ a: 1 }, analytes)).toEqual([])
    })
  })

  describe('formatDate', () => {
    it('devuelve cadena vacía sin iso', () => {
      expect(formatDate('')).toBe('')
      expect(formatDate(null)).toBe('')
    })

    it('formatea una fecha ISO usando toLocaleString', () => {
      const iso = '2026-01-15T10:30:00.000Z'
      expect(formatDate(iso)).toBe(new Date(iso).toLocaleString())
    })

    it('devuelve el valor original si Date lanza', () => {
      const spy = vi.spyOn(global, 'Date').mockImplementation(() => { throw new Error('boom') })
      expect(formatDate('no-fecha')).toBe('no-fecha')
      spy.mockRestore()
    })
  })
})
