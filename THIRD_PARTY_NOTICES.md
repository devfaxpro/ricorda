# Third-party notices / Attribuzioni

The repository's [GPL v3 license](LICENSE) applies to Ricorda's own code. It does not replace the licenses of third-party data, fonts or build tools.

## Saints almanac / Almanacco dei santi

The following data is derived from CreaGratis.com's *Calendario 2026 con Santi*, supplied for the project under CC BY-NC-SA 2.5 Italy:

- `app/src/main/assets/saints_2026_2036.txt`
- `data/saints_2026_original.txt`
- `data/santi_2026_per_Ricorda.txt`

Source: <https://www.creagratis.com/files/calendari/Calendario-2026-Santi.pdf>.
License: <https://creativecommons.org/licenses/by-nc-sa/2.5/it/>.

The derived data retains this attribution and license, including its NonCommercial and ShareAlike terms. It is not GPL-licensed or public-domain data. The GPL license of the application code does not remove these separate data restrictions.

Changes include transcription, normalization, corrections and extension through 2036 with calculated movable observances. See [the original notice](app/src/main/assets/NOTICE-santi.txt) and [sources and methodology](data/SANTI-FONTI-E-METODO.md). No saint biographies or website images are included.

I dati derivati mantengono CC BY-NC-SA 2.5 Italia, attribuzione e indicazione delle modifiche; non rientrano nella GPL del codice.

## Fonts / Caratteri

The bundled font files in `app/src/main/res/font/` are licensed under the **SIL Open Font License 1.1**. Copyright notices extracted from the actual bundled files and the license texts are included here:

| Family | Files | License |
| --- | --- | --- |
| Caveat | `caveat_variable.ttf` | [Caveat OFL](licenses/Caveat-OFL.txt) |
| Lato | `lato_regular.ttf`, `lato_bold.ttf` | [Lato OFL](licenses/Lato-OFL.txt) |
| Lora | `lora_variable.ttf` | [Lora OFL](licenses/Lora-OFL.txt) |
| Nunito | `nunito_variable.ttf` | [Nunito OFL](licenses/Nunito-OFL.txt) |

The fonts retain their original names and embedded metadata; their binaries have not been modified.

## Gradle and dependencies

The Gradle wrapper is included under its existing [Apache 2.0 license](GRADLE-LICENSE.txt). Android Gradle Plugin and test dependencies are downloaded by Gradle and are not vendored here. See `build.gradle` and `app/build.gradle` for their versions. JUnit and JSON-java are test dependencies, not runtime analytics or network SDKs.
