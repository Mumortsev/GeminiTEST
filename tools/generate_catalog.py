"""Original offline catalog and vector illustrations. No downloaded image assets."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
# id, Russian label, group, silhouette, warmth, min °C, max °C, min age months
CLOTHES=[
('body_short','Боди с коротким рукавом','TOP','body_short',0,20,60,0),('body_long','Боди с длинным рукавом','TOP','body_long',1,-60,22,0),('body_wrap','Боди на запах','TOP','body_wrap',1,-60,24,0),('body_sleeveless','Боди без рукавов','TOP','body_sleeveless',0,24,60,0),
('undershirt','Распашонка','TOP','wrap_top',1,-60,25,0),('tshirt','Футболка','TOP','tshirt',0,20,60,3),('longsleeve','Лонгслив','TOP','longsleeve',1,-60,22,3),('tank','Майка','TOP','tank',0,25,60,6),('shirt','Рубашка','TOP','shirt',1,15,26,6),('thermal_top','Термолонгслив','TOP','thermal_top',2,-60,8,6),
('sliders','Ползунки с закрытой стопой','BOTTOM','sliders',1,-60,23,0),('baby_pants','Мягкие штанишки','BOTTOM','pants',1,-60,25,0),('leggings','Легинсы','BOTTOM','leggings',1,-60,24,3),('trousers','Брюки','BOTTOM','trousers',1,5,25,6),('joggers','Джоггеры','BOTTOM','joggers',1,-5,22,6),('shorts','Шорты','BOTTOM','shorts',0,22,60,6),('bloomers','Трусики-блумеры','BOTTOM','bloomers',0,24,60,0),('thermal_bottom','Термолегинсы','BOTTOM','thermal_bottom',2,-60,8,6),('tights','Колготки','BOTTOM','tights',1,-60,18,3),('skirt','Юбка','BOTTOM','skirt',0,18,30,6),
('sleepsuit','Хлопковый слип','ONE','sleepsuit',1,-60,23,0),('romper','Песочник','ONE','romper',0,22,60,0),('cotton_overall','Хлопковый комбинезон','ONE','cotton_overall',1,-60,23,0),('dress','Платье','ONE','dress',0,20,35,6),('dungarees','Полукомбинезон','ONE','dungarees',1,10,25,6),('pyjamas','Пижама','ONE','pyjamas',1,10,25,3),
('cardigan','Кардиган','MID','cardigan',1,12,23,0),('sweatshirt','Свитшот','MID','sweatshirt',1,10,22,3),('hoodie','Толстовка с капюшоном','MID','hoodie',1,8,20,3),('fleece_jacket','Флисовая кофта','MID','fleece_jacket',2,-60,15,0),('sweater','Вязаный джемпер','MID','sweater',2,-60,15,3),('vest','Жилет','MID','vest',1,5,20,3),('fleece_suit','Флисовый комбинезон','MID','fleece_suit',2,-60,12,0),('wool_suit','Шерстяная поддёва','MID','wool_suit',2,-60,10,0),
('windbreaker','Ветровка','OUTER','windbreaker',0,12,24,3),('raincoat','Дождевик','OUTER','raincoat',0,10,26,6),('rain_suit','Непромокаемый комбинезон','OUTER','rain_suit',1,5,20,0),('demi_suit','Демисезонный комбинезон','OUTER','demi_suit',2,-2,14,0),('demi_jacket','Демисезонная куртка','OUTER','demi_jacket',2,0,15,6),('winter_suit','Зимний комбинезон','OUTER','winter_suit',3,-60,4,0),('winter_jacket','Зимняя куртка','OUTER','winter_jacket',3,-60,4,6),('snow_pants','Утеплённый полукомбинезон','OUTER_BOTTOM','snow_pants',3,-60,5,6),
('demi_pants','Демисезонные непромокаемые брюки','OUTER_BOTTOM','trousers',2,0,15,6),('rain_pants','Дождевые брюки','OUTER_BOTTOM','joggers',0,10,25,9),
('bonnet','Хлопковый чепчик','HEAD','bonnet',1,10,20,0),('beanie','Тонкая шапка','HEAD','beanie',1,5,18,0),('warm_hat','Тёплая шапка','HEAD','warm_hat',3,-60,8,0),('balaclava','Шапка-шлем','HEAD','balaclava',3,-60,5,3),('sunhat','Панама','HEAD','sunhat',0,18,60,0),('cap','Кепка','HEAD','cap',0,18,60,9),
('socks','Тонкие носки','SOCKS','socks',1,0,28,0),('warm_socks','Тёплые носки','SOCKS','warm_socks',3,-60,12,0),('booties','Мягкие пинетки','SHOES','booties',1,10,25,0),('warm_booties','Утеплённые пинетки','SHOES','warm_booties',3,-60,10,0),('sandals','Сандалии','SHOES','sandals',0,22,60,9),('sneakers','Кроссовки','SHOES','sneakers',1,10,26,9),('demi_boots','Демисезонные ботинки','SHOES','demi_boots',2,0,15,9),('winter_boots','Зимние ботинки','SHOES','winter_boots',3,-60,5,9),('rain_boots','Резиновые сапоги','SHOES','rain_boots',1,8,22,12),
('mittens','Тёплые варежки','HANDS','mittens',3,-60,5,0),('gloves','Лёгкие перчатки','HANDS','gloves',1,0,12,18),('scratch_mitts','Хлопковые рукавички','HANDS','scratch_mitts',0,10,25,0),
('footmuff','Конверт для коляски','STROLLER','footmuff',3,-60,8,0),('blanket','Лёгкий плед','STROLLER','blanket',1,8,20,0),('stroller_rain','Дождевик для коляски','STROLLER','stroller_rain',0,-5,25,0),('sunshade','Козырёк от солнца','STROLLER','sunshade',0,15,60,0)]
BAGS=[('diaper','Подгузник'),('wipes','Влажные салфетки'),('bags','Пакеты для использованных вещей'),('swaddle','Пелёнка'),('bottle','Бутылочка'),('formula','Смесь'),('water','Вода'),('snack','Перекус'),('cup','Поильник'),('toy','Игрушка'),('documents','Документы'),('medical_notes','Медицинские записи'),('bib','Нагрудник'),('towel','Полотенце'),('swim','Купальные принадлежности'),('nursing_pads','Вкладыши для кормления'),('pacifier','Пустышка'),('cream','Крем'),('thermos','Термос'),('changing_mat','Коврик для переодевания'),('stroller','Коляска'),('carrier','Переноска'),('carseat','Автокресло'),('medicine','Назначенные средства'),('phone','Телефон'),('keys','Ключи'),('container','Контейнер'),('bag','Своя вещь')]
# SVG-like paths directly encoded as Android VectorDrawable; all are original geometric drawings.
COLORS=['#91B9DE','#D6A4B8','#A2BFA8','#E4BA7C','#B9AED8','#80BEBB']
class Art:
 def __init__(self,c): self.parts=[];self.c=c;self.dark='#44516A';self.light='#F8F5EE';self.detail='#FFFFFF'
 def p(self,d,fill=None,stroke='#44516A',width=1.5):self.parts.append(f'<path android:fillColor="{fill or self.c}" android:strokeColor="{stroke}" android:strokeWidth="{width}" android:strokeLineJoin="round" android:strokeLineCap="round" android:pathData="{d}"/>')
 def line(self,d):self.p(d,'@android:color/transparent',width=1.1)
 def circle(self,x,y,r,fill='#FFFFFF'):self.p(f'M{x-r},{y}a{r},{r} 0,1 0,{2*r},0a{r},{r} 0,1 0,{-2*r},0',fill,width=.8)
 def rect(self,x,y,w,h,fill=None,rad=3):self.p(f'M{x+rad},{y}h{w-2*rad}q{rad},0 {rad},{rad}v{h-2*rad}q0,{rad} {-rad},{rad}h{-w+2*rad}q{-rad},0 {-rad},{-rad}v{-h+2*rad}q0,{-rad} {rad},{-rad}z',fill)
 def finish(self):return '<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="96dp" android:height="96dp" android:viewportWidth="96" android:viewportHeight="96">'+''.join(self.parts)+'</vector>\n'
def clothes(kind,i):
 a=Art(COLORS[i%len(COLORS)])
 bodies={'body_short','body_long','body_wrap','body_sleeveless'}
 pants={'pants','sliders','leggings','trousers','joggers','shorts','bloomers','thermal_bottom','tights','snow_pants'}
 one={'sleepsuit','cotton_overall','romper','dungarees','fleece_suit','wool_suit','demi_suit','winter_suit','rain_suit'}
 tops={'tshirt','longsleeve','tank','wrap_top','shirt','thermal_top','cardigan','sweatshirt','hoodie','fleece_jacket','sweater','vest','windbreaker','raincoat','demi_jacket','winter_jacket'}
 if kind in bodies:
  long=kind in {'body_long','body_wrap'};sleeveless=kind=='body_sleeveless'
  arm='L18,48 9,43 22,17' if long else ('L25,32 23,18' if sleeveless else 'L17,37 9,31 22,17')
  a.p('M37,17 Q48,25 59,17 L74,17 '+('L87,43 78,48 68,29' if long else ('L71,32 73,18' if sleeveless else 'L87,31 79,37 68,29'))+' L67,62 Q57,65 57,78 L39,78 Q39,65 29,62 L28,29 '+arm+'Z')
  a.line('M37,17 Q48,33 59,17 M39,74H57')
  for x in [43,48,53]:a.circle(x,77,1.2)
  if kind=='body_wrap':a.line('M36,23L61,49V62');a.circle(61,50,2)
  else:a.p('M43,43 Q48,35 53,43 Q54,47 48,51 Q42,47 43,43',a.light,stroke=a.light)
 elif kind in tops:
  sleeveless=kind in {'vest','tank'};short=kind=='tshirt';longcoat=kind=='raincoat';puffy=kind in {'demi_jacket','winter_jacket'}
  y=82 if longcoat else 72
  a.p(f'M36,19 Q48,26 60,19 L73,23 '+('L70,45 65,41' if sleeveless else ('L87,36 78,45 67,37' if short else 'L88,56 78,62 66,38'))+f' L67,{y}H29L30,38 '+('L26,45 23,23' if sleeveless else ('L18,45 9,36 23,23' if short else 'L18,62 8,56 23,23'))+'Z')
  a.line('M36,19Q48,37 60,19');a.line(f'M30,{y-5}H66')
  if kind in {'hoodie','windbreaker','raincoat','demi_jacket','winter_jacket'}:a.p('M34,21Q30,7 48,8Q66,7 62,21L55,27H41Z',a.c);a.line('M40,20Q48,12 56,20')
  if kind in {'shirt','cardigan','fleece_jacket','windbreaker','raincoat','demi_jacket','winter_jacket'}:
   a.line(f'M48,28V{y-3}');a.rect(35,48,8,9,a.light,1);a.rect(53,48,8,9,a.light,1)
   for v in range(37,y-4,9):a.circle(48,v,1.3)
  if kind=='wrap_top':a.line('M36,25L60,49V71');a.line('M56,51L69,55M56,51L68,47')
  if kind=='sweater':
   for yy in [41,50,59]:a.line(f'M33,{yy}l5,3 5,-3 5,3 5,-3 5,3 5,-3')
  if puffy:
   for yy in [37,47,57,67]:a.line(f'M31,{yy}H65')
  if kind=='thermal_top':a.line('M33,31L40,50V68 M63,31L56,50V68')
 elif kind in pants:
  short=kind in {'shorts','bloomers'};skinny=kind in {'leggings','thermal_bottom','tights'};foot=kind in {'sliders','tights'};end=56 if short else 81
  a.p(f'M29,19H67L{69 if short else 65},{end}H{53 if skinny else 51}L48,{43 if short else 45}L{43 if skinny else 45},{end}H{27 if short else 31}Z')
  a.line('M29,25H67 M48,26V43');a.line('M32,31Q38,35 40,28 M56,28Q59,35 65,31')
  if kind in {'trousers','joggers','snow_pants'}:a.line('M31,32L39,34V43 M65,32L57,34V43')
  if foot:a.p('M31,75H44V85H25Q22,79 31,75Z',a.c);a.p('M52,75H65L72,82V85H52Z',a.c)
  if kind=='joggers':a.rect(31,75,14,7,a.light,1);a.rect(51,75,14,7,a.light,1);a.line('M44,26L40,34 M50,26L54,34')
  if kind=='snow_pants':a.p('M31,20L32,7H40L42,20Z',a.c);a.p('M54,20L56,7H64L65,20Z',a.c)
  if kind=='bloomers':a.line('M28,48Q35,56 44,48 M52,48Q61,56 68,48')
 elif kind in one or kind=='pyjamas':
  if kind=='pyjamas':
   a.p('M30,15L40,11Q46,19 52,11L62,15L75,34L66,39L59,28V49H33V28L25,39L17,34Z');a.p('M34,53H61L64,85H51L47,67L43,85H30Z');a.line('M34,58H61');a.circle(46,31,4,a.light)
  else:
   short=kind=='romper';straps=kind=='dungarees';winter=kind in {'winter_suit','demi_suit','rain_suit'}
   a.p('M35,16Q48,24 61,16L72,22'+('L69,40 63,34' if straps else ('L85,33 78,43 65,34' if short else 'L88,44 79,51 64,32'))+('L67,61H54L48,51L42,61H29' if short else 'L68,82H52L48,58L44,82H28')+'L32,32'+('L26,40 24,22' if straps else ('L18,43 11,33 24,22' if short else 'L17,51 8,44 24,22'))+'Z')
   if winter:a.p('M34,17Q31,3 48,5Q65,3 62,17L56,24H40Z',a.c)
   if straps:a.line('M35,17V37H61V17');a.circle(36,34,2);a.circle(60,34,2);a.rect(41,42,14,10,a.light,2)
   else:
    a.line('M48,24V56L39,78');a.line('M35,17Q48,31 61,17')
    for yy in [34,43,52]:a.circle(48,yy,1.5)
   if kind=='sleepsuit':a.p('M28,76H44V85H24Q21,80 28,76Z',a.c);a.p('M52,76H68L74,82V85H52Z',a.c)
   if kind=='winter_suit':
    for yy in [33,44,55]:a.line(f'M33,{yy}H63')
   if kind=='wool_suit':a.line('M34,39l5,3 5,-3 5,3 5,-3 5,3')
 elif kind in {'dress','skirt'}:
  if kind=='dress':a.p('M36,17Q48,25 60,17L72,24L66,37L60,33L59,43L77,78Q48,88 19,78L37,43L36,33L30,37L24,24Z');a.line('M38,43H58 M36,17Q48,32 60,17')
  else:a.p('M33,25H63L79,73Q48,86 17,73Z');a.line('M33,31H63')
  a.line('M38,48L31,74 M48,49V78 M58,48L65,74')
 elif kind in {'bonnet','beanie','warm_hat','balaclava','sunhat','cap'}:
  if kind=='sunhat':a.p('M31,37Q31,19 48,19Q65,19 65,37L77,63Q48,75 19,63Z');a.line('M30,42Q48,51 66,42 M24,59Q48,69 72,59')
  elif kind=='cap':a.p('M25,50Q25,22 49,22Q72,22 71,50Z');a.p('M48,50H73L85,59Q64,68 48,56Z',a.light);a.line('M48,23V48')
  elif kind=='balaclava':a.p('M25,42Q24,16 48,14Q72,16 71,42L76,78H20Z');a.p('M34,34Q48,24 62,34V51Q48,61 34,51Z',a.light);a.line('M38,70H58')
  elif kind=='bonnet':a.p('M30,59Q16,29 40,20Q65,15 72,40L57,63Z');a.p('M57,26Q39,26 38,45Q39,58 55,64L66,57Q49,51 57,26',a.light);a.line('M38,61L31,79 M56,64L63,79')
  else:
   a.p('M24,62V44Q24,19 48,19Q72,19 72,44V62Z');a.rect(22,55,52,13,a.light,4)
   if kind=='warm_hat':a.circle(48,15,8,a.light);a.p('M25,64H35V79H25Z');a.p('M61,64H71V79H61Z')
   a.line('M35,30V51 M48,26V51 M61,30V51')
 elif kind in {'socks','warm_socks'}:
  for x,y in [(20,25),(49,15)]:
   a.p(f'M{x},{y}h19v32l10,6q9,13 -7,14h-15q-12,-1 -11,-14z');a.rect(x,y,19,8,a.light,1);a.line(f'M{x-1},{y+49}h16')
   if kind=='warm_socks':a.line(f'M{x+2},{y+15}l4,3 4,-3 4,3 M{x+2},{y+23}l4,3 4,-3 4,3')
 elif kind in {'booties','warm_booties','sandals','sneakers','demi_boots','winter_boots','rain_boots'}:
  for x,y in [(12,40),(43,27)]:
   boot=kind in {'demi_boots','winter_boots','rain_boots','warm_booties'}
   a.p(f'M{x+5},{y}h21v{22 if boot else 12}l13,7q7,5 1,12h-37q-5,-4 -2,-10z')
   a.line(f'M{x},{y+(38 if boot else 28)}h40')
   if kind=='sandals':a.p(f'M{x+8},{y+7}h11v13h-11z',a.light);a.line(f'M{x+3},{y+15}h21')
   elif kind=='rain_boots':a.rect(x+4,y,23,6,a.light,1)
   else:
    a.rect(x+3,y,24,6,a.light,2)
    for yy in [12,18]:a.line(f'M{x+12},{y+yy}h10')
 elif kind in {'mittens','gloves','scratch_mitts'}:
  for x,y in [(15,31),(49,20)]:
   if kind=='gloves':a.p(f'M{x+4},{y+36}v-16l-5,-8q1,-5 5,-2l4,5v-23q3,-6 6,0v13v-19q4,-6 6,0v19v-15q5,-6 6,0v17v-10q4,-5 6,0v24z')
   elif kind=='scratch_mitts':a.p(f'M{x+3},{y+34}v-23q0,-15 14,-15q15,0 15,15v23z')
   else:a.p(f'M{x+3},{y+34}v-16q-9,-2 -5,-10q3,-4 8,2v-6q0,-16 14,-15q12,0 12,15v30z')
   a.rect(x+2,y+32,29,9,a.light,2)
 elif kind=='footmuff':a.p('M26,35Q22,9 48,9Q74,9 70,35V71Q69,88 48,88Q27,88 26,71Z');a.p('M31,33Q32,16 48,16Q64,16 65,33Z',a.light);a.line('M48,40V80 M29,51H67 M29,64H67')
 elif kind=='blanket':a.p('M21,17H75V78H21Z');a.p('M30,23H69V69H30Z',a.light);a.line('M37,30L61,61 M61,30L37,61');a.line('M21,80V85 M30,80V85 M39,80V85 M48,80V85 M57,80V85 M66,80V85 M75,80V85')
 elif kind=='stroller_rain':a.p('M17,64Q13,22 47,16Q80,20 79,64L72,74H24Z','#CDE6EE');a.p('M28,54Q26,30 48,27Q68,30 68,54Z','#F5FCFF');a.line('M21,64H75 M39,39L44,31 M51,44L59,32')
 elif kind=='sunshade':a.p('M13,51Q48,4 83,51L72,66Q48,35 24,66Z');a.line('M27,40L35,55 M48,27V49 M69,40L61,55')
 else:a.rect(24,20,48,57)
 return a

def bagart(kind,i):
 a=Art(COLORS[(i+2)%6])
 if kind=='diaper':a.p('M17,27H79L72,65Q48,82 24,65Z', '#F9FBFC');a.p('M20,29L29,55Q48,66 67,55L76,29Z',a.c);a.p('M18,28H32V40H15Z',a.light);a.p('M64,28H78L81,40H64Z',a.light);a.line('M37,46Q48,53 59,46')
 elif kind=='wipes':a.rect(14,31,68,42);a.rect(30,35,36,22,a.light,7);a.p('M40,42Q36,23 46,15L60,23Q50,30 55,43Z','#FFFFFF');a.line('M20,63H31 M65,63H76')
 elif kind in {'bottle','water','thermos','cup'}:
  if kind=='bottle':a.p('M40,25V19Q40,9 48,9Q56,9 56,19V25Z','#EAC3B1');a.rect(33,25,30,9,a.light,2);a.rect(30,34,36,49,'#D5E7F1',7)
  elif kind=='water':a.rect(37,10,22,11,a.light,2);a.p('M37,22H59V29L66,38V78Q48,85 30,78V38L37,29Z','#ACD3E5');a.rect(31,47,34,20,a.light,1)
  elif kind=='thermos':a.rect(33,15,30,15,a.dark,3);a.rect(29,28,38,57,a.c,7);a.rect(34,40,28,24,a.light,3)
  else:a.rect(30,28,36,47,a.c,8);a.p('M37,29V20H59V29Z',a.light);a.p('M35,20L40,12H56L61,20Z',a.c);a.line('M30,40Q15,36 18,57Q20,65 30,60 M66,40Q81,36 78,57Q76,65 66,60')
  if kind=='bottle':
   for yy in [45,53,61,69]:a.line(f'M36,{yy}h8')
 elif kind=='formula':a.rect(25,20,46,62,a.c,6);a.rect(24,17,48,10,a.light,2);a.rect(29,36,38,28,a.light,2);a.circle(48,50,9,a.c);a.line('M40,75H56')
 elif kind=='snack':a.p('M16,46Q48,29 80,46L71,76H25Z');a.p('M25,45Q20,22 35,22Q43,14 49,29Q58,16 70,26L70,44Z','#E8B66F');a.line('M32,54H64 M36,64H60')
 elif kind in {'documents','medical_notes'}:a.rect(21,17,54,66,a.c,4);a.rect(28,24,40,52,a.light,1);a.line('M35,56H61 M35,64H56');a.circle(48,40,9,a.c);a.line('M43,40H53 M48,35V45') if kind=='medical_notes' else a.line('M42,40Q48,47 54,40')
 elif kind=='bib':a.p('M29,25Q29,10 41,15L42,28H54L55,15Q67,10 67,25L75,65Q74,84 48,84Q22,84 21,65Z');a.p('M29,60Q48,72 67,60L64,75H32Z',a.light);a.circle(34,22,2)
 elif kind in {'swaddle','towel','changing_mat'}:a.rect(16,24,64,52,a.c,4);a.p('M19,27H74V70H19Z',a.light);a.line('M23,35H68 M23,63H68');a.p('M57,25H77V45Z',a.c);a.circle(47,49,7,a.c)
 elif kind=='bags':a.p('M30,29V16H42V29H54V16H66V29L73,80H23Z','#B5D2C1');a.line('M35,40Q48,55 61,40 M30,68H66')
 elif kind=='toy':a.circle(31,27,10);a.circle(65,27,10);a.circle(48,41,23);a.circle(48,68,18);a.circle(26,65,9);a.circle(70,65,9);a.circle(35,82,8);a.circle(61,82,8);a.circle(41,37,2,a.dark);a.circle(55,37,2,a.dark);a.circle(48,47,7,a.light);a.circle(48,44,2,a.dark)
 elif kind=='pacifier':a.p('M39,34Q36,11 48,11Q60,11 57,34Z',a.light);a.p('M20,39Q48,23 76,39Q78,58 59,60H37Q18,58 20,39Z');a.circle(48,49,8,a.light);a.circle(48,66,13);a.circle(48,66,8,a.light)
 elif kind in {'cream','medicine'}:a.rect(30,29,36,51,a.c,5);a.rect(33,17,30,14,a.light,2);a.rect(34,44,28,23,a.light,2);a.line('M40,55H56 M48,47V63')
 elif kind=='nursing_pads':a.circle(37,50,24,a.light);a.circle(61,53,24,'#D5E7F1');a.circle(37,50,16,a.light);a.circle(61,53,16,'#D5E7F1')
 elif kind=='swim':a.p('M23,28H73L69,65H52L48,44L44,65H27Z');a.line('M23,34H73');a.circle(62,53,4,a.light);a.line('M44,29L40,39 M50,29L54,39')
 elif kind in {'carseat','stroller','carrier'}:
  if kind=='stroller':a.p('M33,23Q59,10 74,44H31Z');a.p('M25,46H77L65,65H39Z');a.line('M24,32L40,73H69 M16,28H23 M66,64L75,77');a.circle(39,79,8,a.dark);a.circle(72,79,8,a.dark)
  elif kind=='carseat':a.p('M26,52Q11,14 36,10Q57,6 63,25L67,56L81,65L73,82H28L17,66Z');a.p('M34,27Q46,19 54,33L57,59H32Z',a.light);a.line('M35,37L47,58L57,39 M47,58V68')
  else:a.p('M30,28L18,14L12,23L23,43L29,80H67L73,43L84,23L78,14L66,28Z');a.p('M31,33H65L61,69H35Z',a.light);a.line('M25,75H71 M48,34V66')
 elif kind=='phone':a.rect(29,11,38,75,a.dark,7);a.rect(34,19,28,53,'#BCD9EC',3);a.circle(48,79,2,a.light)
 elif kind=='keys':a.circle(41,31,18,a.c);a.circle(41,31,9,a.light);a.p('M39,48H48V65H58V73H48V83H39Z',a.c)
 elif kind=='container':a.rect(16,35,64,42,a.c,7);a.rect(13,29,70,12,a.light,4);a.line('M25,48V64 M71,48V64')
 else:a.p('M25,31H71L78,80H18Z');a.line('M35,32V22Q48,10 61,22V32');a.rect(35,45,26,20,a.light,4)
 return a
out=ROOT/'app/src/main/res/drawable';out.mkdir(parents=True,exist_ok=True)
for i,(id,name,group,kind,w,lo,hi,age) in enumerate(CLOTHES):
 a=clothes(kind,i)
 if id=='demi_pants':a.rect(31,47,12,13,a.light,2);a.rect(53,47,12,13,a.light,2)
 if id=='rain_pants':a.p('M36,40q-10,14 0,15q10,-1 0,-15z',a.light)
 (out/f'art_{id}.xml').write_text(a.finish())
for i,(id,name) in enumerate(BAGS):(out/f'art_bag_{id}.xml').write_text(bagart(id,i).finish())
def q(s):return json.dumps(s,ensure_ascii=False)
rows=[f'  GarmentType({q(id)},{q(name)},GarmentGroup.{g},{w},{lo}.0,{hi}.0,{age}),' for id,name,g,k,w,lo,hi,age in CLOTHES]
source='''package ru.withbaby.domain

// Original local catalog. Temperatures are editable everyday guides, not medical limits.
enum class GarmentGroup(val title:String) { TOP("Верх основы"), BOTTOM("Низ основы"), ONE("Цельная одежда"), MID("Средний слой"), OUTER("Верхняя одежда"), OUTER_BOTTOM("Низ верхней одежды"), HEAD("Головные уборы"), SOCKS("Носки"), SHOES("Обувь"), HANDS("Рукавички"), STROLLER("Для коляски") }
data class GarmentType(val id:String,val name:String,val group:GarmentGroup,val warmth:Int,val minC:Double,val maxC:Double,val minMonths:Int=0) {
 fun matches(t:Double,months:Long)=t in minC..maxC && months>=minMonths
 val warmthLabel:String get()=listOf("Лёгкая","Обычная","Тёплая","Утеплённая")[warmth.coerceIn(0,3)]
}
object GarmentCatalog {
 val all=listOf(
'''+ '\n'.join(rows)+'''
 )
 fun find(id:String?)=all.find {it.id==id}
}
data class IllustrationType(val id:String,val name:String)
object BagIllustrations {
 val all=listOf(
'''+',\n'.join(f'  IllustrationType("bag_{id}",{q(name)})' for id,name in BAGS)+'''
 )
 fun infer(name:String):String {
  val n=name.lowercase()
  return when {
   "подгуз" in n->"bag_diaper";"салфет" in n->"bag_wipes";"бутыл" in n->"bag_bottle";"смеси" in n || "смесь" in n->"bag_formula"
   "дождевик" in n->"stroller_rain";"панам" in n->"sunhat";"козыр" in n->"sunshade";"пижам" in n->"pyjamas";"одежд" in n->"sleepsuit"
   "вкладыш" in n->"bag_nursing_pads";"перекус" in n->"bag_snack";"поильник" in n->"bag_cup";"вод" in n->"bag_water";"игруш" in n->"bag_toy"
   "полотен" in n->"bag_towel";"пелён" in n || "пелен" in n->"bag_swaddle";"пакет" in n->"bag_bags";"документ" in n->"bag_documents"
   "медицин" in n->"bag_medical_notes";"нагруд" in n->"bag_bib";"купаль" in n->"bag_swim";"коляск" in n->"bag_stroller";"переноск" in n->"bag_carrier"
   "автокрес" in n->"bag_carseat";"слой" in n->"cardigan";"сна" in n->"pyjamas";"пустыш" in n->"bag_pacifier";"крем" in n->"bag_cream"
   "назначен" in n->"bag_medicine";"термос" in n->"bag_thermos";"телефон" in n->"bag_phone";"ключ" in n->"bag_keys";else->"bag_bag"
  }
 }
}
'''
(ROOT/'core/domain/src/main/kotlin/ru/withbaby/domain/GarmentCatalog.kt').write_text(source)
doc='# Каталог гардероба 0–3 лет\n\nПеред реализацией разделены основа (верх/низ/цельная одежда), средний и наружный слои, головные уборы, носки, обувь, рукавички и принадлежности коляски. Обувь для ходьбы не предлагается младенцам по умолчанию; размер, материал, слой и конкретное изделие важнее названия сезона. Каталог охватывает основные типы, но не объявляется исчерпывающим: родитель может добавить свою вещь.\n\nИзображения — оригинальные цветные VectorDrawable, с разными силуэтами рукавов, штанин, застёжек, обуви и принадлежностей. Они генерируются tools/generate_catalog.py без скачивания чужих изображений, без растровых дублей по плотностям. Перекраска не используется вместо различий кроя.\n\n| Вещь | Группа | Утепление | Возраст от, мес. |\n|---|---|---|---|\n'
for id,name,g,k,w,lo,hi,age in CLOTHES:doc+=f'| {name} | {g} | {w} | {age} |\n'
doc+='\nДля сумки: '+', '.join(name for id,name in BAGS)+'. Общие предметы одежды используют изображения из гардероба. Диапазоны температур — эвристики сортировки, не медицинские нормы. Варианты вне диапазона доступны с пояснением; выбранное родителем не заменяется молча.\n'
(ROOT/'docs/WARDROBE_CATALOG.md').write_text(doc)
print(f'{len(CLOTHES)} garments, {len(BAGS)} bag illustrations; vector bytes:',sum(p.stat().st_size for p in out.glob('art_*.xml')))

(ROOT/'app/src/main/java/ru/withbaby/app/ArtResources.kt').write_text('package ru.withbaby.app\n\n// Generated explicit vector references.\nobject ArtResources {val ids=mapOf(\n'+',\n'.join(f' "{f.stem[4:]}" to R.drawable.{f.stem}' for f in sorted(out.glob('art_*.xml')))+'\n)}\n')
