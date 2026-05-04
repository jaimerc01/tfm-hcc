// src/utils/validateNIF.js
// Valida NIF, NIE y CIF españoles (básico)
export function validateNIF(nif) {
  if (!nif || typeof nif !== 'string') return false;
  nif = nif.toUpperCase().replace(/\s|-/g, '');
  // NIF: 8 dígitos + letra
  if (/^\d{8}[A-Z]$/.test(nif)) {
    const letras = 'TRWAGMYFPDXBNJZSQVHLCKE';
    return nif[8] === letras[parseInt(nif, 10) % 23];
  }
  // NIE: X/Y/Z + 7 dígitos + letra
  if (/^[XYZ]\d{7}[A-Z]$/.test(nif)) {
    const letras = 'TRWAGMYFPDXBNJZSQVHLCKE';
    let num = nif;
    num = num.replace('X', '0').replace('Y', '1').replace('Z', '2');
    return nif[8] === letras[parseInt(num.slice(0, 8), 10) % 23];
  }
  // CIF: Letra + 7 dígitos + letra/dígito
  if (/^[ABCDEFGHJKLMNPQRSUVW]\d{7}[0-9A-J]$/.test(nif)) {
    // Validación simplificada para CIF
    return true;
  }
  return false;
}
