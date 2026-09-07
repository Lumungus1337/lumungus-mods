# Mengenauftraege ueber mehrere Crafting-Raster

Ein Auftrag darf nicht auf 64 Rezeptausfuehrungen oder die Stapelgroesse
eines Zutaten-Slots gekuerzt werden. Die eingegebene Menge bezeichnet
Ergebnis-Items; ganze Rezepte werden aufgerundet (1999 Bretter = 500 Rezepte
= 2000 Bretter).

## Verhalten

- Auftraege, die vollstaendig in ein Raster passen, behalten den bisherigen
  Ablauf und die lose Ausgabe ins Spielerinventar.
- Groessere Auftraege werden als einzelne Rezeptschritte geplant. Das gilt
  auch fuer nicht stapelbare Zutaten, beispielsweise Milcheimer fuer Kuchen.
- Zwischenrezepte verwenden uebrig gebliebene Materialien und Rezeptreste
  fuer weitere Schritte. Die Vorschau verbraucht keine Gegenstaende und zeigt
  die gesamte geplante Menge.
- Die Ausfuehrung erledigt maximal vier Rezeptschritte pro Spieltick.
- Leere Shulkerboxen kommen aus dem verbundenen Lager. Fertige Items werden
  direkt in diese Boxen im Spielerinventar gepackt; volle Boxen bleiben dort,
  anschliessend wird die naechste leere Box entnommen. Farben und Namen bleiben
  erhalten. Auch die letzte teilweise gefuellte Box bleibt im Inventar.
- Gefuellte Boxen werden absichtlich nicht wieder eingelagert: Der Controller
  entlaedt sie bei der Einlagerung gemaess bestehender Storage-Regel.
- Fehlende Boxen, Zutaten oder Inventarplaetze pausieren den laufenden Auftrag.
  Solange das Terminal offen bleibt, wird automatisch am aktuellen Schritt
  weitergemacht, sobald die benoetigten Ressourcen wieder verfuegbar sind.
- Zutaten werden vor jedem Schritt erneut geprueft. Teilweise entnommene
  Zutaten eines fehlgeschlagenen Schritts werden zurueckgegeben.

## Grenzen

- Die bestehende Eingabegrenze von 4096 Ergebnis-Items bleibt bestehen.
- Maximal 16384 geplante Schritte und die bestehenden Rekursionsschutzgrenzen.
- Shulkerboxen und andere Gegenstaende, die nicht in Container-Items duerfen,
  sind keine zulaessige verpackte Massenausgabe.
- Schliessen des Terminals oder Ausloggen beendet den noch offenen Auftrag.
  Fertige Boxen, Rezeptreste und bereits hergestellte Zwischenprodukte liegen
  in echten Inventaren; es gibt keinen verborgenen fluechtigen Itempuffer.
  Die verbleibende Menge muss neu bestellt werden. Persistente, unbeaufsichtigte
  Produktionsauftraege sind nicht Teil dieser Terminal-Korrektur.

## Automatisierte Regressionen

- 1999 angeforderte Bretter ergeben 2000 Bretter: 1728 + 272 in zwei Boxen.
- 120 Faesser aus 210 Holzstaemmen, inklusive Bretter- und Stufenrezepten.
- Zwei Kuchen aus nicht stapelbaren Milcheimern; sechs Eimer kommen zurueck.
- Fehlende Shulkerbox: kein Zutatenverbrauch, anschliessend Fortsetzung.
- Volles Spielerinventar: weder Zutaten- noch Boxenverbrauch, danach Fortsetzung.
- Schliessen waehrend der Produktion bewahrt fertige Ausgabe und Restmaterial.

Diese Liste beschreibt die Tests, nicht ein bereits bestandenes UAT-Ergebnis.
