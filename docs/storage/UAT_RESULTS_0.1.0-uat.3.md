# UAT Results 0.1.0-uat.3

Gesamtstatus: `AUTOMATED_PASS`

Der Coding- und automatisierte Server-UAT ist bestanden. Visuelle Client- und Multiplayer-Schritte bleiben vor einer oeffentlichen Freigabe offen.

## Build

- Java: Temurin `25.0.4.1`
- Kommando: `./gradlew clean build storageUatBundle --console=plain`
- Ergebnis: `PASS`
- JUnit: 11 von 11 Tests bestanden
- Server-GameTests: 4 von 4 Tests bestanden
- Bundle: `build/uat/lumungus-storage-0.1.0-uat.3.zip`

`storageUatBundle` ist jetzt an die Core- und Storage-Checks gekoppelt. Ein Kandidat mit roten Tests kann nicht mehr erfolgreich paketiert werden.

## Behobene Befunde

- `UAT-04/UAT-17`: Drive Bays speichern Controller-Position und Netzwerk-ID. Ein naeher platzierter Controller uebernimmt keine bereits gebundene Bay. Nach tatsaechlicher Entfernung des Besitzers erfolgt eine kontrollierte Neuzuordnung ohne Verlust des Cell-Inhalts.
- `UAT-13/UAT-15`: Der Zutatenplaner verwendet Backtracking fuer mehrdeutige Rezepte und veraendert den angebotenen Ressourcenpool nicht.
- `UAT-14`: Nicht verschobene Shift-Craft-Ergebnisse bleiben im Result-Slot oder werden wie bei Vanilla kontrolliert gedroppt. Ein GameTest prueft die Gesamtmenge bei fast vollem Inventar und sofortigem Folge-Craft.
- `UAT-15/UAT-16/UAT-18`: Ein fehlgeschlagener Rezepttransfer laesst ein bestehendes Grid unveraendert. Dies ist mit einem echten Server-Spieler und geladenen BlockEntities geprueft.
- `UAT-19/UAT-20`: Ein Terminal ohne erreichbaren Controller verursacht beim Broadcast keine NullPointerException mehr.
- `PERF-01`: Netzwerkzustand wird in einem Durchlauf zusammengefasst und pro Spieltick gecacht. Persistente Bay-Links vermeiden verschachtelte Vollscans bei jedem Zugriff.

## Client-Preflight

Der Fabric-Client lud Minecraft 26.2, Fabric API, JEI, Lumungus Core und Lumungus Storage `0.1.0-uat.3` bis zum fertigen Resource-Reload. Beim anschliessenden automatisierten Aktivieren des OpenGL-Fensters stuerzten sowohl Temurin als auch Zulu nativ in `glfw.dll` ab. Der Absturz trat ausserhalb der JVM und erst bei der Desktop-Automation auf; deshalb wurde kein visueller Client-UAT als bestanden gewertet.

## Noch offen

- Visuelle Kontrolle des 90er-Computer-Looks, Suche, Sortierung und Seitenwechsel
- Manueller JEI-Button-Transfer im Client
- Speichern, kompletter Client-Neustart und erneutes Laden der Testwelt
- Gleichzeitiger Zugriff mit zwei echten Spielern

Eine oeffentliche Release-Freigabe erfolgt erst, wenn diese Schritte ebenfalls `PASS` sind.
