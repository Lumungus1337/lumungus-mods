# Lumungus Storage UAT Results 0.1.0-uat.26

Datum: 01.09.2026

## Ziel

Dieser Kandidat verbessert die Bedienbarkeit im Inventar und beim Anklicken:

- Wichtige Storage-Bloecke zeigen kurze Tooltips im Inventar.
- Wireless Controller und Wireless Inventaranschluesse erklaeren ihre jeweilige Reichweitenstufe.
- Output, Breaker und Placer erklaeren ihre Arbeitsrichtung im Tooltip.
- Kupfer-Schraubenschluessel erklaert Sofortabbau und Arbeitsblock-Rotation.
- Tragbare Interfaces erklaeren Bindung und Reichweite.
- Rechtsklick-Status von Output, Breaker und Placer nennt Filter, Arbeitsrichtung und Redstone-Status.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | `:lumungus-storage:build`, `storageUatBundle` und `:lumungus-integration:build` erfolgreich. |
| Server-GameTests | `PASS` | Lumungus Storage: 28/28 erforderliche Tests bestanden. Lumungus Integration: 3/3 erforderliche Tests bestanden. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.26.zip`, SHA-256 `FB1821E42F4827EA95D2879F56F28A04ED9251E1104655153268F88F2A17E2FB`. |

## Manueller UAT-Fokus

- In Creative/JEI Tooltips der Storage-Bloecke, Wireless-Stufen, Arbeitsbloecke und tragbaren Interfaces ansehen.
- Output, Breaker und Placer ohne Item rechtsklicken und Richtung/Redstone-Status pruefen.
- Einen Arbeitsblock mit Redstone pausieren und erneut rechtsklicken.
