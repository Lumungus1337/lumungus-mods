# Lumungus Storage UAT Results 0.1.0-uat.32

Datum: 01.09.2026

## Ziel

Dieser Kandidat repariert den Client-UAT-Smoke fuer die neue Wireless-Testabdeckung:

- Der Dimension-Wireless-Test bleibt ausserhalb der Kurzdistanz.
- Die Teststruktur bleibt jetzt innerhalb der Minecraft-Registry-Grenze von 128 Bloecken.
- Dadurch kann der Client-Smoke wieder alle Testdefinitionen synchronisieren und das Terminal laden.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | Vollstaendiger Release-Build erfolgreich. |
| Server-GameTests | `PASS` | Storage 28/28 und Integration 3/3 erfolgreich. |
| Client-Smoke | `PASS` | Crafting Terminal oeffnet im echten Client und erzeugt Screenshots. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.32.zip` erstellt. SHA256: `8F35E12C6B1F470F582871BF7FBE34A6677C50A59898D6938B49E19B51350BFD`. |

## Manueller UAT-Fokus

- Crafting Terminal oeffnen und Suchfeld-Hotkeys pruefen.
- Wireless Inventaranschluss II ausserhalb kurzer Distanz pruefen.
- Sicherstellen, dass keine doppelte alte Lumungus-Version im Modrinth-Profil geladen wird.
