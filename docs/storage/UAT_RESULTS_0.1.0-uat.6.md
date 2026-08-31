# Lumungus Storage UAT Results 0.1.0-uat.6

Gesamtstatus: `TECHNICAL_PASS_MANUAL_VISUAL_REVIEW_REQUIRED`

## Ziel dieses Kandidaten

`0.1.0-uat.6` ist der erste Kandidat, der die neue Lumungus-Rohrpost-Sprache
sichtbar in Storage uebernimmt. Die bestehenden technischen IDs bleiben
kompatibel; der sichtbare Name und die Platzhaltermodelle bewegen sich in
Richtung Item-Rohrpost, 90er-Terminal und Werkstattmaschine.

## Aenderungen

- `inventory_cable` heisst im Spiel nun `Rohrpostrohr` / `Pneumatic Pipe`.
- `inventory_connector` heisst im Spiel nun `Inventaranschluss` / `Inventory Port`.
- `inventory_trim` heisst im Spiel nun `Rohrpostblende` / `Pneumatic Trim`.
- Das Rohrpostrohr nutzt gruene Rohrsegmente, dunkle Anschlussringe und einen
  helleren Mittelakzent auf Basis vorhandener Minecraft-Texturen.
- Controller, Crafting Terminal, Inventaranschluss und Blende nutzen staerkere
  gruene und dunkle Akzente, damit sie zur Rohrpostlinie passen.
- `docs/VISUAL_IDENTITY.md` beschreibt die verbindliche Lumungus-Stilrichtung.

## Automatisierte Abnahme

- Lumungus Storage Build: `PASS`
- Storage-GameTests: 13 von 13 `PASS`
- Storage-Logiktests: `PASS`

## Manuelle Sichtpruefung

Noch offen:

- Rohrpostrohr im Kreativinventar suchen und platzieren.
- Controller, Crafting Terminal, Inventaranschluss und Rohrpostblende daneben
  platzieren.
- Pruefen, ob die Bloecke in der Welt klar als zusammengehoerige
  Rohrpost-/Terminalfamilie wirken.
- Pruefen, ob die Namen im Spiel verstaendlich sind.

## Entscheidung

Der Kandidat ist technisch sauber genug fuer eine manuelle Sichtpruefung in der
UAT-Welt. Die Block-IDs wurden nicht umbenannt, damit vorhandene Tests und
gespeicherte Weltbloecke stabil bleiben.
