# Tom's-Migration: sicherer Bestandsvergleich

Status: Read-only-Vergleich implementiert, konkrete Tom's-Anbindung als naechster Slice

## Ziel

Ein bestehendes Tom's-Simple-Storage-Lager wird nicht umgepackt. Kisten, Faesser, Shulkerboxen und alle darin gespeicherten Items bleiben an ihrer Position. Die Migration darf spaeter nur Netzwerkbloecke ersetzen und wird erst freigegeben, wenn der Bestand vor und nach der Umstellung exakt uebereinstimmt.

## Sicherheitsablauf

1. Der Tom's-Adapter liest das bestehende Netz ohne Schreibzugriff ein.
2. Er erzeugt einen Snapshot mit physischen Endpunkten, Slots und aggregierten Itemmengen.
3. Lumungus liest dieselben physischen Inventare ebenfalls nur lesend ein.
4. Der Vergleich meldet Metadatenabweichungen, fehlende Items und zusaetzliche Items getrennt.
5. Erst ein exakter Vergleich darf spaeter die Schaltflaeche fuer die eigentliche Blockkonvertierung freigeben.
6. Nach der Blockkonvertierung wird erneut gelesen und mit dem unveraenderten Ausgangssnapshot verglichen.

## Was als identisch gilt

- gleiche Anzahl kanonischer physischer Endpunkte
- gleiche Gesamtzahl erreichbarer Slots
- gleiche Itemtypen und gleiche Komponenten
- exakt gleiche Menge jeder Itemvariante
- keine fehlenden und keine zusaetzlichen Items

Komponenten gehoeren zur Identitaet. Ein normaler Pflasterstein und ein benannter Pflasterstein werden deshalb getrennt gezaehlt. Doppeltruhen und mehrfach angeschlossene Inventare muessen beide Adapter auf denselben kanonischen Endpunkt abbilden, damit sie nicht doppelt gezaehlt werden.

## Implementierter Stand

Das Paket `dev.lumungus.integration.migration` enthaelt die unveraenderlichen Snapshots, die komponentensichere Itemidentitaet und den Vergleichsbericht. Es enthaelt bewusst keine API zum Veraendern von Bloecken oder Inventaren.

Automatisierte Tests decken ab:

- exakte Uebereinstimmung bei 7.000.000 Items
- getrennte Meldung fehlender und zusaetzlicher Mengen
- Abweichungen bei Endpunkt- und Slotanzahl
- getrennte Behandlung von Items mit unterschiedlichen Komponenten

## Naechster Slice

Der versionsrobuste Blockkatalog fuer Tom's `2.4.1` ist vorbereitet. Er ordnet Controller, Terminals, Trims, Kabel und Kabel-Connectoren passenden Lumungus-Bloecken zu. Unbekannte Bloecke und noch nicht abgebildete Funktionsbloecke blockieren die Migration; ein Filing Cabinet wird wegen moeglicher eigener Inhalte gesondert als blockierend gemeldet.

Der Welt-Scanner kann inzwischen eine physisch zusammenhaengende Tom's-Komponente ab einem Startblock nur lesend erfassen. Er laedt keine Chunks nach, besitzt ein hartes Sicherheitslimit und verweigert die Freigabe bei unbekannten Bloecken, ungeladenen Grenzen oder einem abgebrochenen Scan. Fuer ein vollstaendig erfasstes Netz sammelt er angrenzende Fabric-Inventare, dedupliziert mehrfach beruehrte Inventare sowie Doppeltruhen und erzeugt den komponentensicheren Bestandssnapshot.

Der serverseitige Diagnosebefehl `/lumungus migration scan <x> <y> <z>` fuehrt den Nur-Lese-Scan an einem Tom's-Netzblock aus. Er meldet Blockanzahl, konvertierbare Bloecke, Blocker und bei einem vollstaendigen lokalen Netz Inventaranzahl, Slots, Gesamtmenge sowie Itemtypen. Fernconnectoren werden erkannt und blockieren die Freigabe, solange ihre entfernten Teilnetze noch nicht versionssicher verfolgt werden koennen.

Als Naechstes wird die konkrete Tom's-Version fuer Minecraft 26.2 als optionale Entwicklungsabhaengigkeit festgelegt und deren Kanalregister nur lesend angebunden. Schreibende Konvertierung, Journal und Rollback bleiben gesperrt, bis der vollstaendige Bericht am Welt-Backup erfolgreich abgenommen wurde.
