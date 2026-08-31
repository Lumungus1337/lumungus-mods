# Lumungus Storage UAT Results 0.1.0-uat.23

Datum: 31.08.2026

## Ziel

Dieser Kandidat stabilisiert Wireless-Reichweiten:

- Storage Controller melden sich serverseitig fuer Wireless-Suchen an.
- Wireless Storage Controller melden sich serverseitig an und aktualisieren Links regelmaessig.
- Wireless Links speichern nun Controller-Dimension, Controller-Position und Netzwerk-ID.
- Wireless Storage Controller II kann geladene Storage Controller in derselben Dimension jenseits der Kurzdistanz finden.
- Wireless Inventaranschluss II kann geladene Wireless Storage Controller in derselben Dimension jenseits der Kurzdistanz finden.
- Wireless Inventaranschluss III und Wireless Storage Controller III sind fuer dimensionsuebergreifende Links mit Dimensions-ID vorbereitet.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | `:lumungus-storage:build`, `storageUatBundle` und `:lumungus-integration:build` erfolgreich. |
| Server-GameTests | `PASS` | Lumungus Storage: 26/26 erforderliche Tests bestanden. Lumungus Integration: 3/3 erforderliche Tests bestanden. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.23.zip`, SHA-256 `88745B4568AB7531B1E6B7D8E423617334BE3FF7A04DF27149A4978FF8F6105C`. |

## Manueller UAT-Fokus

- Minecraft/Modrinth komplett schliessen und alte `uat.21`/`uat.22` Lumungus-JARs entfernen, bevor `uat.23` getestet wird.
- Wireless Storage Controller II deutlich weiter als 32 Bloecke vom Storage Controller entfernt setzen.
- Wireless Inventaranschluss II deutlich weiter als 32 Bloecke vom Wireless Storage Controller entfernt neben eine Kiste setzen.
- Pruefen, ob die Kiste im normalen Crafting Terminal und im tragbaren Storage Interface sichtbar ist.
- Danach Stufe III in geladenen Bereichen erneut pruefen; echte Dimensionswechsel bleiben manueller UAT-Fokus.
