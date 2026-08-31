# Tom's-Migration UAT Results 0.1.0-uat.5

Gesamtstatus: `TECHNICAL_PASS_REVIEW_REQUIRED`

## Ziel dieses Kandidaten

`0.1.0-uat.5` erweitert den Nur-Lese-Dry-Run um Tom's
`basic_inventory_hopper`. Diese Bloecke werden als bekannte
Nur-Lese-Netzkomponenten akzeptiert und beim Bestandssnapshot
beruecksichtigt. Sie erhalten noch kein schreibendes Konvertierungsziel.

## Automatisierte Abnahme

- Build mit Java Temurin `25.0.4.1`: `PASS`
- Lumungus Core, Storage und Integration als getrennte JARs: `PASS`
- Storage-Logiktests: `PASS`
- Storage-GameTests: 13 von 13 `PASS`
- Tom's-Integration-GameTests mit Fabric `26.2-2.11.3`: 3 von 3 `PASS`
- Tom's `basic_inventory_hopper` blockiert den Nur-Lese-Snapshot nicht mehr: `PASS`
- Tom's-native Connector-Inventarliste erfasst verkettete Lagerinventare: `PASS`
- UAT-Paket wurde nach bestandenen Tests erzeugt: `PASS`

## Paket

- Datei: `build/uat/lumungus-toms-migration-0.1.0-uat.5.zip`
- Inhalt: Lumungus Core, Storage, Integration und die Migrations-UAT-Dokumente
- Tom's Simple Storage und Fabric API werden nicht eingebettet

## Weltkopie-UAT

Ausgefuehrt am `2026-08-31` an der Weltkopie
`A New Hope - Lumungus UAT 0.1.0-uat.4`, interner Weltenname
`UAT Lumungus - A New Hope`.

- Startposition: `minecraft:overworld -216, 48, 577`
- Spielerposition beim Laden: ungefaehr `-215.4, 50.0, 575.7`
- Geladene Lumungus-Versionen: `0.1.0-uat.5`
- Tom's Simple Storage: `2.11.3`
- Scan 1: `2026-08-31 11:34:34 Europe/Berlin`
- Scan 2: `2026-08-31 11:36:43 Europe/Berlin`
- Scan 3: `2026-08-31 11:51:15 Europe/Berlin` mit final installierten
  `0.1.0-uat.5`-JARs

Alle drei Scans meldeten identische Werte:

- Netzwerk: `443` Tom's-Netzwerkbloecke
- Konvertierbar: `401`
- Nur lesend unterstuetzt: `42`
- Blockierend: `0`
- Teilnetze: `1`, `minecraft:overworld`
- Bestand: `3047` Inventare, `162324` Slots, `4003223` Items, `593` Itemtypen
- Abschlussmeldung: Nur-Lese-Dry-Run abgeschlossen; keine Bloecke oder Items veraendert

## Vergleichszahl aus Weltdateien

Zur Plausibilisierung wurden die fuenf Lagerregionen um den Controller
offline und nur lesend auf gespeicherte Inventar-Blockentities gezaehlt. Diese
Regionen enthalten `4044570` Items in `2836` nichtleeren Inventar-Blockentities.
Der Lumungus-Snapshot liegt damit etwa `41347` Items darunter. Diese Differenz
ist plausibel, weil die Offline-Zahl auch Maschinen, Hopper, Dropper,
Dispenser, Shulkerbox-Bloecke und nicht zwingend ans Tom's-Netz angebundene
Inventare in denselben Regionen enthaelt.

## UAT-Checkliste

- `UAT-M01`: `PASS`
- `UAT-M02`: `PASS`
- `UAT-M03`: `PASS`
- `UAT-M04`: `PASS`
- `UAT-M05`: `PASS`
- `UAT-M06`: `PASS`
- `UAT-M07`: `REVIEW` - gemessener Bestand `4003223`; urspruengliche Erwartung war ungefaehr `7000000`
- `UAT-M08`: `PASS`
- `UAT-M09`: `PENDING_MANUAL` - Stichprobe an Kisten/Faessern durch Spieler noch offen
- `UAT-M10`: `PASS`

## Entscheidung

Der Nur-Lese-Pfad ist technisch stabil und wiederholbar. Eine schreibende
Konvertierung bleibt gesperrt, bis `UAT-M07` fachlich bestaetigt ist oder ein
weiterer Tom's-Startpunkt fuer einen zusaetzlichen Lagerbereich gefunden und
ebenfalls gescannt wurde.
