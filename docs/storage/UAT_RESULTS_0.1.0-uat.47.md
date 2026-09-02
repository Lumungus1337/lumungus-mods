# Lumungus Storage UAT Results 0.1.0-uat.47

## Ziel

Auto-Send ohne versteckte Shift-Bedienung eindeutig schaltbar und diagnostizierbar machen.

## Aenderungen

- Normaler Rechtsklick auf kabelgebundene und Wireless-Inventaranschluesse schaltet Auto-Send ein oder aus.
- Beim Einschalten wird sofort ein Transferlauf gestartet.
- Die Rueckmeldung zeigt `AUTO-SEND AN/AUS`, Controller-Verbindung, erkannte Inventare, erreichbare Drive Bays und die sofort verschobene Itemmenge.
- Ohne Controller bleibt der Modus aktiv und meldet sichtbar, dass er auf eine Verbindung wartet.
- Eine bereits hergestellte Wireless-Bindung wird nicht mehr geloescht, nur weil der Controller-Chunk voruebergehend entladen ist.
- Die GameTests pruefen den echten automatischen Tick-Transfer statt nur eines direkten Methodenaufrufs.

## Manueller UAT

1. Mit leerer Hand normal auf einen Inventaranschluss rechtsklicken.
2. Die Meldung muss `AUTO-SEND AN` und `Controller verbunden` anzeigen.
3. Mindestens ein Item in ein angrenzendes Inventar legen; die Meldung zeigt den Soforttransfer, weitere Items werden einmal pro Sekunde verschoben.
4. Erneut rechtsklicken; die Meldung muss `AUTO-SEND AUS` anzeigen.
5. Am Wireless-Anschluss wiederholen. Bei `KEIN CONTROLLER` zuerst den zugehoerigen Wireless Storage Controller laden beziehungsweise seine Lagerverbindung pruefen.

## Automatische Pruefung

- `PASS`: 39/39 Storage-GameTests, einschliesslich echter Tick-Transfers beider Anschlussarten.
- `PASS`: 4/4 Machines-GameTests.
- `PASS`: Storage- und Machines-Client-GameTests.
- `PASS`: Unit-Tests und UAT-Paketbau.
