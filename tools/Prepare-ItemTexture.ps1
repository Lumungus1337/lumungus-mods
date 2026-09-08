param(
    [Parameter(Mandatory)] [string] $Source,
    [Parameter(Mandatory)] [string] $Destination,
    [int] $Size = 32
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$inputImage = [System.Drawing.Bitmap]::new((Resolve-Path -LiteralPath $Source).Path)
try {
    if ($inputImage.GetPixel(0, 0).A -ne 0) { throw 'Expected a transparent source image.' }
    $outputImage = [System.Drawing.Bitmap]::new($Size, $Size)
    try {
        $graphics = [System.Drawing.Graphics]::FromImage($outputImage)
        try {
            $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
            $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
            $graphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceCopy
            $graphics.DrawImage($inputImage, [System.Drawing.Rectangle]::new(0, 0, $Size, $Size))
        } finally { $graphics.Dispose() }
        $outputImage.Save([System.IO.Path]::GetFullPath($Destination), [System.Drawing.Imaging.ImageFormat]::Png)
    } finally { $outputImage.Dispose() }
} finally { $inputImage.Dispose() }
