#!/usr/bin/env python3
"""
Generador de datos sintéticos para tablas:
 - public.usuario
 - public.perfil_usuario
 - public.historial_clinico

Además genera un archivo CSV con todos los datos en texto plano:
    usuarios_generados.csv

Características:
 - Cifra columnas sensibles con AES-128 ECB + PKCS7 + Base64 (igual a AESEncryptionConverter de Java)
 - Hashea la contraseña con bcrypt (compatible con Spring Security). La contraseña es fija: "password"
 - Regla de perfiles: por cada 15 pacientes, 1 médico; los médicos también tendrán perfil de PACIENTE
 - Crea historial_clinico para cada usuario con perfil PACIENTE (incluye médicos)
 - Exporta INSERTs SQL listos para PostgreSQL
 - Exporta CSV con datos originales para login: nif, email, teléfono, etc.

Requisitos:
    pip install pycryptodome bcrypt
"""

import argparse
import base64
import bcrypt
import csv
import random
import string
import uuid
from datetime import datetime, timedelta
from dataclasses import dataclass

from Crypto.Cipher import AES

# ====== CONSTANTES: IDs de perfil existentes en tu BD ======
PERFIL_PACIENTE = "a208c3c9-78a4-4182-9cf5-a872e86ff0cc"
PERFIL_MEDICO   = "650f90b0-26dc-4799-beb5-9f485eb56baa"

# ====== CONFIG ENCRIPTACIÓN ======
AES_KEY = b"1234567890123456"  # 16 bytes

# Contraseña hasheada con BCrypt - ya encriptada con AES
# Esta contraseña ya está procesada y lista para insertar en la BD
BCRYPT_PASSWORD_HASH = "uLrtuOSJk2Z8Z65Lyt8oWGfceHBxRAAQmH1FOf3K7FsSWqGmST7NPk4SHs+vF6KeG3n3lwr1NKPilSMWicqv1Q=="

def _pkcs7_pad(data: bytes, block_size: int = 16) -> bytes:
    pad_len = block_size - (len(data) % block_size)
    return data + bytes([pad_len]) * pad_len

def aes_encrypt_to_b64(plaintext: str) -> str:
    if plaintext is None:
        return None
    cipher = AES.new(AES_KEY, AES.MODE_ECB)
    padded = _pkcs7_pad(plaintext.encode("utf-8"))
    ct = cipher.encrypt(padded)
    return base64.b64encode(ct).decode("ascii")

# ====== UTILIDADES ======
FIRST_NAMES = ["Luis","María","Carlos","Ana","Jorge","Lucía","Pablo","Laura","Miguel","Sara"]
LAST_NAMES  = ["García","Fernández","González","López","Martínez","Sánchez","Pérez","Gómez","Martín","Jiménez"]
ESPECIALIDADES = ["Medicina general","Cardiología","Neurología","Pediatría","Dermatología","Traumatología","Endocrinología"]

@dataclass
class Usuario:
    id: uuid.UUID
    nombre: str
    apellido1: str
    apellido2: str | None
    email: str
    password_hash: str
    fecha_nacimiento: str
    nif: str
    telefono: str | None
    especialidad: str | None
    fecha_creacion: str
    fecha_ultima_modificacion: str | None
    last_password_change: str | None
    estado_cuenta: str
    fecha_eliminacion: str | None

    # Datos sin cifrar para CSV
    nombre_raw: str
    apellido1_raw: str
    apellido2_raw: str | None
    email_raw: str
    nif_raw: str
    telefono_raw: str


def random_spanish_nif() -> str:
    letras = "TRWAGMYFPDXBNJZSQVHLCKE"
    numero = random.randint(10000000, 99999999)
    letra = letras[numero % 23]
    return f"{numero}{letra}"


def random_phone_es() -> str:
    start = random.choice([6,7])
    rest = ''.join(random.choices(string.digits, k=8))
    return f"{start}{rest}"


def make_email(nombre: str, apellido1: str, idx: int) -> str:
    base = f"{nombre}.{apellido1}".lower().replace(" ", "")
    dominio = random.choice(["example.com","mail.com","demo.es","udc.es"])
    return f"{base}{idx}@{dominio}"


def hash_password_bcrypt(plain: str) -> str:
    """
    Retorna un hash bcrypt fijo para reproducibilidad.
    Siempre retorna el mismo hash para la contraseña "password".
    Compatible con Spring Security BCryptPasswordEncoder (10 rounds).
    """
    if plain == "password":
        return BCRYPT_PASSWORD_HASH
    # Si por alguna razón se pasa otra contraseña, generarla con salt aleatorio
    salt = bcrypt.gensalt(rounds=10)
    return bcrypt.hashpw(plain.encode("utf-8"), salt).decode("utf-8")


def ts_between(days_back: int = 365) -> datetime:
    now = datetime.now()
    delta = timedelta(days=random.randint(0, days_back), seconds=random.randint(0,86400))
    return now - delta


def sql_escape(v: str) -> str:
    return v.replace("'","''")


def build_usuario(idx: int, as_medico: bool) -> Usuario:
    uid = uuid.uuid4()
    nombre_raw = random.choice(FIRST_NAMES)
    ap1_raw = random.choice(LAST_NAMES)
    ap2_raw = random.choice(LAST_NAMES) if random.random() < 0.5 else None
    email_raw = make_email(nombre_raw, ap1_raw, idx)
    nif_raw = random_spanish_nif()
    tel_raw = random_phone_es()

    # Fechas
    fnac = f"{random.randint(1955,2007)}-{random.randint(1,12):02d}-{random.randint(1,28):02d}"
    fcrea_dt = datetime.now()  # Fecha actual siempre
    fcrea = fcrea_dt.strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]

    # Cifrado
    nombre_c = aes_encrypt_to_b64(nombre_raw)
    ap1_c = aes_encrypt_to_b64(ap1_raw)
    ap2_c = aes_encrypt_to_b64(ap2_raw) if ap2_raw else None
    email_c = aes_encrypt_to_b64(email_raw)
    nif_c = aes_encrypt_to_b64(nif_raw)
    tel_c = aes_encrypt_to_b64(tel_raw)

    return Usuario(
        id=uid,
        nombre=nombre_c,
        apellido1=ap1_c,
        apellido2=ap2_c,
        email=email_c,
        password_hash=hash_password_bcrypt("password"),
        fecha_nacimiento=fnac,
        nif=nif_c,
        telefono=tel_c,
        especialidad=(random.choice(ESPECIALIDADES) if as_medico else None),
        fecha_creacion=fcrea,
        fecha_ultima_modificacion=fcrea,
        last_password_change=fcrea,
        estado_cuenta="ACTIVO",
        fecha_eliminacion=None,
        nombre_raw=nombre_raw,
        apellido1_raw=ap1_raw,
        apellido2_raw=ap2_raw,
        email_raw=email_raw,
        nif_raw=nif_raw,
        telefono_raw=tel_raw,
    )


def make_inserts(pacientes: int, seed: int|None=None):
    if seed is not None:
        random.seed(seed)

    usuarios = []

    medicos = pacientes // 15

    for i in range(pacientes):
        usuarios.append(build_usuario(i+1, False))
    for i in range(medicos):
        usuarios.append(build_usuario(pacientes+i+1, True))

    sql_lines = []

    for u in usuarios:
        sql_lines.append(
            f"INSERT INTO public.usuario (id,nombre,apellido1,apellido2,email,password,fecha_nacimiento,nif,telefono,especialidad,fecha_creacion,fecha_ultima_modificacion,last_password_change,estado_cuenta,fecha_eliminacion) VALUES ('{u.id}','{sql_escape(u.nombre)}','{sql_escape(u.apellido1)}',{('NULL' if not u.apellido2 else "'"+sql_escape(u.apellido2)+"'")},'{sql_escape(u.email)}','{sql_escape(u.password_hash)}','{u.fecha_nacimiento}','{sql_escape(u.nif)}',{('NULL' if not u.telefono else "'"+sql_escape(u.telefono)+"'")},{('NULL' if not u.especialidad else "'"+sql_escape(u.especialidad)+"'")},'{u.fecha_creacion}','{u.fecha_ultima_modificacion}','{u.last_password_change}','{u.estado_cuenta}',NULL);"
        )

    # perfiles + historial
    for u in usuarios:
        pid = uuid.uuid4()
        sql_lines.append(f"INSERT INTO public.perfil_usuario VALUES ('{u.id}','{PERFIL_PACIENTE}','{u.fecha_creacion}',NULL,'{pid}');")

        if u.especialidad:
            pid2 = uuid.uuid4()
            sql_lines.append(f"INSERT INTO public.perfil_usuario VALUES ('{u.id}','{PERFIL_MEDICO}','{u.fecha_creacion}',NULL,'{pid2}');")

        hid = uuid.uuid4()
        sql_lines.append(
            f"INSERT INTO public.historial_clinico VALUES ('{hid}','Sin datos relevantes',NULL,'{u.id}','{u.fecha_creacion}');"
        )

    return sql_lines, usuarios


def write_csv(usuarios, path="usuarios_generados.csv"):
    with open(path, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f, delimiter=';')
        w.writerow(["id","nombre","apellido1","apellido2","email","nif","telefono","especialidad"])
        for u in usuarios:
            w.writerow([
                u.id,
                u.nombre_raw,
                u.apellido1_raw,
                u.apellido2_raw or "",
                u.email_raw,
                u.nif_raw,
                u.telefono_raw,
                u.especialidad or ""
            ])


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--pacientes", type=int, default=100, help="Número de pacientes a generar")
    p.add_argument("--seed", type=int, default=None, help="Semilla para reproducibilidad")
    p.add_argument("--out", type=str, default="inserts.sql", help="Archivo de salida SQL")
    p.add_argument("--csv", type=str, default="usuarios_generados.csv", help="Archivo CSV de salida")
    args = p.parse_args()

    print(f"Generando {args.pacientes} pacientes...")
    if args.seed:
        print(f"Usando semilla: {args.seed}")
    
    sql_lines, usuarios = make_inserts(args.pacientes, args.seed)
    
    # Escribir SQL
    with open(args.out, "w", encoding="utf-8") as f:
        f.write("-- Datos generados por generar_datos_usuarios.py\n")
        f.write(f"-- Pacientes: {args.pacientes}\n")
        f.write(f"-- Seed: {args.seed}\n")
        f.write(f"-- Fecha: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")
        for line in sql_lines:
            f.write(line + "\n")
    
    print(f"✓ Archivo SQL generado: {args.out}")
    print(f"  - Total de usuarios: {len(usuarios)}")
    print(f"  - Pacientes: {args.pacientes}")
    print(f"  - Médicos: {args.pacientes // 15}")
    print(f"  - Total de inserts: {len(sql_lines)}")
    
    # Escribir CSV
    write_csv(usuarios, args.csv)
    print(f"✓ Archivo CSV generado: {args.csv}")
    print(f"\n📝 Nota: Todos los usuarios tienen contraseña: 'password'")


if __name__ == "__main__":
    main()
