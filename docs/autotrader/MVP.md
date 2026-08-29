# Lumungus Autotrader MVP

## Rolle in der Modreihe

Lumungus Autotrader wird ein eigenes Gameplay-Modul fuer automatisierte Handelsablaeufe. Es soll nicht in `lumungus-machines` verschwinden, weil Trading spaeter eigene Regeln, eigene Balance und eigene Integrationen braucht.

## Erste Vision

- Trading Terminal fuer Handelsuebersicht und Auftragsverwaltung
- Trade Controller als lokaler Handelsknoten
- Import-/Export-Anbindung an Lumungus Storage
- Filter fuer erlaubte Items, Mengenlimits und Prioritaeten
- spaeter Dorfbewohner-, Markt- oder Server-Shop-Integrationen

## Look and Feel

Autotrader soll optisch zur Storage- und Machines-Linie passen:

- 90er-Computertechnik statt moderner Sci-Fi-Glasoptik
- helle Gehaeuse, dunkle Frontplatten, gruene Monitore und Statusanzeigen
- Tastenfelder, Bon-Drucker-/Kassenterminal-Anmutung und kleine LED-Reihen
- klare, handwerkliche Maschinenoptik passend zu Storage Controller, Crafting Terminal und RailQuarry

## Abgrenzung zu RailQuarry

RailQuarry bleibt als zukuenftiges Modul oder Feature in `lumungus-machines` geplant. Autotrader wird separat gefuehrt, damit spaeter auch Welten ohne Quarry nur das Handelssystem installieren koennen.

## Naechster technischer Schritt

1. Core-Vertraege fuer Handelsangebote und Handelsauftraege definieren.
2. Entscheiden, ob Trading direkt mit Villager-Trades startet oder zuerst mit einem neutralen Trade-Provider.
3. Ersten Autotrader-Block registrieren, sobald das Interaktionsmodell feststeht.
