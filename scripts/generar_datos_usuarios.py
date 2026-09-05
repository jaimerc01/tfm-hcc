#!/usr/bin/env python3
"""
Generador de datos sintéticos para el Historial Clínico Compartido (tfm-hcc).

Puebla las tablas:
 - public.perfil            (PACIENTE / MEDICO / ADMINISTRADOR, con UUID fijos)
 - public.usuario
 - public.perfil_usuario
 - public.historial_clinico
 - public.medico_paciente   (cada médico generado queda con entre 1 y 5 pacientes
   asignados en estado ACTIVA, elegidos al azar entre los pacientes generados)

y escribe además un CSV con los datos en claro para poder iniciar sesión:
    usuarios_generados.csv

Compatibilidad con el esquema ACTUAL del backend
------------------------------------------------
El cifrado de columnas pasó de AES/ECB determinista a **AES/GCM no determinista**
(ver `converter/AESEncryptionConverter.java`), y las búsquedas por igualdad ya no
se hacen sobre la columna cifrada sino sobre un **índice HMAC-SHA256** guardado en
`usuario.nif_hash` / `usuario.email_hash` (ver `service/impl/HmacSearchIndexServiceImpl.java`).
Este script replica ambos algoritmos para que los usuarios insertados por SQL sean
funcionales (login, búsqueda de pacientes, etc.) sin pasar por la API.

La clave tiene que ser la MISMA que use el backend en `TFM_HCC_ENCRYPTION_KEY`
(ver `.env` / `.env.example`). El valor por defecto de abajo coincide con el de la
plantilla `.env.example` para desarrollo local.

Columnas nuevas cubiertas: `email_hash`, `nif_hash`, `totp_secret` (NULL),
`totp_enabled` (false). En `historial_clinico` se han eliminado los antiguos campos
de texto libre (los antecedentes y alergias viven ahora en sus propias tablas).

Requisitos:
    pip install pycryptodome bcrypt
"""

import argparse
import base64
import binascii
import csv
import hashlib
import hmac
import os
import random
import string
import sys
import unicodedata
import uuid
from dataclasses import dataclass
from datetime import datetime

import bcrypt
from Crypto.Cipher import AES

# La consola de Windows suele ser cp1252 y rompería al imprimir acentos.
try:
    sys.stdout.reconfigure(encoding="utf-8")
except (AttributeError, ValueError):
    pass

# ===========================================================================
# CONFIGURACIÓN DE CIFRADO
# ===========================================================================

# Clave maestra AES. Debe decodificar (Base64 o hexadecimal) a 16, 24 o 32 bytes,
# igual que `converter/EncryptionKeyProvider.java`, y coincidir con el valor de
# TFM_HCC_ENCRYPTION_KEY que use el backend. Se puede sobreescribir por entorno.
ENCRYPTION_KEY_RAW = os.environ.get(
    "TFM_HCC_ENCRYPTION_KEY",
    "SENDLWRldiBBRVMga2V5IC0gbm90IGEgcmVhbCBzZWM=",  # 32 bytes, = .env.example
)

# Contexto de separación de dominio del índice de búsqueda (idéntico al backend:
# HmacSearchIndexServiceImpl.INDEX_KEY_CONTEXT).
INDEX_KEY_CONTEXT = b"hcc:search-index:v1"

# UUID fijos para las filas de `perfil`. Sobre una base de datos recién creada por
# Hibernate (ddl-auto=update en desarrollo) la tabla `perfil` está vacía, así que
# el script la siembra. Si tu base de datos ya tiene perfiles con otros UUID,
# ajústalos aquí (o borra el bloque de INSERT de `perfil` del SQL generado).
PERFIL_PACIENTE_ID = "a208c3c9-78a4-4182-9cf5-a872e86ff0cc"
PERFIL_MEDICO_ID = "650f90b0-26dc-4799-beb5-9f485eb56baa"
PERFIL_ADMIN_ID = "b7d1f6e2-4c3a-4b8e-9a1d-2f5c6e7a8b90"

# Idéntico a MedicoPaciente.ESTADO_ACTIVA en el backend.
ESTADO_RELACION_ACTIVA = "ACTIVA"

# Cuántos pacientes, como mínimo y máximo, se asignan a cada médico generado.
MIN_PACIENTES_POR_MEDICO = 1
MAX_PACIENTES_POR_MEDICO = 5


def _parse_key(raw: str) -> bytes:
    """Replica EncryptionKeyProvider: primero Base64, luego hexadecimal; exige 16/24/32 bytes."""
    raw = raw.strip()
    try:
        b = base64.b64decode(raw, validate=True)
        if len(b) in (16, 24, 32):
            return b
    except (binascii.Error, ValueError):
        pass
    try:
        b = bytes.fromhex(raw)
        if len(b) in (16, 24, 32):
            return b
    except ValueError:
        pass
    raise SystemExit(
        f"TFM_HCC_ENCRYPTION_KEY inválida: debe ser Base64 o hexadecimal de 16, 24 o 32 bytes "
        f"(recibido {len(raw)} caracteres)."
    )


MASTER_KEY = _parse_key(ENCRYPTION_KEY_RAW)
_INDEX_KEY = hmac.new(MASTER_KEY, INDEX_KEY_CONTEXT, hashlib.sha256).digest()


def aes_gcm_encrypt(plaintext: str | None) -> str | None:
    """AES/GCM/NoPadding con IV de 12 bytes y tag de 128 bits, codificado como
    Base64(IV || ciphertext || tag). Idéntico a AESEncryptionConverter.encryptToBase64."""
    if plaintext is None:
        return None
    iv = os.urandom(12)
    cipher = AES.new(MASTER_KEY, AES.MODE_GCM, nonce=iv, mac_len=16)
    ciphertext, tag = cipher.encrypt_and_digest(plaintext.encode("utf-8"))
    return base64.b64encode(iv + ciphertext + tag).decode("ascii")


def search_index(value: str | None) -> str | None:
    """Índice de búsqueda determinista: Base64(HMAC-SHA256(indexKey, value)).
    Idéntico a HmacSearchIndexServiceImpl.indexar."""
    if value is None:
        return None
    return base64.b64encode(
        hmac.new(_INDEX_KEY, value.encode("utf-8"), hashlib.sha256).digest()
    ).decode("ascii")


# ===========================================================================
# GENERACIÓN DE DATOS
# ===========================================================================

FIRST_NAMES = ["Luis", "María", "Carlos", "Ana", "Jorge", "Lucía", "Pablo", "Laura", "Miguel", "Sara"]
LAST_NAMES = ["García", "Fernández", "González", "López", "Martínez", "Sánchez", "Pérez", "Gómez", "Martín", "Jiménez"]
ESPECIALIDADES = ["Medicina general", "Cardiología", "Neurología", "Pediatría", "Dermatología", "Traumatología", "Endocrinología"]

ESTADO_CUENTA_ACTIVO = "ACTIVO"


@dataclass
class Usuario:
    id: str
    # Cifrado / hasheado, listo para insertar
    nombre: str
    apellido1: str
    apellido2: str | None
    email: str
    email_hash: str
    password_hash: str
    fecha_nacimiento: str  # texto cifrado (AES/GCM del ISO-8601)
    nif: str
    nif_hash: str
    telefono: str | None
    especialidad: str | None
    estado_cuenta: str
    fecha_creacion: str
    # Datos en claro (para el CSV y las pruebas de login)
    nombre_raw: str
    apellido1_raw: str
    apellido2_raw: str | None
    email_raw: str
    nif_raw: str
    telefono_raw: str
    fecha_nacimiento_raw: str


def hash_password_bcrypt(plain: str) -> str:
    """Hash BCrypt (10 rondas) compatible con Spring Security. La columna `password`
    NO se cifra con AES: guarda el hash BCrypt tal cual."""
    return bcrypt.hashpw(plain.encode("utf-8"), bcrypt.gensalt(rounds=10)).decode("utf-8")


def random_spanish_nif(usados: set) -> str:
    letras = "TRWAGMYFPDXBNJZSQVHLCKE"
    while True:
        numero = random.randint(10000000, 99999999)
        nif = f"{numero}{letras[numero % 23]}"
        if nif not in usados:
            usados.add(nif)
            return nif


def random_phone_es() -> str:
    return f"{random.choice([6, 7])}{''.join(random.choices(string.digits, k=8))}"


def _sin_acentos(texto: str) -> str:
    """Quita tildes y diacríticos (María -> Maria) para que la local-part del email
    sea ASCII: un correo con caracteres no ASCII es técnicamente inválido sin
    SMTPUTF8 y rompería cualquier validación de formato posterior."""
    descompuesto = unicodedata.normalize("NFKD", texto)
    return "".join(c for c in descompuesto if not unicodedata.combining(c))


def make_email(nombre: str, apellido1: str, idx: int) -> str:
    base = _sin_acentos(f"{nombre}.{apellido1}").lower().replace(" ", "")
    dominio = random.choice(["example.com", "mail.com", "demo.es", "udc.es"])
    return f"{base}{idx}@{dominio}"


def build_usuario(idx: int, as_medico: bool, nifs_usados: set, password_hash: str) -> Usuario:
    uid = str(uuid.uuid4())
    nombre_raw = random.choice(FIRST_NAMES)
    ap1_raw = random.choice(LAST_NAMES)
    ap2_raw = random.choice(LAST_NAMES) if random.random() < 0.5 else None
    email_raw = make_email(nombre_raw, ap1_raw, idx)
    nif_raw = random_spanish_nif(nifs_usados)
    tel_raw = random_phone_es()

    fnac_raw = f"{random.randint(1955, 2007)}-{random.randint(1, 12):02d}-{random.randint(1, 28):02d}"
    fnac_iso = f"{fnac_raw}T00:00:00"  # el backend guarda LocalDateTime en ISO-8601
    fcrea = datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]

    return Usuario(
        id=uid,
        nombre=aes_gcm_encrypt(nombre_raw),
        apellido1=aes_gcm_encrypt(ap1_raw),
        apellido2=aes_gcm_encrypt(ap2_raw) if ap2_raw else None,
        email=aes_gcm_encrypt(email_raw),
        email_hash=search_index(email_raw),
        password_hash=password_hash,
        fecha_nacimiento=aes_gcm_encrypt(fnac_iso),
        nif=aes_gcm_encrypt(nif_raw),
        nif_hash=search_index(nif_raw),
        telefono=aes_gcm_encrypt(tel_raw),
        especialidad=(random.choice(ESPECIALIDADES) if as_medico else None),
        estado_cuenta=ESTADO_CUENTA_ACTIVO,
        fecha_creacion=fcrea,
        nombre_raw=nombre_raw,
        apellido1_raw=ap1_raw,
        apellido2_raw=ap2_raw,
        email_raw=email_raw,
        nif_raw=nif_raw,
        telefono_raw=tel_raw,
        fecha_nacimiento_raw=fnac_raw,
    )


def sql_str(v: str | None) -> str:
    """Literal SQL: 'texto con '' escapado' o NULL."""
    if v is None:
        return "NULL"
    return "'" + v.replace("'", "''") + "'"


def make_inserts(pacientes: int, seed: int | None = None):
    if seed is not None:
        random.seed(seed)

    password_hash = hash_password_bcrypt("password")
    medicos = pacientes // 15
    nifs_usados: set = set()

    usuarios_pacientes = [build_usuario(i + 1, False, nifs_usados, password_hash) for i in range(pacientes)]
    usuarios_medicos = [build_usuario(pacientes + i + 1, True, nifs_usados, password_hash) for i in range(medicos)]
    usuarios = usuarios_pacientes + usuarios_medicos

    lines: list[str] = []

    # --- perfiles (idempotente sobre una BD que ya los tenga) ---
    ahora = datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]
    lines.append(
        "INSERT INTO public.perfil (id, rol, fecha_creacion, fecha_ultima_modificacion) VALUES\n"
        f"  ('{PERFIL_PACIENTE_ID}', 'PACIENTE',      '{ahora}', '{ahora}'),\n"
        f"  ('{PERFIL_MEDICO_ID}', 'MEDICO',        '{ahora}', '{ahora}'),\n"
        f"  ('{PERFIL_ADMIN_ID}', 'ADMINISTRADOR', '{ahora}', '{ahora}')\n"
        "ON CONFLICT (rol) DO NOTHING;"
    )

    # --- usuarios ---
    for u in usuarios:
        lines.append(
            "INSERT INTO public.usuario "
            "(id, nombre, apellido1, apellido2, email, email_hash, password, fecha_nacimiento, "
            "nif, nif_hash, telefono, especialidad, estado_cuenta, totp_secret, totp_enabled, "
            "fecha_creacion, fecha_ultima_modificacion, last_password_change, fecha_eliminacion) VALUES ("
            f"'{u.id}', {sql_str(u.nombre)}, {sql_str(u.apellido1)}, {sql_str(u.apellido2)}, "
            f"{sql_str(u.email)}, {sql_str(u.email_hash)}, {sql_str(u.password_hash)}, {sql_str(u.fecha_nacimiento)}, "
            f"{sql_str(u.nif)}, {sql_str(u.nif_hash)}, {sql_str(u.telefono)}, {sql_str(u.especialidad)}, "
            f"{sql_str(u.estado_cuenta)}, NULL, false, "
            f"'{u.fecha_creacion}', '{u.fecha_creacion}', '{u.fecha_creacion}', NULL);"
        )

    # --- perfil_usuario + historial_clinico ---
    for u in usuarios:
        lines.append(
            "INSERT INTO public.perfil_usuario (id, id_perfil, id_usuario, fecha_creacion, fecha_ultima_modificacion) "
            f"VALUES ('{uuid.uuid4()}', '{PERFIL_PACIENTE_ID}', '{u.id}', '{u.fecha_creacion}', '{u.fecha_creacion}');"
        )
        if u.especialidad:
            lines.append(
                "INSERT INTO public.perfil_usuario (id, id_perfil, id_usuario, fecha_creacion, fecha_ultima_modificacion) "
                f"VALUES ('{uuid.uuid4()}', '{PERFIL_MEDICO_ID}', '{u.id}', '{u.fecha_creacion}', '{u.fecha_creacion}');"
            )
        lines.append(
            "INSERT INTO public.historial_clinico (id, id_paciente, fecha_creacion, fecha_ultima_modificacion) "
            f"VALUES ('{uuid.uuid4()}', '{u.id}', '{u.fecha_creacion}', '{u.fecha_creacion}');"
        )

    # --- medico_paciente: cada médico generado queda con entre 1 y 5 pacientes ---
    lines.extend(build_asignaciones(usuarios_pacientes, usuarios_medicos))

    return lines, usuarios


def build_asignaciones(pacientes: list[Usuario], medicos: list[Usuario]) -> list[str]:
    """Asigna a cada médico entre MIN_ y MAX_PACIENTES_POR_MEDICO pacientes al azar,
    insertando la relación en medico_paciente con estado ACTIVA (salta el flujo real
    de solicitud/aceptación, igual que hace un alta administrativa directa)."""
    lines: list[str] = []
    if not pacientes:
        return lines

    ahora = datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]
    for medico in medicos:
        n = min(random.randint(MIN_PACIENTES_POR_MEDICO, MAX_PACIENTES_POR_MEDICO), len(pacientes))
        for paciente in random.sample(pacientes, k=n):
            lines.append(
                "INSERT INTO public.medico_paciente "
                "(id, id_medico, id_paciente, estado, fecha_creacion, fecha_ultima_modificacion) "
                f"VALUES ('{uuid.uuid4()}', '{medico.id}', '{paciente.id}', "
                f"'{ESTADO_RELACION_ACTIVA}', '{ahora}', '{ahora}');"
            )
    return lines


def write_csv(usuarios, path):
    with open(path, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f, delimiter=";")
        w.writerow(["id", "nombre", "apellido1", "apellido2", "email", "nif", "telefono", "fecha_nacimiento", "especialidad", "password"])
        for u in usuarios:
            w.writerow([
                u.id, u.nombre_raw, u.apellido1_raw, u.apellido2_raw or "", u.email_raw,
                u.nif_raw, u.telefono_raw, u.fecha_nacimiento_raw, u.especialidad or "", "password",
            ])


def main():
    p = argparse.ArgumentParser(description="Genera datos sintéticos para tfm-hcc.")
    p.add_argument("--pacientes", type=int, default=100, help="Número de pacientes a generar")
    p.add_argument("--seed", type=int, default=None, help="Semilla para reproducibilidad")
    p.add_argument("--out", type=str, default="inserts.sql", help="Archivo SQL de salida")
    p.add_argument("--csv", type=str, default="usuarios_generados.csv", help="Archivo CSV de salida")
    args = p.parse_args()

    print(f"Generando {args.pacientes} pacientes (+ {args.pacientes // 15} médicos)...")
    if args.seed is not None:
        print(f"Semilla: {args.seed}")
    print(f"Clave AES: {len(MASTER_KEY) * 8} bits")

    lines, usuarios = make_inserts(args.pacientes, args.seed)

    with open(args.out, "w", encoding="utf-8") as f:
        f.write("-- Datos generados por generar_datos_usuarios.py\n")
        f.write(f"-- Pacientes: {args.pacientes} | Médicos: {args.pacientes // 15} | Seed: {args.seed}\n")
        f.write(f"-- Fecha: {datetime.now():%Y-%m-%d %H:%M:%S}\n")
        f.write("-- Cifrado AES/GCM + índice HMAC-SHA256; requiere que el backend use la misma\n")
        f.write("-- TFM_HCC_ENCRYPTION_KEY con la que se generó este fichero.\n")
        f.write(f"-- Cada médico queda con entre {MIN_PACIENTES_POR_MEDICO} y {MAX_PACIENTES_POR_MEDICO} "
                "pacientes asignados (medico_paciente, estado ACTIVA).\n\n")
        f.write("\n".join(lines) + "\n")

    write_csv(usuarios, args.csv)

    n_asignaciones = sum(1 for l in lines if l.startswith("INSERT INTO public.medico_paciente"))
    print(f"[OK] Asignaciones médico-paciente: {n_asignaciones} "
          f"(entre {MIN_PACIENTES_POR_MEDICO} y {MAX_PACIENTES_POR_MEDICO} pacientes por médico)")
    print(f"[OK] SQL:  {args.out}  ({len(lines)} sentencias)")
    print(f"[OK] CSV:  {args.csv}  ({len(usuarios)} usuarios)")
    print("Todos los usuarios tienen la contrasena: 'password'")


if __name__ == "__main__":
    main()
