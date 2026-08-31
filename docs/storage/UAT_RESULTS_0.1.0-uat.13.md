# Lumungus Storage UAT Results 0.1.0-uat.13

Datum: 31.08.2026

## Ziel

Dieser Kandidat macht den Storage-Slice sichtbarer Lumungus:

- Controller, Crafting Terminal, Drive Bay, Inventaranschluss, Rohrpostrohr,
  Rohrpostblende, Storage Cell und Kupfer-Schraubenschluessel erhalten
  ueberarbeitete Kupfer-/Anthrazit-/Rohrpost-Texturen.
- Controller, Crafting Terminal und Drive Bay nutzen eigene, erkennbare
  Vorderseiten statt gemeinsamer Platzhalterflaechen.
- Das Crafting-Terminal-Suchfeld schluckt Tastaturereignisse, solange es aktiv
  ist, damit Buchstaben wie `e` nicht mehr globale Spiel-Hotkeys ausloesen.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | `./gradlew :lumungus-storage:build storageUatBundle :lumungus-integration:build` erfolgreich. |
| Server-GameTests | `PASS` | 18/18 Storage-GameTests bestanden. |
| Integration-GameTests | `PASS` | 3/3 Tom's-Migrations- und Integrations-GameTests bestanden. |
| UAT-Bundle | `PASS` | Paket erstellt: `build/uat/lumungus-storage-0.1.0-uat.13.zip`. |

## Manueller UAT-Fokus

- Crafting Terminal oeffnen, ins Suchfeld `Stein` tippen und pruefen, dass sich
  das Inventar bei `e` nicht oeffnet.
- Controller, Crafting Terminal, Drive Bay, Inventaranschluss, Rohrpostrohr,
  Rohrpostblende, Storage Cell und Kupfer-Schraubenschluessel im Kreativmenue
  und in der Welt ansehen.
- Besonders pruefen: Blockvorderseiten sind eindeutig, Texturen fehlen nicht,
  und die Palette wirkt nach Kupfer, oxidiertem Kupfer, Anthrazit und CRT-Gruen.
