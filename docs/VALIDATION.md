# Validation / Verifiche — Ricorda 11.7

Source snapshot: version **11.7**, version code **18**, package **com.fabio.ricorda**. Prepared on 19 October 2026.

## Italiano

- `bundleRelease`, `assembleRelease`, `testDebugUnitTest` e `lintRelease` completati sulla copia locale della versione 11.7.
- **166 test JVM**, nessun fallimento, errore o test saltato.
- **0 errori lint, 259 avvisi**: includono risorse inutilizzate secondo l'analisi statica, API, quantità plurali e accessibilità. Non viene dichiarata l'assenza di tutti i problemi.
- 40 cataloghi, 205 stringhe per lingua, plurali e 40 informative privacy: controllate chiavi, segnaposto e inclusione nei pacchetti compilati. Non è una revisione linguistica madrelingua.
- Asset dei santi: 4.018 date dal 2026 al 2036, inclusi i giorni bisestili. Conservate fonti e attribuzioni.
- APK e AAB locali verificati per firma, pacchetto, versione e asset; nessuna pubblicazione su Google Play è attestata da questi controlli.

Il codice e le risorse dell'app in questo repository corrispondono alla versione verificata. Per la pubblicazione sono stati aggiunti documentazione, attribuzioni dei caratteri e regole Git. Non sono incluse chiavi di firma, password, dati dell'utente, cache o configurazioni del computer.

Non è stata eseguita una prova su telefono o emulatore funzionante per questa consegna. Grafica, TalkBack, caratteri grandi, audio, widget e notifiche a schermo spento richiedono una verifica sul dispositivo.

## English

- `bundleRelease`, `assembleRelease`, `testDebugUnitTest` and `lintRelease` completed on the local 11.7 source snapshot.
- **166 JVM tests**, with no failures, errors or skipped tests.
- **0 lint errors and 259 warnings**, including statically unused resources, APIs, plural quantities and accessibility. This is not a claim that all issues have been resolved.
- 40 language catalogs, 205 strings per language, plurals and 40 privacy documents checked for key coverage, format placeholders and inclusion in compiled packages. This is not a native-speaker language review.
- Saints assets cover 4,018 dates from 2026 to 2036, including leap days, with sources and attribution preserved.
- Local APK/AAB signatures, package identifiers, version metadata and assets were checked. These checks do not establish publication on Google Play.

Application code and resources in this repository match the validated snapshot. Publication adds documentation, font attribution and Git rules. Signing keys, passwords, user data, caches and machine-specific configuration are excluded.

No working phone or emulator was used for this delivery. Visual appearance, TalkBack, large fonts, audio, widgets and screen-off notifications still require on-device testing.

## Repeat the checks / Ripetere i controlli

```sh
./gradlew bundleRelease assembleRelease testDebugUnitTest lintRelease
```

On Windows use `gradlew.bat`. JDK 21 and Android SDK 36 are required for the documented setup. Generated reports are in `app/build/reports/` and `app/build/test-results/`.
