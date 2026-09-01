# Lumungus Machines MVP

## Implementierungsstand

Phase 3 ist gestartet. Lumungus Core enthaelt den gemeinsamen Vertrag fuer Produktionsauftrag, Zielmenge, erlaubte Rezepte, Fortschritt, fehlende Materialien und Maschinenstatus. Lumungus Machines besitzt eine kapazitaetsbegrenzte Produktionswarteschlange mit stabiler Reihenfolge, Schutz vor doppelten Auftraegen, monotonem Fortschritt und validierten Zustandswechseln.

Der erste Autocrafter-Block ist registriert, im Kreativmenue sichtbar und besitzt ein kupferlastiges Survival-Rezept. Seine BlockEntity speichert Rezept-ID, Zielitem, Zielmenge und Fortschritt dauerhaft. Per Rechtsklick mit einem Item kann bereits ein Ziel gesetzt und mit leerer Hand der Status abgefragt werden.

Als naechster Schritt folgt das Bedienmenue mit 3x3-Muster, Mengenfeld und Start-/Stopp-Steuerung. Danach wird die Warteschlange mit dem Storage-Netzwerk verbunden, damit Zutaten reserviert und Ergebnisse eingelagert werden koennen.

## Produktionsauftraege

Lumungus Machines soll mengenbasierte Produktionsauftraege aus dem Crafting Terminal, dem Build Clipboard und spaeter weiteren Lumungus-Modulen ausfuehren. Ein Auftrag enthaelt mindestens Zielgegenstand, Zielmenge, erlaubte Rezepte und den aktuellen Fortschritt.

Beispiel: Eine Schematic benoetigt 120 Faesser. Der Planer berechnet daraus den Bedarf an Brettern und Holzstufen, verarbeitet vorhandene Holzstaemme ueber die noetigen Zwischenrezepte und laesst den Autocrafter anschliessend genau 120 Faesser herstellen.

Maschinen haengen sichtbar am Lumungus-Rohrpostnetz. Zutaten kommen ueber Rohre an, Zwischenprodukte koennen an andere Maschinen weitergereicht werden und fertige Items laufen zurueck ins physische Lager.

Der Produktionsplaner soll:

- mehrstufige Rezeptketten automatisch und rekursiv aufloesen
- vorhandene Zwischenprodukte aus dem Storage-Netzwerk zuerst verwenden
- Rohstoffe fuer einen Auftrag reservieren, damit parallele Auftraege sie nicht doppelt einplanen
- Rezeptschleifen erkennen und abbrechen
- bei mehreren moeglichen Rezepten eine bevorzugte Variante speichern
- fehlende Rohstoffe an das Build Clipboard und das Terminal zurueckmelden
- Fortschritt als `geplant`, `wartet auf Material`, `in Produktion`, `fertig` oder `blockiert` anzeigen

## Geplante Maschinen

### Autocrafter

- Nimmt ein Crafting-Rezept als Muster auf.
- Produziert eine eingestellte Zielmenge statt nur endlos zu craften.
- Kann Zwischenprodukte ueber weitere verbundene Autocrafter anfordern.
- Soll Rezepte spaeter direkt aus JEI und aus dem Crafting Terminal uebernehmen koennen.
- Besitzt sichtbare Ein- und Ausgangsrohre fuer Zutaten, Zwischenprodukte und fertige Stacks.

### Autosteinsaege

- Verarbeitet alle passenden Steinsaege-Rezepte.
- Nimmt Zielblock und Zielmenge als Produktionsauftrag an.
- Nutzt dasselbe Reservierungs-, Fortschritts- und Fehler-Modell wie der Autocrafter.

### Auto-Braustand

- Produziert einen ausgewaehlten Trank in einer festgelegten Menge.
- Plant Flaschen, Wasser, Brauzutaten und Brennstoff mit ein.
- Kann mehrstufige Brauvorgaenge in der richtigen Reihenfolge ausfuehren.

## Modulgrenzen

Gemeinsame Vertraege fuer Produktionsauftraege, Rezeptanbieter, Materialreservierung und Fortschritt gehoeren in `lumungus-core`. Die konkreten Maschinen bleiben in `lumungus-machines`. Storage und Build Clipboard sprechen nur mit den Core-Vertraegen; optionale JEI-Anbindungen bleiben austauschbare Integrationen.

RailQuarry bleibt ein separates, spaeteres Maschinenprojekt und wird mit diesem MVP noch nicht migriert.
