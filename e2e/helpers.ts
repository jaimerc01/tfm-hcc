import { Page, expect } from '@playwright/test'
import { PASSWORD } from './credentials'

const LETRAS = 'TRWAGMYFPDXBNJZSQVHLCKE'

/** Genera un NIF español con dígito de control válido a partir de un número. */
export function nifValido(seed: number): string {
  const num = String(Math.abs(seed) % 100_000_000).padStart(8, '0')
  return num + LETRAS[Number(num) % 23]
}

/** Login por la interfaz: rellena el formulario y espera a llegar al dashboard. */
export async function login(page: Page, nif: string, password = PASSWORD): Promise<void> {
  await page.goto('/login')
  await page.locator('#nif').fill(nif)
  await page.locator('#password').fill(password)
  await page.locator('.login-btn').click()
  await page.waitForURL('**/dashboard')
}

/** Cierra sesión desde la barra de navegación. */
export async function logout(page: Page): Promise<void> {
  await page.locator('.nav__logout').click()
  await page.getByRole('button', { name: 'Sí, cerrar sesión' }).click()
  await page.waitForURL('**/login')
}

/** Espera a que desaparezca cualquier overlay de carga inicial de una vista. */
export async function esperarSinSpinner(page: Page): Promise<void> {
  await expect(page.locator('.spinner, .loading-container')).toHaveCount(0, { timeout: 15_000 }).catch(() => {})
}
