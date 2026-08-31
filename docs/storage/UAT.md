# Lumungus Storage Terminal UAT

Diese Checkliste definiert die Abnahmekriterien fuer den Release Candidate. Ein Kriterium gilt nur dann als bestanden, wenn das erwartete Ergebnis reproduzierbar eintritt.

## Release Candidate

- Version: `0.1.0-uat.8`
- Minecraft: `26.2`
- Fabric Loader: `0.19.5`
- Fabric API: `0.158.0+26.2`
- Optional fuer Rezepttransfer: JEI `30.28.0.193`
- Build: `./gradlew clean build storageUatBundle`
- Testpaket: `build/uat/lumungus-storage-0.1.0-uat.8.zip`
- Abnahmestatus: `AUTOMATED_SERVER_PASS`; der Client-UAT aus `0.1.0-uat.3` bleibt die letzte visuelle Referenz, manueller Interaktions- und Multiplayer-UAT sind weiter offen

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

## Multiplayer und Grenzfaelle

- [ ] **UAT-17 Server-Autoritaet:** Bei zwei gleichzeitigen Nutzern entscheidet ausschliesslich der Server ueber Mengen; beide Ansichten synchronisieren sich ohne Duplikation, negative Werte oder Geisteritems.
- [ ] **UAT-18 Volles Netzwerk:** Einlagerung in volle Cells oder bei ausgeschoepftem Typenlimit wird sauber abgelehnt; nicht eingelagerte Items verbleiben beim Spieler.
- [ ] **UAT-19 Leeres Netzwerk:** Suche, Seitenwechsel, Entnahme und Crafting mit leerem Netzwerk verursachen keinen Fehler und zeigen einen eindeutigen Leerzustand.
- [ ] **UAT-20 Fehlerzustand:** Entfernte Controller, Drive Bays oder Verbindungsabbrueche schliessen oder aktualisieren das Terminal kontrolliert; bestaetigte Transaktionen bleiben konsistent.

## Ergebnisprotokoll

RC-Version: `0.1.0-uat.8`  Tester: `________________`  Datum: `________________`

| ID | Ergebnis | Bemerkung / Issue-Link |
|---|---|---|
| UAT-01 bis UAT-20 | `PASS` / `FAIL` / `BLOCKED` | Abweichungen je ID dokumentieren |

Gesamtabnahme: `PASS` / `FAIL` / `BLOCKED`

Ein Release ist nur freigegeben, wenn alle Kriterien `PASS` sind. Jeder Fehler muss mit UAT-ID, Reproduktionsschritten, erwartetem und tatsaechlichem Ergebnis dokumentiert werden.
