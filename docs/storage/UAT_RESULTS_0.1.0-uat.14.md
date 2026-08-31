# Lumungus Storage UAT Results 0.1.0-uat.14

Datum: 31.08.2026

## Ziel

Dieser Kandidat legt die naechsten Storage-Geraete als echte Ingame-Basis an:

- Wireless Storage Controller I: geplant fuer kurze Distanz.
- Wireless Storage Controller II: geplant fuer gleiche Dimension.
- Wireless Storage Controller III: geplant fuer mehrere Dimensionen.
- Lager-Output: geplant fuer Export aus dem Storage-Netzwerk in ein Inventar.
- Lager-Breaker: geplant fuer Abbau des Blocks unter sich mit optionalem Filter.
- Lager-Placer: geplant fuer Platzieren eines konfigurierten Blocktyps aus dem Lager.

Die Bloecke sind in diesem Slice registriert, sichtbar, craftbar, haben Loot,
Mining-/Wrench-Tags, eigene Texturen und persistente BlockEntities. Die
laufende Automatik, Wireless-Linking und Filter-/Konfigurationsmenues folgen
im naechsten Funktionsslice.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | `:lumungus-storage:build storageUatBundle :lumungus-integration:build` erfolgreich. |
| Server-GameTests | `PASS` | 18/18 Storage-GameTests und 3/3 Integration-GameTests bestanden; Minecraft lud 1599 Storage-Rezepte und 1610 Integration-Rezepte. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.14.zip` wurde erstellt. |

## Manueller UAT-Fokus

- Im Kreativmenue nach `Wireless Storage Controller`, `Lager-Output`,
  `Lager-Breaker` und `Lager-Placer` suchen.
- Alle sechs neuen Bloecke platzieren und pruefen, dass keine Textur fehlt.
- Mit Spitzhacke und Kupfer-Schraubenschluessel abbauen.
- Rechtsklick auf jeden Block pruefen; aktuell soll eine klare
  Platzhalter-Meldung fuer den naechsten Slice erscheinen.
