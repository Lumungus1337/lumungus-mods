# Lumungus Storage MVP

## Erste Bloecke

- Storage Controller: Zentrum eines Storage-Netzwerks. Er findet angeschlossene physische Inventare, optionale Drives und Terminals und verwaltet eine gemeinsame Netzwerk-ID.
- Crafting Terminal: Spieleroberflaeche fuer Suche, Einlagerung, Entnahme und manuelles Crafting aus Netzwerkbestaenden.
- Inventory Connector: Bindet angrenzende Kisten, Faesser, Shulkerboxen und kompatible Mod-Inventare ein, ohne deren Inhalte zu verschieben.
- Rohrpostrohr/Rohrpostblende: Verbindet Controller, Terminals und Connectoren ueber groessere Lageranlagen. Die aktuelle interne ID `inventory_cable` bleibt vorerst kompatibel, die sichtbare Sprache und Texturen gehen aber in Richtung Item-Rohrpost.
- Drive Bay und 16k Storage Cell: Bereits implementierter Prototyp, der als optionaler Massenspeicher erhalten bleiben kann, aber nicht mehr Voraussetzung fuer das Netzwerk ist.
- Wireless Storage Controller I/II/III: Geplante drahtlose Netzbruecken fuer kurze Distanz, gleiche Dimension und mehrere Dimensionen.
- Lager-Output: Soll Items aus dem Storage-Netzwerk in ein angrenzendes Inventar ausgeben.
- Lager-Breaker: Soll den Block unter sich abbauen und Drops ins Lager legen; ohne Filter jeder abbaubare Block, mit Filter nur der gesetzte Blocktyp.
- Lager-Placer: Soll einen konfigurierten Blocktyp aus dem Lager entnehmen und unter sich platzieren.
- Tom's Migration Assistant: Einmalige Integration, die vorhandene Tom's-Kabel, Trims und Connectoren durch Lumungus-Gegenstuecke ersetzt. Die angeschlossenen Inventare und ihre Items bleiben unberuehrt; danach kann Tom's entfernt werden.

## Stilrichtung

Die Storage-Bloecke sollen wie eine Mischung aus Item-Rohrpost und Computertechnik aus den 90er Jahren wirken:

- helle Metall- oder Kunststoffgehaeuse
- dunkle Frontplatten
- gruene Monitor-/Statusflaechen
- runde, farbige Rohrsegmente statt flacher Kabel
- sichtbare Anschlussstuecke an Controller, Terminals, Maschinen und Inventaren
- spaeter eigene Pixel-Texturen mit Tasten, LEDs, kleinen Lueftungsschlitzen und CRT-Anmutung

Der erste Slice nutzt bewusst Vanilla-Texturen, damit die IDs und Ressourcenstruktur sofort stabil sind. Eigene Texturen koennen nachgezogen werden, ohne die Registrierungslogik anzufassen.

## Aktueller technischer Stand

Seit dem ersten Entwicklungsschritt nach UAT.3 kann der Controller physische Inventare direkt verwenden. Storage Cells bleiben als optionaler Massenspeicher erhalten, sind fuer ein Netzwerk aber nicht mehr erforderlich.

- Controller und Crafting Terminal besitzen eigene, persistente Block-Entities.
- Jeder Controller verwaltet eine dauerhafte Netzwerk-ID.
- Controller und Terminals finden sich in einem begrenzten Radius von acht Bloecken; ein Terminal verbindet sich mit dem naechsten Controller.
- Drive Bays im selben Radius werden vom Controller zu einem gemeinsamen Netzwerkbestand zusammengefasst.
- Der erste native Inventory Connector bindet alle direkt angrenzenden Fabric-kompatiblen Item-Inventare ein. Damit funktionieren insbesondere Vanilla-Kisten ohne Umlagerung ihrer Inhalte.
- Der native Inventory Trim verbindet wie Tom's Trim das Netzwerk und bindet zugleich direkt angrenzende Inventare ein. Dadurch kann ein bestehender Aufbau spaeter blockweise konvertiert werden, ohne an jeder Kiste einen separaten Connector setzen zu muessen.
- Das erste native Rohrpostrohr verbindet Controller, Terminals, Drive Bays und Inventory Connectoren auch ausserhalb des Acht-Block-Radius. Die Suche besucht ausschliesslich bereits geladene Chunks und erzwingt kein Chunk-Laden.
- Zusammenhaengende Rohrkomponenten werden pro Welt zwischengespeichert. Platzieren oder Entfernen eines Netzwerkblocks invalidiert nur die direkt betroffenen Komponenten; Chunk-Laden und -Entladen leeren den Welt-Cache sicherheitshalber vollstaendig.
- Rohrpostnetze koennen geladene Chunk-Grenzen ueberqueren. Trennen und erneutes Verbinden aktualisiert die sichtbaren Bestaende, ohne Items aus den angeschlossenen Inventaren zu bewegen.
- Automatisierte Lasttests pruefen bis zu 240 echte Kisten mit 399.360 Items und 24 Itemtypen. Die reproduzierbaren Ausgangsmessungen stehen in [PERFORMANCE_BASELINE.md](PERFORMANCE_BASELINE.md).
- Mehrere Connectoren am selben Inventar werden dedupliziert; auch beide Haelften einer Doppeltruhe erhalten einen gemeinsamen Endpunkt-Schluessel.
- Lesen, Einlagern und Entnehmen laufen transaktional ueber Fabric Transfer API. Simulationen veraendern den Bestand nicht und Item-Komponenten wie eigene Namen bleiben erhalten.
- Gefuellte Storage Cells behalten ihren gesamten Inhalt beim Herausnehmen und Wiedereinsetzen.
- Befuellte Shulkerboxen werden beim aktiven Einlagern ueber Controller oder Terminal entladen; das Netzwerk speichert danach den Inhalt als normale Items und die Shulkerbox als leere Box.
- Das Crafting Terminal besitzt einen Entnahmemodus fuer befuellte Shulkerboxen: Es nimmt eine leere Shulkerbox aus dem Netzwerk und fuellt sie mit moeglichst vielen Exemplaren des angeklickten Items.
- Items koennen direkt an einer Drive Bay, per Schnellaktion am Terminal oder in der Terminaloberflaeche eingelagert werden.
- Ein leerer Rechtsklick entnimmt an der Drive Bay einen Stapel. Schleichen und Rechtsklick nimmt die Storage Cell heraus.
- Ein normaler Rechtsklick oeffnet das Crafting Terminal. Schleichen und Rechtsklick lagert einen gehaltenen Stapel schnell ein oder entnimmt mit leerer Hand einen Netzwerkstapel.
- Das eigene, synchronisierte Terminalmenue zeigt 36 Itemtypen pro Seite und bietet Suche, Namens-/Mengensortierung, Seitenwechsel, Tooltips sowie genaue Bestands- und Kapazitaetsanzeigen.
- Links-, Rechts- und Shift-Klick entnehmen Stapel, Einzelitems oder direkt ins Spielerinventar; der `IN`-Bereich nimmt Cursorstapel auf.
- Das Terminal enthaelt ein voll funktionsfaehiges 3x3-Crafting-Raster und verwendet Zutaten aus Spielerinventar und Netzwerk.
- Alle Netzwerktransaktionen und Rezeptentscheidungen werden serverseitig validiert und anschliessend an den Client synchronisiert.
- Installiertes JEI kann Rezepte ueber den eigenen Lumungus-Transfer-Handler in das Raster legen. Der Server ermittelt das Rezept erneut und vertraut keinen vom Client gelieferten Zutatenmengen.
- Netzwerk, Cell-Inhalte und Bestandsmengen bleiben nach Speichern und Neustart erhalten.
- `lumungus-integration` besitzt nun einen strikt nur lesenden Snapshot- und Vergleichskern fuer den Tom's-Uebergang. Er vergleicht Endpunkte, Slots und jede Itemvariante inklusive Komponenten und wurde unter anderem mit 7.000.000 Items getestet.
- Der Tom's-Weltscanner erfasst geladene, physisch verbundene Netzwerkbloecke mit Sicherheitslimit und sammelt angrenzende Lagerinventare dedupliziert in einen Read-only-Snapshot. Unbekannte Bloecke, ungeladene Grenzen oder Teilscans verhindern eine Freigabe.
- Der Befehl `/lumungus migration scan <x> <y> <z>` gibt den Dry-Run-Bericht im Spiel aus. Die optionale, nicht eingebettete Tom's-`26.2-2.11.3`-Integration unterscheidet normale Kabel-Connectoren von echten Fernkanaelen und fuehrt geladene entfernte Teilnetze dimensionssicher im Nur-Lese-Bericht zusammen.

### JEI-Kompatibilitaet

JEI bleibt eine optionale Client-Mod und wird von Lumungus Storage nur zur Compile-Zeit angebunden. Der eigene Fabric-Entrypoint `jei_mod_plugin` registriert den Transfer-Handler nur, wenn JEI vorhanden ist. Der Client sendet dabei ausschliesslich Rezept-ID und Mengenmodus; Auswahl, Verfuegbarkeit und Entnahme der Zutaten bleiben Aufgabe des Servers.

## Naechster technischer Schritt

1. Den gemeinsamen Fernnetz-Dry-Run um einen UAT-Bericht je Dimension und Teilnetz erweitern.
2. Echtes Chunk-Unload/-Reload mit einem persistierten Testnetz absichern und die weltweite Chunk-Invalidierung anschliessend auf betroffene Chunks beziehungsweise Komponenten verfeinern.
3. Nach erfolgreichem Dry Run die protokollierte Konvertierung von Tom's-Kabeln, Trims und Connectoren in Lumungus-Rohrpostbloecke mit Journal und Rollback implementieren. Kein Lageritem wird dabei umgelagert.
4. Einen grossen Bestands- und Performance-UAT sowie einen echten Test an einer Sicherung des bestehenden Lagers durchfuehren. Abnahmekriterium ist, dass die Welt anschliessend ohne Tom's denselben Bestand ueber Lumungus anzeigt.
5. Erst danach Produktionsauftraege, Autocrafter, Autosteinsaege, Auto-Braustand und Schematic-Logistik auf dem physischen Netzwerk aufbauen.

Die verbindliche Entscheidung und die Migrationsregeln stehen in [PHYSICAL_INVENTORY_NETWORK.md](PHYSICAL_INVENTORY_NETWORK.md).
Der detaillierte read-only Vergleichsablauf steht in [TOMS_MIGRATION.md](TOMS_MIGRATION.md).

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
