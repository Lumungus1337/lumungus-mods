# Lumungus Storage UAT Results 0.1.0-uat.49

## Ziel

Multidimensionale Funkverbindungen muessen den gebundenen Lager-Chunk bei Bedarf laden und in beiden Dimensionsrichtungen funktionieren.

## Aenderungen

- Wireless Storage Controller III loest gebundene Lager in Overworld und Nether auf, auch wenn der Ziel-Chunk zuvor entladen war.
- Wireless Inventory Connector III verwendet denselben dimensionsuebergreifenden Zugriff.
- Tragbare Storage Interfaces und eingebaute Wireless-Module laden ihren gueltigen gebundenen Ziel-Chunk bei Bedarf.
- Automatische Tests decken Nether nach Overworld und Overworld nach Nether ab.

## Automatische Pruefung

- `PASS`: 41 von 41 Storage-GameTests.
- `PASS`: 4 von 4 Machines-GameTests.
- `PASS`: 3 von 3 Integration-GameTests.
- `PASS`: Storage- und Machines-Client-GameTests.
- `PASS`: Unit-Tests und vollstaendiger UAT-Paketbau.

## Manueller UAT

1. Ein Wireless-Lagermodul am Hauptlager praegen.
2. In der anderen Dimension einen Wireless Storage Controller III mit diesem Modul koppeln.
3. Den Ziel-Chunk verlassen beziehungsweise die Dimension wechseln.
4. Den Controller III normal rechtsklicken; das Crafting Terminal muss das gebundene Lager anzeigen.
5. Den Test mit vertauschten Dimensionen wiederholen.
