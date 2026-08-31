# Lumungus Storage UAT Results 0.1.0-uat.12

Datum: 31.08.2026

## Ziel

Dieser Kandidat ergaenzt die Shulker-Logistik fuer grosse Survival-Lager:

- Befuellte Shulkerboxen werden beim aktiven Einlagern nicht als volle Box gespeichert.
- Der Controller legt den Inhalt als normale Items und die Box als leere Shulkerbox ab.
- Das Crafting Terminal besitzt einen `ITM`/`BOX`-Schalter.
- Im `BOX`-Modus wird beim Klick auf ein Item eine leere Shulkerbox aus dem Netzwerk genommen und mit diesem Item befuellt.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | `./gradlew :lumungus-storage:build storageUatBundle :lumungus-integration:build` erfolgreich. |
| Server-GameTests | `PASS` | 18/18 Storage-GameTests bestanden; neue Tests pruefen Shulker-Entladung und Terminal-Shulker-Entnahme. |
| Integration-GameTests | `PASS` | 3/3 Tom's-Migrations- und Integrations-GameTests bestanden. |
| UAT-Bundle | `PASS` | Paket erstellt: `build/uat/lumungus-storage-0.1.0-uat.12.zip`. |

## Manueller UAT-Fokus

- Eine befuellte Shulkerbox im Crafting Terminal einlagern und pruefen, dass danach nur Inhalt plus leere Shulkerbox im Netzwerk sichtbar sind.
- Den Terminal-Schalter von `ITM` auf `BOX` stellen.
- Ein haeufiges Item anklicken und pruefen, dass eine befuellte Shulkerbox auf dem Cursor landet.
- Dasselbe mit Shift-Klick testen; die befuellte Shulkerbox soll direkt ins Spielerinventar gehen.
- Ohne leere Shulkerbox im Netzwerk testen; das Terminal soll eine klare Meldung anzeigen und keine Items entnehmen.
