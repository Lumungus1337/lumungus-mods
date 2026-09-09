# RailQuarry

Private Fabric Rail Quarry mod.

## Target runtime

- Minecraft Java Edition: 26.2
- Fabric Loader: 0.19.5
- Fabric API: 0.158.0+26.2
- Fabric Loom: 1.17.20
- Java/JDK: 25
- Lumungus Core: 0.1.0-uat.42 or newer
- Lumungus Storage: 0.1.0-uat.42 or newer
- Mod version: 0.7.0-beta.28

## Adjustable mining height

Open the Quarry normally. Set a height from 1 to 64 in the side panel and use Apply or Enter.
The +/- buttons apply one-block adjustments immediately. Height is measured upwards from
the Quarry block, including its own Y level. Existing saves default to 8.
The setting survives saving and movement along the rails. Mining is capped at the world ceiling.
The fluid shield follows the selected height, including its existing one-block border.

Block-search and shield-search passes inspect at most 256 positions per work cycle each.
Large empty slices take several cycles to verify; the Quarry does not advance while a scan is pending.
The existing bounded fluid flood-drain behavior remains unchanged.

This directory remains a standalone build, not a migrated Lumungus Machines module.
Its historical directory name is retained; `gradle.properties` defines the actual version.

## Build

Use JDK 25:

First build Core and Storage jars in the repository root. `lumungus_version` in
`gradle.properties` selects those local jars (currently UAT.60).

```powershell
.\gradlew.bat clean build
.\gradlew.bat runClientGameTest
```

The release JAR is written to `build/libs/`.
