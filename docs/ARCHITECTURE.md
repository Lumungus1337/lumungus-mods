# Architekturkonzept

## Leitidee

Lumungus soll keine einzelne grosse Mod werden, sondern eine Mod-Familie mit gemeinsamer Basisschicht. Core stellt wiederverwendbare Systeme bereit; Gameplay-Module koennen unabhaengig installiert und spaeter auch unabhaengig erweitert werden.

## Abhaengigkeitsrichtung

```text
lumungus-integration
       |
       +-- lumungus-storage
       +-- lumungus-backpack
       +-- lumungus-machines
       +-- lumungus-autotrader
                |
                v
        lumungus-core
```

Core bleibt unten. Kein Code in Core darf konkrete Klassen aus Storage, Backpack, Machines oder Integration referenzieren.

## Geplante Core-Bereiche

- `api.inventory`: neutrale Inventar- und Item-Transfer-Vertraege
- `api.filter`: Filterregeln, Matching und Serialisierung
- `api.upgrade`: Upgrade-Typen, Slots und Validierung
- `api.network`: Netzwerk-IDs, Persistenz und Synchronisation
- `api.gui`: gemeinsame Screen-/Widget-Bausteine
- `config`: gemeinsame Konfigurationsmuster

## Erster Core-Slice

Der erste Code-Slice legt absichtlich nur kleine, stabile API-Bausteine an:

- `ItemTransferView`, `ItemTransferTarget` und `ItemTransferAccess` als gemeinsame Grundlage fuer Storage, Backpack und Maschinen.
- `TransferMode`, damit Simulation und echte Ausfuehrung von Anfang an getrennt sind.
- `ResourceAmount`, um grosse Item-Mengen jenseits normaler Stack-Groessen sauber zu modellieren.
- `ItemFilter` und `ConfiguredItemFilter` als Basis fuer Allow-/Deny-Listen.
- `UpgradeType`, `UpgradeSlot` und `Upgradeable` als gemeinsames Upgrade-Modell.

Noch nicht enthalten sind Block-Registrierung, GUI-Code, Netzwerkpakete oder Datenpersistenz. Diese folgen erst, sobald klar ist, welches konkrete Modul sie zuerst braucht.

## Storage-Vision

Lumungus Storage soll Komfort aus AE2-artigen Systemen mit einer einfachen Bedienung verbinden:

- Storage Controller als Netzwerkzentrum
- Storage-Terminal
- Crafting Terminal mit 3x3-Crafting-Funktion
- Drives und Cells
- Import-/Export-Komponenten
- Such- und Sortierfunktionen
- Request-Mengen
- Schematic-Materialplanung mit automatischer Shulkerbox-Bestueckung und Build Clipboard
- spaeter Autocrafting und Stock-Keeper

Der erste spielbare Storage-Slice registriert `storage_controller` und `crafting_terminal` als echte Bloecke, Items und Block-Entities. Controller besitzen eine persistente Netzwerk-ID und verbinden Terminals in begrenzter Reichweite. Das Crafting Terminal bietet bereits ein voll funktionsfaehiges 3x3-Crafting-Raster; Netzwerk-Inventare und Massenspeicher folgen im naechsten Slice. Die visuelle Richtung ist 90er-Computertechnik: helle Gehaeuse, dunkle Fronten, gruene Monitorflaechen.

## Backpack-Vision

Lumungus Backpack startet als erweiterbarer Rucksack:

- Upgrade-Slots
- Filter
- Sortierung
- kompatible Core-Inventar-API
- spaeter Jetpack-Upgrade fuer den Rucksack

## Machines-Vision

Lumungus Machines nimmt spaeter Automationsbloecke auf. Als erste Produktionsfamilie sind ein mengenbasierter Autocrafter, eine Autosteinsaege und ein Auto-Braustand geplant. Sie erhalten Produktionsauftraege mit Zielgegenstand und Zielmenge, loesen benoetigte Zwischenrezepte auf und melden fehlende Rohstoffe an Storage und Build Clipboard zurueck.

Die gemeinsamen Auftrags- und Rezeptanbieter-Vertraege liegen in Core. Dadurch kann eine Schematic beispielsweise 120 Faesser anfordern, ohne konkrete Maschinenklassen zu kennen. Autocrafter koennen daraus die Kette Holzstaemme, Bretter, Holzstufen und Faesser planen. Details stehen in [machines/MVP.md](machines/MVP.md).

RailQuarry wird hier weiterhin als geplantes zukuenftiges Feature dokumentiert, aber im Initialstand nicht migriert.

## Autotrader-Vision

Lumungus Autotrader wird als eigenes Modul gefuehrt. Es soll automatisierte Handelsablaeufe, Trading-Terminals und spaetere Storage-Anbindung aufnehmen. Eine bestehende Autotrader-Implementierung wird im Initialstand noch nicht migriert; zuerst wird das Modul als Teil der Lumungus-Architektur dokumentiert und buildbar gemacht.

Optisch gehoert Autotrader in dieselbe Linie wie Storage, Machines und RailQuarry: 90er-Computertechnik, helle Gehaeuse, dunkle Frontplatten, gruene Monitore, Status-LEDs und eine klare Werkstatt-/Terminal-Anmutung.

## Integration-Vision

Lumungus Integration verbindet eigene Module miteinander und kann spaeter optionale Kompatibilitaet zu externen Mods aufnehmen. Dieses Modul soll keine Pflichtabhaengigkeit fuer einzelne Gameplay-Module werden.
