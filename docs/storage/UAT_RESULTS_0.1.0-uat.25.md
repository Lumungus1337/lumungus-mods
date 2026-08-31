# Lumungus Storage UAT Results 0.1.0-uat.25

Datum: 01.09.2026

## Ziel

Dieser Kandidat macht die automatischen Arbeitsbloecke sicherer:

- Output, Breaker und Placer pausieren, solange der Block ein Redstone-Signal bekommt.
- Die Schraubenschluessel-Rotation schaltet stabil durch alle sechs Richtungen.
- Der Breaker baut keine Lumungus-Storage-Geraete, Rohrpostrohre oder andere Netzwerkgeraete ab.
- Unit-Test prueft die sichere 6-Richtungs-Rotation.
- GameTests pruefen Redstone-Pause und Breaker-Schutz.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | `:lumungus-storage:build`, `storageUatBundle` und `:lumungus-integration:build` erfolgreich. |
| Server-GameTests | `PASS` | Lumungus Storage: 28/28 erforderliche Tests bestanden. Lumungus Integration: 3/3 erforderliche Tests bestanden. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.25.zip`, SHA-256 `651E5C64D9A8F87A08CC52915276F11D193B2E41E2D9A7D9F6861628826BC835`. |

## Manueller UAT-Fokus

- Output, Breaker und Placer mit einem Hebel oder Redstone-Block pausieren.
- Redstone entfernen und pruefen, ob die Arbeit wieder startet.
- Breaker testweise auf ein Lumungus-Geraet ausrichten und pruefen, dass es nicht abgebaut wird.
- Shift-Rechtsklick mit Kupfer-Schraubenschluessel durch alle Richtungen testen.
