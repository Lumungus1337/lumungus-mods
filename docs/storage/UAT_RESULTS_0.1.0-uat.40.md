# Lumungus Storage UAT Results 0.1.0-uat.40

Datum: 02.09.2026

## Ergebnis

`AUTOMATED_PASS`

Technische Rezepte, die bisher einen bestimmten verwitterten Kupferblock verlangten, verwenden jetzt die gemeinsame Gruppe `lumungus_core:copper_blocks`. Damit koennen normale, angelaufene, verwitterte und oxidierte Kupferbloecke sowie alle vier gewachsten Varianten eingesetzt werden.

## Automatisierte Pruefungen

| Pruefung | Ergebnis | Details |
|---|---|---|
| Storage-GameTests | `PASS` | 33/33 Tests bestanden. |
| Machines-GameTests | `PASS` | 3/3 Tests bestanden. |
| Client-GameTests | `PASS` | Fabric-Client und Autocrafter-Bedienung bestanden. |
| Rezeptladen | `PASS` | Minecraft lud die geaenderten Rezepte ohne Rezeptfehler. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.40.zip`, SHA256 `7A7873043579EADC717008E77D3FB008C4AD1FBE185AD5A9395D34492130E146`. |

## Manueller UAT

- Im Rezeptbuch oder in JEI pruefen, dass bei den betroffenen Rezeptpositionen alle acht Kupferblock-Zustaende durchwechseln.
- Je einen betroffenen Storage-Block und den Autocrafter mit einer beliebigen Kupferblock-Variante herstellen.
