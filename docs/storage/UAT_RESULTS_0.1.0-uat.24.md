# Lumungus Storage UAT Results 0.1.0-uat.24

Datum: 01.09.2026

## Ziel

Dieser Kandidat macht die Arbeitsbloecke richtungsfaehig:

- Lager-Output exportiert nur noch in seine gesetzte Arbeitsrichtung.
- Lager-Breaker baut den Block in seiner Arbeitsrichtung ab, nicht fest nur nach unten.
- Lager-Placer setzt den Block in seiner Arbeitsrichtung, nicht fest nur nach unten.
- Beim Platzieren zeigt die Arbeitsseite auf die angeklickte Blockseite.
- Shift-Rechtsklick mit dem Kupfer-Schraubenschluessel rotiert die Arbeitsrichtung.
- Blockstates fuer Output, Breaker und Placer enthalten `facing`-Varianten fuer korrekte Modellrotation.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | `:lumungus-storage:build`, `storageUatBundle` und `:lumungus-integration:build` erfolgreich. |
| Server-GameTests | `PASS` | Lumungus Storage: 26/26 erforderliche Tests bestanden. Lumungus Integration: 3/3 erforderliche Tests bestanden. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.24.zip`, SHA-256 `D577E5FD546B7F5CE13CE2FA54F0CCB7DA4FBF30D19125204F67D394784E8242`. |

## Manueller UAT-Fokus

- Output neben mehrere Inventare setzen und pruefen, dass nur die Frontseite befuellt wird.
- Breaker seitlich auf einen Testblock ausrichten und pruefen, dass kein anderer Nachbarblock angeruehrt wird.
- Placer seitlich auf Luft ausrichten und pruefen, dass nur dort platziert wird.
- Shift-Rechtsklick mit Kupfer-Schraubenschluessel auf Output, Breaker und Placer testen.
