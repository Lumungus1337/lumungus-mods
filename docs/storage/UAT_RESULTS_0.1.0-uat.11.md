# Lumungus Storage UAT Results 0.1.0-uat.11

Datum: 31.08.2026

## Ziel

Dieser Kandidat macht die Drive Bay sichtbar verwaltbar:

- Rechtsklick auf eine Drive Bay oeffnet ein eigenes Rack-Menue.
- Das Menue zeigt acht echte Cell-Slots.
- Nur 16k Storage Cells koennen in diese Slots gelegt werden.
- Shift-Klick bewegt Cells zwischen Spielerinventar und Drive Bay.
- Der Schraubenschluessel behaelt Vorrang und baut die Drive Bay weiterhin direkt ab.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | `./gradlew :lumungus-storage:build storageUatBundle :lumungus-integration:build` erfolgreich. |
| Server-GameTests | `PASS` | 16/16 Storage-GameTests bestanden; neuer Test prueft Shift-Klick zwischen Spielerinventar und Drive-Bay-Menue. |
| Integration-GameTests | `PASS` | 3/3 Tom's-Migrations- und Integrations-GameTests bestanden. |
| UAT-Bundle | `PASS` | Paket erstellt: `build/uat/lumungus-storage-0.1.0-uat.11.zip`. |

## Manueller UAT-Fokus

- Drive Bay rechtsklicken und pruefen, dass das Cell-Rack-Menue oeffnet.
- Acht Storage Cells einlegen, Tooltip/Fuellstand der Cell ansehen und wieder entnehmen.
- Shift-Klick aus Spielerinventar in die Drive Bay und zurueck testen.
- Mit Kupfer-Schraubenschluessel auf die Drive Bay klicken; der Block soll weiterhin sofort abgebaut werden.
