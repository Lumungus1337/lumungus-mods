# Lumungus Storage UAT Results 0.1.0-uat.15

Datum: 31.08.2026

## Ziel

Dieser Kandidat behebt zwei UAT-Funde am Rohrpostrohr:

- Neue Rohre nutzen die kanonische ID `pneumatic_pipe`; `inventory_cable` bleibt nur als Legacy-Kompatibilitaet fuer bestehende Testwelten registriert.
- Das Rohrpostrohr nutzt eine echte Rohr-Shape und `noOcclusion`, damit es beim Platzieren auf Boden, Wand oder Decke keine Nachbarflaechen als Vollblock verdeckt und keinen X-Ray-Effekt erzeugt.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | `:lumungus-storage:build storageUatBundle :lumungus-integration:build` erfolgreich. |
| Server-GameTests | `PASS` | 18/18 Storage-GameTests und 3/3 Integration-GameTests bestanden. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.15.zip` wurde erstellt. |

## Manueller UAT-Fokus

- Im Kreativmenue nach `Rohrpostrohr` suchen und pruefen, dass kein Cable-Name mehr fuer neue Rohre erscheint.
- Rohrpostrohre auf Boden, Wand und Decke platzieren.
- Pruefen, dass angrenzende Blockflaechen sichtbar bleiben und kein X-Ray-/Durchsicht-Effekt entsteht.
- Bestehende alte `inventory_cable`-Bloecke aus frueheren UAT-Welten sollen weiter als Legacy-Rohre funktionieren.
