# Lumungus Storage UAT Results 0.1.0-uat.52

## Ziel

Der JEI-Transfer und die automatisch ausgefuehrten rekursiven Herstellungsschritte muessen fuer den Spieler nachvollziehbar sein.

## Aenderungen

- Nach einem direkten JEI-Transfer meldet das Terminal die tatsaechlich im Raster vorbereitete Ergebnisanzahl.
- Rekursive Herstellung zeigt eine kompakte Crafting-Plan-Kette in den Spielnachrichten.
- Jeder Abschnitt nennt Eingabe, Ausgabe und die ueber alle identischen Rezeptdurchlaeufe summierten Mengen.
- Die letzte Stufe nennt die Endmenge, deren Zutaten jetzt im normalen 3x3-Raster liegen.
- Sehr verzweigte Plaene werden nach zwoelf sichtbaren Stufen kompakt zusammengefasst.

## Automatisierte Ergebnisse

- `PASS`: Modulweite Kompilierung und Unit-Tests.
- `PASS`: 45 Storage-GameTests einschliesslich Ergebnisanzahl und rekursiver Mehrfachplaene.
- `PASS`: Storage- und Machines-Client-GameTests.
- `PASS`: 4 Machines- und 3 Integration-GameTests.
- `PASS`: `build/uat/lumungus-storage-0.1.0-uat.52.zip`, SHA-256 `E46520D03CBE61E6525F5E12B17387EF350E5050DC5CE04162709B2BE2EFAFA7`.

## Manueller UAT

1. Im Crafting Terminal eine Ergebnisanzahl einstellen.
2. In JEI ein mehrstufiges Rezept uebertragen, beispielsweise Tiefenschieferfliesentreppen aus Tiefenschiefer.
3. Die Spielnachricht muss jede automatisch hergestellte Zwischenstufe mit Ein- und Ausgangsmengen zeigen.
4. Die letzte Meldung muss der im Raster vorbereiteten Endmenge entsprechen.
5. Das Ergebnis einmal normal und einmal per Shift-Klick entnehmen.
