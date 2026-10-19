# Calendario italiano dei santi e delle ricorrenze, 2026–2036

Copertura: **4.018 giorni consecutivi**, dal 1 Gennaio 2026 al 31 Dicembre 2036, compresi il 29 Febbraio 2028, 2032 e 2036. Consultazione e avvisi funzionano con dati inclusi nell'app, senza rete.

È un almanacco per promemoria e onomastici. Non è un calendario liturgico ufficiale e non applica tutte le precedenze, i trasferimenti delle solennità fisse, le particolarità diocesane o il rito ambrosiano. I nomi dei santi restano associati al giorno civile; nei giorni con una ricorrenza mobile, entrambe le informazioni sono conservate e ricercabili. Gli aggiornamenti ecclesiastici successivi alla verifica del 19 Ottobre 2026 non sono prevedibili e richiederanno un aggiornamento dei dati.

## Fonte di partenza conservata

Il file `saints_2026_original.txt` conserva le 365 righe usate dalla 11.5. Il documento originale, con le note di verifica A1–A5, resta in `santi_2026_per_Ricorda.txt`.

Fonte: CreaGratis, [Calendario 2026 con Santi](https://www.creagratis.com/files/calendari/Calendario-2026-Santi.pdf), fornito dall'utente. Licenza dichiarata nel materiale: [CC BY-NC-SA 2.5 Italia](https://creativecommons.org/licenses/by-nc-sa/2.5/it/). Attribuzione e condizioni sono conservate in `app/src/main/assets/NOTICE-santi.txt`; l'estensione del materiale derivato mantiene la stessa licenza.

## Calcolo delle ricorrenze mobili

`generate_saints.py` calcola Pasqua con il computo gregoriano e verifica le undici date contro la tabella annuale del [Messale Romano CEI, pagina LXXVI](https://liturgico.chiesacattolica.it/wp-content/uploads/sites/8/2020/12/07/Messale-Romano-COMPLETO-senza-musica.pdf). La stessa tabella conferma Ceneri, Ascensione, Pentecoste e Corpus Domini. Ascensione e Corpus Domini sono collocati alla domenica secondo l'uso italiano.

Da Pasqua sono ricavate Ceneri (-46 giorni), domeniche di Quaresima, Palme (-7), Lunedì dell'Angelo (+1), Ascensione (+42), Pentecoste (+49), Corpus Domini (+63), Sacro Cuore (+68), Cuore Immacolato (+69).

Il Battesimo è la domenica dopo il 6 Gennaio. L'Avvento parte dalla domenica tra 27 Novembre e 3 Dicembre; Cristo Re è la domenica precedente. La Sacra Famiglia è la domenica dopo Natale, oppure il 30 Dicembre quando Natale cade di domenica.

Riferimenti di verifica:

- [Santa Sede: Cristo Re, ultima domenica dell'anno liturgico](https://www.vatican.va/content/francesco/it/angelus/2017/documents/papa-francesco_angelus_20171126.html).
- [Vatican News: collocazione della Sacra Famiglia](https://www.vaticannews.va/it/papa/news/2019-12/papa-francesco-angelus-santita-famiglia-nazareth-volonta-dio.html).
- Norme per l'Anno liturgico nel Messale Romano CEI, stesso documento sopra.

## Date fisse recuperate e correzioni

Quando nel file 2026 era presente soltanto una ricorrenza mobile, è stato recuperato un santo legato alla data civile. Sono usati soltanto nomi e date, senza biografie, fotografie o altri testi del sito. Verifica sulle pagine corrispondenti di Vatican News, consultate il 19 Ottobre 2026:

| Giorno | Voce recuperata | Riferimento |
|---|---|---|
| 11 Gennaio | Sant'Igino | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/01/11/sant-igino--papa.html) |
| 18 Febbraio | Beato Angelico | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/02/18.html) |
| 22 Febbraio | S. Massimiano di Ravenna | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/02/22.html) |
| 29 Febbraio | Sant'Ilaro Papa | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/02/29.html) |
| 1 Marzo | Sant'Albino | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/03/01.html) |
| 8 Marzo | S. Giovanni di Dio | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/03/08.html) |
| 15 Marzo | S. Zaccaria Papa | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/03/15.html) |
| 22 Marzo | Santa Lea | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/03/22.html) |
| 29 Marzo | Sant'Eustasio | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/03/29.html) |
| 5 Aprile | S. Vincenzo Ferrer | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/04/05.html) |
| 6 Aprile | Santa Galla | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/04/06.html) |
| 17 Maggio | S. Pasquale Baylon | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/05/17.html) |
| 24 Maggio | S. Vincenzo di Lérins | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/05/24.html) |
| 7 Giugno | Sant'Antonio Maria Gianelli | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/06/07.html) |
| 12 Giugno | Sant'Onofrio | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/06/12.html) |
| 13 Giugno | Sant'Antonio di Padova | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/06/13.html) |
| 25 Ottobre | Ss. Crisanto e Daria | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/10/25.html) |
| 29 Novembre | S. Saturnino | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/11/29.html) |
| 6 Dicembre | S. Nicola di Bari | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/12/06.html) |
| 13 Dicembre | Santa Lucia | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/12/13.html) |
| 20 Dicembre | S. Filogonio | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/12/20.html) |
| 26 Dicembre | S. Stefano | [Vatican News](https://www.vaticannews.va/it/santo-del-giorno/12/26.html) |

Corrette le anomalie già segnalate A1–A5: Giovanni Maria Vianney al [4 Agosto](https://www.vaticannews.va/it/santo-del-giorno/08/04.html); al [5 Agosto](https://www.vaticannews.va/it/santo-del-giorno/08/05.html) è indicato Sant'Osvaldo; Giovanni Crisostomo al [13 Settembre](https://www.vaticannews.va/it/santo-del-giorno/09/13.html); grafia Pietrelcina al [23 Settembre](https://www.vaticannews.va/it/santo-del-giorno/09/23.html). Per il 2026 Cristo Re è ricalcolato al 22 Novembre e la Sacra Famiglia al 27 Dicembre. San Tommaso Becket resta al 29 Dicembre, come nella 11.5.

Le altre voci fisse conservano la selezione dell'almanacco fornito: non si dichiara una revisione integrale di tutti gli onomastici né l'esaustività del Martirologio Romano.

## Riproduzione

Eseguire `python data/generate_saints.py` dalla cartella del progetto. Lo script ricrea `app/src/main/assets/saints_2026_2036.txt` e `data/santi-verifica.json`. I test Java controllano copertura, bisestili, date CEI, ricorrenze non congelate al 2026, ricerca per anno e confini degli avvisi giornalieri.
