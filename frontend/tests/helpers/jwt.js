// Construye un JWT de mentira (sin firma real) para los tests: solo importan los claims.
export function makeJwt(payload = {}) {
  const b64 = (obj) => Buffer.from(JSON.stringify(obj)).toString('base64')
    .replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')
  return `${b64({ alg: 'none', typ: 'JWT' })}.${b64(payload)}.sig`
}
