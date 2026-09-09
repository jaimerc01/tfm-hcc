import { test, expect } from '@playwright/test'
import { login, nifValido } from '../helpers'
import { ADMIN, PASSWORD } from '../credentials'

/**
 * Recorrido 5: un administrador da de alta un médico nuevo desde el panel de gestión.
 */
test('el administrador crea un médico nuevo y aparece en la tabla', async ({ page }) => {
  await login(page, ADMIN.nif)
  await page.goto('/admin/medicos')

  const sufijo = Date.now()
  const nif = nifValido(sufijo + 7)
  const email = `dr.${sufijo}@example.com`

  await page.getByRole('button', { name: 'Añadir médico' }).click()

  // Paso 1: comprobar si el NIF ya existe (modal con un solo campo). Como es nuevo,
  // el backend responde 404 y se pasa al formulario completo.
  await page.locator('#nif-search-input').fill(nif)
  await page.locator('#nif-search-input').press('Enter')

  // Paso 2: formulario completo del médico (inputs con id estable).
  await page.locator('#medico-nombre').fill('Doctor')
  await page.locator('#medico-apellido1').fill('Nuevo')
  await page.locator('#medico-email').fill(email)
  await page.locator('#medico-nif').fill(nif)
  await page.locator('#medico-especialidad').fill('Cardiología')
  await page.locator('#medico-fecha-nacimiento').fill('1988-02-02')
  await page.locator('#medico-password').fill(PASSWORD)

  await page.getByRole('button', { name: 'Guardar' }).click()

  await expect(page.locator('.medicos-table')).toContainText(email)
  await expect(page.locator('.medicos-table')).toContainText(nif)
})
