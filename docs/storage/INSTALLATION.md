# Lumungus Storage 0.1.0-uat.57 installieren

Fuer Minecraft 26.2 / Fabric, Fabric Loader mindestens 0.19.5 und Java 25.
Fabric API muss weiterhin im bestehenden Mod-Profil installiert bleiben.

1. Die Welt speichern und Minecraft vollstaendig beenden.
2. Im Modrinth-Profil fuer Minecraft 26.2 den Profilordner oeffnen.
3. Die Welt vor dem ersten Start mit der neuen Version sichern.
4. Im Unterordner `mods` die bisherigen vier JARs fuer `lumungus-core`,
   `lumungus-storage`, `lumungus-machines` und `lumungus-integration` in einen
   Sicherungsordner ausserhalb von `mods` verschieben.
5. Die vier JARs aus dem Unterordner `mods` dieses Pakets in den `mods`-Ordner
   des Profils kopieren. Andere Mods bleiben dort. Keine alte Lumungus-Version
   parallel zu einer neuen Version im `mods`-Ordner behalten.
6. Minecraft starten. In der Mod-Liste fuer diese vier Module die Version
   `0.1.0-uat.57` pruefen. Core und Storage muessen dieselbe Version haben.

Bei einem separaten Server muessen diese vier JARs auch dort gemeinsam
aktualisiert werden. Server vorher sauber stoppen.

## Massencrafting verwenden

- Leere Shulkerboxen ins verbundene Lager legen und Inventarplaetze fuer die
  fertigen Boxen frei lassen.
- Im Crafting-Terminal die gewuenschte Ergebnis-Menge eintragen und das
  Rezept wie bisher ueber JEI auswaehlen. Den angezeigten Plan bestaetigen.
- Auftraege ueber mehrere Rasterfuellungen laufen schrittweise. Das Terminal
  waehrenddessen offen lassen. Bei fehlenden Boxen, Zutaten oder freiem Platz
  pausiert der Auftrag und versucht die Fortsetzung automatisch.
- Gefuellte Boxen landen im Spielerinventar. Nicht direkt ins Lager
  zuruecklegen, wenn sie gefuellt bleiben sollen: Das Lager entlaedt sie.
- Schliessen des Terminals oder Ausloggen beendet die noch offene Restmenge.
  Fertige Boxen und Zwischenprodukte bleiben erhalten.

## Enthalten

- `lumungus-core-0.1.0-uat.57.jar`
- `lumungus-storage-0.1.0-uat.57.jar`
- `lumungus-machines-0.1.0-uat.57.jar`
- `lumungus-integration-0.1.0-uat.57.jar`

`COMMIT.txt` benennt den gebauten Quellstand, `SHA256SUMS.txt` die Pruefsummen.
Die Server-GameTests pruefen unter anderem 2000 Bretter in zwei Shulkerboxen
und 120 Faesser aus Holzstaemmen. Die manuelle Pruefung in deiner Welt folgt
nach der Installation.
