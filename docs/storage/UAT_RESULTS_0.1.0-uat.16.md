# Lumungus Storage UAT Results 0.1.0-uat.16

Datum: 31.08.2026

## Ziel

Dieser Kandidat setzt zwei Rueckmeldungen aus der echten Survival-UAT-Kopie um:

- Die Storage-Geraete bekommen deutlich unterscheidbarere Fronten.
- Rohrpostrohre verbinden visuell und logisch nur noch zu Lumungus-Storage-Geraeten oder anderen Rohrpostrohren, nicht pauschal in alle Richtungen.

Die erfolgreiche manuelle Migration des Survival-Lagers in der UAT-Kopie ist als wichtiges Positivsignal dokumentiert. Der Transfer war langsam, aber verlustfrei nutzbar.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | `:lumungus-storage:build storageUatBundle :lumungus-integration:build` erfolgreich. |
| Server-GameTests | `PASS` | 19/19 Storage-GameTests und 3/3 Integration-GameTests bestanden. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.16.zip` wurde erstellt. |

## Manueller UAT-Fokus

- Neue Maschinenfronten platziert und im Inventar vergleichen.
- Rohrpostrohr neben normale Kiste, Boden, Wand und Luft setzen; es soll keine falschen Arme bilden.
- Rohrpostrohr zwischen Controller und Inventaranschluss setzen; es soll nur die benoetigten Richtungen anzeigen.
- Bestehendes Lager kurz oeffnen und pruefen, dass der Bestand weiterhin sichtbar bleibt.
