// Roles de negocio de la aplicación, tal y como los emite el backend
// (ver AutenticacionFacadeImpl#generarTokenConAuthorities): claim "authorities"
// del JWT, con valores tipo "ROLE_MEDICO".
export const ROLES = Object.freeze({
  PACIENTE: 'PACIENTE',
  MEDICO: 'MEDICO',
  ADMINISTRADOR: 'ADMINISTRADOR'
})

const ROLE_PREFIX = /^ROLE_/

function normalizeAuthority(authority) {
  if (!authority) return ''
  const value = typeof authority === 'string'
    ? authority
    : (authority.authority || authority.role || authority.name || '')
  return String(value).toUpperCase().replace(ROLE_PREFIX, '')
}

// Extrae los roles normalizados (sin prefijo "ROLE_", en mayúsculas) a partir
// de los claims decodificados del JWT (ver authService.getCurrentUser()).
export function extractRoles(claims) {
  const authorities = claims?.authorities
  const list = Array.isArray(authorities) ? authorities : (authorities ? [authorities] : [])
  return list.map(normalizeAuthority).filter(Boolean)
}

export function hasRole(claims, role) {
  return extractRoles(claims).includes(String(role).toUpperCase())
}
