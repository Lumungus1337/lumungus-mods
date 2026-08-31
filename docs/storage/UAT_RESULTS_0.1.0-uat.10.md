# Lumungus Storage UAT Results 0.1.0-uat.10

Datum: 31.08.2026

## Ziel

Dieser Kandidat erweitert die Drive Bay von einem einzelnen Cell-Slot auf acht Cell-Slots.

- Eine Drive Bay kann bis zu acht 16k Storage Cells aufnehmen.
- Alte Speicherstaende mit einer einzelnen gespeicherten Cell werden beim Laden in Slot 1 uebernommen.
- Kapazitaet, Itemtypen, Einlagerung und Entnahme laufen ueber alle eingesetzten Cells.
- Beim Abbau droppen alle eingesetzten Cells samt Inhalt.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | `./gradlew :lumungus-storage:build storageUatBundle :lumungus-integration:build` erfolgreich. |
| Server-GameTests | `PASS` | 15/15 Storage-GameTests bestanden; neuer Test prueft acht Cells in einer Drive Bay. |
| Integration-GameTests | `PASS` | 3/3 Tom's-Migrations- und Integrations-GameTests bestanden. |
| UAT-Bundle | `PASS` | Paket erstellt: `build/uat/lumungus-storage-0.1.0-uat.10.zip`. |

## Manueller UAT-Fokus

- Acht 16k Storage Cells nacheinander in eine Drive Bay rechtsklicken.
- Eine neunte Cell sollte abgelehnt werden.
- Items per Terminal oder `BAY`-Knopf einlagern und Kapazitaetsanzeige pruefen.
- Shift-Rechtsklick ohne Item auf die Drive Bay entnimmt jeweils die zuletzt eingesetzte Cell.
