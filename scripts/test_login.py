#!/usr/bin/env python3
"""
Prueba el login de los usuarios generados por `generar_datos_usuarios.py`.

Lee `usuarios_generados.csv` (NIF y datos en claro), llama al endpoint real de
login y comprueba que devuelve 200 + token. No hardcodea NIFs: así sigue
funcionando aunque se regenere la siembra con otra semilla.

Requisitos:  pip install requests
Uso:         python test_login.py            # backend en http://localhost:8081
             API_URL=http://otra:8081 python test_login.py
"""

import csv
import os
import sys

import requests

try:
    sys.stdout.reconfigure(encoding="utf-8")
except (AttributeError, ValueError):
    pass

API_URL = os.environ.get("API_URL", "http://localhost:8081")
LOGIN_ENDPOINT = f"{API_URL}/authentication/login"
PASSWORD = "password"
CSV_PATH = os.path.join(os.path.dirname(__file__), "usuarios_generados.csv")
N_PACIENTES = 10  # cuántos usuarios sin especialidad probar


def cargar_usuarios():
    with open(CSV_PATH, newline="", encoding="utf-8") as f:
        return list(csv.DictReader(f, delimiter=";"))


def elegir_muestra(usuarios):
    pacientes = [u for u in usuarios if u["rol"] == "paciente"][:N_PACIENTES]
    medicos = [u for u in usuarios if u["rol"] == "medico"][:1]
    admins = [u for u in usuarios if u["rol"] == "admin"]
    return pacientes + medicos + admins


def test_login(usuario):
    nif = usuario["nif"]
    etiqueta = f'{usuario["nombre"]} {usuario["apellido1"]}'.strip()
    rol = usuario["rol"]
    fecha_nacimiento = usuario["fecha_nacimiento"]
    try:
        r = requests.post(LOGIN_ENDPOINT, json={"nif": nif, "password": PASSWORD}, timeout=5)
    except requests.exceptions.ConnectionError:
        print(f"  [ERROR] sin conexión a {API_URL} (¿está el backend arrancado?)")
        return False

    if r.status_code == 200 and r.json().get("token"):
        print(f"  [OK]   {nif:<10} {rol:<8} {fecha_nacimiento:<12} {etiqueta}")
        return True
    print(f"  [FALLO] {nif:<10} {rol:<8} HTTP {r.status_code}: {r.text[:120]}")
    return False


def main():
    print(f"Login endpoint: {LOGIN_ENDPOINT}")
    print(f"Contraseña de todos los usuarios: {PASSWORD}\n")

    muestra = elegir_muestra(cargar_usuarios())
    ok = sum(test_login(u) for u in muestra)

    print(f"\n{ok}/{len(muestra)} logins correctos")
    if ok == len(muestra):
        print("Todo correcto: la siembra SQL y el cifrado son compatibles con el backend.")
        return 0
    print("Revisa: backend en el puerto correcto, inserts.sql cargado, y que\n"
          "TFM_HCC_ENCRYPTION_KEY del backend coincide con la del script generador.")
    return 1


if __name__ == "__main__":
    sys.exit(main())
