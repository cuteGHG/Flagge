# Flagge

Paper-Plugin für Minecraft 1.21.11, das das Herkunftsland eines Spielers neben
seinem Namen in der Tablist und optional im Display-Namen anzeigt.

## Verwendung

1. `./gradlew build` ausführen.
2. `build/libs/Flagge-1.0.1.jar` in den `plugins`-Ordner kopieren.
3. Server starten.
4. Anzeige und Geo-IP-Endpunkt in `plugins/Flagge/config.yml` anpassen.

Standardmäßig wird der Länder-Code angezeigt, zum Beispiel `[RO] Spieler`.
Über `%country%`, `%country_code%` und `%flag%` können Land, Code und
konfiguriertes Flaggenformat verwendet werden.

Die IP-Adresse wird für die Länderbestimmung an den konfigurierten Geo-IP-
Dienst gesendet. Der Dienst und die Cache-Dauer können in der Konfiguration
geändert werden. Mit `/flaggereload` (Berechtigung `flagge.reload`) wird die
Konfiguration neu geladen.