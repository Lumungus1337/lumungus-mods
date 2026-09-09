# Rail Quarry 0.7.0-beta.26 - Lumungus Storage Integration

## Bedienung

1. Ein Wireless-Netzwerkmodul per Rechtsklick auf den gewuenschten Wireless Storage Controller praegen.
2. Das Quarry-Menue oeffnen und das gepraegte Modul in Slot 51 legen. Das ist der siebte Slot der untersten Quarry-Reihe, direkt links neben den beiden Einstellknoepfen.
3. Mit leerer Hand auf die Quarry klicken, um den Status zu pruefen. `storage: linked` bestaetigt die aktive Verbindung.

Bei aktiver Verbindung lagert die Quarry Blockdrops und gefuellte Fluessigkeitseimer direkt im gebundenen Lumungus-Lager ein. Ist das Modul nicht erreichbar oder das Lager voll, verwendet die Quarry weiterhin ihre Shulkerbox-Logik. Nicht einlagerbare Reste werden in der Welt gedroppt und nicht geloescht.

Das Modul bleibt beim automatischen Vorruecken der Quarry erhalten und wird mit dem Blockinventar gespeichert.

## Teststatus

- Eigenstaendiger Build: bestanden
- Gemeinsamer Dedicated-Server-Start mit RailQuarry beta.26, Lumungus Core/Storage uat.40 und Fabric Loader 0.19.5: bestanden
- Manueller UAT in einer Spielwelt: offen

## Abhaengigkeiten

- Minecraft 26.2
- Fabric Loader 0.19.5 oder neuer
- Fabric API 0.158.0+26.2 oder neuer
- Lumungus Core 0.1.0-uat.40 oder neuer
- Lumungus Storage 0.1.0-uat.40 oder neuer
