# Lumungus Storage UAT Results 0.1.0-uat.31

Datum: 01.09.2026

## Ziel

Dieser Kandidat haertet das Suchfeld im Crafting Terminal gegen globale Spiel-Hotkeys ab:

- Das Suchfeld bleibt fokussiert, solange das Terminal offen ist.
- Tastendruecke ausser Escape werden bei aktivem Suchfeld nicht an den Container-Screen weitergereicht.
- Zeichenereignisse werden vom Suchfeld geschluckt, auch wenn ein Zeichen nicht eingefuegt wurde.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | Vollstaendiger Release-Build erfolgreich. |
| Server-GameTests | `PASS` | Storage 28/28 und Integration 3/3 erfolgreich. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.31.zip` erstellt. SHA256: `919A2B0ADE0AD437BD4CA998530BC13DF269612946EDE0EC5D33E92910201639`. |

## Manueller UAT-Fokus

- Crafting Terminal oeffnen und `Stein`, `erde`, `eiche` ins Suchfeld tippen.
- Erwartung: `E` oeffnet nicht das Spielerinventar und andere Hotkeys loesen keine Spielaktion aus.
- Escape schliesst das Terminal weiterhin.
