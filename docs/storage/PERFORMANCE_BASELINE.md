# Lumungus Storage Performance Baseline

## Physisches Testlager

Stand: 2026-08-30, Minecraft/Fabric 26.2, lokaler automatisierter GameTest.

- 1 Storage Controller
- 12 Inventory Cables
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

## Einordnung

Dieser Test prueft echte Blockinventare und den vollstaendigen Fabric-Transferpfad. Er bildet noch nicht das bestehende Lager mit etwa sieben Millionen Items ab. Fuer die Produktionsfreigabe bleiben deshalb notwendig:

1. Ein groesserer synthetischer Test mit deutlich mehr Inventaren und Itemtypen.
2. Ein Test an einer Kopie des bestehenden Tom's-Lagers.
3. Messungen fuer Terminal-Snapshot, Suche, Einlagerung und Entnahme bei realer Chunk-Verteilung.
4. Eine Abnahme, bei der Bestandssummen vor und nach der Migration exakt uebereinstimmen.
