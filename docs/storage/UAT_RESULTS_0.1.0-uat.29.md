# Lumungus Storage UAT Results 0.1.0-uat.29

Datum: 01.09.2026

## Ziel

Dieser Kandidat macht Wireless-Statusmeldungen im Spiel hilfreicher:

- Wireless Storage Controller melden beim Rechtsklick die Position des verbundenen Storage Controllers.
- Wireless Inventaranschluesse melden beim Rechtsklick weiter die gefundene Inventaranzahl und zusaetzlich die Controller-Position.
- Die Positionsanzeige ist gemeinsam gekapselt, damit spaetere Wireless-Bloecke dieselbe Darstellung nutzen koennen.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | Vollstaendiger Release-Build erfolgreich. |
| Server-GameTests | `PASS` | Storage 28/28 und Integration 3/3 erfolgreich. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.29.zip` erstellt. SHA256: `74E7180058FAAAB33C4B03B2365973ACED6C81742A74585542DC5AC3F506BC61`. |

## Manueller UAT-Fokus

- Wireless Storage Controller I/II/III mit einem Lager verbinden und rechtsklicken.
- Erwartung: Die Chatmeldung nennt die Controller-Position.
- Wireless Inventaranschluss I/II/III ueber einen Wireless Storage Controller verbinden und rechtsklicken.
- Erwartung: Die Chatmeldung nennt Inventaranzahl und Controller-Position.
