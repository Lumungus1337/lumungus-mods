# Lizenzentscheidung

Dieses Repository enthaelt noch keine finale Lizenzdatei. Vor dem ersten oeffentlichen Release sollte die Lizenz explizit entschieden und als `LICENSE` im Repository abgelegt werden.

## Option A: MIT

MIT ist die einfachste und freundlichste Wahl fuer Modpacks, Addons und Community-Beitraege.

Vorteile:

- sehr leicht verstaendlich
- kompatibel mit vielen Projekten
- wenig Reibung fuer Modpack-Nutzung
- gut geeignet, wenn Lumungus als offene Mod-Familie wachsen soll

Nachteile:

- Verbesserungen koennen in proprietaere Forks uebernommen werden
- wenig Schutz fuer gemeinsame Infrastruktur

## Option B: LGPL-3.0

LGPL-3.0 passt, wenn Core als wiederverwendbare Bibliothek offen bleiben soll, ohne normale Modpack-Nutzung stark zu erschweren.

Vorteile:

- staerkerer Schutz fuer Bibliothekscode
- Verbesserungen an Core bleiben eher zurueckfuehrbar
- externe Mods koennen trotzdem gegen Core linken

Nachteile:

- etwas komplizierter zu erklaeren
- manche Contributor oder Modpack-Ersteller reagieren vorsichtiger

## Option C: MPL-2.0

MPL-2.0 ist ein Mittelweg: Veraenderte Dateien bleiben offen, aber die Lizenz ist weniger streng als GPL/LGPL.

Vorteile:

- guter Kompromiss zwischen Offenheit und Schutz
- klarer dateibasierter Copyleft-Umfang

Nachteile:

- in der Minecraft-Modding-Szene weniger bekannt als MIT

## Vorlaeufige Empfehlung

Fuer den Start: MIT fuer Code, spaeter separate Asset-Lizenz pruefen.

Wenn Lumungus Core ausdruecklich als stabile API fuer andere Mods gedacht ist, sollte LGPL-3.0 oder MPL-2.0 noch einmal bewusst diskutiert werden.
