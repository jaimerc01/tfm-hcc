import { defineConfig, devices } from '@playwright/test'

/**
 * Config de Playwright para los tests de extremo a extremo de tfm-hcc.
 *
 * Flujo: `npm test` arranca las bases de datos (pretest → docker compose), luego
 * Playwright levanta el backend (perfil `e2e`) y el frontend (`vue-cli-service serve`)
 * vía `webServer`; el proyecto `setup` siembra los perfiles y los usuarios de prueba
 * (ya con el backend arriba), y finalmente corren los specs contra Chromium.
 */

export const FRONTEND_URL = process.env.E2E_FRONTEND_URL ?? 'http://localhost:8080'
export const BACKEND_URL = process.env.E2E_BACKEND_URL ?? 'http://localhost:8081'

export default defineConfig({
  testDir: './tests',
  fullyParallel: false,          // los tests comparten un backend/BD con estado
  workers: 1,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  timeout: 60_000,
  expect: { timeout: 10_000 },
  reporter: [['list'], ['html', { open: 'never' }]],

  use: {
    baseURL: FRONTEND_URL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    locale: 'es-ES',
  },

  projects: [
    // Siembra perfiles y usuarios de prueba una vez, con el backend ya levantado.
    { name: 'setup', testMatch: /seed\.setup\.ts/ },
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
      dependencies: ['setup'],
    },
  ],

  webServer: [
    {
      // Backend Spring Boot con el perfil e2e (Postgres 5433 + Mongo 27018).
      // Se espera solo a que el puerto acepte conexiones; la disponibilidad real
      // (login funcional) la garantiza el proyecto `setup` con sus reintentos.
      command: 'mvn -q spring-boot:run -Dspring-boot.run.profiles=e2e',
      cwd: '..',
      port: 8081,
      reuseExistingServer: !process.env.CI,
      timeout: 180_000,
      stdout: 'pipe',
      stderr: 'pipe',
    },
    {
      // Frontend: servidor de desarrollo de vue-cli apuntando al backend e2e.
      command: 'npm run serve',
      cwd: '../frontend',
      url: FRONTEND_URL,
      reuseExistingServer: !process.env.CI,
      timeout: 240_000,
      env: { VUE_APP_API_URL: BACKEND_URL },
    },
  ],
})
