/**
 * Usuarios de prueba que siembra `tests/seed.setup.ts`. Los NIF son válidos según
 * el algoritmo de dígito de control español (el frontend y el backend los validan).
 */
export const PASSWORD = 'Secreto123'

export const ADMIN = {
  nif: '00000001R',
  email: 'admin.e2e@example.com',
  nombre: 'Admin',
  apellido1: 'E2E',
  fechaNacimiento: '1980-01-01T00:00:00',
}

export const MEDICO = {
  nif: '11111111H',
  email: 'medico.e2e@example.com',
  nombre: 'Marta',
  apellido1: 'Médica',
  fechaNacimiento: '1985-03-15T00:00:00',
}

export const PACIENTE = {
  nif: '12345678Z',
  email: 'paciente.e2e@example.com',
  nombre: 'Pablo',
  apellido1: 'Paciente',
  fechaNacimiento: '1990-06-10T00:00:00',
}

/** Paciente que el médico busca y solicita en el recorrido de asignación. */
export const PACIENTE_BUSCADO = {
  nif: '22222222J',
  email: 'paciente2.e2e@example.com',
  nombre: 'Lucía',
  apellido1: 'Buscada',
  fechaNacimiento: '1995-05-05T00:00:00',
  // La búsqueda del médico usa la parte de fecha (YYYY-MM-DD).
  fechaNacimientoInput: '1995-05-05',
}

export const TODOS = [ADMIN, MEDICO, PACIENTE, PACIENTE_BUSCADO]
