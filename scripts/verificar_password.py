#!/usr/bin/env python3
"""
Comprueba que el hash BCrypt que genera `generar_datos_usuarios.py` para la
contraseña "password" es válido y compatible con Spring Security
(BCryptPasswordEncoder, 10 rondas). La columna `usuario.password` guarda el hash
BCrypt tal cual, sin cifrado AES.

Requisitos:  pip install bcrypt
"""

import sys

import bcrypt

try:
    sys.stdout.reconfigure(encoding="utf-8")
except (AttributeError, ValueError):
    pass

PASSWORD = b"password"

hash_generado = bcrypt.hashpw(PASSWORD, bcrypt.gensalt(rounds=10)).decode("utf-8")

print("=" * 60)
print("VERIFICACIÓN DE HASH BCrypt")
print("=" * 60)
print(f"Hash generado:  {hash_generado}")
print(f"Prefijo/rondas: {hash_generado[:7]}  (esperado $2b$10$)")
print(f"Longitud:       {len(hash_generado)}  (esperado 60)")
print(f"Verifica 'password':          {bcrypt.checkpw(PASSWORD, hash_generado.encode())}")
print(f"Rechaza contraseña incorrecta: {not bcrypt.checkpw(b'wrong', hash_generado.encode())}")
print("=" * 60)
