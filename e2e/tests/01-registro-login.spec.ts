import { test, expect } from '@playwright/test'
import { nifValido } from '../helpers'
import { PASSWORD } from '../credentials'

/**
 * Recorrido 1: una persona nueva se registra por la interfaz y luego inicia sesión.
 */
test('registro de un usuario nuevo y primer login', async ({ page }) => {
  const sufijo = Date.now()
  const nif = nifValido(sufijo)
  const email = `nuevo.${sufijo}@example.com`

  await page.goto('/register')

  await page.locator('#nombre').fill('Nueva')
  await page.locator('#apellido1').fill('Persona')
  await page.locator('#fechaNacimiento').fill('1992-04-20')
  await page.locator('#nif').fill(nif)
  await page.locator('#email').fill(email)
  await page.locator('#password').fill(PASSWORD)
  await page.locator('#password2').fill(PASSWORD)

  await page.getByRole('button', { name: 'Crear Cuenta' }).click()

  // Tras registrarse, la app redirige al login (mensaje de éxito + redirección).
  await expect(page.locator('.alert-success')).toBeVisible()
  await page.waitForURL('**/login', { timeout: 10_000 })

  // Y con esas credenciales ya se puede entrar.
  await page.locator('#nif').fill(nif)
  await page.locator('#password').fill(PASSWORD)
  await page.locator('.login-btn').click()

  await page.waitForURL('**/dashboard')
  await expect(page.getByText(/hola|bienvenido/i)).toBeVisible()
})

test('login con contraseña incorrecta muestra error y no entra', async ({ page }) => {
  await page.goto('/login')
  await page.locator('#nif').fill('12345678Z')
  await page.locator('#password').fill('estaNoEsLaBuena')
  await page.locator('.login-btn').click()

  await expect(page.locator('.error-message')).toBeVisible()
  await expect(page).toHaveURL(/\/login$/)
})
