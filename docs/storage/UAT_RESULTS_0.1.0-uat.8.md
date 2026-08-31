# Lumungus Storage UAT Results 0.1.0-uat.8

Datum: 31.08.2026

## Ziel

Dieser Kandidat verbessert die Survival-Nutzbarkeit der Storage-Bloecke:

- Crafting-Terminal-Suche ist beim Oeffnen direkt fokussiert.
- Suche findet Items ueber sichtbaren Namen und Item-ID.
- Storage-Bloecke haben kupferlastige Survival-Rezepte.
- Lumungus-Storage-Bloecke sind als Spitzhacken-Bloecke markiert.
- Kupfer-Schraubenschluessel kann Lumungus-Storage-Bloecke sofort mit normalen Drops entfernen.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | `./gradlew clean build storageUatBundle :lumungus-integration:build` erfolgreich. |
| Ressourcen und Rezepte | `PASS` | Minecraft lud die Datapacks ohne Rezept-Parse-Fehler. |
| Integration-GameTests | `PASS` | 3/3 Tom's-Migrations- und Integrations-GameTests bestanden. |
| Server-GameTests | `PASS` | 13/13 Storage-GameTests bestanden. |
| UAT-Bundle | `PASS` | Paket erstellt: `build/uat/lumungus-storage-0.1.0-uat.8.zip`. |

## Manueller UAT-Fokus

- Im Kreativmenue nach `Kupfer-Schraubenschluessel`, `Rohrpostrohr`, `Crafting Terminal` und `Storage Controller` suchen.
- Crafting Terminal oeffnen und direkt tippen; die Suche sollte ohne extra Klick reagieren.
- Survival-Rezepte in JEI oder Rezeptbuch pruefen.
- Storage-Block mit Spitzhacke abbauen und Drop pruefen.
- Storage-Block mit Kupfer-Schraubenschluessel rechtsklicken und Sofortabbau plus Haltbarkeitsverlust pruefen.
