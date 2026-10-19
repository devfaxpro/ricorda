"""Build Ricorda's Italian offline almanac. Python 3, standard library only.
The source is preserved separately; this is not a full liturgical precedence engine.
See SANTI-FONTI-E-METODO.md for sources, corrections and scope.
"""
from pathlib import Path
from datetime import date,timedelta
import json

ROOT=Path(__file__).resolve().parents[1]
DATA=ROOT/'data'; ASSETS=ROOT/'app/src/main/assets'
HEADER='data|tipo|voce_originale|contiene_ricorrenza_mobile|nota_verifica'
# Names needed when a 2026 movable celebration no longer falls on that civil date.
# Factual date/name pairs verified against Vatican News's corresponding MM/DD pages.
RESTORED={
 '01-11':"Sant'Igino Papa",'02-18':'Beato Angelico','02-22':'S. Massimiano di Ravenna',
 '03-01':"Sant'Albino",'03-08':'S. Giovanni di Dio','03-15':'S. Zaccaria Papa',
 '03-22':'Santa Lea','03-29':"Sant'Eustasio",'04-05':'S. Vincenzo Ferrer',
 '04-06':'Santa Galla','05-17':'S. Pasquale Baylon','05-24':'S. Vincenzo di Lérins',
 '06-07':"Sant'Antonio Maria Gianelli",'06-12':"Sant'Onofrio",'06-13':"Sant'Antonio di Padova",
 '10-25':'Ss. Crisanto e Daria','11-29':'S. Saturnino martire','12-06':'S. Nicola di Bari',
 '12-13':'Santa Lucia','12-20':'S. Filogonio','12-26':'S. Stefano',
 '02-29':"Sant'Ilaro Papa",
}
CORRECTIONS={'08-04':'S. Giovanni Maria Vianney','08-05':"Sant'Osvaldo",'09-13':'S. Giovanni Crisostomo','09-23':'S. Pio da Pietrelcina'}
# Independent expected Easter dates from the CEI Roman Missal, table p. LXXVI.
EASTER={2026:(4,5),2027:(3,28),2028:(4,16),2029:(4,1),2030:(4,21),2031:(4,13),2032:(3,28),2033:(4,17),2034:(4,9),2035:(3,25),2036:(4,13)}

def easter(year):
 a=year%19;b=year//100;c=year%100;d=b//4;e=b%4;f=(b+8)//25;g=(b-f+1)//3
 h=(19*a+b-d-g+15)%30;i=c//4;k=c%4;l=(32+2*e+2*i-h-k)%7;m=(a+11*h+22*l)//451
 return date(year,(h+l-7*m+114)//31,(h+l-7*m+114)%31+1)

def sunday_after(day):return day+timedelta(days=7-day.isoweekday()) if day.isoweekday()!=7 else day+timedelta(days=7)

def celebrations(year):
 pascha=easter(year);assert pascha==date(year,*EASTER[year])
 result={}
 def put(day,name):assert day not in result;result[day]=name
 for offset,name in [(-46,'Mercoledì delle Ceneri'),(-42,'I Domenica di Quaresima'),(-35,'II Domenica di Quaresima'),(-28,'III Domenica di Quaresima'),(-21,'IV Domenica di Quaresima'),(-14,'V Domenica di Quaresima'),(-7,'Domenica delle Palme'),(0,'Pasqua'),(1,"Lunedì dell’Angelo"),(42,'Ascensione del Signore'),(49,'Pentecoste'),(63,'Corpus Domini'),(68,'Sacro Cuore di Gesù'),(69,'Cuore Immacolato di Maria')]:
  put(pascha+timedelta(days=offset),name)
 put(sunday_after(date(year,1,6)),'Battesimo del Signore')
 advent=date(year,11,27)
 while advent.isoweekday()!=7:advent+=timedelta(days=1)
 put(advent-timedelta(days=7),"N.S. Gesù Re dell’Universo")
 for n,roman in enumerate(['I','II','III','IV']):put(advent+timedelta(days=7*n),roman+' Domenica di Avvento')
 christmas=date(year,12,25)
 family=date(year,12,30) if christmas.isoweekday()==7 else sunday_after(christmas)
 put(family,'Sacra Famiglia')
 return result

def main():
 rows=(DATA/'saints_2026_original.txt').read_text(encoding='utf-8').splitlines()
 assert rows[0]==HEADER
 fixed={}
 for row in rows[1:]:
  if not row.strip():continue
  day,kind,name,movable,note=row.split('|');key=day[5:]
  if movable=='SI':
   assert key in RESTORED,key
   kind,name,note='SANTI',RESTORED[key],'VN'
  if key in CORRECTIONS:kind,name,note='SANTI',CORRECTIONS[key],'VN_CORREZIONE'
  fixed[key]=(kind,name,note)
 fixed['02-29']=('SANTI',RESTORED['02-29'],'VN_BISESTILE')
 output=[HEADER];years=[]
 for year in range(2026,2037):
  feasts=celebrations(year);day=date(year,1,1);count=0
  while day.year==year:
   kind,name,note=fixed[day.strftime('%m-%d')];mobile=day in feasts
   if mobile:kind='SANTI_E_RICORRENZA_RELIGIOSA';name=feasts[day]+' · '+name;note='CALCOLO_ROMANO_ITALIA;'+note
   output.append('|'.join([day.isoformat(),kind,name,'SI' if mobile else 'NO',note]))
   count+=1;day+=timedelta(days=1)
  years.append({'year':year,'days':count,'easter':easter(year).isoformat(),'movable_celebrations':len(feasts)})
 assert len(output)-1==4018
 (ASSETS/'saints_2026_2036.txt').write_text('\n'.join(output)+'\n',encoding='utf-8')
 (DATA/'santi-verifica.json').write_text(json.dumps({'from':'2026-01-01','through':'2036-12-31','days':4018,'years':years,'note':'Almanacco dei santi e principali ricorrenze; non calendario liturgico ufficiale.'},ensure_ascii=False,indent=2),encoding='utf-8')
 print('Generated 4018 consecutive dates, 2026–2036, including 3 leap days.')

if __name__=='__main__':main()
