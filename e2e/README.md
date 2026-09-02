# Tests de extremo a extremo (E2E)

Navegador real (Chromium vía Playwright) → frontend real (`vue-cli-service serve`) →
backend real (Spring Boot, perfil `e2e`) → bases de datos reales (Postgres + MongoDB
en Docker). **Sin mocks.** Es el nivel 3 de la estrategia de tests:

| Nivel | Dónde | Qué se simula |
|---|---|---|
| Unitario | `src/test/**/*Test.java`, `frontend/tests/unit` | casi todo (mocks) |
| Integración backend | `src/test/**/*IT.java` (`mvn verify`) | no hay navegador |
| Integración frontend | `frontend/tests/integration` (MSW) | el backend |
| **E2E (esto)** | `e2e/tests` | **nada** |

## Requisitos

- Docker en marcha (Postgres + MongoDB de usar y tirar).
- JDK 25 + Maven (para el backend).
- Node 18+ (frontend y Playwright).

## Puesta en marcha

```bash
cd e2e
npm install
npx playwright install chromium      # una vez
```

## Ejecución

```bash
# Todo de una (levanta BDs, corre la suite, limpia al terminar):
./scripts/e2e.sh                     # Linux/Mac
.\scripts\e2e.ps1                    # Windows

# O paso a paso:
npm run db:up                        # Postgres :5433, MongoDB :27018
npm test                             # Playwright arranca backend + frontend y ejecuta los specs
npm run db:down                      # parar y borrar las BDs

npm run test:headed                  # ver el navegador
npm run test:ui                      # modo interactivo de Playwright
npm run report                       # abrir el informe HTML del último run
```

`npm test` ejecuta `pretest` (`db:up`) automáticamente. Playwright, mediante `webServer`,
levanta:

- el **backend** con `mvn spring-boot:run -Dspring-boot.run.profiles=e2e`
  ([`application-e2e.yml`](../src/main/resources/application-e2e.yml): Postgres `:5433`,
  Mongo `:27018`, `ddl-auto: create-drop`, secretos fijos de test);
- el **frontend** con `npm run serve` y `VUE_APP_API_URL=http://localhost:8081`.

El proyecto `setup` de Playwright ([`tests/seed.setup.ts`](tests/seed.setup.ts)) siembra,
con el backend ya arrancado:

1. las filas de `perfil` (`PACIENTE`, `MEDICO`, `ADMINISTRADOR`) — vía SQL directo;
2. los usuarios de prueba ([`credentials.ts`](credentials.ts)) — vía el endpoint real
   `/authentication/signup` (así el cifrado de campos y los hashes de búsqueda los
   calcula la propia aplicación);
3. los roles `ADMINISTRADOR` y `MEDICO` — insertando en `perfil_usuario` (tabla no cifrada).

## Recorridos cubiertos

| Spec | Recorrido |
|---|---|
| `01-registro-login` | Alta de un usuario nuevo por la interfaz + primer login; login con contraseña incorrecta. |
| `02-datos-clinicos-paciente` | El paciente añade una alergia y un análisis de sangre; recarga la página y siguen ahí. |
| `03-asignacion-medico-paciente` | El médico busca un paciente y solicita asignación → el paciente la acepta → el médico abre su historial. |
| `04-derechos-rgpd` | El paciente descarga sus datos en JSON (portabilidad) y limita/reanuda el tratamiento (art. 18). |
| `05-admin-alta-medico` | El administrador da de alta un médico desde el panel de gestión. |
| `06-cambio-idioma` | Cambio es↔gl: la interfaz se traduce y la preferencia persiste. |

## Notas

- Los tests corren **en serie** (`workers: 1`): comparten un backend con estado.
- Los selectores se apoyan en `id` estables y en textos en español (el locale por
  defecto). Si la interfaz cambia, hay que ajustar los selectores del spec afectado.
- `create-drop` hace que cada `npm test` empiece con una base de datos relacional
  vacía. Los logs de Mongo no se limpian entre runs (no afecta a los asserts).
- CI: fijar `CI=1` (activa reintentos y `forbidOnly`) y usar `./scripts/e2e.sh`.
