# Lumungus Storage UAT Results 0.1.0-uat.21

Datum: 31.08.2026

## Ziel

Dieser Kandidat fuegt tragbare Storage Interfaces als Handheld-Zugang zum Lager hinzu:

- Tragbares Storage Interface I fuer kurze Distanz.
- Tragbares Storage Interface II fuer gleiche Dimension.
- Tragbares Storage Interface III fuer dimensionsuebergreifende Bindungen.
- Shift-Rechtsklick auf einen Storage Controller bindet das Interface an dieses Netzwerk.
- Rechtsklick oeffnet das Storage Terminal, wenn das gebundene oder naechste Netzwerk erreichbar ist.
- Die Interfaces haben eigene IDs, Kreativmenue-Eintraege, Rezepte und gespeicherte Controller-Bindung.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | `:lumungus-storage:build`, `storageUatBundle` und `:lumungus-integration:build` erfolgreich. |
| Server-GameTests | `PASS` | Lumungus Storage: 24/24, Lumungus Integration: 3/3. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.21.zip`, SHA256 `1128041E0E82231FE5BDC3A5F7EBA1372E27D53B18BE34C97CC30EE21C6DF623`. |

## Manueller UAT-Fokus

- Interface I/II/III im Kreativmenue und per Rezept pruefen.
- Interface mit Shift-Rechtsklick auf einen Storage Controller binden.
- Mit Rechtsklick aus verschiedenen Entfernungen das Terminal oeffnen.
- Pruefen, dass Entnahme, Einlagerung, Suche und Shulker-Modus im portablen Terminal wie am normalen Terminal funktionieren.
