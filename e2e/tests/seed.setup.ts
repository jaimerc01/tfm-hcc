import { test as setup, expect, request } from '@playwright/test'
import { Client } from 'pg'
import { writeFileSync } from 'node:fs'
import { join } from 'node:path'
import { BACKEND_URL } from '../playwright.config'
import { ADMIN, MEDICO, PACIENTE, PASSWORD, TODOS } from '../credentials'

const PG = process.env.E2E_PG_URL ?? 'postgresql://hcc:hcc@localhost:5433/hcc_e2e'
const ROLES = ['PACIENTE', 'MEDICO', 'ADMINISTRADOR']

/** Espera a que el backend responda de verdad (el puerto abre antes de estar listo). */
async function esperarBackend(api: Awaited<ReturnType<typeof request.newContext>>) {
  for (let intento = 0; intento < 60; intento++) {
    try {
      const r = await api.post('/authentication/login', { data: {}, timeout: 5_000 })
      if (r.status() < 500) return
    } catch {
      /* aún no responde */
    }
    await new Promise((r) => setTimeout(r, 2_000))
  }
  throw new Error('El backend no respondió a tiempo')
}

async function sembrarPerfiles(pg: Client) {
  for (const rol of ROLES) {
    await pg.query(
      `INSERT INTO perfil (id, rol, fecha_creacion, fecha_ultima_modificacion)
       VALUES (gen_random_uuid(), $1, now(), now())
       ON CONFLICT (rol) DO NOTHING`,
      [rol],
    )
  }
}

/** Alta por el endpoint real (tolera que el usuario ya exista) + login + id vía /usuario/me. */
async function crearUsuarioYObtenerId(api: Awaited<ReturnType<typeof request.newContext>>, u: typeof PACIENTE) {
  await api.post('/authentication/signup', {
    data: {
      nombre: u.nombre,
      apellido1: u.apellido1,
      nif: u.nif,
      email: u.email,
      password: PASSWORD,
      fechaNacimiento: u.fechaNacimiento,
    },
  })

  const login = await api.post('/authentication/login', { data: { nif: u.nif, password: PASSWORD } })
  expect(login.ok(), `login de ${u.nif}`).toBeTruthy()
  const token = (await login.json()).token as string

  const me = await api.get('/usuario/me', { headers: { Authorization: `Bearer ${token}` } })
  expect(me.ok()).toBeTruthy()
  return (await me.json()).id as string
}

async function asignarRol(pg: Client, usuarioId: string, rol: string) {
  await pg.query(
    `INSERT INTO perfil_usuario (id, id_perfil, id_usuario, fecha_creacion, fecha_ultima_modificacion)
     SELECT gen_random_uuid(), p.id, $1, now(), now() FROM perfil p
     WHERE p.rol = $2
       AND NOT EXISTS (
         SELECT 1 FROM perfil_usuario pu WHERE pu.id_usuario = $1 AND pu.id_perfil = p.id
       )`,
    [usuarioId, rol],
  )
}

setup('siembra perfiles y usuarios de prueba', async () => {
  setup.setTimeout(180_000)

  const pg = new Client({ connectionString: PG })
  await pg.connect()
  const api = await request.newContext({ baseURL: BACKEND_URL })

  try {
    await esperarBackend(api)
    await sembrarPerfiles(pg)

    const ids: Record<string, string> = {}
    for (const u of TODOS) {
      ids[u.nif] = await crearUsuarioYObtenerId(api, u)
    }

    await asignarRol(pg, ids[ADMIN.nif], 'ADMINISTRADOR')
    await asignarRol(pg, ids[MEDICO.nif], 'MEDICO')

    // Deja los ids a disposición de los specs (por si alguno los necesita).
    writeFileSync(join(__dirname, '..', '.seed-ids.json'), JSON.stringify(ids, null, 2))
  } finally {
    await api.dispose()
    await pg.end()
  }
})
