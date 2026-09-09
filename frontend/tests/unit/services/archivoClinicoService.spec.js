import { describe, it, expect, vi, beforeEach } from 'vitest'

const { client, create } = vi.hoisted(() => {
  const client = {
    get: vi.fn(() => Promise.resolve({ data: {}, headers: {} })),
    post: vi.fn(() => Promise.resolve({ data: {} })),
    put: vi.fn(),
    delete: vi.fn(() => Promise.resolve()),
    interceptors: { request: { use: vi.fn() } }
  }
  return { client, create: vi.fn(() => client) }
})
vi.mock('axios', () => ({ default: { create } }))

import svc from '@/services/archivoClinicoService'

describe('archivoClinicoService', () => {
  beforeEach(() => { client.get.mockClear(); client.post.mockClear(); client.delete.mockClear() })

  it('list devuelve data de GET /historia/archivos', async () => {
    client.get.mockResolvedValueOnce({ data: [{ id: 1 }], headers: {} })
    await expect(svc.list()).resolves.toEqual([{ id: 1 }])
  })

  it('upload envía FormData con multipart/form-data', async () => {
    client.post.mockResolvedValueOnce({ data: { id: 2 } })
    const file = new File(['x'], 'a.pdf', { type: 'application/pdf' })
    await svc.upload(file)
    const [url, form, opts] = client.post.mock.calls[0]
    expect(url).toBe('/historia/archivos')
    expect(form).toBeInstanceOf(FormData)
    expect(form.get('file')).toBe(file)
    expect(opts.headers['Content-Type']).toBe('multipart/form-data')
  })

  it('download infiere el filename de filename*=UTF-8', async () => {
    client.get.mockResolvedValueOnce({
      data: new Blob(['x']),
      headers: { 'content-disposition': "attachment; filename*=UTF-8''an%C3%A1lisis.pdf" }
    })
    const { filename } = await svc.download(5)
    expect(filename).toBe('análisis.pdf')
  })

  it('download infiere el filename de filename="..."', async () => {
    client.get.mockResolvedValueOnce({
      data: new Blob(['x']),
      headers: { 'content-disposition': 'attachment; filename="informe.pdf"' }
    })
    const { filename } = await svc.download(6)
    expect(filename).toBe('informe.pdf')
  })

  it('download usa un nombre por defecto si no hay content-disposition', async () => {
    client.get.mockResolvedValueOnce({ data: new Blob(['x']), headers: {} })
    const { filename } = await svc.download(7)
    expect(filename).toBe('archivo_7')
  })

  it('remove hace DELETE a la ruta con id', async () => {
    await svc.remove(9)
    expect(client.delete).toHaveBeenCalledWith('/historia/archivos/9')
  })
})
