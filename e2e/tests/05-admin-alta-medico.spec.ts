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

  // Paso 1: comprobar si el NIF ya existe. Como es nuevo, pasa al formulario completo.
  await page.getByPlaceholder('NIF').fill(nif)
  await page.getByRole('button', { name: 'Buscar' }).click()

  // Paso 2: formulario completo del médico.
  await page.getByPlaceholder('Nombre').fill('Doctor')
  await page.getByPlaceholder('Primer apellido').fill('Nuevo')
  await page.getByPlaceholder('Email').fill(email)
  await page.getByPlaceholder('NIF').fill(nif)
  await page.getByPlaceholder('Especialidad').fill('Cardiología')
  await page.locator('input[type="date"]').fill('1988-02-02')
  await page.getByPlaceholder('Contraseña').fill(PASSWORD)

  await page.getByRole('button', { name: 'Guardar' }).click()

  await expect(page.locator('.medicos-table')).toContainText(email)
  await expect(page.locator('.medicos-table')).toContainText(nif)
})
