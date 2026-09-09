# UAT.60 - Device fronts and wrench rotation

Date: 2026-09-09

## Scope

- Preserve the designer's copper casing textures; add shallow model geometry to 14 directional Storage blocks and the Autocrafter.
- Controller: cooling fins. Terminal: screen frame and keyboard keys. Drive Bay: cartridge handles.
- Inventory connectors: coupling frame. Trim: twin rails. Output: arrow. Breaker: teeth. Placer: piston head.
- Wireless controllers: rising signal bars, with one/two/three green bars identifying the tier. Wireless inventory connectors: coupling frame and one/two/three tier markers.
- Autocrafter: raised 3x3 crafting motif.
- Existing pipes keep their automatic connections and geometry. All device models retain closed cube bodies.
- Regenerate model relief with `tools/Build-BlockReliefModels.ps1`.

## Wrench

- Right-click dismantles; Shift-right-click rotates.
- Rotation is handled by the wrench item as well as block interactions, because sneaking can bypass a block's interaction method.
- Horizontal devices cycle four directions; directional work blocks cycle all six.
- Rotating preserves the block entity and invalidates nearby network topology.
- Shift-right-click on an automatically connected pipe does not dismantle it.
- The Autocrafter is now included in the wrench-removable block tag.

## Verification

- Storage server: 54 tests passed, including a complete orientation cycle for every tagged Storage block and block-entity identity checks.
- Machines server: 5 tests passed, including Autocrafter rotation with the target quantity preserved.
- Integration server: 3 tests passed.
- Full `storageUatBundle` build passed, including unit checks and both client game tests.
- Both client game tests passed again after adding detailed model screenshots. Storage still exercises the terminal with JEI loaded.
- All 15 generated models passed closed-body and face-culling validation; client logs contain no missing model or texture errors.
- Storage gallery and Autocrafter screenshots inspected in game. See `images/uat60-block-relief.png` and `images/uat60-autocrafter-relief.png`.

## Boundaries

This change does not modify or deploy the separately built RailQuarry or Autotrader. Their models and wrench integration are not covered by these tests. No survival world is changed.
