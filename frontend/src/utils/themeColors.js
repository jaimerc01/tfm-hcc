export function getThemeColor(variableName, fallback) {
  if (typeof window === 'undefined' || typeof document === 'undefined') {
    return fallback
  }

  const value = getComputedStyle(document.documentElement)
    .getPropertyValue(variableName)
    .trim()

  return value || fallback
}
