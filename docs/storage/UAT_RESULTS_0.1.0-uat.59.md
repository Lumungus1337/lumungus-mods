# UAT.59 - Recognizable network card and wrench

- Replaced the network module's vanilla echo-shard placeholder with its own transparent PCB/antenna sprite.
- Replaced the thin wrench sprite with a visible open-jaw copper wrench and anthracite grip.
- Wrench now uses the vanilla handheld model parent for tool positioning.
- Runtime sprites are 32x32. Generated sources and reproducible preparation tools are included outside runtime resources.
- Reviewed Storage block fronts in art/items/storage-asset-review.png. Block textures and geometry are unchanged in this update.
- Client test inventory now includes both items for visual inspection.

Validation: bundle build, module checks and Storage client test passed with JEI.
Inspected both icons in the rendered hotbar at 854x480; both render with transparency
and their intended silhouettes. PNG dimensions, nonempty alpha and texture references
were also checked. Server/gameplay logic is unchanged; server game tests passed in
UAT.58 and are not repeated for these artwork changes.

Deployment: not installed during this update because the live Modrinth profile is running.
