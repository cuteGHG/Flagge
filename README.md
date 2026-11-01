# Flagge

Paper-Plugin für Minecraft 1.21.11, das das Herkunftsland eines Spielers neben
seinem Namen in der Tablist und optional im Display-Namen anzeigt.

## Verwendung

1. `./gradlew build` ausführen.
2. `build/libs/Flagge-1.1.0.jar` in den `plugins`-Ordner kopieren.
3. Server starten.
4. Anzeige und Geo-IP-Endpunkt in `plugins/Flagge/config.yml` anpassen.

Standardmäßig wird eine Pixel-Flagge angezeigt, zum Beispiel die rumänische
Flagge neben dem Namen. Dafür muss das mitgelieferte Resourcepack installiert
werden.
Über `%country%`, `%country_code%` und `%flag%` können Land, Code und
konfiguriertes Flaggenformat verwendet werden.

Die IP-Adresse wird für die Länderbestimmung an den konfigurierten Geo-IP-
Dienst gesendet. Der Dienst und die Cache-Dauer können in der Konfiguration
geändert werden. Mit `/flaggereload` (Berechtigung `flagge.reload`) wird die
Konfiguration neu geladen. In TAB/Nametags wird `%flagge_flag%` verwendet.