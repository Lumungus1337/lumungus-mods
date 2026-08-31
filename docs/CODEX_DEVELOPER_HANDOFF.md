# Codex Developer Handoff

Dieser Prompt uebergibt die Coding-Verantwortung fuer die Lumungus-Modreihe an einen zweiten Codex-Arbeitsplatz. UAT, Build-Abnahme und manuelle Minecraft-Tests bleiben beim Projektinhaber.

```text
Du arbeitest als Coding-Verantwortlicher an der Lumungus-Modreihe.

Repository:
https://github.com/Lumungus1337/lumungus-mods

Aktueller Arbeitsordner auf dem Rechner des Projektinhabers:
C:\Users\User\Documents\Codex\2026-08-29\referenced-chatgpt-conversation-this-is-an

Dieser lokale Pfad gilt nur auf seinem Rechner. Auf deinem Rechner klonst du das Repository in einen eigenen Arbeitsordner:

git clone https://github.com/Lumungus1337/lumungus-mods.git
cd lumungus-mods
git switch main
git pull --ff-only
git switch -c codex/<kurzer-aufgabenname>

Technische Basis:
- Minecraft 26.2
- Fabric Loader 0.19.5
- Fabric API 0.158.0+26.2
- Java 25
- Gradle Wrapper verwenden
- JEI 30.28.0.193 ist eine optionale Client-Integration
- Paketwurzel: dev.lumungus

Architektur:
- modules/lumungus-core
  Gemeinsame APIs und technische Basisschicht.
- modules/lumungus-storage
  Controller, physische Kisten-/Fass-Netzwerke, Crafting Terminal, optionale Storage Cells und spaeter Autocrafting.
- modules/lumungus-backpack
  Geplanter modularer Rucksack mit spaeterem Jetpack-Upgrade.
- modules/lumungus-machines
  Geplante Maschinen; RailQuarry ist dokumentiert, aber noch nicht migriert.
- modules/lumungus-autotrader
  Geplante Migration der bestehenden Autotrader-Mod im Lumungus-Look.
- modules/lumungus-integration
  Spaetere Cross-Mod-Integrationen.

Wichtige Dokumente:
- README.md
- docs/ARCHITECTURE.md
- docs/VISUAL_IDENTITY.md
- docs/storage/MVP.md
- docs/storage/PHYSICAL_INVENTORY_NETWORK.md
- docs/storage/UAT.md
- docs/storage/UAT_RESULTS_0.1.0-uat.2.md
- docs/machines/MVP.md
- docs/autotrader/MVP.md
- docs/LICENSE_DECISION.md

Aktueller Storage-Stand:
- Storage Controller, Drive Bay und persistente 16k Storage Cell
- Eigenes synchronisiertes Crafting-Terminal im 90er-Computerstil
- Sichtbare Storage-Verbindungen sollen als Item-Rohrpost gedacht und gestaltet werden. Die bestehende interne ID `inventory_cable` bleibt nur aus Kompatibilitaetsgruenden bestehen.
- Netzwerkbestand mit Suche, Sortierung, Seiten, Tooltips und Kapazitaetsanzeige
- Einlagern und Entnehmen ueber Cursor, Links-, Rechts- und Shift-Klick
- Serverseitig autorisierte Netzwerkaktionen
- 3x3-Crafting mit Zutaten aus Spielerinventar und Storage-Netzwerk
- Eigener JEI-Rezepttransfer; der Server loest und validiert die Rezept-ID erneut
- `0.1.0-uat.2` wurde am Build-Gate abgelehnt; alle Anforderungen fuer den naechsten Kandidaten stehen in `docs/storage/UAT_RESULTS_0.1.0-uat.2.md`.
- Zielversion des naechsten UAT-Kandidaten: 0.1.0-uat.3
- Wichtige Korrektur der Zielarchitektur: Cells sind nur optional. Primaerer Speicher sind bestehende Kisten, Faesser, Shulkerboxen und kompatible Mod-Inventare.
- Hoechste Prioritaet ist der verlustfreie Tom's-Uebergang: `lumungus-integration` soll den alten Bestand lesend vergleichen und danach Tom's-Kabel, Trims und Connectoren in Lumungus-Rohrpostbloecke konvertieren. Die Items bleiben in ihren bisherigen Inventaren; nach erfolgreicher Validierung muss die Welt ohne Tom's funktionieren.

Aufgabenteilung:
- Du bist fuer Coding, Bugfixes, Refactoring und passende automatisierte Tests verantwortlich.
- Codex beim Projektinhaber fuehrt UAT, Build-Pruefung und manuelle Minecraft-Tests durch.
- UAT-Fehler werden mit einer ID wie UAT-13 dokumentiert. Implementiere nur klar beschriebene Fehler oder vereinbarte Features.
- Aendere UAT-Ergebnisse nicht selbst. Verweise in Commit oder PR auf die betreffende UAT-ID.
- RailQuarry und Autotrader noch nicht migrieren, solange keine ausdrueckliche Aufgabe dafuer vorliegt.
- Keine neuen Features in einen UAT-Bugfix mischen.

Arbeitsweise:
1. Lies zuerst README.md, docs/ARCHITECTURE.md und die relevanten MVP-/UAT-Dokumente.
2. Pruefe git status und den neuesten Stand von main.
3. Arbeite immer auf einem Branch mit dem Praefix codex/, niemals direkt auf main.
4. Behalte bestehende Patterns und Modulgrenzen bei.
5. Netzwerkaktionen muessen serverautoritativ sein. Client-Payloads niemals als vertrauenswuerdige Mengen- oder Itemquelle behandeln.
6. Verhindere Itemverlust, Duplikation, negative Mengen und Geisteritems.
7. Fuege fuer jeden reproduzierbaren Logikfehler einen fokussierten Test hinzu, soweit technisch sinnvoll.
8. Verwende kleine, thematisch klare Commits.
9. Fuehre vor der Uebergabe mindestens ./gradlew clean build aus.
10. Melde bei der Uebergabe Branchname, Commit-SHA, geaenderte Dateien, behobene UAT-IDs, Tests und bekannte Restrisiken.

Erste Aktion:
Hole den neuesten Stand von main und lies `README.md`, `docs/ARCHITECTURE.md`, `docs/VISUAL_IDENTITY.md` und die aktuellen Storage-UAT-Dokumente. Arbeite neue Storage- und Maschinenfeatures in der Rohrpost-/90er-Terminal-Sprache aus und teste sie vor der Uebergabe.
```
