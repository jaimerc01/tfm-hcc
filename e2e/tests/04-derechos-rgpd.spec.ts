import { test, expect } from '@playwright/test'
import { login } from '../helpers'
import { PACIENTE, PASSWORD } from '../credentials'

/**
 * Recorrido 4: el paciente ejerce derechos RGPD desde "Datos del usuario":
 * portabilidad (descarga de sus datos en JSON) y limitación del tratamiento (art. 18).
 *
 * Nota: los botones de esta vista llevan `aria-label`, que sustituye al texto visible
 * como nombre accesible; por eso se seleccionan por el texto visible (`:has-text`).
 */
test('el paciente descarga sus datos personales en JSON', async ({ page }) => {
  await login(page, PACIENTE.nif)
  await page.goto('/usuario')

  await page.locator('#reauth-password').fill(PASSWORD)

  const descarga = page.waitForEvent('download')
  await page.locator('button:has-text("Descargar mis datos")').click()
  const fichero = await descarga

  expect(fichero.suggestedFilename()).toMatch(/\.json$/)
})

test('el paciente limita el tratamiento de sus datos y luego lo reanuda', async ({ page }) => {
  await login(page, PACIENTE.nif)
  await page.goto('/usuario')

  const limitar = page.locator('button:has-text("Limitar tratamiento de mis datos")')
  const reanudar = page.locator('button:has-text("Reanudar tratamiento de mis datos")')

  await limitar.click()
  // El botón cambia a "Reanudar…" (v-if/v-else sobre el estado de la cuenta).
  await expect(reanudar).toBeVisible()
  await expect(limitar).toHaveCount(0)

  await reanudar.click()
  await expect(limitar).toBeVisible()
})
