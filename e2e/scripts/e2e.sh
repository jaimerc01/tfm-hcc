#!/usr/bin/env bash
# Ejecuta la suite E2E completa y limpia al terminar (pase lo que pase).
#
#   ./scripts/e2e.sh              # todos los recorridos
#   ./scripts/e2e.sh 03-asig      # solo los specs que coincidan
#
set -euo pipefail
cd "$(dirname "$0")/.."

cleanup() {
  echo "→ Parando bases de datos de test"
  docker compose -f docker-compose.test.yml down -v || true
}
trap cleanup EXIT

echo "→ Levantando Postgres y MongoDB de test"
docker compose -f docker-compose.test.yml up -d --wait

echo "→ Ejecutando Playwright (levanta backend perfil e2e + frontend)"
npx playwright test "$@"
