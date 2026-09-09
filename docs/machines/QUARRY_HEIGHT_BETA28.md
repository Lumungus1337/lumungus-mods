# RailQuarry beta.28: adjustable mining height

Date: 2026-09-09

## Use

Open the Quarry normally, enter a height between 1 and 64, then press Apply or Enter.
The +/- controls apply their changes immediately. Height counts upwards from the Quarry's
own Y coordinate, including that level. Default height is 8, including for older saves.
Existing slots, the network card and the two inventory-based setting controls are retained.
The height control is in a side panel so the menu still fits a 240-pixel GUI viewport.

Height is saved in `MiningHeight` and copied when the Quarry advances. It is clamped to the
world ceiling. The fluid shield follows the height, retaining its existing one-block border.
This is not an adjustable downward mining depth or a change to the Quarry's physical model.

## Performance and validation

Mining and fluid-shield searches each examine at most 256 positions per work cycle.
Incomplete searches yield without advancing or consuming work fuel. The existing fluid
flood-drain limit is unchanged. Very wide/tall slices can therefore take multiple cycles.
Invalid height button packets and requests from players outside menu range are rejected.
The text field consumes inventory hotkeys while focused.

## Verified

- Seven server tests passed (six Quarry regression tests plus the runner's smoke test).
- Default 8; lower/upper clamping; saving and loading; old saves without the height key.
- Actual mining at height 1 and 16, with the block above the selected height preserved.
- A 65 x 64 empty slice takes 17 bounded mining scans; world ceiling clamping.
- Fluid shield reaches the selected upper boundary without using the old fixed height.
- Actual movement copies height and inventory; invalid/out-of-range menu requests fail.
- Client test opens the menu, enters 32, exercises the inventory hotkey, applies with Enter,
  and verifies both client synchronization and the server's stored height.
- Screenshot checked at 854 x 480: `images/quarry-height-beta28.png`.
- Both the dedicated test server and integrated test world shut down successfully.

Tested with Minecraft 26.2, Fabric Loader 0.19.5, Fabric API 0.158.0+26.2 and Lumungus
Core/Storage UAT.60. No survival world or installed Modrinth profile was modified.

## Related fixes found during testing

- Drop origin now uses `Vec3.atCenterOf`, replacing lookup of removed `BlockPos.getCenter`.
  This previously caused normal block drop calculation to return no result.
- Chunk-removal checking uses an already-loaded chunk only. It no longer asks the level to
  load the same chunk during shutdown/unload, which deadlocked the first regression run.

## Source and builds

The existing standalone project is now versioned under `work/railquarry-beta25`; its historical
directory name is retained. It has not been migrated into the Lumungus Machines module.
Build the root Core and Storage jars first, then run `gradlew.bat build runClientGameTest`
in the Quarry directory with JDK 25. Runtime and source jars are in its `build/libs` folder.
