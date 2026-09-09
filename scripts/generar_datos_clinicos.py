#!/usr/bin/env python3
"""
Generador de datos clínicos sintéticos para el Historial Clínico Compartido (tfm-hcc).

Complementa a `generar_datos_usuarios.py`: aquél crea los usuarios y su
`historial_clinico` vacío; éste lo rellena con mediciones de análisis de sangre,
signos vitales y análisis de orina, produciendo un `inserts_datos_clinicos.sql`
listo para cargar sobre PostgreSQL.

Origen de los datos
-------------------
Los valores de análisis de sangre y signos vitales proceden de la National Health
and Nutrition Examination Survey (NHANES), ciclo 2017-marzo 2020 (pre-pandemia),
convertidos de XPT a CSV con `nhanes/conversor.py`. Ficheros y variables usadas
(ver `nhanes/README.md`):

  - P_GLU     LBXGLU                       -> Glucosa (mg/dL)
  - P_CBC     LBXHGB, LBXHCT               -> Hemoglobina (g/dL), Hematocrito (%)
  - P_BIOPRO  LBXSCR                       -> Creatinina (mg/dL)
  - P_TCHOL   LBXTC                        -> Colesterol total (mg/dL)
  - P_HDL     LBDHDD                       -> Colesterol HDL (mg/dL)
  - P_TRIGLY  LBXTR, LBDLDL                -> Triglicéridos, Colesterol LDL (mg/dL)
  - P_BPXO    BPXOSY1-3, BPXODI1-3,        -> Presión sistólica / diastólica (mmHg),
              BPXOPLS1-3                      Frecuencia cardíaca (lpm) [media de las 3 lecturas]
  - P_BMX     BMXBMI                       -> Índice de masa corporal (kg/m^2)

El pH de orina no se publica en NHANES 2017-2020: se genera sintéticamente con una
distribución normal centrada en 6,0 dentro del rango de referencia 4,5-8,0.

Estrategia de asignación
------------------------
NHANES es transversal (una medición por participante). Para construir series
temporales:

  1. Se cruzan los CSV por SEQN y se conserva solo el subconjunto de participantes
     con las 12 variables presentes y dentro de un rango fisiológicamente plausible
     (~3.700 perfiles completos).
  2. A cada usuario se le asignan entre --min y --max "analíticas" (por defecto 5 y
     10), cada una en una fecha sintética distinta repartida hacia atrás en el tiempo.
  3. En cada analítica se elige un perfil NHANES completo al azar y se registran
     TODOS los parámetros con esa misma fecha, de modo que cada punto temporal es el
     perfil real y coherente de un participante (el colesterol total, LDL, HDL y
     triglicéridos de una misma fecha corresponden a la misma persona).

Con la configuración por defecto NO se vuelca todo NHANES: solo se usan
`n_usuarios * n_analiticas` perfiles del pool (con reemplazo).

Cifrado
-------
`dato_clinico` cifra `tipo`, `valor`, `unidad` y `observacion` con AES/GCM y guarda
un índice de búsqueda HMAC-SHA256 en `tipo_hash`. Este script reutiliza
`aes_gcm_encrypt` y `search_index` de `generar_datos_usuarios.py`, así que requiere
la MISMA `TFM_HCC_ENCRYPTION_KEY` que el backend (ver `.env` / `.env.example`).

Orden de carga
--------------
    psql ... < scripts/inserts.sql                     # usuarios + historial vacío
    psql ... < scripts/insert_rangos_referencia.sql    # rangos de referencia
    psql ... < scripts/inserts_datos_clinicos.sql      # este fichero

El `id_historial_clinico` de cada fila se resuelve con una subconsulta por
`id_paciente`, y el `id_rango` con una subconsulta por nombre (igual que hace
`HistorialClinicoServiceImpl.buscarYAsignarRango`), así que este script no necesita
conocer esos UUID: basta con que los dos ficheros anteriores se hayan cargado antes.

Requisitos:
    pip install pandas pycryptodome bcrypt
"""

import argparse
import csv as csvmod
import os
import random
import shlex
import subprocess
import sys
import uuid
from datetime import datetime, timedelta

import pandas as pd

# Reutiliza el cifrado del generador de usuarios (misma clave, mismos algoritmos).
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
try:
    from generar_datos_usuarios import aes_gcm_encrypt, search_index, MASTER_KEY
except ImportError as e:  # pragma: no cover
    raise SystemExit(
        "No se pudo importar generar_datos_usuarios.py (debe estar en la misma carpeta "
        f"'scripts/'). Detalle: {e}"
    )

try:
    sys.stdout.reconfigure(encoding="utf-8")
except (AttributeError, ValueError):
    pass

# ===========================================================================
# CONFIGURACIÓN
# ===========================================================================

# Zona horaria de las fechas (coincide con ZONE_ID_EUROPA_MADRID del backend, que
# usa hora local sin desfase explícito en la columna).
HORA_ANALITICA = (8, 13)  # franja horaria [inicio, fin) de una analítica

# Catálogo de parámetros. El 'tipo' es EXACTAMENTE la `key` que usa el frontend
# (composables/useAnalisisSangre.js, useSignosVitales.js, useAnalisisOrina.js), de
# modo que:
#   - HistorialClinicoConverter lo clasifica en el apartado correcto
#     (SIGNOS_VITALES / ANALISIS_ORINA / resto -> ANALISIS_SANGRE),
#   - mapTipoToKey lo reconoce sin ambigüedad en las gráficas,
#   - buscarYAsignarRango encuentra su rango por findByNombreIgnoreCase.
#
# (tipo, unidad, columnas_nhanes, (min_plausible, max_plausible), decimales)
PARAMS = [
    ("glucosa",                     "mg/dL", ["LBXGLU"],                          (50, 400),  0),
    ("hemoglobina",                 "g/dL",  ["LBXHGB"],                          (7, 20),    1),
    ("colesterol",                  "mg/dL", ["LBXTC"],                           (80, 400),  0),
    ("Colesterol LDL",              "mg/dL", ["LBDLDL"],                          (20, 350),  0),
    ("Colesterol HDL",              "mg/dL", ["LBDHDD"],                          (15, 150),  0),
    ("trigliceridos",               "mg/dL", ["LBXTR"],                           (20, 1000), 0),
    ("creatinina",                  "mg/dL", ["LBXSCR"],                          (0.3, 3.0), 2),
    ("hematocrito",                 "%",     ["LBXHCT"],                          (25, 60),   1),
    ("Frecuencia Cardiaca",         "lpm",   ["BPXOPLS1", "BPXOPLS2", "BPXOPLS3"], (40, 140), 0),
    ("Presion Arterial Sistolica",  "mmHg",  ["BPXOSY1", "BPXOSY2", "BPXOSY3"],   (80, 220),  0),
    ("Presion Arterial Diastolica", "mmHg",  ["BPXODI1", "BPXODI2", "BPXODI3"],   (40, 140),  0),
    ("IMC",                         "kg/m²", ["BMXBMI"],                          (14, 60),   1),
]

# Cada fichero NHANES y las columnas que se extraen de él.
FICHEROS_NHANES = {
    "P_GLU":    ["LBXGLU"],
    "P_CBC":    ["LBXHGB", "LBXHCT"],
    "P_BIOPRO": ["LBXSCR"],
    "P_TCHOL":  ["LBXTC"],
    "P_HDL":    ["LBDHDD"],
    "P_TRIGLY": ["LBXTR", "LBDLDL"],
    "P_BPXO":   ["BPXOSY1", "BPXOSY2", "BPXOSY3",
                 "BPXODI1", "BPXODI2", "BPXODI3",
                 "BPXOPLS1", "BPXOPLS2", "BPXOPLS3"],
    "P_BMX":    ["BMXBMI"],
}

# pH de orina sintético (no hay fuente en NHANES 2017-2020).
PH_ORINA_TIPO = "PH Orina"
PH_ORINA_UNIDAD = "pH"
PH_ORINA_MEDIA = 6.0
PH_ORINA_SIGMA = 0.6
PH_ORINA_RANGO = (4.5, 8.0)


# ===========================================================================
# CARGA Y LIMPIEZA DE NHANES
# ===========================================================================

def cargar_pool(datos_dir: str) -> pd.DataFrame:
    """Cruza los CSV de NHANES por SEQN y devuelve solo los participantes con todas
    las variables presentes y dentro de un rango fisiológicamente plausible."""
    base = None
    for fichero, columnas in FICHEROS_NHANES.items():
        ruta = os.path.join(datos_dir, f"{fichero}.csv")
        if not os.path.isfile(ruta):
            raise SystemExit(
                f"No se encuentra {ruta}. Genera los CSV con nhanes/conversor.py "
                f"(ver nhanes/README.md)."
            )
        df = pd.read_csv(ruta, usecols=lambda c: c == "SEQN" or c in columnas)
        base = df if base is None else base.merge(df, on="SEQN", how="inner")

    # Medias de las tres lecturas de tensión / pulso.
    base["FC"] = base[["BPXOPLS1", "BPXOPLS2", "BPXOPLS3"]].mean(axis=1)
    base["SYS"] = base[["BPXOSY1", "BPXOSY2", "BPXOSY3"]].mean(axis=1)
    base["DIA"] = base[["BPXODI1", "BPXODI2", "BPXODI3"]].mean(axis=1)

    # Valor "resuelto" de cada parámetro (una sola columna por parámetro).
    resueltos = {
        "glucosa": base["LBXGLU"], "hemoglobina": base["LBXHGB"],
        "colesterol": base["LBXTC"], "Colesterol LDL": base["LBDLDL"],
        "Colesterol HDL": base["LBDHDD"], "trigliceridos": base["LBXTR"],
        "creatinina": base["LBXSCR"], "hematocrito": base["LBXHCT"],
        "Frecuencia Cardiaca": base["FC"], "Presion Arterial Sistolica": base["SYS"],
        "Presion Arterial Diastolica": base["DIA"], "IMC": base["BMXBMI"],
    }
    pool = pd.DataFrame(resueltos)

    # Filtro de plausibilidad: se descarta el perfil si algún valor falta o se sale.
    mascara = pd.Series(True, index=pool.index)
    for tipo, _unidad, _cols, (lo, hi), _dec in PARAMS:
        mascara &= pool[tipo].between(lo, hi)
    pool = pool[mascara].reset_index(drop=True)

    if pool.empty:
        raise SystemExit("El pool de perfiles NHANES completos quedó vacío tras el filtro.")
    return pool


# ===========================================================================
# GENERACIÓN
# ===========================================================================

def formatear_valor(valor: float, decimales: int) -> str:
    """Texto numérico normalizado (punto decimal), sin ceros de más."""
    if decimales == 0:
        return str(int(round(valor)))
    return f"{round(valor, decimales):.{decimales}f}"


def fechas_analiticas(n: int, rng: random.Random) -> list[datetime]:
    """n fechas sintéticas, de la más reciente a la más antigua, repartidas hacia
    atrás desde hace pocas semanas con ~2-4 meses de separación."""
    fechas = []
    cursor = datetime.now() - timedelta(days=rng.randint(5, 45))
    for _ in range(n):
        cursor = cursor.replace(
            hour=rng.randint(HORA_ANALITICA[0], HORA_ANALITICA[1] - 1),
            minute=rng.randint(0, 59),
            second=rng.randint(0, 59),
            microsecond=rng.randint(0, 999) * 1000,
        )
        fechas.append(cursor)
        cursor = cursor - timedelta(days=rng.randint(50, 130))
    return fechas


def ph_orina_sintetico(rng: random.Random) -> float:
    valor = rng.gauss(PH_ORINA_MEDIA, PH_ORINA_SIGMA)
    return max(PH_ORINA_RANGO[0], min(PH_ORINA_RANGO[1], valor))


def sql_str(v: str | None) -> str:
    if v is None:
        return "NULL"
    return "'" + v.replace("'", "''") + "'"


def generar_filas(usuarios: list[dict], pool: pd.DataFrame, min_a: int, max_a: int,
                  seed: int | None):
    """Devuelve (filas_sql, resumen) donde cada fila_sql es la tupla VALUES de un
    INSERT en dato_clinico."""
    rng = random.Random(seed)
    filas: list[str] = []
    total_analiticas = 0

    pool_idx = list(pool.index)

    for u in usuarios:
        uid = u["id"]
        n_analiticas = rng.randint(min_a, max_a)
        total_analiticas += n_analiticas
        fechas = fechas_analiticas(n_analiticas, rng)

        for fecha in fechas:
            perfil = pool.loc[rng.choice(pool_idx)]
            ts = fecha.strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]

            for tipo, unidad, _cols, _rango, decimales in PARAMS:
                valor_txt = formatear_valor(float(perfil[tipo]), decimales)
                filas.append(_fila_sql(uid, ts, tipo, valor_txt, unidad))

            # pH de orina sintético en la misma fecha.
            filas.append(_fila_sql(
                uid, ts, PH_ORINA_TIPO,
                formatear_valor(ph_orina_sintetico(rng), 1), PH_ORINA_UNIDAD,
            ))

    resumen = {
        "usuarios": len(usuarios),
        "analiticas": total_analiticas,
        "filas": len(filas),
        "parametros": len(PARAMS) + 1,
        "pool": len(pool),
    }
    return filas, resumen


def _fila_sql(uid: str, ts: str, tipo: str, valor_txt: str, unidad: str) -> str:
    """Una tupla VALUES para dato_clinico. Columnas:
    id, fecha_creacion, fecha_ultima_modificacion, tipo, tipo_hash, valor, unidad,
    observacion, id_rango, id_historial_clinico."""
    return (
        f"('{uuid.uuid4()}', '{ts}', '{ts}', "
        f"{sql_str(aes_gcm_encrypt(tipo))}, {sql_str(search_index(tipo))}, "
        f"{sql_str(aes_gcm_encrypt(valor_txt))}, {sql_str(aes_gcm_encrypt(unidad))}, "
        f"NULL, "
        f"(SELECT id FROM public.rangos WHERE LOWER(nombre) = LOWER({sql_str(tipo)}) LIMIT 1), "
        f"(SELECT id FROM public.historial_clinico WHERE id_paciente = '{uid}'))"
    )


def leer_usuarios(ruta_csv: str, solo_pacientes: bool) -> list[dict]:
    with open(ruta_csv, "r", encoding="utf-8", newline="") as f:
        filas = list(csvmod.DictReader(f, delimiter=";"))
    if solo_pacientes:
        filas = [r for r in filas if (r.get("rol") or "").strip().lower() == "paciente"]
    if not filas:
        raise SystemExit(f"{ruta_csv} no contiene usuarios (¿ruta o filtro incorrectos?).")
    return filas


# SQL que lista los historiales de la BD: id del paciente, su rol efectivo y si el
# historial ya tiene datos clínicos (para poder respetar los que se rellenaron a mano).
_SQL_HISTORIALES = (
    "SELECT hc.id_paciente, "
    "CASE WHEN EXISTS (SELECT 1 FROM perfil_usuario pu JOIN perfil p ON p.id = pu.id_perfil "
    "WHERE pu.id_usuario = hc.id_paciente AND p.rol = 'MEDICO') THEN 'medico' "
    "WHEN EXISTS (SELECT 1 FROM perfil_usuario pu JOIN perfil p ON p.id = pu.id_perfil "
    "WHERE pu.id_usuario = hc.id_paciente AND p.rol = 'ADMINISTRADOR') THEN 'admin' "
    "ELSE 'paciente' END AS rol, "
    "EXISTS (SELECT 1 FROM dato_clinico dc WHERE dc.id_historial_clinico = hc.id) AS tiene_datos "
    "FROM historial_clinico hc"
)


def leer_usuarios_db(psql_cmd: str, solo_pacientes: bool, incluir_con_datos: bool) -> list[dict]:
    """Lee los id_paciente directamente de la base de datos (modo --from-db), en vez
    del CSV. Útil cuando la BD no se sembró con el `inserts.sql` versionado."""
    argv = shlex.split(psql_cmd) + ["-tAF\t", "-c", _SQL_HISTORIALES]
    try:
        salida = subprocess.run(argv, capture_output=True, text=True, check=True).stdout
    except FileNotFoundError:
        raise SystemExit(f"No se pudo ejecutar psql: {argv[0]!r} no está en el PATH. Ajusta --psql.")
    except subprocess.CalledProcessError as e:
        raise SystemExit(f"psql falló:\n{e.stderr or e.stdout}")

    usuarios = []
    for linea in salida.splitlines():
        if not linea.strip():
            continue
        id_paciente, rol, tiene_datos = linea.split("\t")
        if solo_pacientes and rol != "paciente":
            continue
        if not incluir_con_datos and tiene_datos.strip().lower() in ("t", "true"):
            continue
        usuarios.append({"id": id_paciente, "rol": rol})

    if not usuarios:
        raise SystemExit(
            "La consulta a la BD no devolvió historiales que rellenar "
            "(¿--solo-pacientes de más, o todos ya tienen datos? prueba --incluir-con-datos)."
        )
    return usuarios


def escribir_sql(ruta: str, filas: list[str], resumen: dict, args) -> None:
    columnas = ("id, fecha_creacion, fecha_ultima_modificacion, tipo, tipo_hash, "
                "valor, unidad, observacion, id_rango, id_historial_clinico")
    with open(ruta, "w", encoding="utf-8") as f:
        f.write("-- Datos clínicos generados por generar_datos_clinicos.py\n")
        f.write(f"-- Fecha: {datetime.now():%Y-%m-%d %H:%M:%S} | Seed: {args.seed}\n")
        f.write(f"-- Usuarios: {resumen['usuarios']} | Analíticas por usuario: "
                f"{args.min}-{args.max} | Analíticas totales: {resumen['analiticas']}\n")
        f.write(f"-- Filas (dato_clinico): {resumen['filas']}  "
                f"({resumen['parametros']} parámetros por analítica)\n")
        f.write(f"-- Origen: NHANES 2017-2020 (pool de {resumen['pool']} perfiles completos) "
                "+ pH de orina sintético.\n")
        f.write("-- Cifrado AES/GCM + índice HMAC-SHA256: requiere la misma "
                "TFM_HCC_ENCRYPTION_KEY que el backend.\n")
        f.write("-- Cargar DESPUÉS de inserts.sql e insert_rangos_referencia.sql.\n\n")

        for i in range(0, len(filas), args.chunk):
            lote = filas[i:i + args.chunk]
            f.write(f"INSERT INTO public.dato_clinico ({columnas}) VALUES\n")
            f.write(",\n".join(lote))
            f.write(";\n\n")


def main() -> None:
    p = argparse.ArgumentParser(description="Genera datos clínicos sintéticos (NHANES) para tfm-hcc.")
    aqui = os.path.dirname(os.path.abspath(__file__))
    p.add_argument("--csv", default=os.path.join(aqui, "usuarios_generados.csv"),
                   help="CSV de usuarios generado por generar_datos_usuarios.py")
    p.add_argument("--datos", default=os.path.join(aqui, "nhanes"),
                   help="Carpeta con los CSV de NHANES (P_GLU.csv, P_CBC.csv, ...)")
    p.add_argument("--out", default=os.path.join(aqui, "inserts_datos_clinicos.sql"),
                   help="Fichero SQL de salida")
    p.add_argument("--min", type=int, default=5, help="Analíticas mínimas por usuario")
    p.add_argument("--max", type=int, default=10, help="Analíticas máximas por usuario")
    p.add_argument("--chunk", type=int, default=400, help="Filas por sentencia INSERT")
    p.add_argument("--solo-pacientes", action="store_true",
                   help="Solo usuarios con rol 'paciente' (por defecto: todos)")
    p.add_argument("--from-db", action="store_true",
                   help="Toma los id_paciente de la BD en vez del CSV (para una BD "
                        "que no se sembró con el inserts.sql versionado)")
    p.add_argument("--psql", default="docker exec -e PGPASSWORD=hcc -i tfm-hcc-postgres-1 "
                                     "psql -U hcc -d HCC_DEV",
                   help="Comando psql para --from-db (debe apuntar a la BD de desarrollo)")
    p.add_argument("--incluir-con-datos", action="store_true",
                   help="Con --from-db, también rellena historiales que YA tienen datos "
                        "clínicos (por defecto se respetan)")
    p.add_argument("--seed", type=int, default=None, help="Semilla para reproducibilidad")
    args = p.parse_args()

    if args.min < 1 or args.max < args.min:
        raise SystemExit("--min debe ser >=1 y --max >= --min.")

    if args.from_db:
        usuarios = leer_usuarios_db(args.psql, args.solo_pacientes, args.incluir_con_datos)
        origen = "BD" + ("" if args.incluir_con_datos else ", solo historiales vacíos")
    else:
        usuarios = leer_usuarios(args.csv, args.solo_pacientes)
        origen = os.path.basename(args.csv)
    print(f"Usuarios: {len(usuarios)} (origen: {origen}"
          f"{'; solo pacientes' if args.solo_pacientes else ''})")
    print(f"Clave AES: {len(MASTER_KEY) * 8} bits")

    pool = cargar_pool(args.datos)
    print(f"Pool NHANES: {len(pool)} perfiles completos y plausibles")

    filas, resumen = generar_filas(usuarios, pool, args.min, args.max, args.seed)
    escribir_sql(args.out, filas, resumen, args)

    print(f"[OK] {resumen['analiticas']} analíticas -> {resumen['filas']} filas de dato_clinico")
    print(f"[OK] SQL: {args.out}")
    print("Orden de carga: inserts.sql -> insert_rangos_referencia.sql -> inserts_datos_clinicos.sql")


if __name__ == "__main__":
    main()
