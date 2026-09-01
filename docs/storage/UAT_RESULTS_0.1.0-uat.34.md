# Lumungus Storage UAT Results 0.1.0-uat.34

Datum: 01.09.2026

## Ziel

Dieser Kandidat schliesst einen sichtbaren Phase-1-Punkt fuer die Rohrpost:

- Rohrpostrohre verbinden weiterhin Controller, Terminals, Bays, Connectoren und Arbeitsbloecke.
- Tote Seitenarme werden nicht mehr automatisch als aktive Rohrverbindung gerendert.
- Normale Kisten/Faesser werden nur ueber Connectoren ins Netzwerk geholt, nicht direkt durch Rohrkontakt.

## Automatisierter Stand

| Bereich | Ergebnis | Notiz |
|---|---|---|
| Java/Kompilierung | `PASS` | Vollstaendiger Release-Build erfolgreich. |
| Server-GameTests | `PASS` | Storage: 29/29; neuer Test prueft tote Rohrpost-Seitenarme. |
| Client-Smoke | `PASS` | Crafting Terminal bleibt beim Inventar-Hotkey offen, wenn das Suchfeld fokussiert ist. |
| Integration | `PASS` | Tom's-Integration: 3/3 GameTests bestanden. |
| UAT-Bundle | `PASS` | `build/uat/lumungus-storage-0.1.0-uat.34.zip`, SHA256 `02C268E3BC18AF95593CFCA757105505CFC43B2B7EA8235B7E676458C350693B`. |

## Manueller UAT-Fokus

- Rohrpostrohr neben Controller und Inventaranschluss setzen.
- Einen losen Seitenarm ohne Ziel daneben setzen.
- Erwartung: Hauptstrecke verbindet sich, der tote Seitenarm erscheint nicht als aktive Verbindung.
