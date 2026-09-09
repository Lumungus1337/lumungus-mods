$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot
$storage = Join-Path $repo 'modules/lumungus-storage/src/main/resources/assets/lumungus_storage/models/block'
$machines = Join-Path $repo 'modules/lumungus-machines/src/main/resources/assets/lumungus_machines/models/block'

function Add-Relief([double]$x1, [double]$y1, [double]$x2, [double]$y2, [string]$material = 'copper', [double]$depth = 0.3) {
    $faces = [ordered]@{}
    foreach ($side in @('north','south','east','west','up','down')) {
        $faces[$side] = @{ texture = "#relief_$material"; uv = @(6,6,7,7) }
    }
    $script:elements.Add(@{ from = @($x1,$y1,(-$depth)); to = @($x2,$y2,0); faces = $faces })
}

$models = @(Get-ChildItem $storage -Filter '*.json' | Where-Object { $_.BaseName -notmatch 'pipe|cable' })
$models += Get-Item (Join-Path $machines 'autocrafter.json')
foreach ($file in $models) {
    $model = Get-Content -LiteralPath $file.FullName -Raw | ConvertFrom-Json -AsHashtable
    if ($model.parent -ne 'minecraft:block/cube') { continue }
    $model.textures.relief_copper = 'minecraft:block/copper_block'
    $model.textures.relief_dark = 'minecraft:block/deepslate'
    $model.textures.relief_green = 'minecraft:block/oxidized_copper'
    $elements = [System.Collections.Generic.List[object]]::new()
    $faces = [ordered]@{}
    foreach ($side in @('north','south','east','west','up','down')) {
        $faces[$side] = @{ texture = "#$side"; uv = @(0,0,16,16); cullface = $side }
    }
    # Keep the full cube surface: relief never opens holes into adjacent blocks.
    $elements.Add(@{ from = @(0,0,0); to = @(16,16,16); faces = $faces })
    $name = $file.BaseName
    switch -Regex ($name) {
        '^crafting_terminal$' {
            Add-Relief 2 4 3 14; Add-Relief 13 4 14 14
            Add-Relief 3 13 13 14; Add-Relief 3 4 13 5
            foreach ($x in @(4,6,8,10)) { Add-Relief $x 2 ($x+1) 3 'green' }
        }
        '^storage_controller$' {
            Add-Relief 3 3 13 13 'dark' 0.15
            foreach ($x in @(4,6,8,10)) { Add-Relief $x 5 ($x+1) 12 }
            Add-Relief 4 3 6 4 'green'; Add-Relief 8 3 12 4
        }
        '^drive_bay$' {
            foreach ($y in @(3,6,9,12)) { Add-Relief 3 $y 5 ($y+1); Add-Relief 11 $y 13 ($y+1) }
        }
        '^inventory_connector$' {
            Add-Relief 4 4 5 12; Add-Relief 11 4 12 12
            Add-Relief 5 4 11 5; Add-Relief 5 11 11 12
        }
        '^inventory_trim$' {
            Add-Relief 2 5 14 6; Add-Relief 2 10 14 11
        }
        '^storage_output$' {
            Add-Relief 3 3 13 13 'dark' 0.15
            Add-Relief 4 7 11 9 'green'
            Add-Relief 9 5 10 11 'green'; Add-Relief 10 6 11 10 'green'; Add-Relief 11 7 12 9 'green'
        }
        '^storage_breaker$' {
            Add-Relief 3 3 13 13 'dark' 0.15
            Add-Relief 4 11 12 12; Add-Relief 4 4 12 5
            foreach ($x in @(4,7,10)) { Add-Relief $x 9 ($x+2) 11; Add-Relief $x 5 ($x+2) 7 }
        }
        '^storage_placer$' {
            Add-Relief 3 3 13 13 'dark' 0.15
            Add-Relief 4 4 12 6; Add-Relief 7 6 9 10
            Add-Relief 4 10 12 12 'green'
        }
        '^wireless_storage_controller_' {
            Add-Relief 3 3 13 13 'dark' 0.15
            $tier = if ($name.EndsWith('_short')) { 1 } elseif ($name.EndsWith('_dimension')) { 2 } else { 3 }
            for ($i=0; $i -lt 3; $i++) {
                $material = if ($i -lt $tier) { 'green' } else { 'copper' }
                Add-Relief (4+$i*3) 5 (6+$i*3) (7+$i*2) $material
            }
        }
        '^wireless_inventory_connector_' {
            Add-Relief 3 5 4 13; Add-Relief 12 5 13 13
            Add-Relief 4 12 12 13; Add-Relief 4 5 12 6
            $tier = if ($name.EndsWith('_short')) { 1 } elseif ($name.EndsWith('_dimension')) { 2 } else { 3 }
            for ($i=0; $i -lt $tier; $i++) { Add-Relief (4+$i*3) 2 (6+$i*3) 3 'green' }
        }
        '^autocrafter$' {
            Add-Relief 3 3 13 13 'dark' 0.15
            foreach ($y in @(4,7,10)) { foreach ($x in @(4,7,10)) { Add-Relief $x $y ($x+2) ($y+2) 'green' } }
        }
    }
    $textureJson = ConvertTo-Json -InputObject $model.textures -Depth 5 -Compress
    $elementJson = ($elements | ForEach-Object { '    ' + (ConvertTo-Json -InputObject $_ -Depth 10 -Compress) }) -join ",`n"
    $json = "{`n  `"parent`": `"minecraft:block/cube`",`n  `"textures`": $textureJson,`n  `"elements`": [`n$elementJson`n  ]`n}`n"
    [System.IO.File]::WriteAllText($file.FullName, $json, [System.Text.UTF8Encoding]::new($false))
}
