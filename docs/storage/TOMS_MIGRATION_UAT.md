# Tom's-Migration UAT 0.1.0-uat.4

Dieser UAT prueft ausschliesslich den lesenden Zugriff auf ein bestehendes
Tom's-Simple-Storage-Lager. Der Kandidat enthaelt keine schreibende
Konvertierung und darf keine Bloecke oder Inventare veraendern.

## Testpaket

- Minecraft: `26.2`
- Fabric Loader: `0.19.5`
- Fabric API: `0.158.0+26.2`
- Tom's Simple Storage: Fabric `26.2-2.11.3` (separat installiert)
- Lumungus: `0.1.0-uat.4`
- Paket: `build/uat/lumungus-toms-migration-0.1.0-uat.4.zip`

Das ZIP enthaelt `lumungus-core`, `lumungus-storage` und
`lumungus-integration` als getrennte JARs im Ordner `mods/`.

## Sicherheitsregeln

- Ausschliesslich mit einer neuen Kopie der Welt testen.
- Die unveraenderte Originalsicherung bis zum Abschluss nicht starten oder ueberschreiben.
- Tom's fuer diesen Test installiert lassen.
- Bei Ladefehlern, unbekannten Tom's-Bloecken, ungeladenen Grenzen oder einem abgebrochenen Scan sofort stoppen.
- Nach dem Scan keine Tom's-Bloecke entfernen. Dieser Kandidat prueft nur den Bericht.

## Ablauf

1. Minecraft mit Fabric API, Tom's `26.2-2.11.3` und den drei Lumungus-JARs starten.
2. Eine frische Kopie der bestehenden Lagerwelt laden und alle Lagerbereiche sowie entfernten Funksegmente laden.
3. Am Tom's-Controller stehen und dessen Koordinaten notieren.
4. Als Spieler mit Befehlsrechten `/lumungus migration scan <x> <y> <z>` ausfuehren.
5. Den vollstaendigen Chatbericht und `latest.log` sichern.
6. Die Welt ohne Umbauten speichern, beenden und danach verwerfen oder getrennt aufbewahren.

## Abnahme

- [ ] `UAT-M01` Die Weltkopie startet ohne fehlende oder inkompatible Mods.
- [ ] `UAT-M02` Der Befehl erkennt den Tom's-Startblock.
- [ ] `UAT-M03` Alle erwarteten Dimensionen und entfernten Teilnetze erscheinen im Bericht.
- [ ] `UAT-M04` Es gibt keine Meldung zu ungeladenen Grenzen oder zum 100.000-Block-Limit.
- [ ] `UAT-M05` Es gibt keine unbekannten oder blockierenden Tom's-Bloecke.
- [ ] `UAT-M06` Jedes Teilnetz meldet plausible Inventar-, Slot-, Item- und Typzahlen.
- [ ] `UAT-M07` Die Gesamtsumme entspricht dem erwarteten Lagerbestand von ungefaehr 7.000.000 Items.
- [ ] `UAT-M08` Der Bericht endet mit der ausdruecklichen Nur-Lese-Erfolgsmeldung.
- [ ] `UAT-M09` Stichproben an Kisten und Faessern zeigen nach dem Scan unveraenderte Inhalte.
- [ ] `UAT-M10` Ein erneuter Scan liefert dieselben Summen.

## Ergebnis

Version: `0.1.0-uat.4`  Tester: `________________`  Datum: `________________`

Gesamt: `PASS` / `FAIL` / `BLOCKED`

Bei `FAIL` oder `BLOCKED` werden Chatbericht, `latest.log`, Startkoordinaten und
die betroffene Dimension festgehalten. Die Originalwelt bleibt unangetastet.
