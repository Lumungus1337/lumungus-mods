# Lumungus Storage UAT Results 0.1.0-uat.41

Datum: 02.09.2026

## Ergebnis

`AUTOMATED_PASS`

Die ersten finalen Designer-Texturen sind fuer Storage Controller und Crafting Terminal eingebaut. Beide Bloecke besitzen eine horizontale Frontausrichtung, zeigen beim Platzieren zum Spieler und lassen sich per Shift-Rechtsklick mit dem Kupfer-Schraubenschluessel drehen.

## Automatisierte Pruefungen

| Pruefung | Ergebnis | Details |
|---|---|---|
| Storage-GameTests | `PASS` | 34/34 Tests; neue Nord-Ost-Rotation und gespeicherte Displayrichtung bestanden. |
| Storage-Client-GameTest | `PASS` | Client, JEI, Texturatlas und gerichtete Blockmodelle wurden ohne fehlende Modell- oder Texturressourcen geladen. |
| Machines-GameTests | `PASS` | 3/3 Autocrafter-Tests bestanden. |
| Machines-Client-GameTest | `PASS` | Gemeinsamer Clientstart mit Core, Storage und Machines bestanden. |
| Kompilierung | `PASS` | Controller- und Terminal-Zustandslogik kompiliert mit Java 25. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.41.zip`, SHA256 `BA6984528683DEE00E3E727259E0F3DA0054DB5701A4FA192F1E4257D8B9D6A2`. |

## Manueller UAT

- Controller und Crafting Terminal aus jeder Spielerblickrichtung platzieren und pruefen, dass das Display zum Spieler zeigt.
- Beide Bloecke mehrfach per Shift-Rechtsklick mit dem Kupfer-Schraubenschluessel drehen.
- Normalen Schraubenschluessel-Rechtsklick weiterhin auf sofortige Demontage pruefen.
- Front, Rueckseite, beide Seiten, Ober- und Unterseite beider Bloecke im Spiel visuell abnehmen.
