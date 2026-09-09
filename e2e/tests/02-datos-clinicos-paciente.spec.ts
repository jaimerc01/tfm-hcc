import { test, expect } from '@playwright/test'
import { login } from '../helpers'
import { PACIENTE } from '../credentials'

/**
 * Recorrido 2: un paciente registra datos clínicos y comprueba que persisten
 * tras recargar la página (frontend → backend → BD → frontend).
 */
test('el paciente añade una alergia y persiste tras recargar', async ({ page }) => {
  await login(page, PACIENTE.nif)

  await page.goto('/historia-clinica')
  await page.locator('#alergias-tab').click()

  const descripcion = `Alergia E2E ${Date.now()}`
  await page.locator('#alergia-input').fill(descripcion)
  await page.locator('#alergias-panel').getByRole('button', { name: 'Añadir alergia' }).click()

  await expect(page.locator('#alergias-panel')).toContainText(descripcion)

  // Recarga completa: si sigue ahí, es que el backend la guardó de verdad.
  await page.reload()
  await page.locator('#alergias-tab').click()
  await expect(page.locator('#alergias-panel')).toContainText(descripcion)
})

test('el paciente registra un análisis de sangre y aparece en la tabla', async ({ page }) => {
  await login(page, PACIENTE.nif)

  await page.goto('/historia-clinica')
  await page.locator('#analisis-tab').click()

  const panel = page.locator('#analisis-panel')
  await panel.locator('#param-select').selectOption({ label: 'Glucosa' })
  await panel.locator('#value-input').fill('95')
  await panel.locator('.btn-add').click()      // el botón lleva aria-label, se selecciona por clase

  await expect(panel.locator('.results-table')).toContainText('95')

  await page.reload()
  await page.locator('#analisis-tab').click()
  await expect(page.locator('#analisis-panel .results-table')).toContainText('95')
})
