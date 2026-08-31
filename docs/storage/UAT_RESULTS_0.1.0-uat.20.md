# Lumungus Storage UAT Results 0.1.0-uat.20

Datum: 31.08.2026

## Ziel

Dieser Kandidat macht die neu angelegten Geraetebloecke erstmals funktional:

- Lager-Output exportiert Items aus dem Storage-Netzwerk in ein angrenzendes Inventar.
- Lager-Breaker baut den Block direkt unter sich ab und lagert die Drops ein.
- Lager-Placer setzt einen Block aus dem Storage-Netzwerk direkt unter sich.
- Wireless Storage Controller I kann ein Storage-Netzwerk in kurzer Distanz erreichen und das Terminal-Menue oeffnen.
- Output, Breaker und Placer koennen per Rechtsklick mit einem Item gefiltert und per Shift-Rechtsklick leer wieder auf Automatik gesetzt werden.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | `:lumungus-storage:build`, `storageUatBundle` und `:lumungus-integration:build` erfolgreich. |
| Server-GameTests | `PASS` | Lumungus Storage: 23/23, Lumungus Integration: 3/3. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.20.zip`, SHA256 `D44965D830732D8C18859266BEF86B4E28340A31D8222AC99DFEBBDC3D5FE904`. |

## Manueller UAT-Fokus

- Output neben eine Kiste setzen, Filter setzen und pruefen, ob Items aus dem Lager exportiert werden.
- Breaker ueber einen Block setzen, optional Filter setzen und pruefen, ob Drops im Lager landen.
- Placer ueber Luft setzen, Block im Lager bereitstellen und pruefen, ob er unter dem Placer gesetzt wird.
- Wireless Storage Controller I in kurzer Distanz zum Lager platzieren und Terminal oeffnen.
