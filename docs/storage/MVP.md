# Lumungus Storage MVP

## Erste Bloecke

- Storage Controller: Zentrum eines Storage-Netzwerks. Er soll spaeter angeschlossene Inventare, Drives und Terminals finden und eine gemeinsame Netzwerk-ID verwalten.
- Crafting Terminal: Spieleroberflaeche fuer Suche, Einlagerung, Entnahme und manuelles Crafting aus Netzwerkbestaenden.

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
- Das Crafting Terminal oeffnet ein voll funktionsfaehiges 3x3-Crafting-Raster mit Spielerinventar.
- Installiertes JEI kann Rezepte ueber seinen Transfer-Button direkt in dieses 3x3-Raster legen.
- Crafting funktioniert auch ohne Controller lokal; die Netzwerkverbindung wird dem Spieler beim Oeffnen angezeigt.

### JEI-Kompatibilitaet

JEI bleibt eine optionale Mod und wird nicht in Lumungus Storage eingebaut oder vorausgesetzt. Das Crafting Terminal verwendet bewusst den Vanilla-Menue-Typ und dessen Slotreihenfolge. JEIs eigener Crafting-Transfer-Handler kann deshalb Rezepte aus der JEI-Ansicht in das Terminal uebertragen. Die aktuelle JEI-Fabric-Version ist nur in der lokalen Entwicklungsumgebung aktiv, damit diese Kompatibilitaet bei weiteren Arbeiten getestet werden kann. Auf einem dedizierten Multiplayer-Server muss JEI auch serverseitig installiert sein, damit dessen Rezepttransfer funktioniert.

Sobald das Terminal zusaetzliche Netzwerk-Slots erhaelt, wird ein eigener JEI-Transfer-Handler noetig. Dieser soll zuerst das Spielerinventar und danach den verbundenen Storage-Bestand verwenden; fehlende Zutaten werden weiterhin von JEI markiert.

## Naechster technischer Schritt

1. Storage-Provider-API-Anbindung ueber Lumungus Core.
2. Erste Storage Cell oder Drive Bay als echter Massenspeicher.
3. Netzwerkbestand im Crafting Terminal anzeigen und fuer Rezepte verwenden.

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
