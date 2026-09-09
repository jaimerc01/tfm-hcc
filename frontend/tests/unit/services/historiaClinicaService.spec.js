import { describe, it, expect, vi, beforeEach } from 'vitest'

const state = vi.hoisted(() => ({ interceptor: null }))
const { client, create } = vi.hoisted(() => {
  const client = {
    get: vi.fn(() => Promise.resolve({ data: {} })),
    post: vi.fn(() => Promise.resolve({ data: {} })),
    put: vi.fn(() => Promise.resolve({ data: {} })),
    delete: vi.fn(() => Promise.resolve({ data: {} })),
    interceptors: { request: { use: vi.fn() } }
  }
  return { client, create: vi.fn(() => client) }
})

vi.mock('axios', () => ({ default: { create } }))

import svc from '@/services/historiaClinicaService'
state.interceptor = client.interceptors.request.use.mock.calls[0]?.[0]

describe('historiaClinicaService', () => {
  beforeEach(() => {
    client.get.mockClear(); client.post.mockClear(); client.put.mockClear(); client.delete.mockClear()
  })

  it('registra un interceptor de request para el token', () => {
    expect(state.interceptor).toBeTypeOf('function')
  })

  it('el interceptor añade Authorization solo si hay token en localStorage', () => {
    const interceptor = state.interceptor
    localStorage.removeItem('authToken')
    expect(interceptor({ headers: {} }).headers.Authorization).toBeUndefined()
    localStorage.setItem('authToken', 'abc.def.ghi')
    expect(interceptor({ headers: {} }).headers.Authorization).toBe('Bearer abc.def.ghi')
  })

  it('getMine hace GET /historia', () => {
    svc.getMine()
    expect(client.get).toHaveBeenCalledWith('/historia')
  })

  it('crearAntecedente hace POST con charset utf-8', () => {
    svc.crearAntecedente({ categoria: 'PERSONAL', descripcion: 'x' })
    expect(client.post).toHaveBeenCalledWith('/historia/antecedentes', { categoria: 'PERSONAL', descripcion: 'x' }, { headers: { 'Content-Type': 'application/json; charset=utf-8' } })
  })

  it('editarAntecedente hace PUT a la ruta con id', () => {
    svc.editarAntecedente(7, { descripcion: 'y' })
    expect(client.put).toHaveBeenCalledWith('/historia/antecedentes/7', { descripcion: 'y' }, expect.any(Object))
  })

  it('deleteAntecedente hace DELETE a la ruta con id', () => {
    svc.deleteAntecedente(3)
    expect(client.delete).toHaveBeenCalledWith('/historia/antecedentes/3')
  })

  it('crearAlergia y deleteAlergia', () => {
    svc.crearAlergia({ descripcion: 'polen' })
    expect(client.post).toHaveBeenCalledWith('/historia/alergias', { descripcion: 'polen' }, expect.any(Object))
    svc.deleteAlergia(9)
    expect(client.delete).toHaveBeenCalledWith('/historia/alergias/9')
  })

  it('deleteDatoClinico', () => {
    svc.deleteDatoClinico(11)
    expect(client.delete).toHaveBeenCalledWith('/historia/datos-clinicos/11')
  })

  it('análisis de sangre: update (PUT) y añadir (POST)', () => {
    svc.updateAnalisisSangre([{ tipo: 'Glucosa', valor: 90 }])
    expect(client.put).toHaveBeenCalledWith('/historia/analisis-sangre', [{ tipo: 'Glucosa', valor: 90 }])
    svc.añadirAnalisisSangre([{ tipo: 'Glucosa', valor: 91 }])
    expect(client.post).toHaveBeenCalledWith('/historia/analisis-sangre', [{ tipo: 'Glucosa', valor: 91 }])
  })

  it('signos vitales: update y añadir', () => {
    svc.updateSignosVitales([{ tipo: 'IMC', valor: 22 }])
    expect(client.put).toHaveBeenCalledWith('/historia/signos-vitales', [{ tipo: 'IMC', valor: 22 }])
    svc.añadirSignosVitales([{ tipo: 'IMC', valor: 23 }])
    expect(client.post).toHaveBeenCalledWith('/historia/signos-vitales', [{ tipo: 'IMC', valor: 23 }])
  })

  it('análisis de orina: update y añadir', () => {
    svc.updateAnalisisOrina([{ tipo: 'PH Orina', valor: 6 }])
    expect(client.put).toHaveBeenCalledWith('/historia/analisis-orina', [{ tipo: 'PH Orina', valor: 6 }])
    svc.añadirAnalisisOrina([{ tipo: 'PH Orina', valor: 7 }])
    expect(client.post).toHaveBeenCalledWith('/historia/analisis-orina', [{ tipo: 'PH Orina', valor: 7 }])
  })

  it('getRangos hace GET /rangos', () => {
    svc.getRangos()
    expect(client.get).toHaveBeenCalledWith('/rangos')
  })

  it('editarDatoClinico hace PUT al dato con la medición', () => {
    svc.editarDatoClinico('d1', { label: 'Glucosa', value: '95' })
    const [url, body] = client.put.mock.calls.at(-1)
    expect(url).toBe('/historia/datos-clinicos/d1')
    expect(body).toEqual({ label: 'Glucosa', value: '95' })
  })

  it('listarPropuestasCambio hace GET /historia/propuestas-cambio', () => {
    svc.listarPropuestasCambio()
    expect(client.get).toHaveBeenCalledWith('/historia/propuestas-cambio')
  })

  it('responderPropuestaCambio hace POST con { aceptar }', () => {
    svc.responderPropuestaCambio('p1', true)
    const [url, body] = client.post.mock.calls.at(-1)
    expect(url).toBe('/historia/propuestas-cambio/p1')
    expect(body).toEqual({ aceptar: true })
  })
})
