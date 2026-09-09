# Ejecuta la suite E2E completa y limpia al terminar.
#
#   .\scripts\e2e.ps1              # todos los recorridos
#   .\scripts\e2e.ps1 03-asig     # solo los specs que coincidan
#
$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..')

try {
  Write-Host '-> Levantando Postgres y MongoDB de test'
  docker compose -f docker-compose.test.yml up -d --wait

  Write-Host '-> Ejecutando Playwright (levanta backend perfil e2e + frontend)'
  npx playwright test @args
}
finally {
  Write-Host '-> Parando bases de datos de test'
  docker compose -f docker-compose.test.yml down -v
}
