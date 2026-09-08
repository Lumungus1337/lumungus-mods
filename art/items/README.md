# Lumungus item artwork

The source PNGs are generated artwork for the Lumungus copper/anthracite item set.
The network module is an emerald PCB with copper contact fingers and an antenna.
The wrench has an open jaw, copper shaft and anthracite grip.

Runtime textures are 32x32 transparent PNGs. Rebuild them from the repository root:

```powershell
./tools/Prepare-ItemTexture.ps1 -Source art/items/copper_wrench-source.png -Destination modules/lumungus-storage/src/main/resources/assets/lumungus_storage/textures/item/copper_wrench.png
./tools/Prepare-ItemTexture.ps1 -Source art/items/wireless_network_module-source.png -Destination modules/lumungus-storage/src/main/resources/assets/lumungus_storage/textures/item/wireless_network_module.png
./tools/Preview-StorageAssets.ps1
```

Nearest-neighbor reduction preserves the transparent background and hard pixel edges.
The contact sheet reviews the actual block-front references and the two item textures.
