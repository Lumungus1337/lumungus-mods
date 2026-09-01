# Lumungus Storage UAT Results 0.1.0-uat.37

Datum: 01.09.2026

## Ergebnis

`AUTOMATED_CLIENT_AND_SERVER_PASS`

Dieser Kandidat aktiviert den ersten echten Autocrafter-Arbeitszyklus. Ein Autocrafter mit gepraegtem Wireless-Netzwerkmodul findet ein passendes normales Crafting-Rezept, entnimmt dessen Zutaten aus dem exakt gebundenen Lager und lagert Ergebnis und Rezeptreste wieder ein. Zielmenge und Fortschritt werden beachtet; klare Maschinenzustaende melden fehlendes Modul, Controller, Rezept, Zutaten oder Ausgabekapazitaet.

## Automatisierte Pruefungen

| Pruefung | Ergebnis | Details |
|---|---|---|
| Java-Kompilierung und Unit-Tests | `PASS` | Core, Storage und Machines einschliesslich gemeinsamer Rezeptplanung. |
| Machines-GameTests | `PASS` | 2/2; der Autocrafter verarbeitet einen Eichenstamm aus einer Storage Cell zu vier Brettern und lagert sie zurueck. |
| Vollstaendiger Build | `PASS` | Core, Storage, Machines und Integration wurden als getrennte JARs gebaut. |
| Storage- und Integration-GameTests | `PASS` | Storage 32/32 und Tom's-Integration 3/3; Netzwerk-, Migrations- und Arbeitsblocktests bleiben gruen. |
| Client-Smoke | `PASS` | Clientstart mit JEI, Terminal-Suche und Hotkey-Fokus. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.37.zip`, Core, Storage und Machines enthalten. |

## Restliche manuelle Abnahme

Im echten Client werden Zielwahl, Statusmeldungen, exakte Zielmenge und Dauerbetrieb des Autocrafters geprueft. Mehrstufige Rezeptketten und die komfortable Rezeptwahl aus JEI folgen in einem separaten Ausbau.
