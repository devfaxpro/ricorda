# Ricorda

**Un pensiero in meno.** Compleanni, scadenze e cose da fare, in un'app Android semplice e senza pubblicità.

**Italiano** · [English](README.en.md)

Ricorda ti aiuta a organizzare gli impegni con calendario, promemoria e sveglie. Funziona offline, senza account: i tuoi promemoria rimangono sul dispositivo.

| Versione | Android minimo | Pacchetto |
| --- | --- | --- |
| 11.7 · codice 18 | Android 8.0 (API 26) | `com.fabio.ricorda` |

## Cosa puoi fare

- **Organizzare la giornata:** calendario mensile disponibile dal primo avvio, giorni con impegni evidenziati e lista dei promemoria del giorno selezionato.
- **Creare e ritrovare i promemoria:** titolo, note, data e ora; ricerca per testo e per giorno, filtri, modifica e cestino con recupero.
- **Scegliere quando ripetere un avviso:** ogni giorno, settimana, mese o anno, oppure selezionando più date nel calendario annuale.
- **Personalizzare gli avvisi:** notifiche, suoni di sistema, vibrazione e sveglia con volume regolabile e ascolto durante la regolazione. Dalla sveglia puoi interrompere l'avviso o rimandarlo di 10 minuti.
- **Scegliere l'aspetto:** 21 colori, modalità chiara o scura e caratteri come Nunito, Lato, Lora e Caveat.
- **Usare i widget:** agenda e, in italiano, santo del giorno, con layout adattabili allo spazio disponibile.
- **Portare con te i dati:** esportazione e importazione dei promemoria in file JSON con un nome che identifica il backup.
- **Usare la tua lingua:** 40 lingue, scelta iniziale basata sul telefono e selettore con nomi nativi. Le traduzioni possono essere migliorate con il contributo di persone madrelingua.

In italiano sono disponibili anche ricerca dei santi, avviso quotidiano e almanacco dal **2026 al 2036**. È un almanacco di onomastici e ricorrenze, non un calendario liturgico ufficiale: [fonti e metodo](data/SANTI-FONTI-E-METODO.md).

### Novità della 11.7

La barra del volume parte dal bianco e raggiunge il colore pieno del tema aumentando il volume. La regolazione e la riproduzione del suono restano quelle della versione precedente.

## Aprire il progetto in Android Studio

1. Clona questo repository oppure scegli **Code → Download ZIP** su GitHub ed estrai l'archivio.
2. In Android Studio scegli **Open** e apri la cartella che contiene `settings.gradle`.
3. Installa **Android SDK 36** se richiesto e imposta **Gradle JDK 21** nelle impostazioni di Gradle. Il codice usa Java 17.
4. Attendi la sincronizzazione, seleziona un telefono o un emulatore con Android 8.0 o successivo e premi **Run ▶**.

Il progetto include il wrapper **Gradle 8.11.1** e usa **Android Gradle Plugin 8.9.2**. La prima sincronizzazione richiede Internet per scaricare gli strumenti; l'app installata funziona offline.

### Creare un APK e avviare i controlli

Su Windows, dal terminale nella cartella del progetto:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintRelease
```

Su macOS o Linux:

```sh
./gradlew assembleDebug testDebugUnitTest lintRelease
```

L'APK installabile si trova in `app/build/outputs/apk/debug/app-debug.apk`. Per il Play Store, usa **Build → Generate Signed Bundle / APK → Android App Bundle** e la tua chiave di firma. Chiavi e password non sono incluse nel repository.

## Privacy e notifiche

L'app non richiede il permesso Internet, non include pubblicità o analytics e non legge rubrica, posizione o calendario di sistema. Le impostazioni e i promemoria sono conservati nella memoria privata dell'app. I backup esportati **non sono cifrati**: scegli con attenzione dove salvarli e con chi condividerli.

Le notifiche, gli allarmi precisi e gli avvisi a schermo intero dipendono dalle autorizzazioni e dalle impostazioni Android. Modalità Non disturbare, risparmio energetico e configurazione del dispositivo possono influenzare gli avvisi.

Leggi l'[informativa privacy in italiano](app/src/main/assets/privacy-it.txt).

## Verifiche e contributi

La versione 11.7 ha superato la compilazione e **166 test JVM**. Il controllo `lintRelease` riporta 0 errori e 259 avvisi da valutare. Questi controlli non sostituiscono le prove su telefono, in particolare per notifiche a schermo spento, audio, widget e accessibilità. Dettagli in [docs/VALIDATION.md](docs/VALIDATION.md).

Per segnalare un problema, apri una [issue](https://github.com/devfaxpro/ricorda/issues) indicando versione Android, versione dell'app, passaggi per riprodurlo e comportamento atteso. Non allegare backup o promemoria personali. Sono benvenuti miglioramenti alle traduzioni, all'accessibilità e al codice.

## Licenza e attribuzioni

Il codice di Ricorda è pubblicato con la licenza **GNU GPL v3** già presente nel repository: [LICENSE](LICENSE). I componenti e i dati di terze parti mantengono le rispettive licenze: [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

In particolare, i dati derivati dell'almanacco dei santi sono indicati come **CC BY-NC-SA 2.5 Italia** e non sono inclusi nella GPL del codice. I caratteri sono distribuiti con **SIL Open Font License 1.1**.

Sviluppatore: **Fabio Avitabile** · Assistenza e privacy: [fabiocorby@gmail.com](mailto:fabiocorby@gmail.com).
