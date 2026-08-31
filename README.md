# Lumungus Mods

Lumungus Mods ist als modulare Fabric-Modreihe fuer Minecraft 26.2 geplant. Das Repository ist als Monorepo aufgebaut: Gemeinsame APIs und Komponenten leben in Lumungus Core, die eigentlichen Gameplay-Module bauen darauf auf und koennen spaeter getrennt als JARs gebaut, getestet und veroeffentlicht werden.

## Architektur

```text
lumungus-mods
|-- modules
|   |-- lumungus-core
|   |-- lumungus-storage
|   |-- lumungus-backpack
|   |-- lumungus-machines
|   |-- lumungus-autotrader
|   `-- lumungus-integration
`-- docs
```

Lumungus Core ist die gemeinsame Basisschicht. Core soll moeglichst wenig direkten Gameplay-Content enthalten und stattdessen wiederverwendbare Infrastruktur bereitstellen:

- Inventar- und Item-Transfer-APIs
- Filter- und Upgrade-Systeme
- Netzwerk- und Persistenz-Helfer
- gemeinsame GUI-Bausteine
- Such- und Mengenparser
- Server/Client-Synchronisation
- gemeinsame Konfiguration und Datenmodell-Konventionen

Die Gameplay-Module haengen von Core ab, aber Core soll nicht von den Gameplay-Modulen wissen. So bleiben Updates und getrennte Releases beherrschbar.

## Module

| Modul | Rolle | Status |
|---|---|---|
| `lumungus-core` | Gemeinsame APIs, Basistypen und technische Infrastruktur | angelegt |
| `lumungus-storage` | Physisches Kisten-/Fass-Netzwerk, Rohrpostverbindungen, Terminals, optionale Cells, Import/Export und spaeter Autocrafting | `0.1.0-uat.24` UAT Candidate; gerichtete Output-/Breaker-/Placer-Arbeitsseiten |
| `lumungus-backpack` | Modularer Rucksack mit Upgrade-Slots und spaeter Jetpack-Upgrade | angelegt |
| `lumungus-machines` | Maschinen- und Automationsmodule | angelegt |
| `lumungus-autotrader` | Automatisierte Handelsablaeufe, Trading-Terminals und spaetere Storage-Anbindung | angelegt |
| `lumungus-integration` | Cross-Mod-Integration zwischen Lumungus-Modulen und optional externen Mods | Tom's-2.11.3-Nur-Lese-Scanner fuer den Migrations-UAT |

RailQuarry wird noch nicht migriert. Es ist als zukuenftiges Modul oder Feature innerhalb `lumungus-machines` dokumentiert, sobald die Core-APIs stabil genug sind.

Autotrader wird ebenfalls noch nicht migriert, ist aber als eigenes Modul im Monorepo angelegt. Es soll optisch und spielerisch zur Lumungus-Linie passen: 90er-Computertechnik, klare Terminals, dunkle Frontplatten, gruene Statusanzeigen und sichtbare Rohrpost-Anbindung.

Lumungus Storage verlangt keine Migration vorhandener Lagerbestaende in digitale Cells. Bestehende Kisten, Faesser und kompatible Mod-Inventare bleiben die primaeren Speicherorte. Befuellte Shulkerboxen werden beim aktiven Einlagern entladen; im Lager liegen danach nur einzelne Items und leere Shulkerboxen. Fuer bestehende Tom's-Simple-Storage-Anlagen ist ein einmaliger Migrationsassistent in `lumungus-integration` vorgesehen: Er uebernimmt das vorhandene Kabel-/Connector-Netz als Lumungus-Rohrpostnetz, ohne Items umzupacken. Nach erfolgreicher Validierung kann Tom's vollstaendig entfernt werden.

Die gemeinsame Stilrichtung steht in [docs/VISUAL_IDENTITY.md](docs/VISUAL_IDENTITY.md): Lumungus nutzt sichtbare Item-Rohrpost, 90er-Computertechnik und eigene, leicht arcadehafte Werkstattoptik statt glatter Sci-Fi-Kabel.

## Build-Strategie

Jedes Modul ist ein eigenes Gradle-Subprojekt mit eigener `fabric.mod.json`. Dadurch kann jedes Modul eine eigene installierbare JAR erzeugen:

```powershell
./gradlew :lumungus-core:build
./gradlew :lumungus-storage:build
./gradlew build
./gradlew storageUatBundle
./gradlew tomsMigrationUatBundle
```

`storageUatBundle` erzeugt unter `build/uat/` ein Testpaket mit den getrennten
Core- und Storage-JARs sowie der UAT-Checkliste.

`tomsMigrationUatBundle` erzeugt ein separates, schreibgeschuetztes Testpaket
mit Core, Storage und Integration. Es enthaelt Tom's Simple Storage bewusst
nicht; fuer den Test wird die bereits installierte Fabric-Version `26.2-2.11.3`
verwendet.

Die Module erzeugen getrennte installierbare JARs. Runtime-Abhaengigkeiten werden in den jeweiligen `fabric.mod.json`-Dateien deklariert. Konkrete Java-Abhaengigkeiten zwischen den Modulen werden erst aktiviert, sobald ein Modul Core-API-Typen wirklich importiert; fuer 26.x muss diese Stelle mit Looms aktueller Multi-Project-Empfehlung gegengeprueft werden.

Quellen-JARs sind im Initialstand deaktiviert, weil die 26.2-Identitaets-Mappings kein klassisches `named`-Namespace-Remapping fuer `remapSourcesJar` bereitstellen.

Fuer Minecraft 26.x wird keine Mapping-Abhaengigkeit mehr eingetragen. Die 26.x-Linie setzt auf offizielle, nicht verschleierte Namen; die Fabric-Werte in `gradle.properties` sollten trotzdem regelmaessig gegen die offizielle Fabric-Develop-Seite geprueft werden.

## Versionierung und Releases

Vorgeschlagene Strategie:

- Gemeinsame Versionslinie fuer die Modreihe: `0.1.0`, `0.2.0`, `1.0.0`.
- Snapshot-Versionen waehrend Entwicklung: `0.1.0-SNAPSHOT`.
- UAT-Kandidaten vor einer Freigabe: `0.1.0-uat.N`.
- Tags pro Gesamtstand: `v0.1.0`.
- Optional zusaetzliche Modul-Tags, falls Releases auseinanderlaufen: `core-v0.1.0`, `storage-v0.1.0`.
- Breaking Changes in Core erhoehen mindestens die Minor-Version, nach `1.0.0` die Major-Version.
- Jedes Release sollte getrennte Artefakte fuer installierbare Module enthalten.

## Lizenz

Noch nicht final entschieden. Siehe [docs/LICENSE_DECISION.md](docs/LICENSE_DECISION.md).

Meine Empfehlung fuer die aktuelle Zielrichtung: MIT oder LGPL-3.0. MIT ist maximal einfach fuer Addons und Modpacks; LGPL-3.0 schuetzt gemeinsame Bibliotheksverbesserungen etwas staerker. Wenn Assets, Texturen und Modelle spaeter dazukommen, sollten Code- und Asset-Lizenz getrennt betrachtet werden.

## Entwicklungsnotizen

- Java-Ziel: 25
- Mod Loader: Fabric
- Minecraft-Ziel: 26.2
- Paketwurzel: `dev.lumungus`
- Mod-IDs: `lumungus_core`, `lumungus_storage`, `lumungus_backpack`, `lumungus_machines`, `lumungus_autotrader`, `lumungus_integration`
