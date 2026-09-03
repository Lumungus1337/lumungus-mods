# Lumungus Storage UAT Results 0.1.0-uat.50

## Ziel

Wireless-Bloecke muessen ein Lager ueber eine eindeutige, sichtbare Netzwerkkarte erreichen. Dadurch darf es bei mehreren Lagern keine zufaellige Auswahl geben.

## Aenderungen

- Eine ungepraegte Wireless-Netzwerkkarte wird per Rechtsklick direkt am Storage Controller gepraegt.
- Wireless Storage Controller und Wireless Inventaranschluesse besitzen einen sichtbaren Karten-Slot, erreichbar per Schleichen + Rechtsklick.
- Eine gepraegte Karte kann alternativ per direktem Rechtsklick eingesetzt werden.
- Ohne Karte bleibt ein Wireless-Block inaktiv und meldet den fehlenden Einsatz sofort.
- Stufe I gilt in kurzer Distanz, Stufe II in derselben Dimension und Stufe III dimensionsuebergreifend.
- Bestehende Direktbindungen aus UAT.49 werden beim Laden automatisch in eingesetzte Karten migriert.
- Beim Abbau wird die eingesetzte Karte samt Netzwerkbindung gedroppt.

## Automatisierte Ergebnisse

- `PASS`: 43 von 43 Storage-GameTests.
- `PASS`: 4 von 4 Machines-GameTests.
- `PASS`: 3 von 3 Integration-GameTests.
- `PASS`: Storage- und Machines-Client-GameTests.
- `PASS`: Modulweite Unit-Tests und JSON-Ressourcenvalidierung.

## Manueller UAT

1. Eine Wireless-Netzwerkkarte herstellen und am Storage Controller rechtsklicken.
2. Einen Wireless Storage Controller III in einer anderen Dimension platzieren.
3. Schleichen + Rechtsklick auf den Wireless Controller und die gepraegte Karte in den Slot legen.
4. Das Menue schliessen und den Wireless Controller normal rechtsklicken.
5. Das Crafting Terminal muss das exakt auf der Karte gespeicherte Lager oeffnen.
6. Dieselbe Pruefung mit einem Wireless Inventaranschluss III und einer angrenzenden Kiste wiederholen.
7. Karte entfernen: Beide Wireless-Bloecke muessen sofort inaktiv sein und "Keine Netzwerkkarte" melden.
