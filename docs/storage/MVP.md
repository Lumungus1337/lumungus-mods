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

## Naechster technischer Schritt

1. Block-Entities fuer Controller und Crafting Terminal.
2. Controller-Netzwerkscan in begrenztem Radius.
3. Einfaches Terminal-Menue mit Spielerinventar und 3x3-Crafting-Raster.
4. Storage-Provider-API-Anbindung ueber Lumungus Core.
5. Erste Storage Cell oder Drive Bay als echter Massenspeicher.
