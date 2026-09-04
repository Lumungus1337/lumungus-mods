# Lumungus Storage UAT 0.1.0-uat.56

## Focus

- Work-block usability after the first live storage migration.
- Clearer diagnostics for Storage Output, Storage Breaker, and Storage Placer.
- Wireless module visibility on functional work blocks.

## Changes

- Storage Output status now shows the block or inventory in its working direction.
- Work-block redstone pause status now names the rear control side.
- Storage Breaker and Storage Placer now accept only block items as filters.
- Shift-right-click with an empty hand opens the wireless module slot on Output, Breaker, and Placer when no filter needs clearing or a module is installed.
- Tooltips now describe wireless module slots, block filters, work direction, and rear-side redstone pause behavior.

## Verification

- `storageUatBundle`
- Storage GameTests: 47/47 passed.
- Machines GameTests: 4/4 passed.
- Integration GameTests: 3/3 passed.
- Client launch checks completed for Storage and Machines.

## Notes

- Windows performance counter warnings and the JEI optional mixin warning appeared again and are non-fatal.
- UAT.56 was built successfully for all four Lumungus modules.
