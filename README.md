# Flagge

Paper-Plugin für Minecraft 1.21.11, das über PlaceholderAPI eine Pixel-Flagge
bereitstellt. Das Plugin verändert Namen und Tablist nicht automatisch.

## Verwendung

1. `./gradlew build` ausführen.
2. `build/libs/Flagge-1.2.0.jar` in den `plugins`-Ordner kopieren.
3. Server starten.
4. Anzeige und Geo-IP-Endpunkt in `plugins/Flagge/config.yml` anpassen.

In TAB verwendest du `%flagge_flag%` und für den Ländercode `%flagge_country_code%`.
Das Resourcepack muss auf dem Client installiert sein, damit `%flagge_flag%`
als Pixel-Flagge erscheint.

Die IP-Adresse wird für die Länderbestimmung an den konfigurierten Geo-IP-
Dienst gesendet. Der Dienst und die Cache-Dauer können in der Konfiguration
geändert werden. Mit `/flaggereload` (Berechtigung `flagge.reload`) wird die
Konfiguration neu geladen. In TAB/Nametags wird `%flagge_flag%` verwendet.