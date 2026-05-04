#!/usr/bin/env python3
"""
Script para probar el login de usuarios generados.
Envía una petición al endpoint de login y verifica que funcione.
"""

import requests
import sys

# Configuración
API_URL = "http://localhost:8081"
LOGIN_ENDPOINT = f"{API_URL}/authentication/login"

# Usuarios de prueba del CSV
USUARIOS_PRUEBA = [
    {
        "nif": "39958838H",
        "email": "maría.garcía1@mail.com",
        "nombre": "María García"
    },
    {
        "nif": "10872248X", 
        "email": "miguel.pérez2@demo.es",
        "nombre": "Miguel Pérez Jiménez"
    },
    {
        "nif": "20576383P",
        "email": "luis.gómez3@udc.es", 
        "nombre": "Luis Gómez"
    }
]

PASSWORD = "password"

def test_login(nif, nombre):
    """Intenta hacer login con el NIF y contraseña."""
    print(f"\n{'='*60}")
    print(f"🔐 Probando login: {nombre}")
    print(f"   NIF: {nif}")
    print(f"   Password: {PASSWORD}")
    print(f"{'='*60}")
    
    payload = {
        "nif": nif,
        "password": PASSWORD
    }
    
    try:
        response = requests.post(LOGIN_ENDPOINT, json=payload, timeout=5)
        
        print(f"\n📡 Status Code: {response.status_code}")
        
        if response.status_code == 200:
            data = response.json()
            token = data.get('token', '')
            print(f"✅ LOGIN EXITOSO!")
            print(f"🎫 Token JWT recibido: {token[:50]}..." if len(token) > 50 else f"🎫 Token JWT: {token}")
            
            # Verificar datos del usuario
            if 'user' in data:
                user = data['user']
                print(f"\n👤 Datos del usuario:")
                print(f"   - ID: {user.get('id', 'N/A')}")
                print(f"   - NIF: {user.get('nif', 'N/A')}")
                print(f"   - Email: {user.get('email', 'N/A')}")
                print(f"   - Roles: {user.get('roles', 'N/A')}")
            
            return True
            
        elif response.status_code == 401:
            print(f"❌ LOGIN FALLIDO - Credenciales incorrectas")
            print(f"   Respuesta: {response.text}")
            return False
            
        else:
            print(f"⚠️  Error inesperado")
            print(f"   Respuesta: {response.text}")
            return False
            
    except requests.exceptions.ConnectionError:
        print(f"❌ ERROR: No se pudo conectar al servidor en {API_URL}")
        print(f"   Asegúrate de que la aplicación Spring Boot está ejecutándose.")
        return False
        
    except Exception as e:
        print(f"❌ ERROR: {str(e)}")
        return False


def main():
    print("="*60)
    print("🧪 TEST DE LOGIN - Usuarios Generados")
    print("="*60)
    print(f"API URL: {API_URL}")
    print(f"Login Endpoint: {LOGIN_ENDPOINT}")
    print(f"Contraseña para todos: {PASSWORD}")
    
    exitosos = 0
    fallidos = 0
    
    for usuario in USUARIOS_PRUEBA:
        if test_login(usuario['nif'], usuario['nombre']):
            exitosos += 1
        else:
            fallidos += 1
    
    print(f"\n{'='*60}")
    print(f"📊 RESUMEN DE PRUEBAS")
    print(f"{'='*60}")
    print(f"✅ Exitosos: {exitosos}/{len(USUARIOS_PRUEBA)}")
    print(f"❌ Fallidos: {fallidos}/{len(USUARIOS_PRUEBA)}")
    print(f"{'='*60}")
    
    if exitosos == len(USUARIOS_PRUEBA):
        print("\n🎉 ¡TODOS LOS LOGINS FUNCIONARON CORRECTAMENTE!")
        print("   La contraseña 'password' está configurada correctamente.")
        return 0
    elif exitosos > 0:
        print("\n⚠️  Algunos logins funcionaron, pero otros fallaron.")
        return 1
    else:
        print("\n❌ NINGÚN LOGIN FUNCIONÓ")
        print("   Verifica:")
        print("   1. La aplicación Spring Boot está corriendo en puerto 8081")
        print("   2. Los usuarios fueron insertados en la base de datos")
        print("   3. El hash de contraseña es correcto")
        return 2


if __name__ == "__main__":
    sys.exit(main())
