# Moya Rossiya TV — Android TV

Native App für den 24/7-Livestream auf [moyarossiya.tv](https://moyarossiya.tv/). Kotlin, Jetpack Compose und Media3/ExoPlayer; für Android TV, Google TV, Smartphones und Tablets ab Android 7.0.

## Funktionen

- **Adaptives Layout:** TV-Navigation links, mobile Navigation unten, Hochformat-Unterstützung und automatischer Querformat-Vollbildmodus
- **Sprache nach Geräteeinstellung:** Englisch als Standard/Fallback sowie Deutsch, Französisch, Russisch und Spanisch; Sendungstitel bleiben im Original
- **Live:** automatischer Stream-Start, Pause/Weiter, Vollbild und Wiederholung nach Wiedergabefehlern
- **Jetzt / Als Nächstes:** Aktualisierung alle 15 Sekunden
- **Programm heute:** vollständiger Sendeplan mit Berliner Uhrzeiten, aktueller Sendung und Sendungsdetails
- **TV-Bedienung:** D-Pad-, OK-, Zurück- und Touch-Unterstützung, große Fokusmarkierungen und TV-Launcher-Banner
- **Über uns:** Senderprofil und Verweise auf moyarossiya.tv und de.moyarossiya.com

Die App benötigt kein Benutzerkonto und enthält keine Werbung oder Tracking-Dienste.

## Datenquellen

| Inhalt | Öffentlicher Endpoint |
|---|---|
| Livestream | `https://moyarossiya.tv:8889/live/stream/index.m3u8` |
| Aktuelle Sendung | `https://moyarossiya.tv/now.json` |
| Nächste Sendung | `https://moyarossiya.tv/next.json` |
| Tagesprogramm | `https://moyarossiya.tv/plan.json` |

Der MediaMTX-HLS-Einstieg verwendet `cookieCheck` und `hlsSession`. Deshalb nutzt der Player Media3 mit OkHttp und einem gemeinsamen `JavaNetCookieJar` für Master-Playlist, Varianten und Segmente. Die Sendeplan-Zeitstempel werden mit der Zone `Europe/Berlin` formatiert.

## Lokal bauen

Voraussetzung: JDK 17 und Android SDK 36. Projekt in Android Studio öffnen oder:

```bash
./gradlew lintDebug assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

Release-AAB lokal:

```bash
KEYSTORE_PATH=/pfad/upload.keystore \
KEYSTORE_PASSWORD=… KEY_ALIAS=… KEY_PASSWORD=… \
./gradlew bundleRelease
```

## GitHub Actions

Der Workflow `.github/workflows/android.yml` baut bei jedem Push auf `main` Lint und eine Debug-APK. Das APK steht unter **Actions → Build Moya Rossiya TV → Artifacts**.

Für eine signierte Play-AAB dieselben Repository-Secrets wie bei MoyaVPN hinterlegen:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Ohne Secrets wird nur die Debug-APK erzeugt. Der Workflow veröffentlicht weder Releases noch Store-Uploads automatisch.

## Projektstruktur

| Pfad | Aufgabe |
|---|---|
| `MainActivity.kt` | App-Einstieg und Bildschirm-an |
| `ui/MoyaTvApp.kt` | Bereiche, Navigation und Zurück-Verhalten |
| `ui/LiveScreen.kt` | Live-Player, Vollbild, Cookie-fähige Media3-Wiedergabe |
| `ui/ProgramScreen.kt` | Tagesprogramm und Sendungsdetails |
| `ui/ProgramViewModel.kt` | Aktualisierung und Fehlerzustände |
| `data/` | JSON-Modelle und Retrofit-API |
| `ui/theme/` | Moya-Rossiya-Farben und TV-Typografie |

## Ausblick: Samsung und LG

Samsung Tizen und LG webOS benötigen jeweils eigene Web-Apps. Livestream und Programm können unverändert über die oben genannten HTTPS-Schnittstellen angebunden werden. Für diese Plattformen müssen insbesondere HLS-Cookie-Handling, Fernbedienungs-Events und Plattform-Zertifizierung separat geprüft werden; eine gemeinsame native Android-Codebasis ist dort nicht lauffähig.
