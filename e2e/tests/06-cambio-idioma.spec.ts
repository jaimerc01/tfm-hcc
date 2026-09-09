import { test, expect } from '@playwright/test'
import { login } from '../helpers'
import { PACIENTE } from '../credentials'

/**
 * Recorrido 6: el cambio de idioma es↔gl afecta a toda la interfaz y se recuerda.
 */
test('cambiar el idioma a gallego traduce la interfaz y persiste', async ({ page }) => {
  await login(page, PACIENTE.nif)

  await expect(page.locator('.nav__logout')).toHaveText('Cerrar Sesión')

  await page.getByRole('button', { name: 'Galego' }).click()

  // setLanguage recarga la página; después el <html lang> y los textos están en gallego.
  await expect(page.locator('html')).toHaveAttribute('lang', 'gl')
  await expect(page.locator('.nav__logout')).toHaveText('Pechar sesión')

  const locale = await page.evaluate(() => localStorage.getItem('app-locale'))
  expect(locale).toBe('gl')

  // Vuelve a español para no dejar el estado sucio para otros tests.
  await page.getByRole('button', { name: 'Español' }).click()
  await expect(page.locator('html')).toHaveAttribute('lang', 'es')
})
