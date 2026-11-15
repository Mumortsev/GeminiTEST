"""Design token contrast calculation. No app execution or software tests."""
from pathlib import Path
import re
root=Path(__file__).resolve().parents[1]
text=(root/'app/src/main/java/ru/withbaby/app/Theme.kt').read_text()
def rgb(h):return [int(h[i:i+2],16)/255 for i in (0,2,4)]
def light(c):
 linear=[v/12.92 if v<=.04045 else ((v+.055)/1.055)**2.4 for v in c]
 return sum(x*y for x,y in zip(linear,[.2126,.7152,.0722]))
def ratio(a,b):x,y=sorted([light(a),light(b)]);return (y+.05)/(x+.05)
rows=[]
for key,body in re.findall(r'private val (\w+)=(?:light|dark)ColorScheme\((.*?)\)\n',text,re.S):
 colors={k:rgb(v) for k,v in re.findall(r'(\w+)=Color\(0xFF([A-Fa-f0-9]{6})\)',body)}
 for fg,bg in [('onBackground','background'),('onSurface','surfaceVariant'),('onSurfaceVariant','surfaceVariant'),('onPrimaryContainer','primaryContainer')]:
  rows.append(f'| {key} | {fg} / {bg} | {ratio(colors[fg],colors[bg]):.2f}:1 |')
(root/'docs/CONTRAST.md').write_text('# Контраст статических цветовых токенов\n\nПричина чёрного текста устранена явной передачей LocalContentColor через корневую Surface, contentColor Scaffold и onPrimaryContainer карточки Aurora. Подложки обычных карточек теперь непрозрачны, свечение ослаблено. Ниже — математический расчёт исходных цветовых пар, а не проверка отрисованного экрана. Он не учитывает сглаживание шрифта, фотографии, disabled-состояния или наложение Aurora; полная проверка на устройстве не выполнялась по указанию пользователя.\n\n| Палитра | Пара | Контраст |\n|---|---|---|\n'+'\n'.join(rows)+'\n')
print('\n'.join(rows))
