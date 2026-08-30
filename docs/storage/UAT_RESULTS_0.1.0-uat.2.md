# UAT Results 0.1.0-uat.2

Gesamtstatus: `REJECTED_AT_BUILD`

Der Kandidat wurde isoliert entpackt und nicht in `main` uebernommen. Da der verpflichtende Java-25-Build fehlschlaegt, wurde kein Minecraft-UAT gestartet.

## Geprueftes Artefakt

- Datei: `lumungus-storage-0.1.0-uat.2-codex-storage-uat1-final.zip`
- SHA-256: `9C7A3DD11CFFEDFAE8F90B17026F8A998333991FC60D734EFD4ADC391C6ED93B`
- Version laut Uebergabe: `0.1.0-uat.2`
- Buildkommando: `./gradlew clean build storageUatBundle --console=plain`
- Ergebnis: `FAILED` mit 4 fehlgeschlagenen von 11 Tests

## Build-Blocker

### Ownership ausserhalb des Scanwuerfels

Test: `StorageControllerOwnershipTest.ignoresControllersOutsideTheScanCube`

Ergebnis: Erwartet `true`, erhalten `false`.

Technischer Befund: `StorageControllerOwnership.ownerOf` filtert mit einer quadrierten Gesamtdistanz von `radius^2 * 3`. Dadurch gilt beispielsweise `(radius + 1, 0, 0)` weiterhin als erreichbar. Die tatsaechliche Suche verwendet jedoch einen achsenbegrenzten Wuerfel. Der Besitzerfilter muss dieselben Grenzen je X-, Y- und Z-Achse anwenden.

### Crafting-Sicherheitstest verwendet ungueltigen Stack

Test: `LumungusCraftingMenuSafetyTest.preservesAResultRemainderWhenTheResultSlotWasClearedByTake`

Ergebnis: Erwartete Stackgroesse `2`, erhalten `1`.

Technischer Befund: Der Test erzeugt den Stack ueber `Holder.direct` mit leerer Komponentenmap. In diesem Testkontext wird die Menge beim Einsetzen in den Container auf `1` begrenzt. Der Test prueft damit nicht verlaesslich das Produktionsverhalten und muss registrierte Vanilla-Stacks verwenden.

### Planner-Tests erkennen ihre Zutaten nicht

Tests:

- `RecipeIngredientPlannerTest.backtracksWhenTheFirstMatchingAlternativeBlocksALaterIngredient`
- `RecipeIngredientPlannerTest.planningDoesNotMutateTheAvailablePool`

Ergebnis: Planung leer (`NoSuchElementException`) beziehungsweise erwartet `true`, erhalten `false`.

Technischer Befund: Auch diese Tests bauen ItemStacks ueber eigene direkte Holder. Die aus registrierten Items erzeugten `Ingredient`-Instanzen erkennen diese Teststacks nicht zuverlaessig. Die Fixtures muessen regulaere registrierte ItemStacks verwenden; danach sind Backtracking und Unveraenderlichkeit erneut zu pruefen.

## Weiterhin offener Produktionsbefund

### Release-Paket ist nicht an erfolgreiche Tests gekoppelt

Prioritaet: `P1`

`storageUatBundle` haengt nur von den JAR-Tasks ab. Dadurch kann ein UAT-ZIP auch bei fehlschlagenden Tests entstehen und als Kandidat weitergegeben werden. Das Bundle-Task muss von `check` beziehungsweise den relevanten Modul-Tests abhaengen; die dokumentierte Einzelanweisung `./gradlew storageUatBundle` muss dann denselben Build-Gate erzwingen.

### UAT-14: Teilweiser Shift-Craft kann weiterhin Ergebnis verlieren

Prioritaet: `P1`

`quickMoveStack` sichert den nicht verschobenen Ergebnisrest vor `slot.onTake`. Erzeugt `onTake` bereits das Ergebnis des naechsten Crafts, verwirft `preserveResultRemainder` den gesicherten Rest, um den neuen Slotinhalt nicht zu ueberschreiben. Der neue Test bestaetigt nur, dass das Folgeergebnis erhalten bleibt; er weist nicht nach, dass der alte Rest in Inventar, Netzwerk oder als kontrollierter Drop erhalten wurde.

Erforderlich ist ein Test der Gesamtmenge bei teilweise vollem Inventar: verschobene Menge plus erhaltener Rest muss exakt der urspruenglichen Rezeptausgabe entsprechen.

### PERF-01: Verschachtelte Vollscans

Prioritaet: `P2`

Die Zusammenfassung von Bestand, Snapshot und Kapazitaet auf einen Aufruf reduziert Mehrfachabfragen. `driveBays` scannt jedoch den kompletten Controllerwuerfel und `ownsDriveBay` scannt fuer jede gefundene Bay erneut einen kompletten Wuerfel. Vor Multiplayer-UAT ist eine Messung mit mehreren Bays und offenen Terminals erforderlich; bevorzugt wird eine einmal ermittelte Controllerliste oder ein gecachter Netzwerkindex.

### Dynamische Drive-Bay-Zuordnung benoetigt eine definierte Regel

Prioritaet: `P1`

Der Besitzer wird bei jedem Zugriff aus den aktuell geladenen Controllern neu bestimmt. Ein naeher platzierter, entfernter oder zeitweise nicht geladener Controller kann eine Bay dadurch mitsamt Inhalt einem anderen Netzwerk zuordnen. Fuer UAT-04 und UAT-17 muss festgelegt und getestet werden, ob die Zuordnung dauerhaft gespeichert wird oder ein kontrollierter Konfliktzustand entsteht; ein stiller Netzwechsel darf keinen fremden Bestand freigeben.

### Fehlender Regressionstest fuer atomaren JEI-Transfer

Prioritaet: `P1`

Die neuen Tests rufen `placeRecipeFromNetwork` nicht auf. Damit ist fuer UAT-15, UAT-16 und UAT-18 nicht automatisiert belegt, dass ein Fehler nach Beginn der Entnahme das vorhandene Grid, Spielerinventar und Netzwerk exakt wiederherstellt. Benoetigt wird mindestens ein Test mit fehlgeschlagener Teilentnahme und eine Mengeninvarianz ueber Grid, Inventar und Netzwerk.

## Rueckgabe an Coding

Fuer `0.1.0-uat.3` erforderlich:

- Scanwuerfel-Grenzen korrigieren und den bestehenden Ownership-Test bestehen lassen.
- Crafting- und Planner-Tests auf registrierte Vanilla-ItemStacks umstellen.
- UAT-14 so beheben, dass auch bei einem sofort neu erzeugten Folgeergebnis kein Rest verloren geht.
- Einen Mengeninvarianz-Test fuer partielles Shift-Crafting ergaenzen.
- Atomaren fehlgeschlagenen JEI-Transfer mit unveraendertem Grid und Gesamtbestand testen.
- Drive-Bay-Verhalten beim Platzieren, Entfernen und Entladen konkurrierender Controller definieren und testen.
- Verschachtelte Vollscans beseitigen oder mit einer nachvollziehbaren Lastmessung absichern.
- `storageUatBundle` an erfolgreiche Tests beziehungsweise `check` koppeln.
- Mit Java 25 `./gradlew clean build storageUatBundle` erfolgreich ausfuehren.
- Neuen ZIP-Kandidaten mit Commit-SHA, Pruefergebnis und bekannten Restrisiken bereitstellen.
