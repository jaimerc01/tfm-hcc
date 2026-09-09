import pandas as pd
from pathlib import Path

# Ruta donde tienes los archivos .XPT
input_dir = Path("C:\\Users\\jaime\\Universidad\\Q11\\TFM\\Datos")
output_dir = Path("C:\\Users\\jaime\\Universidad\\Q11\\TFM\\Datos\\csv_convertidos")
output_dir.mkdir(exist_ok=True)

for xpt_file in input_dir.glob("*.XPT"):
    df = pd.read_sas(xpt_file)
    csv_file = output_dir / f"{xpt_file.stem}.csv"
    df.to_csv(csv_file, index=False)
    print(f"Convertido: {xpt_file.name} -> {csv_file.name}")

print("Conversión completada ✅")
