#!/usr/bin/env bash
# Genera la CA y el certificado del servidor PostgreSQL para el TLS de desarrollo.
# Ver CONFIGURACION-SSL-TLS-POSTGRESQL.md (raíz del proyecto).
#
#   bash certs/postgres/generar.sh
#
# Vuelve a ejecutarlo para rotar los certificados y luego `docker compose up -d`.
set -euo pipefail

cd "$(dirname "$0")"
export MSYS_NO_PATHCONV=1   # Git Bash no debe convertir el -subj en una ruta

DIAS=3650

openssl req -new -x509 -days "$DIAS" -nodes \
  -out ca.crt -keyout ca.key \
  -subj "/CN=tfm-hcc-dev-ca/O=tfm-hcc"

openssl req -new -nodes \
  -out server.csr -keyout server.key \
  -subj "/CN=localhost/O=tfm-hcc"

printf 'subjectAltName = DNS:localhost, DNS:postgres, DNS:db, IP:127.0.0.1\nextendedKeyUsage = serverAuth\n' > server.ext
openssl x509 -req -in server.csr -days "$DIAS" \
  -CA ca.crt -CAkey ca.key -CAcreateserial \
  -out server.crt -extfile server.ext

rm -f server.csr server.ext ca.srl
chmod 600 server.key ca.key

echo "Certificados generados en $(pwd):"
ls -l ca.crt ca.key server.crt server.key pg_hba.conf
echo
echo "Siguiente paso: docker compose up -d"
