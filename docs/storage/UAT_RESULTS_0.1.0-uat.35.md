# Lumungus Storage UAT Results 0.1.0-uat.35

Datum: 01.09.2026

## Ergebnis

`AUTOMATED_CLIENT_AND_SERVER_PASS`

Ein gebundenes tragbares Storage Interface oeffnet bei einer veralteten oder ungueltigen Bindung kein anderes Lager in der Naehe mehr. Ungebundene Interfaces duerfen weiterhin automatisch ein erreichbares Lager verwenden. Output, Breaker und Placer sind nun auch ohne gesetzten Filter in einem gemeinsamen Server-GameTest abgedeckt.

## Automatisierte Pruefungen

| Pruefung | Ergebnis | Details |
|---|---|---|
| Java-/Mod-Build | `PASS` | Lumungus Core, Storage und Integration. |
| Server-GameTests | `PASS` | Storage: 31/31; ungefilterte Arbeitsbloecke und feste Interface-Bindung eingeschlossen. |
| Client-Smoke | `PASS` | Clientstart mit JEI, Terminal-Suche und Hotkey-Fokus. |
| Tom's-Integration | `PASS` | 3/3 GameTests fuer den Uebergangsadapter. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.35.zip`, SHA256 `FC75A0B15C34A078C1BCA36EE8C25D8051A7BCC2891F80ADB5814BE7ECDEBD18`. |

## Restliche manuelle Abnahme

Die Bedienung im echten Client, Mehrspieler-Synchronisation und die visuelle Bewertung bleiben Teil des manuellen UAT. Die bereits abgeschlossene Live-Lagermigration wird nicht erneut verlangt.
