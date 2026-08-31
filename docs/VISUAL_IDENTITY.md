# Lumungus Visual Identity

Status: verbindliche Stilrichtung fuer neue Bloecke, Items, Menues und spaetere Texturen

Aktueller Konzeptbogen: [Lumungus Storage Plumber Pipe Concept](storage/images/lumungus-storage-plumber-pipe-concept-uat12.png)

## Leitidee

Lumungus soll sich nicht wie ein weiteres glattes Sci-Fi-Lagersystem anfuehlen.
Die Modreihe verbindet physische Lagertechnik, sichtbare Rohrpost-Logistik und
90er-Jahre-Computertechnik. Items reisen gedanklich durch Rohre statt durch
abstrakte Kabel.

Die spielerische Richtung:

- Rohrpost fuer Items statt Energiekabel-Optik
- runde, farbige Rohre mit klar sichtbaren Anschlussstuecken
- Werkstattmaschinen mit Tastenfeldern, Statuslampen und kleinen Monitoren
- Terminals mit CRT-Anmutung, dunklen Frontplatten und gruener Schrift
- leichte Arcade-/Plattformspiel-Anmutung, ohne direkte Fremdmarken zu kopieren

## Storage-Sprache

Neue sichtbare Storage-Verbindungen heissen Rohre:

- `Rohrpostrohr`: normale Netzwerkverbindung
- `Inventaranschluss`: verbindet Kisten, Faesser, Shulkerboxen und Mod-Inventare
- `Rohrpostblende`: verkleidbare Verbindung und Inventaranschluss in einem Block
- `Storage Controller`: die zentrale Steuereinheit
- `Crafting Terminal`: Bedienpult mit Suche, Crafting und spaeter JEI-/Auftragsfluss

Die aktuelle interne ID `inventory_cable` bleibt bis zu einem geplanten
Kompatibilitaetsschnitt erhalten. Sichtbare Namen, Doku, Rezepte und Texturen
duerfen bereits die Rohrpost-Sprache nutzen.

## Blockdesign

Storage- und Maschinenbloecke sollen wie robuste, leicht verspielte Werkstatt-
Hardware wirken:

- Rohrteile: satte Farben, dunkle Schattenfugen, helle Kanten, klare Oeffnungen
- Controller: anthrazitfarbener Maschinenkoerper, Kupferplatten, seitliche
  Rohranschluesse, gruene CRT-Statusflaeche und kleine Kontrolllampen
- Terminals: dunkler Bildschirm, gruene oder bernsteinfarbene CRT-Anzeige,
  grobe Tasten, kleine Labelstreifen
- Drive Bay: sichtbares Cell-Rack, Kupferrahmen, gruene Speicherzylinder oder
  Leuchtfenster, klare Vorderseite fuer schnellen Wiedererkennungswert
- Inventaranschluss: robuste Rohrkupplung mit dunkler Frontplatte und einem
  tiefen Anschlussloch, damit sofort klar ist, wo Items ein- und auslaufen
- Maschinen: sichtbare Ein- und Ausgangsrohre, Fortschrittsanzeige, Wartungsklappe
- Autotrader: Kassenterminal-/Bon-Drucker-Gefuehl mit Handelsstatus und Rohranschluss

## Materialpalette

- Kupfer wird als Hauptmaterial genutzt: roh/orange fuer aktive Technik,
  angelaufen/braun fuer Rahmen und Abdeckplatten, oxidiert/gruen fuer Rohre,
  Speicherzellen und wichtige Akzente.
- Anthrazit ist das tragende Maschinenmaterial fuer Kanten, Rueckseiten,
  Schraubplatten, Luefter und Schattenfugen.
- Gruenes CRT-Licht bleibt das Lumungus-Signal fuer Netzwerk, Status und
  interaktive Flaechen.
- Bernstein/Orange wird sparsam fuer Warnlampen, aktive Knopfe und
  Energie-/Transferzustand verwendet.

## Texturregeln

- Jede Blockseite braucht eine grosse Hauptform, die auch bei 16x16 oder 32x32
  Texturen lesbar bleibt.
- Details wie Schrauben, Nieten, Luefter und Anzeigen duerfen die Silhouette
  unterstuetzen, aber nicht die komplette Flaeche verrauschen.
- Vorderseiten muessen pro Block eindeutig sein: Terminal = Bildschirm und
  Tastatur, Controller = zentrale Anzeige, Bay = Cell-Rack, Anschluss = Rohrkupplung.
- Rohre sollen rund wirken, aber als Minecraft-Bloecke klare Kanten behalten.
- Kein modernes Glas-/Neon-Sci-Fi; die Optik bleibt Werkstatt, Klempnertechnik,
  Rohrpost und 90er-Computer.

## Menues

Menues sollen denselben Charakter aufnehmen:

- dunkle Bedienflaechen statt helle Standardfenster
- gruene oder bernsteinfarbene Akzentschrift
- klare Raster, damit grosse Lager schnell lesbar bleiben
- kleine Statusanzeigen fuer Netzwerk, Bestand, Filter, Auftrag und Rohrfluss
- keine ueberladene Sci-Fi-Optik; Lumungus bleibt handwerklich und praktisch

## Abgrenzung

Die Richtung darf an klassische Arcade-Rohre und 90er-Computer erinnern, soll
aber eigene Texturen, Namen und Formen bekommen. Keine direkten Logos, Figuren,
Sounds oder kopierten Designs anderer Marken.
