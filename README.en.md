# Ricorda

**One less thing on your mind.** Birthdays, deadlines and everyday tasks in a simple, ad-free Android app.

[Italiano](README.md) · **English**

Ricorda helps you organize your commitments with a calendar, reminders and alarms. It works offline, without an account: your reminders stay on your device.

| Version | Minimum Android version | Package |
| --- | --- | --- |
| 11.7 · version code 18 | Android 8.0 (API 26) | `com.fabio.ricorda` |

## Features

- **Plan your day:** a monthly calendar available from the first launch, markers for days with reminders and a list of the selected day's commitments.
- **Create and find reminders:** titles, notes, dates and times; text and date search, filters, editing and a trash area with recovery.
- **Choose when to repeat a reminder:** daily, weekly, monthly or yearly, or select multiple dates in the yearly calendar.
- **Customize alerts:** notifications, system sounds, vibration and alarms with adjustable volume and a preview while adjusting it. Dismiss an alarm or snooze it for 10 minutes.
- **Make it your own:** 21 colors, light and dark modes, and fonts including Nunito, Lato, Lora and Caveat.
- **Use home screen widgets:** an agenda and, in Italian, the saint of the day, with layouts that adapt to the available space.
- **Keep a copy of your data:** export and import reminders as JSON files with recognizable backup names.
- **Use your language:** 40 languages, an initial selection based on the phone's language and a selector showing native language names. Contributions from native speakers can help improve the translations.

The Italian interface also offers saint search, a daily notification and an almanac covering **2026–2036**. This is a name-day and observance almanac, not an official liturgical calendar: see the [sources and methodology, in Italian](data/SANTI-FONTI-E-METODO.md).

### What's new in 11.7

The volume bar fades from white at the low end to the full theme color at the high end. Volume adjustment and sound playback retain their previous behavior.

## Open in Android Studio

1. Clone this repository, or select **Code → Download ZIP** on GitHub and extract it.
2. In Android Studio, choose **Open** and select the folder containing `settings.gradle`.
3. Install **Android SDK 36** if prompted, and select **Gradle JDK 21** in the Gradle settings. The source code uses Java 17.
4. Wait for Gradle sync, select a device or emulator running Android 8.0 or later, and press **Run ▶**.

The project includes the **Gradle 8.11.1** wrapper and uses **Android Gradle Plugin 8.9.2**. The first sync requires Internet access to download build tools; the installed app works offline.

### Build an APK and run checks

On Windows, in a terminal at the project root:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintRelease
```

On macOS or Linux:

```sh
./gradlew assembleDebug testDebugUnitTest lintRelease
```

The installable APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. For Google Play, select **Build → Generate Signed Bundle / APK → Android App Bundle** and use your signing key. Signing keys and passwords are not included in this repository.

## Privacy and notifications

The app does not request Internet permission, includes no advertising or analytics, and does not read your contacts, location or system calendar. Settings and reminders are stored in the app's private storage. Exported backups are **not encrypted**: choose carefully where you save them and whom you share them with.

Notifications, exact alarms and full-screen alerts depend on Android permissions and settings. Do Not Disturb, battery saving and device configuration can affect alert delivery.

Read the [English privacy policy](app/src/main/assets/privacy-en.txt).

## Validation and contributions

Version 11.7 passed the build and **166 JVM tests**. `lintRelease` reports 0 errors and 259 warnings requiring review. These checks do not replace testing on a phone, especially for screen-off notifications, audio, widgets and accessibility. See [docs/VALIDATION.md](docs/VALIDATION.md).

To report a problem, open an [issue](https://github.com/devfaxpro/ricorda/issues) with your Android version, app version, reproduction steps and expected behavior. Do not attach personal backups or reminders. Translation, accessibility and code improvements are welcome.

## License and attribution

Ricorda's code is published under the **GNU GPL v3** license already present in this repository: [LICENSE](LICENSE). Third-party components and data retain their own licenses: [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

In particular, the derived saints almanac data is identified as **CC BY-NC-SA 2.5 Italy** and is not covered by the code's GPL license. Fonts are distributed under the **SIL Open Font License 1.1**.

Developer: **Fabio Avitabile** · Support and privacy: [fabiocorby@gmail.com](mailto:fabiocorby@gmail.com).
