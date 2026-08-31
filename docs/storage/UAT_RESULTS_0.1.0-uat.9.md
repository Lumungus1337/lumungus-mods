# Lumungus Storage UAT Results 0.1.0-uat.9

Datum: 31.08.2026

## Ziel

Dieser Kandidat ergaenzt den bewussten Migrationsknopf im Crafting Terminal:

- Neuer `BAY`-Knopf im Terminal.
- Der Knopf verschiebt Items aus angeschlossenen physischen Inventaren in Drive Bays mit eingesetzten Storage Cells.
- Items werden nicht geloescht, wenn Cells fehlen oder voll sind; nicht verschiebbare Reste bleiben in den Quellinventaren.
- Der Transfer ist pro Klick begrenzt, damit grosse Survival-Lager nicht einen einzelnen Server-Tick zu lange blockieren.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | `./gradlew :lumungus-storage:build storageUatBundle` erfolgreich. |
| Server-GameTests | `PASS` | 14/14 Storage-GameTests bestanden; neuer Test prueft Kiste -> Drive Bay -> Cell. |
| UAT-Bundle | `PASS` | Paket erstellt: `build/uat/lumungus-storage-0.1.0-uat.9.zip`. |

## Manueller UAT-Fokus

- Eine Kiste oder ein Fass per Inventaranschluss/Rohrpost mit Controller und Crafting Terminal verbinden.
- Mindestens eine Drive Bay mit eingesetzter 16k Storage Cell im selben Netzwerk platzieren.
- Terminal oeffnen und `BAY` klicken.
- Erwartung: Items wandern in die Cell; bei vollem Ziel bleibt der Rest in der Kiste oder im Fass.
