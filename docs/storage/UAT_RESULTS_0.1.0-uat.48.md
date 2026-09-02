# Lumungus Storage UAT Results 0.1.0-uat.48

## Ziel

Multidimensionale Wireless Storage Controller duerfen ihre Overworld-Bindung beim Wechsel in den Nether nicht verlieren.

## Aenderungen

- Eine gespeicherte Controller-Bindung bleibt erhalten, wenn der Ziel-Chunk oder die Zieldimension voruebergehend entladen ist.
- Ein gepraegtes Wireless-Lagermodul uebertraegt seine feste Lageradresse per Rechtsklick auf einen Wireless Storage Controller.
- Stufe III akzeptiert diese Kopplung dimensionsuebergreifend.

## Automatische Pruefung

- `PASS`: 40 von 40 Storage-GameTests, einschliesslich Nether-zu-Overworld-Zugriff.
- `PASS`: 4 von 4 Machines-GameTests.
- `PASS`: Storage- und Machines-Client-GameTests.
- `PASS`: Unit-Tests und vollstaendiger UAT-Paketbau.

## Manueller UAT

1. Ein Wireless-Lagermodul am verbundenen Wireless Storage Controller des Hauptlagers praegen.
2. Im Nether einen Wireless Storage Controller III platzieren.
3. Mit dem gepraegten Modul auf den Nether-Controller rechtsklicken; die Meldung muss das gekoppelte Netzwerk nennen.
4. Den Nether-Controller normal rechtsklicken; das Crafting Terminal muss sich mit dem Overworld-Lager oeffnen.
5. Dimension zweimal wechseln und Schritt 4 wiederholen. Die Bindung muss erhalten bleiben.
