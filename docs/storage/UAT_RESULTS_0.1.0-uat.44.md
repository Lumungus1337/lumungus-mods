# Lumungus Storage UAT Results 0.1.0-uat.44

Datum: 02.09.2026

## Ergebnis

`AUTOMATED_PASS`

Der Haenger beim Rechtsklick auf einen nicht verbundenen Wireless Inventaranschluss ist behoben. Die bisherige kubische Umgebungssuche konnte bei der multidimensionalen Stufe rund 135 Millionen Blockpositionen pruefen. Wireless Inventaranschluesse und Wireless Storage Controller verwenden jetzt ausschliesslich registrierte, geladene Gegenstellen oder echte Rohrnetzverbindungen. Der Rechtsklick selbst startet keine Verbindungssuche mehr und zeigt den vorhandenen Status sofort an.

## Automatisierte Pruefungen

| Pruefung | Ergebnis | Details |
|---|---|---|
| Storage-GameTests | `PASS` | 36/36 Tests; neuer Regressionstest begrenzt die multidimensionale Verbindungsaktualisierung auf unter zwei Sekunden. |
| Machines-GameTests | `PASS` | 4/4 Tests. |
| Integration-GameTests | `PASS` | 3/3 Tom's-Migrationstests. |
| Storage-Client-GameTest | `PASS` | Minecraft 26.2 und JEI starteten mit UAT.44 erfolgreich. |
| Machines-Client-GameTest | `PASS` | Gemeinsamer Clientstart mit Core, Storage und Machines bestanden. |

## Manueller UAT

- Einen Wireless Inventaranschluss III ohne Wireless Storage Controller platzieren und rechtsklicken.
- Die Statusmeldung muss sofort erscheinen; Welt, Inventar und Eingabe duerfen nicht stehen bleiben.
- Den Connector anschliessend mit einem erreichbaren Wireless Storage Controller betreiben und die angeschlossene Kiste im Lager pruefen.
- Dieselbe Kontrolle mit Stufe I und II wiederholen.
