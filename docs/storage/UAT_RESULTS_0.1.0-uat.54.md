# Lumungus Storage UAT Results 0.1.0-uat.54

## Ziel

Der Storage Breaker muss den Block bearbeiten, an dessen Flaeche er platziert wurde. Ein geduckter Rechtsklick mit dem Kupferschraubenschluessel darf den Breaker nicht abbauen, sondern muss seine Arbeitsrichtung aendern.

## Aenderungen

- Die Platzierungsrichtung des Breakers zeigt ausdruecklich zur angeklickten Stuetzflaeche zurueck.
- `Shift` + Rechtsklick mit dem Kupferschraubenschluessel dreht den Breaker durch alle sechs Arbeitsrichtungen.
- Ein normaler Rechtsklick mit dem Kupferschraubenschluessel baut den Breaker weiterhin sofort ab.
- Der direkte Werkzeugpfad und die Blockinteraktion verwenden dieselbe Drehfunktion.

## Automatisierte Ergebnisse

- `PASS`: Storage-Kompilierung und Unit-Tests.
- `PASS`: Platzierungsrichtung fuer alle sechs Blockseiten.
- `PASS`: Geduckter Schraubenschluessel-Klick laesst den Breaker stehen und aendert seine Richtung.
- `PASS`: Alle 46 Storage-GameTests.
- `PASS`: Storage- und Machines-Client-GameTests.
- `PASS`: Machines- und Integration-GameTests.
- `PASS`: UAT-Paket mit Core, Storage, Machines und Integration gemeinsam in Version UAT.54.

## Manueller UAT

1. Einen Breaker gegen die Oberseite eines Testblocks setzen.
2. Sicherstellen, dass kein Redstone-Signal am Breaker anliegt und er mit dem Lager verbunden ist.
3. Kontrollieren, dass der Block direkt unter dem Breaker abgebaut wird.
4. Einen zweiten Breaker seitlich gegen einen Testblock setzen und kontrollieren, dass dieser angeklickte Block abgebaut wird.
5. Einen Breaker mit `Shift` + Rechtsklick und dem Kupferschraubenschluessel anklicken.
6. Kontrollieren, dass der Breaker stehen bleibt und die angezeigte Arbeitsrichtung wechselt.
7. Den Breaker ohne `Shift` mit dem Kupferschraubenschluessel anklicken und kontrollieren, dass er sofort abgebaut wird.
