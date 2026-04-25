import es from '@/locales/es.json'
import gl from '@/locales/gl.json'

const FALLBACK_LOCALE = 'es'
const SUPPORTED_LOCALES = new Set(['es', 'gl'])
const MESSAGES = { es, gl }

function getCurrentLocale() {
  try {
    const stored = localStorage.getItem('app-locale')
    if (stored && SUPPORTED_LOCALES.has(stored)) return stored
  } catch (_) { void 0 }

  if (typeof navigator !== 'undefined' && navigator.language) {
    const fromNavigator = String(navigator.language).slice(0, 2).toLowerCase()
    if (SUPPORTED_LOCALES.has(fromNavigator)) return fromNavigator
  }

  return FALLBACK_LOCALE
}

export function tService(key, fallback = key) {
  const locale = getCurrentLocale()
  return MESSAGES[locale]?.[key] || MESSAGES[FALLBACK_LOCALE]?.[key] || fallback
}
