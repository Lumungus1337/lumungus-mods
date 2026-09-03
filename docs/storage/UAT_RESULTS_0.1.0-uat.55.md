# Lumungus Storage UAT 0.1.0-uat.55

## Focus

- Storage Breaker/Placer behavior inside compact constructions.
- Redstone pause behavior for work blocks.
- In-game diagnostics for work-block direction and target block.

## Changes

- Work blocks now treat redstone pause as a deliberate rear-side control signal.
- Side redstone that is part of a construction no longer pauses a down-facing breaker.
- Storage Breaker and Storage Placer status messages now show the currently targeted block.
- Direction messages now use readable localized names instead of raw translation keys.

## Verification

- `storageUatBundle`
- Storage GameTests: 47/47 passed.
- Machines GameTests: 4/4 passed.
- Integration GameTests: 3/3 passed.

## Notes

- UAT.55 was built successfully.
- Profile installation was held back because Minecraft was still running.
