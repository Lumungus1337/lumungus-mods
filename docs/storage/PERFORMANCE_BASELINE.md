# Lumungus Storage Performance Baseline

## Physisches Testlager

Stand: 2026-08-30, Minecraft/Fabric 26.2, lokaler automatisierter GameTest.

- 1 Storage Controller
- 12 Rohrpostrohre
- 36 Inventory Connectoren
- 36 echte Vanilla-Kisten
- 59.904 eingelagerte Items
- 12 unterschiedliche Itemtypen
- keine Storage Cells oder Drive Bays
- 30 vollstaendige Abfragen aller 12 Itemtypen

Erster gemessener Lauf auf der Entwicklungsmaschine:

- 943 ms fuer alle 30 Abfragen
- rund 31 ms pro vollstaendiger Zwoelf-Typ-Abfrage
- Topologie-Cache: ein Aufbau, danach Wiederverwendung
- anschliessende Einlagerung in verteilte freie Slots erfolgreich
- anschliessende Entnahme aus dem physischen Lager erfolgreich

Der Test hat eine grosszuegige obere Schranke von 10.000 ms, um langsame CI- oder Entwicklungsrechner nicht durch normale Schwankungen auszusperren. Der gemessene Wert wird im Log ausgegeben und dient als vergleichbare Baseline, nicht als allgemeine Leistungszusage.

## Grosses physisches Testlager

Zweiter automatisierter Lauf auf derselben Entwicklungsmaschine:

- 1 Storage Controller
- 120 Rohrpostrohre in einem zehnstoeckigen Netz
- 240 Inventory Connectoren
- 240 echte Vanilla-Kisten
- 399.360 eingelagerte Items
- 24 unterschiedliche Itemtypen
- 5 vollstaendige Abfragen aller 24 Itemtypen
- 735 ms fuer alle 5 Abfragen
- rund 147 ms pro vollstaendiger 24-Typ-Abfrage
- Einlagerung und Entnahme nach der Messung erfolgreich

Die Messungen haben unterschiedliche Anzahlen von Abfragen und Itemtypen und sind daher nicht als einfacher direkter Geschwindigkeitsvergleich gedacht. Sie zeigen, dass der physische Zugriffspfad auch mit 240 Inventaren stabil und innerhalb der automatischen Abnahmeschranke bleibt.

## Einordnung

Diese Tests pruefen echte Blockinventare und den vollstaendigen Fabric-Transferpfad. Sie bilden noch nicht das bestehende Lager mit etwa sieben Millionen Items ab.

## Verbleibende Produktionspruefungen

1. Ein Test an einer Kopie des bestehenden Tom's-Lagers.
2. Messungen fuer Terminal-Snapshot, Suche, Einlagerung und Entnahme bei realer Chunk-Verteilung.
3. Eine Abnahme, bei der Bestandssummen vor und nach der Migration exakt uebereinstimmen.
