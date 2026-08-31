# Lumungus Storage UAT Results 0.1.0-uat.17

Datum: 31.08.2026

## Ziel

Dieser Kandidat poliert das Crafting-Terminal nach dem Screenshot-UAT:

- kompaktere Bestands- und Typenanzeige, damit Statuswerte nicht ueber das UI laufen
- klarere linke Lagerkonsole und rechter Craftingbereich
- kleinere Aktionsbuttons zwischen Lager und Crafting
- sichtbare Crafting-Pfeilzone und sauberer Spielerinventarrahmen

Der Kandidat enthaelt weiterhin die gerichtete Rohrpost und die unterscheidbareren Maschinenfronten aus `uat.16`.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | `:lumungus-storage:build storageUatBundle :lumungus-integration:build` erfolgreich. |
| Server-GameTests | `PASS` | 19/19 Storage-GameTests und 3/3 Integration-GameTests bestanden. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.17.zip` wurde erstellt. |

## Manueller UAT-Fokus

- Crafting Terminal mit JEI rechts oeffnen.
- Suche, Lagerliste, Status, Seitennavigation, `IN`, `BAY` und `ITM`/`BOX` pruefen.
- Pruefen, dass keine Texte in Craftingraster, Spielerinventar oder JEI hineinlaufen.
