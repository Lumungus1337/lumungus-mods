$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$assetRoot = Join-Path $PSScriptRoot '..\modules\lumungus-storage\src\main\resources\assets\lumungus_storage'
$entries = @()
foreach ($model in Get-ChildItem (Join-Path $assetRoot 'models\block') -Filter '*.json') {
    $data = Get-Content -LiteralPath $model.FullName -Raw | ConvertFrom-Json
    if ($data.textures.north -like 'lumungus_storage:*') {
        $texture = $data.textures.north.Substring('lumungus_storage:'.Length)
        $entries += [pscustomobject]@{ Name = $model.BaseName; Path = Join-Path $assetRoot "textures\$texture.png" }
    }
}
foreach ($name in @('copper_wrench', 'wireless_network_module')) {
    $entries += [pscustomobject]@{ Name = $name; Path = Join-Path $assetRoot "textures\item\$name.png" }
}
$sheet = [System.Drawing.Bitmap]::new(1000, [int]([Math]::Ceiling($entries.Count / 5.0) * 180))
$graphics = [System.Drawing.Graphics]::FromImage($sheet)
$font = [System.Drawing.Font]::new('Consolas', 9)
try {
    $graphics.Clear([System.Drawing.Color]::FromArgb(48, 50, 52))
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
    for ($i = 0; $i -lt $entries.Count; $i++) {
        $x = ($i % 5) * 200
        $y = [Math]::Floor($i / 5) * 180
        $sprite = [System.Drawing.Bitmap]::new([System.IO.Path]::GetFullPath($entries[$i].Path))
        try { $graphics.DrawImage($sprite, [System.Drawing.Rectangle]::new($x + 36, $y + 4, 128, 128)) }
        finally { $sprite.Dispose() }
        $graphics.DrawString($entries[$i].Name, $font, [System.Drawing.Brushes]::White,
            [System.Drawing.RectangleF]::new($x + 6, $y + 136, 188, 42))
    }
    $sheet.Save([System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\art\items\storage-asset-review.png')))
} finally {
    $font.Dispose()
    $graphics.Dispose()
    $sheet.Dispose()
}
