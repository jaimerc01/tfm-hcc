#!/usr/bin/env python3
"""Verifica que el hash de contraseña sea correcto"""
import bcrypt

# Hash generado por el script
HASH_GENERADO = "$2b$10$N9qo8uLOickgx2ZMRZoMye8fOsiTWZqYtkxvXkKm8BMzjT7t/vIdq"

# Verificar que funciona con "password"
resultado = bcrypt.checkpw(b"password", HASH_GENERADO.encode('utf-8'))

print("=" * 60)
print("VERIFICACIÓN DE HASH BCrypt")
print("=" * 60)
print(f"Hash generado: {HASH_GENERADO}")
print(f"Contraseña: password")
print(f"Verificación: {'✓ CORRECTO' if resultado else '✗ INCORRECTO'}")
print(f"Compatible con Spring Security: {'✓ SÍ' if resultado else '✗ NO'}")
print("=" * 60)

# Verificar que NO funciona con otra contraseña
resultado_incorrecto = bcrypt.checkpw(b"wrong_password", HASH_GENERADO.encode('utf-8'))
print(f"Prueba con contraseña incorrecta: {'✗ FALLÓ (esperado)' if not resultado_incorrecto else '✓ CORRECTO'}")
print("=" * 60)
