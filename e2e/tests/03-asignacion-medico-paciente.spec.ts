import { test, expect } from '@playwright/test'
import { login, logout } from '../helpers'
import { MEDICO, PACIENTE_BUSCADO } from '../credentials'

/**
 * Recorrido 3 (multi-actor): un médico busca un paciente y le envía una solicitud
 * de asignación; el paciente la acepta; el médico ya puede ver su historial.
 */
test('un médico solicita acceso, el paciente lo acepta y el médico ve el historial', async ({ page }) => {
  // --- El médico busca al paciente y solicita la asignación ---
  await login(page, MEDICO.nif)
  await page.goto('/medico')

  await page.locator('#dni').fill(PACIENTE_BUSCADO.nif)
  await page.locator('#fechaNacimiento').fill(PACIENTE_BUSCADO.fechaNacimientoInput)
  await page.getByRole('button', { name: 'Buscar paciente' }).click()

  await expect(page.getByText('Datos del paciente')).toBeVisible()
  await expect(page.getByText(PACIENTE_BUSCADO.nombre)).toBeVisible()

  await page.getByRole('button', { name: 'Solicitar asignación' }).click()
  await expect(page.locator('.success, .asignacionMsg, [class*="success"]')).toBeVisible()

  await logout(page)

  // --- El paciente acepta la solicitud recibida ---
  await login(page, PACIENTE_BUSCADO.nif)
  await page.goto('/mis-solicitudes')

  const tarjeta = page.locator('.solicitud-card', { hasText: MEDICO.nombre }).first()
  await expect(tarjeta).toBeVisible()
  await tarjeta.getByRole('button', { name: 'Aceptar' }).click()
  await page.getByRole('button', { name: /sí, aceptar/i }).click()

  await expect(page.locator('.solicitud-card', { hasText: 'Aceptada' }).first()).toBeVisible()

  await logout(page)

  // --- El médico ya tiene al paciente asignado y abre su historial ---
  await login(page, MEDICO.nif)
  await page.goto('/medico')

  const tarjetaPaciente = page.locator('.paciente-card', { hasText: PACIENTE_BUSCADO.nombre })
  await expect(tarjetaPaciente).toBeVisible()
  await tarjetaPaciente.getByRole('link', { name: /ver historial/i }).click()

  await expect(page).toHaveURL(/\/medico\/pacientes\/.+\/historial/)
  await expect(page.getByRole('heading', { name: new RegExp(PACIENTE_BUSCADO.nombre, 'i') })).toBeVisible()
})
