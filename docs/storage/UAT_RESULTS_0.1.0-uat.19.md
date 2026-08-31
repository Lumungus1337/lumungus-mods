# Lumungus Storage UAT Results 0.1.0-uat.19

Datum: 31.08.2026

## Ziel

Dieser Kandidat ersetzt das Crafting-Terminal-Layout durch einen klareren, modernen Aufbau:

- Lagerliste links mit sechs Spalten statt gedrungenem Sieben-Spalten-Raster.
- Feste Aktionsleiste zwischen Lager und Crafting.
- Craftingbereich rechts mit neu gesetzten Slotpositionen.
- Spielerinventar sauber zentriert und getrennt vom oberen Terminalbereich.
- Kompakte Mengenanzeigen, damit grosse Lagerwerte nicht ueber Nachbarslots oder Rahmen laufen.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | `:lumungus-storage:build`, `storageUatBundle` und `:lumungus-integration:build` erfolgreich. |
| Server-GameTests | `PASS` | Lumungus Storage: 19/19, Lumungus Integration: 3/3. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.19.zip`, SHA256 `F0EB07AE8E437E3DA3B65E1FB7795FB1F9713609568DC81283E1A22253675242`. |

## Manueller UAT-Fokus

- Crafting Terminal in der grossen UAT-Lagerwelt oeffnen.
- Suche, Sortierung, Seitenwechsel, Itementnahme, `IN`, `BAY` und `I`/`S` pruefen.
- Pruefen, dass mit JEI rechts keine Texte, Buttons oder Linien ueberlappen.
