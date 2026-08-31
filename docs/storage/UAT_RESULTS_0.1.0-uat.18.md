# Lumungus Storage UAT Results 0.1.0-uat.18

Datum: 31.08.2026

## Ziel

Dieser Kandidat behebt den Suchfeld-Hotkey-Fehler im Crafting Terminal:

- Wenn das Suchfeld fokussiert ist, verbraucht es Tastendruecke jetzt komplett.
- Buchstaben wie `E` fallen dadurch nicht mehr bis zu Minecrafts globalen Keybinds durch und oeffnen nicht das Inventar.
- `Esc` bleibt als normales Schliessen/Zurueck-Verhalten erhalten.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | `:lumungus-storage:build storageUatBundle :lumungus-integration:build` erfolgreich. |
| Server-GameTests | `PASS` | 19/19 Storage-GameTests und 3/3 Integration-GameTests bestanden. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.18.zip` wurde erstellt. |

## Manueller UAT-Fokus

- Crafting Terminal oeffnen.
- Im Suchfeld `Stein`, `Eiche`, `redstone` und andere Begriffe tippen.
- Pruefen, dass Inventar, Map, JourneyMap/Mod-Keybinds oder andere globale Hotkeys nicht ausloesen, solange das Suchfeld aktiv ist.
