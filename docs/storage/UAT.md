# Lumungus Storage Terminal UAT

Diese Checkliste definiert die Abnahmekriterien fuer den Release Candidate. Ein Kriterium gilt nur dann als bestanden, wenn das erwartete Ergebnis reproduzierbar eintritt.

## Release Candidate

- Version: `0.1.0-uat.34`
- Minecraft: `26.2`
- Fabric Loader: `0.19.5`
- Fabric API: `0.158.0+26.2`
- Optional fuer Rezepttransfer: JEI `30.28.0.193`
- Build: `./gradlew clean build storageUatBundle`
- Testpaket: `build/uat/lumungus-storage-0.1.0-uat.34.zip`
- Abnahmestatus: `AUTOMATED_CLIENT_AND_SERVER_PASS`; Arbeitszyklen, gerichtete Arbeitsseiten, Redstone-Pause, Breaker-Schutz, Tooltips, tragbare Storage Interfaces, Wireless Inventory Connectoren, Wireless-Reichweiten jenseits der Kurzdistanz, Terminal-Suchfeld-Hotkeys und Rohrpost-Sackgassen sind automatisiert geprueft, manueller Interaktions- und Multiplayer-UAT sind weiter offen

Das Testpaket enthaelt `lumungus-core` und `lumungus-storage` als getrennte JARs im Ordner `mods/` sowie diese Checkliste. Fabric API und optional JEI werden separat installiert.

## Entwickler-Preflight

Am 30.08.2026 wurde vor UAT ein lokaler Creative-Smoke-Test durchgefuehrt. Dieser Preflight ersetzt nicht die untenstehende Benutzerabnahme.

| Bereich | Ergebnis | Geprueft |
|---|---|---|
| Start und Abhaengigkeiten | `PASS` | Minecraft 26.2 startet mit Core, Storage, Fabric API und JEI. |
| Persistenz | `PASS` | Testnetz, Cell-Inhalt und Mengen bleiben nach Speichern und Client-Neustart erhalten. |
| Terminal | `PASS` | Oeffnen, Suche, Sortierung, Cursor-Einlagerung und Stapelentnahme funktionieren. |
| Netzwerk-Crafting | `PASS` | Ein Rezept nutzt einen Stamm aus dem Netzwerk und erzeugt vier Bretter. |
| JEI-Transfer | `PASS` | Der Lumungus-Transferknopf befuellt das 3x3-Raster serverseitig aus dem Netzwerk. |
| Automatisierte Logiktests | `PASS` | Elf JUnit-Tests pruefen Cells, Ownership, Shift-Craft-Reste und Rezept-Backtracking. |
| Server-GameTests | `PASS` | Dreizehn reale GameTests pruefen unter anderem physische Inventare, Rohrpostrohre, Blenden, Chunk-Grenzen, Lastfaelle und Crafting. |
| Client-GameTest | `SMOKE uat.7` | Der Fabric-Client startet mit den neuen Lumungus-Assets ohne fehlende Lumungus-Modell- oder Texturwarnungen. |
| Rezepte und Werkzeug | `PENDING uat.8` | Kupferlastige Rezepte, fokussierte Crafting-Terminal-Suche, Spitzhacken-Abbau und Kupfer-Schraubenschluessel werden im naechsten Client-Smoke geprueft. |

## Testaufbau

- [ ] **UAT-01 Setup:** Minecraft 26.2 startet mit Fabric, Lumungus Core, Lumungus Storage und der vorgesehenen JEI-Version ohne Ladefehler.
- [ ] **UAT-02 Testnetz:** Ein Controller, mehrere Kisten oder Faesser ueber Rohrpostrohre, Inventaranschluesse oder Rohrpostblenden und ein Terminal bilden ein gemeinsames Netzwerk; ein zweiter Spieler kann beitreten.
- [ ] **UAT-03 Testdaten:** Das Netzwerk enthaelt mehrere Itemtypen, grosse Mengen, Items mit Komponenten sowie freie und nahezu volle Cells.

## Netzwerk und Persistenz

- [ ] **UAT-04 Controller:** Der Controller erkennt alle erreichbaren Drive Bays und Terminals, ohne fremde oder ausserhalb der Reichweite liegende Bloecke einzubeziehen.
- [ ] **UAT-05 Drive Bay:** Einsetzen und Entnehmen einer Cell aktualisiert den sichtbaren Netzwerkbestand genau einmal und ohne Itemverlust oder Duplikation.
- [ ] **UAT-06 Cell-Inhalt:** Eine entnommene und erneut eingesetzte Cell behaelt Itemtypen, Mengen und Komponenten unveraendert.
- [ ] **UAT-07 Neustart:** Nach Speichern, Verlassen und Serverneustart bleiben Netzwerk-ID, Cell-Inhalte und Gesamtmengen erhalten.

## Terminal

- [ ] **UAT-08 Suche:** Die Suche filtert nach sichtbarem Itemnamen, reagiert auf Eingaben und zeigt bei keinem Treffer einen eindeutigen Leerzustand.
- [ ] **UAT-09 Sortierung:** Alle angebotenen Sortierungen liefern eine stabile, nachvollziehbare Reihenfolge und verlieren weder Auswahl noch Items.
- [ ] **UAT-10 Seitenwechsel:** Vorwaerts- und Rueckwaertsblaettern zeigt jeden Treffer genau einmal; Seitenzahl und Schaltflaechenzustand stimmen an erster und letzter Seite.
- [ ] **UAT-11 Einlagern:** Die vorgesehenen Klick- und Shift-Klick-Aktionen lagern genau die angezeigte Menge ein und aktualisieren Terminal sowie Spielerinventar sofort.
- [ ] **UAT-12 Entnehmen:** Links-, Rechts- und Shift-Klick entnehmen jeweils die vorgesehene Menge; Cursor, Spielerinventar und Netzwerkbestand bleiben konsistent.

## Crafting und JEI

- [ ] **UAT-13 Gemischte Zutaten:** Ein Rezept kann Zutaten gleichzeitig aus Spielerinventar und Netzwerk verwenden; nur tatsaechlich verbrauchte Mengen werden abgezogen.
- [ ] **UAT-14 Crafting-Reste:** Container und Rezeptreste landen gemaess Minecraft-Verhalten im Inventar oder Netzwerk; bei Platzmangel entsteht weder Verlust noch Duplikation.
- [ ] **UAT-15 Fehlende Zutaten:** Crafting bleibt gesperrt und markiert den Mangel eindeutig, wenn die gemeinsame Zutatenmenge nicht ausreicht.
- [ ] **UAT-16 JEI-Transfer:** Der JEI-Transfer befuellt das Rezept aus Spieler- und Netzwerkbestand, meldet fehlende Zutaten korrekt und verbraucht beim reinen Transfer noch keine Zutaten; die Gesamtmenge aus Grid, Inventar und Netzwerk bleibt gleich.
- [ ] **UAT-16a Terminal-Suche:** Das Crafting-Terminal fokussiert die Suche beim Oeffnen; Suchbegriffe finden Items ueber Anzeigenamen und Item-ID.

## Rezepte und Werkzeug

- [ ] **UAT-16b Rezepte:** Controller, Crafting Terminal, Drive Bay, Inventaranschluss, Rohrpostrohr, Rohrpostblende, Storage Cell und Kupfer-Schraubenschluessel sind im Survival-Modus craftbar; Kupfer ist der primaere Metallbestandteil.
- [ ] **UAT-16c Standard-Abbau:** Lumungus-Storage-Bloecke verhalten sich beim normalen Abbauen wie technische Metallbloecke und droppen mit Spitzhacke korrekt.
- [ ] **UAT-16d Schraubenschluessel:** Der Kupfer-Schraubenschluessel baut Lumungus-Storage-Bloecke sofort mit normalen Drops ab und verliert Haltbarkeit.
- [ ] **UAT-16e Drive-Bay-Transfer:** Der `BAY`-Knopf im Crafting Terminal verschiebt Items aus angeschlossenen Kisten/Faessern in erreichbare Drive Bays mit eingesetzten Cells; nicht verschiebbare Items bleiben in den Quellinventaren.
- [ ] **UAT-16f Multi-Cell-Bay:** Eine Drive Bay nimmt bis zu acht 16k Storage Cells auf, zaehlt Kapazitaet und Inhalte ueber alle Cells zusammen und dropt beim Abbau alle eingesetzten Cells.
- [ ] **UAT-16g Drive-Bay-Menue:** Rechtsklick auf eine Drive Bay oeffnet ein Rack-Menue mit acht sichtbaren Cell-Slots; Cells lassen sich per Klick und Shift-Klick zwischen Spielerinventar und Bay bewegen.
- [ ] **UAT-16h Shulker-Logistik:** Befuellte Shulkerboxen werden beim Einlagern entladen; im Netzwerk bleiben nur die enthaltenen Items und leere Shulkerboxen. Der Terminal-Schalter `ITM`/`BOX` entnimmt wahlweise lose Items oder eine mit dem angeklickten Item befuellte Shulkerbox.
- [ ] **UAT-16i Suchfeld-Hotkeys:** Wenn das Suchfeld aktiv ist, werden Buchstaben in die Suche geschrieben und nicht als globale Spiel-Hotkeys ausgefuehrt.
- [ ] **UAT-16j Blockdesign:** Controller, Crafting Terminal, Drive Bay, Inventaranschluss, Rohrpostrohr, Rohrpostblende, Storage Cell und Kupfer-Schraubenschluessel nutzen die Kupfer-/Anthrazit-/Rohrpost-Optik ohne fehlende Texturen.
- [ ] **UAT-16k Neue Geraetebloecke:** Wireless Storage Controller I/II/III, Lager-Output, Lager-Breaker und Lager-Placer erscheinen im Kreativmenue, sind craftbar, droppen beim Abbau und reagieren auf den Kupfer-Schraubenschluessel.
- [ ] **UAT-16l Rohrpost-Rendering:** Das Rohrpostrohr erscheint im Kreativmenue und in Rezepten als Rohrpostrohr, nicht als Cable; beim Platzieren auf Boden, Wand oder Decke entsteht kein X-Ray-/Durchsicht-Effekt.
- [ ] **UAT-16m Gerichtete Rohrpost:** Rohrpostrohre bilden sichtbare Arme nur zu Lumungus-Storage-Geraeten oder anderen Rohrpostrohren; normale Bloecke, Boden, Waende und Kisten ohne Inventaranschluss erzeugen keine falschen Anschluesse.
- [ ] **UAT-16n Block-Unterscheidbarkeit:** Controller, Terminal, Drive Bay, Output, Breaker, Placer und Wireless-Controller sind im Inventar und platziert auf einen Blick unterscheidbar.
- [ ] **UAT-16o Terminal-Layout:** Im Crafting Terminal laufen Suchfeld, Lagerliste, Seitenanzeige, Statuszeile, Aktionsbuttons, Craftingraster, Ergebnisfeld und Spielerinventar nicht ineinander; JEI bleibt rechts benutzbar.
- [ ] **UAT-16p Terminal-Neuentwurf:** Das Crafting Terminal nutzt klare getrennte Bereiche fuer Lager, Aktionen, Crafting und Spielerinventar; lange Titel und Mengenanzeigen bleiben innerhalb ihrer Flaechen.
- [ ] **UAT-16q Arbeitsbloecke:** Lager-Output exportiert Items in ein Nachbarinventar, Lager-Breaker baut gefilterte Bloecke in Arbeitsrichtung ab und lagert Drops ein, Lager-Placer setzt gefilterte Bloecke aus dem Lager in Arbeitsrichtung, und Wireless Storage Controller I verbindet sich mit einem Controller in kurzer Distanz.
- [ ] **UAT-16r Tragbare Interfaces:** Tragbares Storage Interface I/II/III erscheinen im Kreativmenue, sind craftbar, koennen per Shift-Rechtsklick an einen Storage Controller gebunden werden und oeffnen per Rechtsklick das Storage Terminal innerhalb ihrer jeweiligen Reichweite.
- [ ] **UAT-16s Wireless Inventory Connectoren:** Wireless Inventaranschluss I/II/III erscheinen im Kreativmenue, sind craftbar, droppen beim Abbau, reagieren auf den Kupfer-Schraubenschluessel und verbinden entfernte Kisten/Faesser ueber einen erreichbaren Wireless Storage Controller mit dem Lager.
- [ ] **UAT-16t Wireless-Reichweite:** Wireless Storage Controller und Wireless Inventaranschluesse der Stufe II verbinden geladene Gegenstellen in derselben Dimension auch ausserhalb der Kurzdistanz; Stufe III speichert Links mit Dimensions-ID fuer dimensionsuebergreifende Gegenstellen.
- [ ] **UAT-16u Gerichtete Arbeitsseiten:** Lager-Output exportiert nur zur sichtbaren Arbeitsseite, Lager-Breaker/Placer arbeiten nur in ihrer gesetzten Richtung, und Shift-Rechtsklick mit dem Kupfer-Schraubenschluessel richtet die Arbeitsseite neu aus.
- [ ] **UAT-16v Arbeitsblock-Sicherheit:** Output, Breaker und Placer pausieren bei Redstone-Signal; der Breaker baut keine Lumungus-Storage-Geraete oder Rohrpostknoten ab.
- [ ] **UAT-16w Bedienhilfen:** Storage-Bloecke, Wireless-Stufen, Arbeitsbloecke, Kupfer-Schraubenschluessel und tragbare Interfaces zeigen kurze Tooltips; Arbeitsblock-Statusmeldungen nennen Filter, Arbeitsrichtung, Redstone-Status und fehlende Controller.

## Multiplayer und Grenzfaelle

- [ ] **UAT-17 Server-Autoritaet:** Bei zwei gleichzeitigen Nutzern entscheidet ausschliesslich der Server ueber Mengen; beide Ansichten synchronisieren sich ohne Duplikation, negative Werte oder Geisteritems.
- [ ] **UAT-18 Volles Netzwerk:** Einlagerung in volle Cells oder bei ausgeschoepftem Typenlimit wird sauber abgelehnt; nicht eingelagerte Items verbleiben beim Spieler.
- [ ] **UAT-19 Leeres Netzwerk:** Suche, Seitenwechsel, Entnahme und Crafting mit leerem Netzwerk verursachen keinen Fehler und zeigen einen eindeutigen Leerzustand.
- [ ] **UAT-20 Fehlerzustand:** Entfernte Controller, Drive Bays oder Verbindungsabbrueche schliessen oder aktualisieren das Terminal kontrolliert; bestaetigte Transaktionen bleiben konsistent.

## Ergebnisprotokoll

RC-Version: `0.1.0-uat.34`  Tester: `________________`  Datum: `________________`

| ID | Ergebnis | Bemerkung / Issue-Link |
|---|---|---|
| UAT-01 bis UAT-20 | `PASS` / `FAIL` / `BLOCKED` | Abweichungen je ID dokumentieren |

Gesamtabnahme: `PASS` / `FAIL` / `BLOCKED`

Ein Release ist nur freigegeben, wenn alle Kriterien `PASS` sind. Jeder Fehler muss mit UAT-ID, Reproduktionsschritten, erwartetem und tatsaechlichem Ergebnis dokumentiert werden.
