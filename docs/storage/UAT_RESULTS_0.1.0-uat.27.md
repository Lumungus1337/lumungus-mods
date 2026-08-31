# Lumungus Storage UAT Results 0.1.0-uat.27

Datum: 01.09.2026

## Ziel

Dieser Kandidat macht Arbeitsblock-Statusmeldungen genauer:

- Output, Breaker und Placer zeigen beim Rechtsklick nicht mehr nur `laeuft` oder `Redstone-Pause`.
- Wenn kein Storage Controller erreichbar ist, steht im Status jetzt `kein Controller`.
- Die Statuslogik ist gemeinsam gekapselt, damit alle Arbeitsbloecke dieselben Begriffe verwenden.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | Vollstaendiger Release-Build erfolgreich. |
| Server-GameTests | `PASS` | Storage 28/28 und Integration 3/3 erfolgreich. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.27.zip` erstellt. SHA256: `EB59D87E8CC626403D1C62A4DC6C19E404A251B27933E16B399A15219D7D129B`. |

## Manueller UAT-Fokus

- Output, Breaker und Placer ohne Controller platzieren und rechtsklicken.
- Erwartung: Statusmeldung zeigt `kein Controller`.
- Danach Controller verbinden und pruefen, dass der Status auf `laeuft` wechselt.
