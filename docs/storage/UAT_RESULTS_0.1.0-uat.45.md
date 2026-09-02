# Lumungus Storage UAT Results 0.1.0-uat.45

Datum: 02.09.2026

## Ergebnis

`AUTOMATED_PASS`

Das Crafting Terminal kann fehlende Zwischenprodukte rekursiv aus dem Spieler- und Lagerbestand herstellen. Ein ueber JEI eingelegtes Fass-Rezept wird beispielsweise aus Eichenstaemmen ueber Bretter und Holzstufen vorbereitet. Die komplette Rezeptkette wird vor der Ausfuehrung geplant; Rezeptschleifen, mehr als acht Ebenen und mehr als 256 Teilschritte werden abgebrochen. Nicht benoetigte Ausgaben gelangen zurueck ins Lager oder Spielerinventar.

## Automatisierte Pruefungen

| Pruefung | Ergebnis | Details |
|---|---|---|
| Storage-GameTests | `PASS` | 37/37 Tests; der neue Test bereitet ein Vanilla-Fass aus drei Eichenstaemmen vor und prueft Bretter- sowie Stufenreste. |
| Machines-GameTests | `PASS` | 4/4 Tests. |
| Integration-GameTests | `PASS` | 3/3 Tom's-Migrationstests. |
| Storage-Client-GameTest | `PASS` | Minecraft 26.2 und JEI starteten mit UAT.45 erfolgreich. |
| Machines-Client-GameTest | `PASS` | Gemeinsamer Clientstart mit Core, Storage und Machines bestanden. |

## Manueller UAT

- Nur drei Eichenstaemme ins Lager legen und im Crafting Terminal das Fass-Rezept ueber JEI uebertragen.
- Das 3x3-Feld muss das vollstaendige Fass-Rezept zeigen; das Ergebnisfeld muss ein Fass anzeigen.
- Nach dem Craften muessen uebrige Bretter und Holzstufen im Lager liegen.
- Ein nicht herstellbares Rezept darf vorhandene Items im Crafting-Feld und Lager weder verlieren noch duplizieren.
