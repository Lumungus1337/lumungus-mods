# Lumungus Storage UAT Results 0.1.0-uat.53

## Ziel

Rekursive Herstellung muss als eigener, bestaetigter Auftrag funktionieren. Ein JEI-Transfer darf vor der Bestaetigung keine Materialien bewegen.

## Aenderungen

- Der JEI-Transfer berechnet zunaechst nur eine Vorschau fuer Rezept und gewuenschte Ergebnisanzahl.
- Eine eigene Auftragsmaske zeigt Zielitem, Zielmenge und alle direkten oder rekursiven Herstellungsschritte.
- Lange Plaene koennen in der Auftragsmaske gescrollt werden.
- Erst die Schaltflaeche `HERSTELLEN` prueft den aktuellen Bestand erneut, entnimmt Zutaten und fuehrt den Auftrag aus.
- Fertige Items landen direkt im Spielerinventar.
- Bei zu wenig Inventarplatz stoppt der Auftrag ohne weitere Zutaten zu verbrauchen; vorbereitete Restzutaten bleiben im Crafting-Raster.

## Automatisierte Ergebnisse

- `PASS`: Modulweite Kompilierung und Unit-Tests.
- `PASS`: 45 Storage-GameTests, darunter unveraenderter Lagerbestand waehrend der Vorschau.
- `PASS`: Rekursive Auftraege fuer ein und zwei Faesser sowie aufgerundete Mehrfachausgabe fuer zehn Bretter.
- `PASS`: Storage- und Machines-Client-GameTests.
- `PASS`: 4 Machines- und 3 Integration-GameTests.
- `PASS`: UAT-Paket mit Core, Storage, Machines und Integration gemeinsam in Version UAT.53.

## Manueller UAT

1. Im Crafting Terminal die Zielmenge einstellen.
2. In JEI ein mehrstufiges Rezept auswaehlen, beispielsweise Tiefenschieferfliesentreppen aus Tiefenschiefer.
3. Den Lumungus-Transfer ausloesen und kontrollieren, dass die neue Auftragsmaske erscheint.
4. Pruefen, dass jede Zwischenstufe in der richtigen Reihenfolge und mit Mengen angezeigt wird.
5. `ZURUECK` testen; dabei duerfen keine Materialien bewegt werden.
6. Den Plan erneut oeffnen und `HERSTELLEN` waehlen.
7. Kontrollieren, dass die fertige Zielmenge im Spielerinventar landet und korrekte Ueberschuesse im Lager verbleiben.
