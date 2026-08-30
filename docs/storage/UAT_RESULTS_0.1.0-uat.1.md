# UAT Results 0.1.0-uat.1

Gesamtstatus: `BLOCKED`

Der zentrale Creative-Smoke-Test ist bestanden. Die vollstaendige Benutzerabnahme wird jedoch nicht gestartet, bevor die folgenden Befunde durch den Coding-Verantwortlichen behoben und als neuer Kandidat bereitgestellt wurden.

## UAT-14: Teilweiser Shift-Craft kann Ergebnis verlieren

Status: `BLOCKED`  Prioritaet: `P1`

Betroffen: `LumungusCraftingMenu.quickMoveStack`

Reproduktion:

1. Spielerinventar bis auf weniger freie Kapazitaet als die Rezeptausgabe fuellen.
2. Im Terminal ein Rezept mit mehreren Ausgabeitems vorbereiten, zum Beispiel vier Bretter.
3. Das Ergebnis mit Shift-Klick entnehmen.

Erwartet: Der passende Anteil landet im Inventar; ein nicht verschiebbarer Rest bleibt erhalten oder wird wie bei Vanilla kontrolliert gedroppt.

Technischer Befund: `moveItemStackTo` kann nur einen Teil verschieben. Der verbleibende Ergebnis-Stack wird vor `slot.onTake` derzeit weder erhalten noch gedroppt.

## UAT-04 / UAT-17: Drive Bays koennen zwei Controllern gehoeren

Status: `BLOCKED`  Prioritaet: `P1`

Betroffen: `StorageControllerBlockEntity.findDriveBays`

Reproduktion:

1. Zwei Controller so platzieren, dass sich ihre Scanbereiche ueberlappen.
2. Eine Drive Bay mit Cell in den gemeinsamen Bereich setzen.
3. Beide Netzwerke gleichzeitig ueber je ein Terminal oeffnen.

Erwartet: Eine Drive Bay gehoert eindeutig genau einem Netzwerk oder ein Konflikt wird sichtbar abgelehnt.

Technischer Befund: Beide Controller nehmen jede Bay im Scanwuerfel auf; eine Besitzer- oder Netzwerkzuordnung fehlt.

## UAT-15 / UAT-16 / UAT-18: Fehlgeschlagener Rezepttransfer veraendert das Grid

Status: `BLOCKED`  Prioritaet: `P1`

Betroffen: `LumungusCraftingMenu.placeRecipeFromNetwork`

Reproduktion:

1. Ein bestehendes Rezept oder mehrere Items in das 3x3-Grid legen.
2. Spielerinventar und Storage-Netzwerk fuellen.
3. Aus JEI ein Rezept mit fehlenden Zutaten uebertragen.

Erwartet: Der Transfer wird abgelehnt und das bestehende Grid bleibt unveraendert.

Technischer Befund: Das Grid wird vor der Verfuegbarkeitspruefung geleert und verteilt. Bei fehlenden Zutaten wird der vorherige Zustand nicht wiederhergestellt.

## UAT-13 / UAT-15: Gueltige mehrdeutige Rezepte koennen abgelehnt werden

Status: `BLOCKED`  Prioritaet: `P2`

Betroffen: `LumungusCraftingMenu.planRecipe`

Reproduktion:

1. Ein Rezept verwenden, dessen fruehe Zutat mehrere Alternativen akzeptiert und dessen spaetere Zutat eine dieser Alternativen zwingend benoetigt.
2. Genau eine passende Menge jeder benoetigten Alternative bereitstellen.
3. Rezept aus JEI uebertragen.

Erwartet: Eine gueltige Zutatenkombination wird gefunden.

Technischer Befund: Die Auswahl nimmt jeweils den ersten Treffer und versucht bei einem spaeteren Konflikt keine alternative Zuordnung.

## PERF-01: Mehrfache Vollscans pro offenem Terminal

Status: `OPEN`  Prioritaet: `P2`

`broadcastChanges` ruft Bestand, Snapshot und Kapazitaet getrennt ab. Jeder Aufruf scannt aktuell den kompletten Controllerbereich. Vor einem Multiplayer-Release sollte ein gemeinsamer Scan oder ein gecachter Netzwerkzustand verwendet und mit mehreren offenen Terminals gemessen werden.

## Uebergabe an Coding

Empfohlener Branch: `codex/storage-uat1-fixes`

Vor Rueckgabe an UAT erforderlich:

- Fokus-Tests fuer alle vier funktionalen Befunde
- `./gradlew clean build`
- Branchname und Commit-SHA
- Liste der behobenen UAT-IDs
- Hinweis auf bekannte Restrisiken
