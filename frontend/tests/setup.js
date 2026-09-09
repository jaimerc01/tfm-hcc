import { config } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import { vi, beforeEach } from 'vitest'
import es from '@/locales/es.json'
import gl from '@/locales/gl.json'
import privacyEs from '@/locales/privacy_es.json'
import privacyGl from '@/locales/privacy_gl.json'

// i18n real (mismas claves que producción) para que los componentes puedan
// resolver $t() y el test compruebe textos traducidos de verdad.
export const i18n = createI18n({
  legacy: false,
  locale: 'es',
  fallbackLocale: 'es',
  messages: {
    es: { ...es, ...privacyEs },
    gl: { ...gl, ...privacyGl }
  }
})

config.global.plugins = [i18n]

// vue-i18n emite este aviso en entorno de test cuando un componente con useI18n()
// se monta sin un ámbito i18n padre; la traducción se resuelve igualmente por el
// ámbito global (que sí tiene los mensajes), así que es ruido de test.
const originalWarn = console.warn
console.warn = (...args) => {
  if (typeof args[0] === 'string' && args[0].includes('Not found parent scope')) return
  originalWarn(...args)
}

// jsdom no implementa estas APIs que usan algunos componentes/composables.
if (!window.matchMedia) {
  window.matchMedia = vi.fn().mockImplementation((query) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn()
  }))
}

if (!window.URL.createObjectURL) {
  window.URL.createObjectURL = vi.fn(() => 'blob:mock')
  window.URL.revokeObjectURL = vi.fn()
}

beforeEach(() => {
  localStorage.clear()
  vi.clearAllMocks()
  i18n.global.locale.value = 'es'
})
