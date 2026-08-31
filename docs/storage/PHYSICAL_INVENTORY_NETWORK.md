# Physisches Inventarnetzwerk und Tom's-Uebergang

Status: verbindliche Architekturentscheidung

## Nutzerziel

Ein bestehendes Lager mit Millionen Items in vielen Kisten und Faessern darf fuer Lumungus Storage nicht umgepackt werden. Die physischen Inventare bleiben an ihrem Platz und behalten ihre Daten. Der Lumungus Controller indexiert und bedient diese Inventare ueber das Netzwerk.

Fuer ein vorhandenes Tom's-Simple-Storage-Lager gilt als Zielablauf:

1. Bestehende Tom's-Kabel, Trims, Connectoren, Kisten und Faesser bleiben unveraendert.
2. Lumungus Core, Storage und Integration werden installiert.
3. Der Migrationsassistent liest das vorhandene Tom's-Netz und vergleicht Inventaranzahl, Slots, Itemtypen und Gesamtmenge, ohne etwas zu veraendern.
4. Nach ausdruecklichem Start ersetzt er ausschliesslich Tom's-Controller, Connectoren, Kabel, Trims und Terminals durch passende Lumungus-Rohrpostbloecke. Kisten, Faesser, Shulkerboxen und deren Inhalte werden nicht angefasst.
5. Lumungus liest das konvertierte Netz erneut ein und vergleicht es mit dem vorherigen Snapshot.
6. Erst bei erfolgreicher Validierung wird die Migration als abgeschlossen markiert. Danach wird gespeichert, neu gestartet und Tom's entfernt.

## Zielarchitektur

```text
Crafting Terminal
       |
Lumungus Controller
       |
       +-- Lumungus Rohrpostrohr/-blende -- Inventaranschluss -- Kisten/Faesser
       |
       +-- einmalig konvertiertes ehemaliges Tom's-Kabelnetz als Lumungus-Rohrpost
       |
       `-- optionale Drive Bay / Storage Cell
```

Der Controller arbeitet ausschliesslich gegen allgemeine Storage-Endpunkte. Ein Endpunkt kann ein physisches Inventar oder eine optionale Cell sein. Storage kennt keine konkreten Tom's-Klassen. Nur der einmalige Migrationsassistent in `lumungus-integration` kennt die unterstuetzten Tom's-Netzwerkbloecke; nach der Konvertierung besteht keine Laufzeitabhaengigkeit mehr zu Tom's.

## Verbindliche Regeln

- Keine automatische Verschiebung oder Konvertierung vorhandener Items.
- Tom's wird nur waehrend der Migration parallel benoetigt und kann danach vollstaendig deinstalliert werden.
- Der Assistent konvertiert Netzwerkbloecke, niemals angeschlossene Lagerinventare.
- Vor jeder schreibenden Konvertierung wird ein Migrationsjournal mit Position, altem Blockzustand, Block-Entity-Daten und geplantem Lumungus-Ersatz gespeichert.
- Ein abgebrochener Lauf muss anhand des Journals fortgesetzt oder zurueckgerollt werden koennen, solange Tom's noch installiert ist.
- Kisten, Faesser und andere Blockinventare sind primaere Speicher, Cells nur optionale Erweiterungen.
- Doppeltruhen und andere mehrteilige Inventare duerfen nur einmal indexiert werden.
- Derselbe physische Endpunkt darf auch ueber mehrere Rohrwege oder Connectoren nur einmal erscheinen.
- Einlagerung versucht zuerst vorhandene passende Stapel und danach freie Slots.
- Extraktion, Crafting und JEI-Transfer bleiben serverautorisiert.
- Nicht vollstaendig ausgefuehrte Transfers muessen den Rest zurueckgeben; Verlust und Duplikation sind unzulaessig.
- Ungefundene oder entladene Chunks werden nicht automatisch geladen. Ihr Bestand wird als voruebergehend nicht erreichbar behandelt.
- Blockabbau, Chunk-Unload und Topologieaenderungen invalidieren den Index kontrolliert.
- Das Oeffnen eines Terminals darf nicht bei jedem Tick Millionen ItemStacks neu scannen.
- Neue Lumungus-Verbindungsbloecke verwenden nach aussen die Rohrpost-Sprache. Alte interne IDs duerfen nur aus Kompatibilitaetsgruenden bestehen bleiben.

## Skalierung fuer grosse Lager

Das Netzwerk verwendet einen Topologie-Cache und getrennte Bestandsindizes. Connectoren melden Aenderungen; als Fallback werden Inventare zeitlich gestaffelt neu geprueft. Vollscans laufen nur beim ersten Verbinden, nach einer Topologieaenderung oder auf ausdrueckliche Diagnose.

Der Bestandsindex aggregiert nach Item und Komponenten, speichert aber weiterhin den physischen Quellendpunkt fuer eine spaetere Extraktion. Eine Anzeige von sieben Millionen Items bedeutet deshalb nicht, dass sieben Millionen einzelne Objekte im Controller gespeichert oder synchronisiert werden.

## Umsetzungsslices

### Slice 1: Allgemeine Endpunkte

- Core-Vertrag fuer Discovery und Storage-Endpunkte
- Adapter fuer Minecraft-Blockinventare
- atomare Simulation und Ausfuehrung
- Tests fuer Komponenten, volle Inventare, Reste und Doppeltruhen

### Slice 2: Natives Lumungus-Netz

- Inventory Connector
- Rohrpostrohre und verkleidbare Rohrpostblenden
- Topologie-Cache und Deduplizierung
- Controller und Terminal verwenden physische Endpunkte gemeinsam mit optionalen Drive Bays

### Slice 3: Tom's-Uebergang

- temporaere Compile-/Runtime-Integration in `lumungus-integration`
- read-only Snapshot und komponentensicherer Bestandsvergleich (Vergleichskern implementiert)
- Dry Run mit Liste aller erkannten, konvertierbaren und nicht unterstuetzten Netzwerkbloecke
- protokollierte Ersetzung von Tom's Connectoren, Kabeln, Trims und Terminals durch Lumungus-Rohrpostbloecke
- Wiederaufnahme und Rollback ueber ein persistentes Migrationsjournal
- Bestands- und Topologievergleich vor und nach der Konvertierung
- Kompatibilitaetstest gegen die konkret eingesetzte Tom's-Version

Der genaue Sicherheitsablauf und der aktuelle Stand sind in [TOMS_MIGRATION.md](TOMS_MIGRATION.md) dokumentiert.

### Slice 4: Abnahme

- Kopie der Welt sichern
- Read-only-Bestandsvergleich zwischen Tom's und Lumungus
- Netzwerkbloecke in der Weltkopie konvertieren, ohne Lagerinventare anzufassen
- Einlagern und Entnehmen mit kleinen Testmengen ueber Lumungus
- Neustart-, Chunk-Unload- und Zwei-Spieler-Test
- Lasttest mit einer Topologie in der Groessenordnung des echten Lagers
- Tom's aus der Weltkopie entfernen und erneut pruefen, dass Lumungus denselben Bestand anzeigt

Erst nach dieser Abnahme wird Lumungus Storage als einziges Storage-Mod fuer das bestehende Lager freigegeben.
