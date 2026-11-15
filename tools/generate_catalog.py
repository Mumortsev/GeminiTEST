"""Update domain catalog rows from metadata; does not generate or replace pictures.

Photorealistic WebP files are committed resources. To re-export supplied PNG
originals, use prepare_photos.py. No AI service is used by the Android app.
"""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
items=json.loads((ROOT/'art_sources/catalog.json').read_text())
path=ROOT/'core/domain/src/main/kotlin/ru/withbaby/domain/GarmentCatalog.kt'
source=path.read_text()
a=source.index(' val all=listOf(',source.index('object GarmentCatalog'))+len(' val all=listOf(')
b=source.index('\n )\n fun find',a)
q=lambda text:json.dumps(text,ensure_ascii=False)
rows=[f'  GarmentType({q(it["id"])},{q(it["name"])},GarmentGroup.{it["group"]},{it["warmth"]},{float(it["min_c"])},{float(it["max_c"])},{it["min_months"]}),' for it in items if 'group' in it]
path.write_text(source[:a]+'\n'+'\n'.join(rows)+source[b:])
print(len(rows),'garment types; image resources unchanged')
