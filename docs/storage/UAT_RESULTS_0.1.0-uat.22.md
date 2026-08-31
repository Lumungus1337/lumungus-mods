# Lumungus Storage UAT Results 0.1.0-uat.22

Datum: 31.08.2026

## Ziel

Dieser Kandidat fuegt Wireless Inventory Connectoren als drahtlose Lager-Endpunkte hinzu:

- Wireless Inventaranschluss I fuer kurze Distanz.
- Wireless Inventaranschluss II fuer gleiche Dimension.
- Wireless Inventaranschluss III fuer dimensionsuebergreifende Reichweite vorbereitet.
- Connectoren werden neben Kisten, Faesser oder kompatible Inventare gesetzt.
- Ein erreichbarer Wireless Storage Controller stellt die Verbindung zum Storage Controller her.
- Der Storage Controller zaehlt drahtlos angeschlossene Inventare wie normale physische Lager-Endpunkte.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | `:lumungus-storage:build`, `storageUatBundle` und `:lumungus-integration:build` erfolgreich. |
| Server-GameTests | `PASS` | Lumungus Storage: 25/25 erforderliche Tests bestanden. Lumungus Integration: 3/3 erforderliche Tests bestanden. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.22.zip`, SHA-256 `53012CB2CB3B35540363DBB4C49D87076937B4785EE07823DDD9B4C92FDF3FD1`. |

## Manueller UAT-Fokus

- Wireless Storage Controller I in Reichweite des Storage Controllers setzen.
- Wireless Inventaranschluss I neben eine entfernte Kiste setzen.
- Kisteninhalt im normalen Terminal und im tragbaren Interface suchen und entnehmen.
- Items ueber das Terminal einlagern und pruefen, ob sie in der drahtlosen Kiste landen koennen.
- Danach dieselben Grundtests fuer Stufe II und III wiederholen.
