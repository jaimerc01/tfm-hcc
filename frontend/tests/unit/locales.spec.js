import { describe, it, expect } from 'vitest'
import { readFileSync, readdirSync, statSync } from 'node:fs'
import { join, relative } from 'node:path'
import es from '@/locales/es.json'
import gl from '@/locales/gl.json'
import privacyEs from '@/locales/privacy_es.json'
import privacyGl from '@/locales/privacy_gl.json'

// vitest se ejecuta con el cwd en la raíz del frontend.
const srcDir = join(process.cwd(), 'src')

const placeholders = (value) => (String(value).match(/\{[a-zA-Z0-9_]+\}/g) || []).sort()

function walk(dir) {
  const out = []
  for (const name of readdirSync(dir)) {
    const full = join(dir, name)
    if (statSync(full).isDirectory()) out.push(...walk(full))
    else if (/\.(vue|js)$/.test(name)) out.push(full)
  }
  return out
}

// Claves que se construyen dinámicamente en el código (interpolación) y que por
// tanto no aparecen como literal completo; se validan aparte o se aceptan como prefijo.
const DYNAMIC_KEY_PREFIXES = [
  'analysis_chart_type_'
]

describe('paridad de locales es/gl', () => {
  const esKeys = Object.keys(es)
  const glKeys = Object.keys(gl)

  it('es.json y gl.json tienen exactamente el mismo conjunto de claves', () => {
    const soloEs = esKeys.filter(k => !(k in gl))
    const soloGl = glKeys.filter(k => !(k in es))
    expect({ soloEs, soloGl }).toEqual({ soloEs: [], soloGl: [] })
  })

  it('privacy_es.json y privacy_gl.json tienen las mismas claves', () => {
    const soloEs = Object.keys(privacyEs).filter(k => !(k in privacyGl))
    const soloGl = Object.keys(privacyGl).filter(k => !(k in privacyEs))
    expect({ soloEs, soloGl }).toEqual({ soloEs: [], soloGl: [] })
  })

  it('ninguna traducción está vacía', () => {
    const vaciasEs = esKeys.filter(k => !String(es[k]).trim())
    const vaciasGl = glKeys.filter(k => !String(gl[k]).trim())
    expect({ vaciasEs, vaciasGl }).toEqual({ vaciasEs: [], vaciasGl: [] })
  })

  it('cada clave usa los mismos placeholders {x} en es y en gl', () => {
    const desajustes = esKeys
      .filter(k => k in gl)
      .filter(k => JSON.stringify(placeholders(es[k])) !== JSON.stringify(placeholders(gl[k])))
      .map(k => ({ key: k, es: placeholders(es[k]), gl: placeholders(gl[k]) }))
    expect(desajustes).toEqual([])
  })

  it('no hay claves duplicadas en los ficheros JSON', () => {
    for (const file of ['es.json', 'gl.json']) {
      const raw = readFileSync(join(srcDir, 'locales', file), 'utf8')
      const keys = [...raw.matchAll(/^\s*"([^"]+)"\s*:/gm)].map(m => m[1])
      const dup = keys.filter((k, i) => keys.indexOf(k) !== i)
      expect({ file, dup }).toEqual({ file, dup: [] })
    }
  })
})

describe('todas las claves i18n usadas en el código existen en los locales', () => {
  const known = new Set([...Object.keys(es), ...Object.keys(privacyEs)])
  const files = walk(srcDir)
  const usages = []

  for (const file of files) {
    const content = readFileSync(file, 'utf8')
    // $t('clave') | $t("clave") | tService('clave') | i18n-t keypath="clave"
    const re = /(?:\$t|tService|\bt)\(\s*(['"])([a-zA-Z0-9_.]+)\1|keypath=(['"])([a-zA-Z0-9_.]+)\3/g
    let m
    while ((m = re.exec(content))) {
      const key = m[2] || m[4]
      if (key) usages.push({ key, file: relative(srcDir, file) })
    }
  }

  it('encuentra usos de i18n en el código (sanidad del propio test)', () => {
    expect(usages.length).toBeGreaterThan(50)
  })

  it('no hay ninguna clave i18n literal sin traducción en es.json', () => {
    const faltan = usages
      .filter(u => !known.has(u.key))
      .filter(u => !DYNAMIC_KEY_PREFIXES.some(p => u.key.startsWith(p)))
      // de-duplica por clave para un informe legible
      .filter((u, i, arr) => arr.findIndex(x => x.key === u.key) === i)
    expect(faltan).toEqual([])
  })
})
