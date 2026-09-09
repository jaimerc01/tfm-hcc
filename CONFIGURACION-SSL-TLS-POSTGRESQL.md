# Cifrado TLS de la conexión entre la aplicación y PostgreSQL

> Este documento, `compose.override.yaml`, `certs/postgres/pg_hba.conf` y
> `certs/postgres/generar.sh` **sí se versionan**. Lo único que queda fuera del
> repositorio es el material criptográfico (`certs/postgres/*.crt` y `*.key`),
> que cada máquina genera con `bash certs/postgres/generar.sh`.
>
> Sin esos certificados, el contenedor arranca PostgreSQL sin cifrar (igual que
> el `compose.yaml` base), así que un clon recién hecho funciona sin más.

La aplicación guarda datos de salud. Además del cifrado de campos concretos en la
base de datos (AES-GCM, `AESEncryptionConverter`) y del HTTPS del frontend, la
conexión TCP entre el backend y PostgreSQL viaja cifrada con TLS 1.3. Así se cubre
también el "cifrado en tránsito" que exigen el RGPD y el Esquema Nacional de
Seguridad para los datos de categoría especial.

---

## 1. Cómo funciona (las dos puntas)

Para que la conexión vaya cifrada hacen falta las dos partes:

1. **El servidor PostgreSQL tiene que ofrecer TLS.** Necesita un certificado de
   servidor y su clave privada, arrancar con `ssl=on` y un `pg_hba.conf` que
   obligue a cifrar (`hostssl`).
2. **El cliente (driver JDBC) tiene que pedir TLS** con el parámetro `sslmode`, y
   para protegerse de un intermediario, **validar el certificado del servidor**
   (`sslmode=verify-full` + certificado de la CA).

En desarrollo, PostgreSQL corre en Docker. La imagen oficial no habla TLS por
defecto, así que se le añade la configuración con un `compose.override.yaml`
(Docker lo fusiona automáticamente sobre `compose.yaml`).

---

## 2. Puesta en marcha en un equipo nuevo (de cero)

Guía completa para clonar y arrancar el proyecto con TLS incluido. El resto del
documento entra en el detalle de la parte TLS.

### Requisitos

- **Git** y **Docker Desktop** (incluye Docker Compose)
- **JDK 25** con `JAVA_HOME` apuntando a él (Maven lo trae `./mvnw`)
- **Node.js 18+** (probado con 22) y npm
- **Git Bash** en Windows: trae `openssl`, necesario para generar los certificados

### Pasos

```bash
# 1. Clonar
git clone <URL-del-repo> tfm-hcc
cd tfm-hcc

# 2. Backend: copiar la plantilla de variables (los valores por defecto valen en local)
cp .env.example .env

# 3. Generar la CA y el certificado del servidor PostgreSQL
bash certs/postgres/generar.sh

# 4. Activar TLS en el cliente: en .env poner
#      DB_SSLMODE=require        (o verify-full + DB_SSLROOTCERT=certs/postgres/ca.crt)

# 5. Levantar las bases de datos (PostgreSQL con TLS + MongoDB)
docker compose up -d

# 6. Comprobar que PostgreSQL tiene TLS
docker exec tfm-hcc-postgres-1 psql -U hcc -d HCC_DEV -c "SHOW ssl;"     # -> on

# 7. Arrancar el backend (Hibernate crea el esquema en el primer arranque; escucha en :8081)
./mvnw spring-boot:run
```

```bash
# 8. En otra terminal: sembrar los datos (perfiles obligatorios + usuarios de prueba + rangos)
docker exec -i tfm-hcc-postgres-1 psql -U hcc -d HCC_DEV < scripts/inserts.sql
docker exec -i tfm-hcc-postgres-1 psql -U hcc -d HCC_DEV < scripts/insert_rangos_referencia.sql
```

```bash
# 9. En otra terminal: arrancar el frontend
cd frontend && npm install && npm run serve      # -> http://localhost:8080
```

Notas:

- **Sin `inserts.sql`** no hay filas en `perfil` y no se puede ni registrar un
  usuario. El script trae 100 pacientes + 6 médicos + 1 administrador (NIF
  `00000000T`), todos con contraseña `password` y login por **NIF** (los NIF y su
  rol están en `scripts/usuarios_generados.csv`).
- **Sin los pasos 3–4** (sin certificados) el paso 5 arranca PostgreSQL sin
  cifrar y `.env` con `DB_SSLMODE=prefer` (valor de `.env.example`) conecta
  igual: sirve para una primera prueba, pero sin TLS.
- **Login con Google:** `.env.example` deja `GOOGLE_CLIENT_ID` /
  `GOOGLE_CLIENT_SECRET` en blanco (son secretos, no van a git). Sin ellas el
  botón "Iniciar sesión con Google" lleva a una ruta inexistente; el resto de la
  app funciona. Para activarlo, copia esos dos valores **a mano** (nunca por git)
  desde otro `.env` tuyo, o crea un cliente OAuth en Google Cloud Console (tipo
  *Aplicación web*, URI de redirección `http://localhost:8081`).
- **Correo:** `MAIL_ENABLED=false` por defecto; el enlace de recuperación de
  contraseña se escribe en el log, no hace falta SMTP.
- **Puertos:** 8080 frontend · 8081 backend · 5432 PostgreSQL · 27017 MongoDB.
- Los tests (`./mvnw test`, `npm test` en `frontend/`) usan H2/mocks y no
  dependen de nada de esto.

### Verificación de punta a punta

```bash
docker exec tfm-hcc-postgres-1 psql -U hcc -d HCC_DEV -c \
  "SELECT a.application_name, s.ssl, s.version, s.cipher
     FROM pg_stat_ssl s JOIN pg_stat_activity a USING (pid)
    WHERE a.client_addr IS NOT NULL;"
```

Las conexiones de "Historia Clinica Comun" deben salir `ssl = t` y
`version = TLSv1.3`. Luego abre `http://localhost:8080` e inicia sesión con un
NIF del CSV y contraseña `password`.

---

## 3. Generación de certificados

`bash certs/postgres/generar.sh` (paso 3 de la sección 2) hace exactamente lo de
abajo; esto es el equivalente manual y sirve para entender qué genera.

Se crea una CA propia y con ella se firma el certificado del servidor. Es
suficiente para desarrollo; en producción se usaría la CA de la organización o
un certificado emitido por la propia infraestructura de base de datos.

```bash
mkdir -p certs/postgres && cd certs/postgres
export MSYS_NO_PATHCONV=1        # Git Bash no debe convertir el -subj a ruta

# CA (10 años)
openssl req -new -x509 -days 3650 -nodes \
  -out ca.crt -keyout ca.key \
  -subj "/CN=tfm-hcc-dev-ca/O=tfm-hcc"

# Clave + CSR del servidor
openssl req -new -nodes \
  -out server.csr -keyout server.key \
  -subj "/CN=localhost/O=tfm-hcc"

# Firma con SAN: los nombres por los que se le puede conectar
printf "subjectAltName = DNS:localhost, DNS:postgres, DNS:db, IP:127.0.0.1\nextendedKeyUsage = serverAuth\n" > server.ext
openssl x509 -req -in server.csr -days 3650 \
  -CA ca.crt -CAkey ca.key -CAcreateserial \
  -out server.crt -extfile server.ext

rm -f server.csr server.ext ca.srl
chmod 600 server.key ca.key
cd ../..
```

Resultado en `certs/postgres/`:

| Archivo         | Qué es                                        | Lo usa                         |
|-----------------|-----------------------------------------------|--------------------------------|
| `ca.crt`        | Certificado público de la CA                  | El cliente, para `verify-full` |
| `ca.key`        | Clave privada de la CA (firma certificados)   | Solo al (re)generar            |
| `server.crt`    | Certificado del servidor PostgreSQL           | El contenedor                  |
| `server.key`    | Clave privada del servidor                    | El contenedor                  |
| `pg_hba.conf`   | Reglas de acceso: solo `hostssl` por TCP      | El contenedor                  |

El `CN`/`SAN` del certificado del servidor tiene que incluir el nombre por el que
la aplicación se conecta. El backend corre en el host y se conecta a
`localhost:5432`, así que el SAN lleva `localhost`. También se incluyen `postgres`
y `db` por si en el futuro el backend se contenedoriza y conecta por el nombre del
servicio de Docker.

---

## 4. Qué hace `compose.override.yaml`

Docker Compose lo fusiona automáticamente sobre `compose.yaml` al hacer
`docker compose up -d`. El entrypoint:

1. Si **no** hay `server.crt`/`server.key` en `certs/postgres/`, arranca
   PostgreSQL normal (sin TLS) y lo avisa por el log. Es lo que pasa en un clon
   antes de generar los certificados.
2. Si los hay: los copia a una ruta interna del contenedor y corrige dueño y
   permisos (PostgreSQL exige que la clave privada sea `0600` y de su propio
   usuario; un bind mount desde Windows no lo conserva). Luego arranca el
   servidor con:

   ```
   postgres -c ssl=on \
     -c ssl_cert_file=.../server.crt \
     -c ssl_key_file=.../server.key \
     -c hba_file=.../pg_hba.conf
   ```

El `pg_hba.conf` (`certs/postgres/pg_hba.conf`, versionado) no tiene ninguna
línea `host` de texto plano, solo `hostssl`: cualquier conexión TCP sin cifrar
se rechaza con `no pg_hba.conf entry ... no encryption`. Las conexiones por
socket Unix dentro del contenedor (el `healthcheck`) siguen con `local ... trust`.

---

## 5. Configuración de la aplicación

`src/main/resources/application.yml` (perfil por defecto):

```yaml
spring:
  datasource:
    url: "jdbc:postgresql://localhost:5432/HCC_DEV?sslmode=${DB_SSLMODE:prefer}&sslrootcert=${DB_SSLROOTCERT:}"
```

Perfil `prod`: mismo esquema pero con `sslmode` por defecto en `verify-full` y
`sslrootcert` apuntando a `/etc/tfm-hcc/certs/ca.crt`.

`.env` (local, en `.gitignore`):

```properties
DB_SSLMODE=require
# DB_SSLMODE=verify-full
# DB_SSLROOTCERT=certs/postgres/ca.crt
```

- `sslrootcert` solo lo leen `verify-ca` y `verify-full`; con `prefer`/`require`
  se ignora, por eso el valor por defecto vacío no molesta.
- La ruta de `DB_SSLROOTCERT` es relativa a la carpeta desde la que se arranca el
  backend (normalmente la raíz del proyecto).

Los tests (`application.properties` → H2) y el perfil `e2e` sobreescriben la URL
entera, así que no se ven afectados.

---

## 6. Los modos de `sslmode` y el compromiso

| modo          | cifra | valida al servidor            | protege de intermediario (MITM) |
|---------------|-------|-------------------------------|---------------------------------|
| `disable`     | no    | –                             | no                              |
| `prefer`      | sí\*  | no                            | no                              |
| `require`     | sí    | **no**                        | no                              |
| `verify-ca`   | sí    | cadena sí, hostname no        | parcial                         |
| `verify-full` | sí    | cadena + hostname             | **sí**                          |

\* `prefer` usa TLS si el servidor lo ofrece y, si no, texto plano.

- `require` cifra pero no comprueba con quién habla: alguien que se ponga en medio
  y presente otro certificado seguiría funcionando. A cambio no hay que repartir
  el certificado de la CA.
- `verify-full` es el único que cierra el MITM. Cuesta gestionar la CA y que el
  hostname del certificado cuadre con el host real.

**En este proyecto:**

- **Desarrollo:** `require` en el `.env`, y el servidor solo admite `hostssl`, así
  que el cifrado es obligatorio en ambos lados. `verify-full` también funciona en
  local (el certificado vale para `localhost` y tenemos la CA): basta cambiar el
  `.env` a `verify-full` + `DB_SSLROOTCERT=certs/postgres/ca.crt`.
- **Producción:** `verify-full` por defecto (perfil `prod`), documentado como
  medida de cifrado en tránsito para RGPD/ENS.

---

## 7. Verificación

```bash
# ¿El servidor tiene TLS activo?
docker exec tfm-hcc-postgres-1 psql -U hcc -d HCC_DEV -c "SHOW ssl;"          # -> on

# ¿Rechaza las conexiones TCP sin cifrar?
docker exec -e PGSSLMODE=disable tfm-hcc-postgres-1 \
  psql "host=127.0.0.1 user=hcc dbname=HCC_DEV" -c "SELECT 1;"
#  -> FATAL: no pg_hba.conf entry ... no encryption

# ¿Con qué van las conexiones de la aplicación?
docker exec tfm-hcc-postgres-1 psql -U hcc -d HCC_DEV -c \
  "SELECT a.application_name, s.ssl, s.version, s.cipher
     FROM pg_stat_ssl s JOIN pg_stat_activity a USING (pid)
    WHERE a.client_addr IS NOT NULL;"
#  -> Historia Clinica Comun | t | TLSv1.3 | TLS_AES_256_GCM_SHA384
```

También se puede comprobar desde una conexión de la propia app:
`SELECT ssl_is_used();`.

---

## 8. MongoDB

Los registros de acceso y auditoría van a MongoDB. La idea es la misma: añadir
`?tls=true` a `MONGODB_URI` y `--tlsCAFile` / `net.tls` al `mongod` del
`compose.override.yaml`. MongoDB Atlas (producción) ya fuerza TLS. No está hecho
todavía; queda como continuación.

---

## 9. Rotación y caducidad

Los certificados de arriba caducan a los 10 años. Para rotarlos: repetir la
sección 3 y `docker compose up -d` (recrea el contenedor). Si cambia el `ca.crt`
hay que redistribuirlo a donde esté `DB_SSLROOTCERT`.

Si `openssl x509 -enddate -noout -in certs/postgres/server.crt` está cerca de
vencer, regenerar antes.

---

## 10. Volver atrás

```bash
rm -f certs/postgres/server.crt certs/postgres/server.key
docker compose up -d              # el contenedor arranca sin TLS
# y en .env: DB_SSLMODE=prefer   (o quitar la línea)
```

No hace falta tocar `compose.override.yaml`: sin certificados degrada solo a
texto plano. El cambio en `application.yml` también es inocuo así:
`sslmode=prefer` se comporta igual que antes cuando el servidor no ofrece TLS.

---

## 11. Archivos implicados

| Archivo                                   | Versionado | Qué aporta                                    |
|-------------------------------------------|:----------:|----------------------------------------------|
| `src/main/resources/application.yml`      | sí         | `sslmode`/`sslrootcert` en la URL (dev+prod)  |
| `.env.example`                            | sí         | Documenta `DB_SSLMODE` / `DB_SSLROOTCERT`     |
| `.env`                                    | no         | `DB_SSLMODE=require` en local                 |
| `.gitignore`                              | sí         | Ignora solo `certs/**/*.crt` y `*.key`        |
| `compose.override.yaml`                   | **sí**     | Activa TLS en el contenedor (degrada si faltan certs) |
| `certs/postgres/generar.sh`               | **sí**     | Genera la CA y el certificado del servidor    |
| `certs/postgres/pg_hba.conf`              | **sí**     | Solo `hostssl` por TCP                        |
| `certs/postgres/{ca,server}.{crt,key}`    | no         | Material criptográfico (se genera por máquina)|
| `CONFIGURACION-SSL-TLS-POSTGRESQL.md`     | **sí**     | Este documento                               |
