# Lumungus Storage UAT Results 0.1.0-uat.36

Datum: 01.09.2026

## Ergebnis

`AUTOMATED_SERVER_PASS`

Dieser Kandidat fuehrt ein einsetzbares Wireless-Netzwerkmodul ein. Ein Rechtsklick auf einen verbundenen Wireless Storage Controller praegt das Modul auf dessen exakte Dimension, Position und Netzwerk-ID. Gepraegte Module koennen in Lager-Output, Lager-Breaker, Lager-Placer und Autocrafter eingesetzt werden. Die Arbeitsbloecke verwenden dann das gebundene Lager auch ohne Rohrverbindung und fallen nicht auf ein zufaelliges nahes Lager zurueck.

Module lassen sich per Shift-Rechtsklick mit leerer Hand wieder entnehmen. Beim Abbau des Blocks wird ein eingesetztes Modul mitsamt Bindung gedroppt.

## Automatisierte Pruefungen

| Pruefung | Ergebnis | Details |
|---|---|---|
| Java-Kompilierung | `PASS` | Core, Storage und Machines mit gemeinsamer Funkmodul-API. |
| Server-GameTests | `PASS` | Storage: 32/32; ein isolierter Output exportiert ueber sein gepraegtes Modul exakt aus dem gebundenen Lager. |
| Vollstaendiger Build | `PASS` | Core, Storage, Machines und Integration wurden als getrennte JARs gebaut. |
| Client-Smoke | `PASS` | Clientstart mit JEI, Terminal-Suche und Hotkey-Fokus. |
| Tom's-Integration | `PASS` | 3/3 GameTests fuer den Uebergangsadapter. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.36.zip`, Core, Storage und Machines enthalten, SHA256 `ADB42E28B087FFCFECBB42BCF396C9EE11B9905A23709F8FCCE546E7C429D62D`. |

## Restliche manuelle Abnahme

Im echten Client werden das Praegen, Einsetzen, Entnehmen und der Modul-Drop fuer alle vier Host-Bloecke geprueft. Die bereits abgeschlossene Live-Lagermigration wird nicht erneut verlangt.
