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

## Storage-Vision

Lumungus Storage soll Komfort aus AE2-artigen Systemen mit einer einfachen Bedienung verbinden:

- Storage-Terminal
- Drives und Cells
- Import-/Export-Komponenten
- Such- und Sortierfunktionen
- Request-Mengen
- spaeter Autocrafting und Stock-Keeper

## Backpack-Vision

Lumungus Backpack startet als erweiterbarer Rucksack:

- Upgrade-Slots
- Filter
- Sortierung
- kompatible Core-Inventar-API
- spaeter Jetpack-Upgrade fuer den Rucksack

## Machines-Vision

Lumungus Machines nimmt spaeter Automationsbloecke auf. RailQuarry wird hier als geplantes zukuenftiges Feature dokumentiert, aber im Initialstand nicht migriert.

## Integration-Vision

Lumungus Integration verbindet eigene Module miteinander und kann spaeter optionale Kompatibilitaet zu externen Mods aufnehmen. Dieses Modul soll keine Pflichtabhaengigkeit fuer einzelne Gameplay-Module werden.
