# Lumungus Storage MVP

## Erste Bloecke

- Storage Controller: Zentrum eines Storage-Netzwerks. Er soll spaeter angeschlossene Inventare, Drives und Terminals finden und eine gemeinsame Netzwerk-ID verwalten.
- Crafting Terminal: Spieleroberflaeche fuer Suche, Einlagerung, Entnahme und manuelles Crafting aus Netzwerkbestaenden.
- Drive Bay: Nimmt eine herausnehmbare 16k Storage Cell auf und stellt deren Inhalt dem Netzwerk bereit.
- 16k Storage Cell: Speichert insgesamt 16.384 Items aus maximal 64 unterschiedlichen Itemtypen inklusive ihrer Komponenten.

## Stilrichtung

Die Storage-Bloecke sollen wie Computertechnik aus den 90er Jahren wirken:

- helle Metall- oder Kunststoffgehaeuse
- dunkle Frontplatten
- gruene Monitor-/Statusflaechen
- spaeter eigene Pixel-Texturen mit Tasten, LEDs, kleinen Lueftungsschlitzen und CRT-Anmutung

Der erste Slice nutzt bewusst Vanilla-Texturen, damit die IDs und Ressourcenstruktur sofort stabil sind. Eigene Texturen koennen nachgezogen werden, ohne die Registrierungslogik anzufassen.

## Aktueller technischer Stand

- Controller und Crafting Terminal besitzen eigene, persistente Block-Entities.
- Jeder Controller verwaltet eine dauerhafte Netzwerk-ID.
- Controller und Terminals finden sich in einem begrenzten Radius von acht Bloecken; ein Terminal verbindet sich mit dem naechsten Controller.
- Drive Bays im selben Radius werden vom Controller zu einem gemeinsamen Netzwerkbestand zusammengefasst.
- Gefuellte Storage Cells behalten ihren gesamten Inhalt beim Herausnehmen und Wiedereinsetzen.
- Items koennen direkt an einer Drive Bay, per Schnellaktion am Terminal oder in der Terminaloberflaeche eingelagert werden.
- Ein leerer Rechtsklick entnimmt an der Drive Bay einen Stapel. Schleichen und Rechtsklick nimmt die Storage Cell heraus.
- Ein normaler Rechtsklick oeffnet das Crafting Terminal. Schleichen und Rechtsklick lagert einen gehaltenen Stapel schnell ein oder entnimmt mit leerer Hand einen Netzwerkstapel.
- Das eigene, synchronisierte Terminalmenue zeigt 36 Itemtypen pro Seite und bietet Suche, Namens-/Mengensortierung, Seitenwechsel, Tooltips sowie genaue Bestands- und Kapazitaetsanzeigen.
- Links-, Rechts- und Shift-Klick entnehmen Stapel, Einzelitems oder direkt ins Spielerinventar; der `IN`-Bereich nimmt Cursorstapel auf.
- Das Terminal enthaelt ein voll funktionsfaehiges 3x3-Crafting-Raster und verwendet Zutaten aus Spielerinventar und Netzwerk.
- Alle Netzwerktransaktionen und Rezeptentscheidungen werden serverseitig validiert und anschliessend an den Client synchronisiert.
- Installiertes JEI kann Rezepte ueber den eigenen Lumungus-Transfer-Handler in das Raster legen. Der Server ermittelt das Rezept erneut und vertraut keinen vom Client gelieferten Zutatenmengen.
- Netzwerk, Cell-Inhalte und Bestandsmengen bleiben nach Speichern und Neustart erhalten.

### JEI-Kompatibilitaet

JEI bleibt eine optionale Client-Mod und wird von Lumungus Storage nur zur Compile-Zeit angebunden. Der eigene Fabric-Entrypoint `jei_mod_plugin` registriert den Transfer-Handler nur, wenn JEI vorhanden ist. Der Client sendet dabei ausschliesslich Rezept-ID und Mengenmodus; Auswahl, Verfuegbarkeit und Entnahme der Zutaten bleiben Aufgabe des Servers.

## Naechster technischer Schritt

1. Den Release Candidate `0.1.0-uat.1` anhand von [UAT.md](UAT.md) mit einem zweiten Spieler und den dokumentierten Grenzfaellen abnehmen.
2. UAT-Abweichungen beheben und den ersten freigegebenen Storage-Build erstellen.
3. Danach Produktionsauftraege, Autocrafter, Autosteinsaege und Auto-Braustand als naechsten vertikalen Slice planen.

## Geplante Build Logistics

Dieses Feature gehoert zur spaeteren Roadmap und wird im aktuellen Slice noch nicht implementiert:

- Eine Schematic-Datei einlesen und daraus automatisch die vollstaendige Materialliste ermitteln.
- Benoetigte Mengen mit dem aktuellen Bestand des Storage-Netzwerks abgleichen.
- Vorhandene Baumaterialien automatisch auf eine oder mehrere Shulkerboxen verteilen und passend beschriften.
- Fehlende Materialien getrennt ausweisen, damit sofort klar ist, was noch gefarmt oder hergestellt werden muss.
- Fortschritt mit den Zustaenden `benoetigt`, `vorhanden`, `verpackt` und `fehlend` verfolgen.
- Fehlende herstellbare Materialien als mengenbasierte Produktionsauftraege an Autocrafter, Autosteinsaege oder Auto-Braustand uebergeben.
- Ein eigenes Lumungus Build Clipboard als tragbare Checkliste anbieten, funktional von Creates Clipboard inspiriert und optisch im 90er-Computerstil der Modreihe gestaltet.
- Die Schematic-Unterstuetzung ueber Format-Adapter kapseln, damit spaeter mehrere gaengige Formate angebunden werden koennen.
