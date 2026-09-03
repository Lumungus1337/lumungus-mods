# Lumungus Storage UAT Results 0.1.0-uat.51

## Ziel

Die Ergebnisanzahl eines ueber JEI eingelegten Rezepts muss im Crafting Terminal frei waehlbar sein. Dieselbe Menge muss fuer direkte und rekursive Rezepte gelten.

## Aenderungen

- Unter dem Craftingraster steht eine numerische Mengenwahl mit Minus- und Plus-Schaltflaeche bereit.
- Die Zahl bezeichnet die gewuenschte Ergebnisanzahl, nicht pauschal die Zahl der Rezeptdurchlaeufe.
- Rezepte mit mehreren Ergebnisitems werden auf die kleinste ausreichende Zahl voller Rezeptdurchlaeufe aufgerundet.
- Der rekursive Planer erzeugt Zwischenprodukte fuer alle vorbereiteten Endrezepte und legt Ueberschuesse zurueck ins Lager.
- Das normale 3x3-Craftingraster begrenzt einen Transfer auf 64 Rezeptdurchlaeufe.

## Automatisierte Ergebnisse

- `PASS`: 45 von 45 Storage-GameTests.
- `PASS`: Gewuenschte 10 Bretter bereiten drei Stamm-Rezeptdurchlaeufe vor.
- `PASS`: Zwei Faesser werden rekursiv aus vier Staemmen vorbereitet; ein Brett und zwei Stufen bleiben als korrekter Ueberschuss.
- `PASS`: Modulweite Unit-Tests und Client-Kompilierung.
- `PASS`: UAT-Paket enthaelt Core, Storage, Machines und Integration gemeinsam in Version UAT.51.
- `PASS`: `build/uat/lumungus-storage-0.1.0-uat.51.zip`, SHA-256 `C25C85ABDB4A15FFFF76DDD32F8B9747458259818CF8C7585DE4A20A480268D7`.

## Manueller UAT

1. Crafting Terminal oeffnen und unter dem Craftingraster eine Menge eintragen.
2. In JEI ein Rezept auswaehlen und mit dem Lumungus-Transferknopf einlegen.
3. Pruefen, dass die Zutatenstapel der gewuenschten Ergebnisanzahl entsprechen.
4. Ein Rezept mit fehlenden Zwischenprodukten testen, etwa zwei Faesser aus vier Eichenstaemmen.
5. Mit Plus/Minus jeweils um eins und mit Shift + Plus/Minus jeweils um zehn aendern.
