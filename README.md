# Historia Clínica Común (HCC)

Aplicación web para la **gestión y visualización de datos médicos**. Centraliza la
información clínica de un paciente (antecedentes, alergias, análisis de sangre y de
orina, signos vitales y documentos) en un único lugar seguro y la presenta mediante
gráficas interactivas pensadas tanto para el paciente como para el personal
sanitario. El paciente decide en todo momento qué médicos tienen acceso a su
historial.

Este repositorio es el desarrollo de un Trabajo de Fin de Máster (Máster
Universitario en Enxeñaría Informática, Universidade da Coruña).

> Aviso: proyecto académico. No usar con datos reales de pacientes sin completar
> antes las medidas de seguridad marcadas como pendientes en la Evaluación de
> Impacto (anexo de la memoria): TLS en producción tras proxy inverso, límite de
> intentos en el inicio de sesión y segundo factor obligatorio para los perfiles
> con acceso clínico.

## Características principales

- **Tres perfiles de usuario**: paciente, médico y administrador, con permisos
  diferenciados.
- **Historia clínica del paciente**: antecedentes personales y familiares, alergias,
  análisis de sangre, análisis de orina, signos vitales y archivos clínicos.
- **Visualización de datos** con D3.js: línea temporal simple e interactiva (con
  zoom), barras, medidor, gráfica circular comparativa (colesterol LDL/HDL), gráfica
  de doble línea (presión arterial sistólica/diastólica), anillos de salud del panel
  y línea de tiempo clínica anotada para el médico. Cada valor se contrasta con su
  rango de referencia y se avisa cuando queda fuera.
- **Relación médico-paciente por consentimiento**: el médico solicita el acceso y el
  paciente lo acepta o lo revoca cuando quiera.
- **Propuestas de cambio clínico**: el médico propone altas, ediciones o borrados
  sobre el historial y el paciente los confirma; todo queda en la auditoría.
- **Anotaciones y notificaciones** del médico al paciente.
- **Autenticación** por NIF y contraseña, con verificación en dos pasos (TOTP)
  opcional e inicio de sesión con Google.
- **Cumplimiento RGPD/LOPDGDD**: consentimiento explícito registrado, control de
  edad, cifrado de datos sensibles en reposo (AES-GCM), registro de accesos y de
  cambios, y ejercicio de los derechos del interesado (acceso, rectificación,
  supresión, limitación, portabilidad, oposición).

## Arquitectura

| Capa | Tecnología |
|---|---|
| Frontend | Vue 3 (Vue CLI) + Vue Router + Vue I18n (español y gallego) + D3.js + Axios |
| Backend | Java 25 + Spring Boot 3.5 (Web, Security, Data JPA, Data MongoDB, OAuth2 Client, Validation, Mail) |
| Persistencia | PostgreSQL (datos clínicos y de negocio) + MongoDB (registros de acceso, auditoría de cambios, archivos clínicos, códigos OAuth y retos de segundo factor) |
| Autenticación | JWT (JJWT) + TOTP + OAuth2 / OpenID Connect (Google) |
| Pruebas | JUnit 5 + Mockito + Testcontainers/H2 embebidos (backend), Vitest + Vue Test Utils + MSW (frontend), Playwright (extremo a extremo) |
| Calidad | JaCoCo, SonarQube, WAVE (accesibilidad) |

El backend sigue una arquitectura en capas: filtros de seguridad, controladores REST,
fachadas, servicios de dominio, repositorios y capa de datos, con componentes
transversales de seguridad y un manejador global de excepciones. El frontend es una
SPA de componentes con una capa de servicios que encapsula las llamadas a la API.

## Estructura del repositorio

```
.
├── src/main/java/com/hcc/tfm_hcc/   Backend (proyecto Maven en la raíz)
│   ├── controller/  facade/  service/  repository/  model/  dto/
│   ├── converter/   config/   exception/   constants/
├── src/test/java/                   Pruebas del backend (*Test unitarias, *IT de integración)
├── frontend/                        Aplicación Vue (views, components, composables, services)
│   └── tests/                       Pruebas unitarias y de integración (Vitest)
├── e2e/                             Pruebas de extremo a extremo (Playwright)
├── scripts/                         Utilidades de siembra de la base de datos
│   ├── generar_datos_usuarios.py    Genera usuarios + historial vacío -> inserts.sql
│   ├── insert_rangos_referencia.sql Rangos de referencia (guías clínicas)
│   ├── generar_datos_clinicos.py    Genera datos clínicos (NHANES) -> inserts_datos_clinicos.sql
│   └── nhanes/                      Datos de NHANES y conversor XPT -> CSV (ver su README)
├── certs/postgres/                  Generación de certificados para el TLS de PostgreSQL
├── compose.yaml / compose.override.yaml   PostgreSQL + MongoDB para desarrollo
└── .env.example                     Plantilla de variables de entorno
```

## Requisitos previos

- **JDK 25**
- **Node.js 18 o superior** y npm
- **Docker** y Docker Compose (para PostgreSQL y MongoDB)
- **Python 3.10 o superior** (solo si se van a regenerar los datos de siembra):
  `pip install pandas pycryptodome bcrypt`
- Git Bash u otro shell POSIX en Windows para los scripts de `certs/`

## Puesta en marcha (desarrollo local)

### 1. Variables de entorno

```bash
cp .env.example .env
```

Los valores por defecto funcionan tal cual en local. No son secretos reales.

### 2. Bases de datos

```bash
docker compose up -d
```

Levanta PostgreSQL en el puerto 5432 (base `HCC_DEV`) y MongoDB en el 27017. Los
datos persisten en volúmenes de Docker; `docker compose down -v` los borra.

Opcional, para cifrar la conexión con PostgreSQL:

```bash
bash certs/postgres/generar.sh
docker compose up -d
```

Ver [CONFIGURACION-SSL-TLS-POSTGRESQL.md](CONFIGURACION-SSL-TLS-POSTGRESQL.md).

### 3. Backend

```bash
./mvnw spring-boot:run
```

Escucha en `http://localhost:8081`. Hibernate crea el esquema en el primer arranque.

### 4. Frontend

En otra terminal:

```bash
npm --prefix frontend install
npm --prefix frontend run serve
```

Disponible en `http://localhost:8080`.

### 5. Siembra de datos de prueba

Con las bases de datos levantadas y el esquema ya creado (tras arrancar el backend
al menos una vez), cargar en este orden, dentro de una transacción para que un fallo
no deje carga parcial:

```bash
for f in inserts.sql insert_rangos_referencia.sql inserts_datos_clinicos.sql; do
  docker exec -i tfm-hcc-postgres-1 psql -U hcc -d HCC_DEV \
    -v ON_ERROR_STOP=1 --single-transaction < scripts/$f
done
```

- `inserts.sql`: 100 pacientes, sus médicos y un administrador, con el historial vacío.
- `insert_rangos_referencia.sql`: rangos de referencia de cada parámetro.
- `inserts_datos_clinicos.sql`: entre 5 y 10 analíticas por usuario a partir de
  perfiles reales de NHANES (más un pH de orina sintético). Ver
  [scripts/nhanes/README.md](scripts/nhanes/README.md).

Todas las cuentas de prueba usan la contraseña `password`. El administrador tiene el
NIF `00000000T`. Los NIF de los demás usuarios están en
`scripts/usuarios_generados.csv`.

**Los tres ficheros son un conjunto acoplado.** `inserts_datos_clinicos.sql` enlaza
cada dato con el historial del paciente mediante subconsultas por `id_paciente`, y
esos identificadores tienen que ser los del `inserts.sql` versionado (que van a la
par con `usuarios_generados.csv`). Si cargas un `inserts_datos_clinicos.sql` generado
contra otra base de datos verás
`null value in column "id_historial_clinico" ... violates not-null`.

Regenera `inserts_datos_clinicos.sql` (necesita `pip install pandas pycryptodome
bcrypt` y que `TFM_HCC_ENCRYPTION_KEY` coincida con la de tu `.env`):

```bash
# A) para la siembra versionada (usuarios de usuarios_generados.csv):
python scripts/generar_datos_clinicos.py --seed 42

# B) si tu base de datos ya tenía usuarios de otra ejecución (no del inserts.sql
#    versionado), lee los identificadores de la propia base de datos:
python scripts/generar_datos_clinicos.py --from-db --seed 42
```

## Pruebas

### Backend

```bash
./mvnw test        # pruebas unitarias (Surefire, clases *Test)
./mvnw verify      # unitarias + integración (Failsafe, clases *IT) + informe JaCoCo
```

El informe de cobertura combinado queda en `target/site/jacoco/index.html`.

### Frontend

```bash
npm --prefix frontend test              # unitarias + integración (Vitest)
npm --prefix frontend run test:coverage # con informe de cobertura
```

### Extremo a extremo

```bash
cd e2e
npm install
npx playwright install chromium
npm test          # levanta BD de test, backend y frontend, ejecuta los recorridos y limpia
```

Usa bases de datos efímeras en los puertos 5433 / 27018 (`e2e/docker-compose.test.yml`).

## Seguridad y privacidad

- Cifrado en reposo de los campos sensibles (NIF, teléfono, fecha de nacimiento,
  valores clínicos, alergias, antecedentes, anotaciones y archivos) con AES-GCM. Las
  búsquedas por igualdad se hacen sobre un índice HMAC-SHA256, no sobre el texto
  cifrado.
- Control de acceso basado en roles y en la relación asistencial activa entre médico
  y paciente.
- Registro de accesos y auditoría inmutable de los cambios sobre datos clínicos en
  MongoDB.
- Consentimiento explícito registrado en el alta (fecha y versión de la política) y
  control de edad mínima.
- El borrado de cuenta anonimiza los datos en lugar de eliminarlos físicamente, para
  preservar la integridad del resto de registros y de la auditoría.

Las claves y secretos de `.env.example` son valores de desarrollo. En producción se
inyectan por variables de entorno del sistema, que tienen prioridad sobre el fichero.

## Documentación adicional

- [CONFIGURACION-SSL-TLS-POSTGRESQL.md](CONFIGURACION-SSL-TLS-POSTGRESQL.md) — TLS de la
  conexión con PostgreSQL en local.
- [CONTRIBUTING.md](CONTRIBUTING.md) — convenciones de estilos CSS del frontend.
- [scripts/nhanes/README.md](scripts/nhanes/README.md) — origen y conversión de los
  datos de NHANES.
- `frontend/src/composables/README_useChart.md` — API del módulo de gráficas.

## Licencia y autoría

Trabajo de Fin de Máster de Jaime Roade Conejo. Los derechos de propiedad intelectual
son compartidos entre el estudiante y la dirección del trabajo.