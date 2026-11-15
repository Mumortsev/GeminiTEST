"""Export ready catalog photographs and texture sheets to offline WebP resources.
No content redraw or external image service. Requires ImageMagick (`magick`).
Sheets are sliced by BitmapRegionDecoder at display time, not copied into 6 files.
"""
from pathlib import Path
import argparse,json,subprocess,hashlib
ROOT=Path(__file__).resolve().parents[1]
parser=argparse.ArgumentParser();parser.add_argument('--sources',type=Path,required=True);args=parser.parse_args()
items=json.loads((ROOT/'art_sources/catalog.json').read_text());names={it['id']:it['name'] for it in items}
layout=json.loads((ROOT/'art_sources/layout.json').read_text())
coverage=layout['singles']+[id for sheet in layout['sheets'] for id in sheet['items']]
if len(coverage)!=len(set(coverage)) or set(coverage)!=set(names):raise SystemExit('Layout does not cover all unique catalog IDs')
sources=layout['singles']+[sheet['id'] for sheet in layout['sheets']]
missing=[id for id in sources if not (args.sources/(id+'.png')).exists()]
if missing:raise SystemExit('Missing PNG sources: '+', '.join(missing))
out=ROOT/'app/src/main/res/drawable-nodpi';out.mkdir(parents=True,exist_ok=True)
resources={};records=[];references={}
def encode(source_id,resource,limit):
 src=args.sources/(source_id+'.png');dest=out/(resource+'.webp')
 subprocess.run(['magick',str(src),'-resize',str(limit)+'x'+str(limit)+'>','-strip','-define','webp:method=6','-quality','90',str(dest)],check=True)
 dims=subprocess.check_output(['magick','identify','-format','%w %h',str(dest)],text=True).split();width,height=map(int,dims)
 resources[resource]=dict(file=str(dest.relative_to(ROOT)),bytes=dest.stat().st_size,width=width,height=height,source_sha256=hashlib.sha256(src.read_bytes()).hexdigest(),webp_sha256=hashlib.sha256(dest.read_bytes()).hexdigest())
 return width,height
for id in layout['singles']:
 resource='art_'+id;w,h=encode(id,resource,768)
 records.append(dict(id=id,name=names[id],resource=resource,columns=1,rows=1,index=0,width=w,height=h));references[id]=f'CatalogPhoto(R.drawable.{resource})'
for sheet in layout['sheets']:
 resource=sheet['id'];w,h=encode(sheet['id'],resource,1536)
 for index,id in enumerate(sheet['items']):
  records.append(dict(id=id,name=names[id],resource=resource,columns=sheet['columns'],rows=sheet['rows'],index=index,width=w//sheet['columns'],height=h//sheet['rows']))
  references[id]=f'CatalogPhoto(R.drawable.{resource},{sheet["columns"]},{sheet["rows"]},{index})'
for item in items:(ROOT/'app/src/main/res/drawable'/('art_'+item['id']+'.xml')).unlink(missing_ok=True)
expected={name+'.webp' for name in resources}
for file in out.glob('*.webp'):
 if file.name not in expected and (file.name.startswith('art_') or file.name.startswith('photo_sheet_')):file.unlink()
(ROOT/'app/src/main/java/ru/withbaby/app/ArtResources.kt').write_text('package ru.withbaby.app\n\n// Generated stable ID -> bundled photograph or rectangular sheet region.\ndata class CatalogPhoto(val drawable:Int,val columns:Int=1,val rows:Int=1,val index:Int=0)\nobject ArtResources {val images=mapOf(\n'+',\n'.join(f' "{id}" to {references[id]}' for id in sorted(references))+'\n)}\n')
manifest=dict(style='Generated photorealistic product imagery approved in conversation',format='WebP',quality=90,count=len(records),resource_files=len(resources),total_bytes=sum(r['bytes'] for r in resources.values()),resources=resources,images=records)
(ROOT/'art_sources/manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
print(len(records),'images;',len(resources),'WebP resources;',manifest['total_bytes'],'bytes')
