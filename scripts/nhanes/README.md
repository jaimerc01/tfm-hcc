# Datos de NHANES para la siembra clínica

Origen de los valores de análisis de sangre y signos vitales que
`../generar_datos_clinicos.py` inserta en `dato_clinico`.

## Qué es

**NHANES** (National Health and Nutrition Examination Survey), encuesta del
**National Center for Health Statistics** (NCHS / CDC). Ciclo **2017–marzo 2020
(pre-pandemia)**, prefijo `P_`. Datos anónimos, uso educativo y de investigación.

## Ficheros

Los `.csv` de esta carpeta son un recorte (solo `SEQN` + las columnas que se usan)
de la conversión a CSV de los ficheros `.XPT` originales.

| Fichero | Variable | Parámetro de la aplicación | Unidad |
|---|---|---|---|
| `P_GLU`    | `LBXGLU`            | Glucosa (en ayunas)          | mg/dL |
| `P_CBC`    | `LBXHGB`            | Hemoglobina                  | g/dL  |
| `P_CBC`    | `LBXHCT`            | Hematocrito                  | %     |
| `P_CBC`    | `LBXRBCSI`          | *(reserva: recuento de eritrocitos)* | millones/µL |
| `P_BIOPRO` | `LBXSCR`            | Creatinina                   | mg/dL |
| `P_BIOPRO` | `LBXSGL`            | *(reserva: glucosa sérica no en ayunas)* | mg/dL |
| `P_TCHOL`  | `LBXTC`            | Colesterol total             | mg/dL |
| `P_HDL`    | `LBDHDD`           | Colesterol HDL               | mg/dL |
| `P_TRIGLY` | `LBXTR`            | Triglicéridos (en ayunas)    | mg/dL |
| `P_TRIGLY` | `LBDLDL`          | Colesterol LDL (Friedewald)  | mg/dL |
| `P_BPXO`   | `BPXOSY1..3`       | Presión arterial sistólica (media) | mmHg |
| `P_BPXO`   | `BPXODI1..3`       | Presión arterial diastólica (media) | mmHg |
| `P_BPXO`   | `BPXOPLS1..3`      | Frecuencia cardíaca (media)  | lpm   |
| `P_BMX`    | `BMXBMI`          | Índice de masa corporal      | kg/m² |
| `P_BMX`    | `BMXWT`, `BMXHT`  | *(reserva: peso, talla)*     | kg, cm |

El **pH de orina** no se publica en NHANES 2017–2020; `generar_datos_clinicos.py`
lo genera sintéticamente (normal centrada en 6,0 dentro de 4,5–8,0).

## Cómo reproducir la descarga

1. Descargar los 8 `.XPT` de la web de NHANES (mismo patrón de URL para todos):

   ```
   https://wwwn.cdc.gov/Nchs/Data/Nhanes/Public/2017/DataFiles/P_GLU.xpt
   https://wwwn.cdc.gov/Nchs/Data/Nhanes/Public/2017/DataFiles/P_CBC.xpt
   https://wwwn.cdc.gov/Nchs/Data/Nhanes/Public/2017/DataFiles/P_BIOPRO.xpt
   https://wwwn.cdc.gov/Nchs/Data/Nhanes/Public/2017/DataFiles/P_TCHOL.xpt
   https://wwwn.cdc.gov/Nchs/Data/Nhanes/Public/2017/DataFiles/P_HDL.xpt
   https://wwwn.cdc.gov/Nchs/Data/Nhanes/Public/2017/DataFiles/P_TRIGLY.xpt
   https://wwwn.cdc.gov/Nchs/Data/Nhanes/Public/2017/DataFiles/P_BPXO.xpt
   https://wwwn.cdc.gov/Nchs/Data/Nhanes/Public/2017/DataFiles/P_BMX.xpt
   ```

2. Convertirlos a CSV con `conversor.py` (ajustando `input_dir` / `output_dir`):

   ```
   pip install pandas
   python conversor.py
   ```

   `conversor.py` recorre los `*.XPT` de la carpeta y hace `pandas.read_sas` +
   `DataFrame.to_csv` de cada uno.

3. Los CSV completos son ~6,5 MB. Los de esta carpeta están recortados a las
   columnas usadas para no versionar datos innecesarios; el generador solo lee
   esas columnas, así que ambos formatos sirven.
