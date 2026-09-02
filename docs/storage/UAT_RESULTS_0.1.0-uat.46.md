# Lumungus Storage UAT Results 0.1.0-uat.46

Datum: 02.09.2026

## Ergebnis

`AUTOMATED_PASS`

Kabelgebundene und Wireless-Inventaranschluesse besitzen jetzt einen dauerhaft gespeicherten Auto-Send-Modus. Shift-Rechtsklick schaltet ihn ein oder aus. Ein aktiver Anschluss verschiebt einmal pro Sekunde bis zu 64 Stacks aus seinen eigenen angrenzenden Kisten, Faessern oder Maschineninventaren in erreichbare Drive Bays. Standardmaessig ist der Modus aus, sodass bestehende Lager nach dem Update unveraendert bleiben.

## Automatisierte Pruefungen

| Pruefung | Ergebnis | Details |
|---|---|---|
| Storage-GameTests | `PASS` | 39/39 Tests; kabelgebundener und Wireless-Anschluss verschieben ihre Quellinventare in Cells, waehrend andere Anschluesse unberuehrt bleiben. |
| Machines-GameTests | `PASS` | 4/4 Tests. |
| Integration-GameTests | `PASS` | 3/3 Tom's-Migrationstests. |
| Storage-Client-GameTest | `PASS` | Minecraft 26.2 und JEI starteten mit UAT.46 erfolgreich. |
| Machines-Client-GameTest | `PASS` | Gemeinsamer Clientstart mit Core, Storage und Machines bestanden. |

## Manueller UAT

- Einen Inventaranschluss mit einer gefuellten Kiste verbinden und mindestens eine Drive Bay mit freier Cell bereitstellen.
- Mit leerer Hand Shift-Rechtsklick auf den Anschluss ausfuehren; die Meldung muss Auto-Send als aktiv bestaetigen.
- Die Kiste muss schrittweise geleert werden und die Itemmenge in der Drive Bay entsprechend steigen.
- Eine Kiste an einem anderen Anschluss darf dabei nicht geleert werden.
- Erneuter Shift-Rechtsklick muss Auto-Send abschalten. Dieselben Schritte an einem Wireless Inventaranschluss wiederholen.
