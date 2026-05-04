import json
import sys
from collections import Counter

def find_duplicates(file_path):
    with open(file_path, encoding='utf-8') as f:
        lines = f.readlines()
    keys = [
        line.split(':', 1)[0].strip().strip('"')
        for line in lines if ':' in line and not line.strip().startswith('//')
    ]
    counter = Counter(keys)
    return [k for k, v in counter.items() if v > 1]

def main():
    if len(sys.argv) != 3:
        print("Uso: python compare_locales.py <archivo1.json> <archivo2.json>")
        sys.exit(1)
    file1, file2 = sys.argv[1], sys.argv[2]
    with open(file1, encoding='utf-8') as f:
        data1 = json.load(f)
    with open(file2, encoding='utf-8') as f:
        data2 = json.load(f)

    dup1 = find_duplicates(file1)
    dup2 = find_duplicates(file2)

    keys1 = set(data1.keys())
    keys2 = set(data2.keys())

    only_in_1 = keys1 - keys2
    only_in_2 = keys2 - keys1
    in_both = keys1 & keys2

    print(f'Claves repetidas en {file1}:', dup1)
    print(f'Claves repetidas en {file2}:', dup2)
    print(f'\nClaves solo en {file1}:', sorted(only_in_1))
    print(f'Claves solo en {file2}:', sorted(only_in_2))
    print(f'\nTotal claves en {file1}: {len(keys1)}')
    print(f'Total claves en {file2}: {len(keys2)}')
    print(f'Claves en ambos: {len(in_both)}')

if __name__ == "__main__":
    main()
