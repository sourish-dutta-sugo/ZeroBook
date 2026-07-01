<#
Generate Android launcher mipmaps and Play Store icon from a single source image.
Usage (from repo root):
  powershell -ExecutionPolicy Bypass -File scripts\generate_launcher_icons.ps1 \
    -SourceImage "app/src/main/res/drawable/logo_icon.png" \
    -PlayStoreOut "playstore/icon_512x512.png" \
    -RemoveLegacy

Requirements: ImageMagick installed (`magick` command available).
This script only writes files; it does NOT run `git commit`. You asked you'll commit changes yourself.
#>
param(
    [Parameter(Mandatory=$true)]
    [string]$SourceImage,

    [string]$ResRoot = "app/src/main/res",

    [string]$PlayStoreOut = "playstore/icon_512x512.png",

    [switch]$RemoveLegacy
)

function Ensure-Dir([string]$p){ if (-not (Test-Path $p)) { New-Item -ItemType Directory -Force -Path $p | Out-Null } }

if (-not (Test-Path $SourceImage)){
    Write-Error "Source image not found: $SourceImage"; exit 1
}

# Verify ImageMagick
$magick = Get-Command magick -ErrorAction SilentlyContinue
if (-not $magick) { Write-Error "ImageMagick 'magick' not found in PATH. Install ImageMagick and retry."; exit 2 }

# Target sizes map: mipmap folder -> size
$sizes = @{
    'mipmap-mdpi' = 48;
    'mipmap-hdpi' = 72;
    'mipmap-xhdpi' = 96;
    'mipmap-xxhdpi' = 144;
    'mipmap-xxxhdpi' = 192;
}

foreach ($k in $sizes.Keys) {
    $outDir = Join-Path $ResRoot $k
    Ensure-Dir $outDir
    $outFile = Join-Path $outDir 'ic_launcher.png'
    Write-Host "Generating $outFile (${sizes[$k]}x${sizes[$k]})"
    magick convert $SourceImage -resize ${sizes[$k]}x${sizes[$k]} $outFile
}

# Create round icons by copying xxxhdpi/xxhdpi as reasonable fallbacks
$xxx = Join-Path $ResRoot 'mipmap-xxxhdpi\ic_launcher.png'
$xx = Join-Path $ResRoot 'mipmap-xxhdpi\ic_launcher.png'
if (Test-Path $xxx) { Copy-Item $xxx -Destination (Join-Path $ResRoot 'mipmap-xxxhdpi\ic_launcher_round.png') -Force }
if (Test-Path $xx)  { Copy-Item $xx  -Destination (Join-Path $ResRoot 'mipmap-xxhdpi\ic_launcher_round.png') -Force }

# Generate Play Store 512x512
$playDir = Split-Path $PlayStoreOut -Parent
if ($playDir) { Ensure-Dir $playDir }
Write-Host "Generating Play Store icon: $PlayStoreOut (512x512)"
magick convert $SourceImage -resize 512x512 $PlayStoreOut

Write-Host "Done. Files written under $ResRoot and $PlayStoreOut."

if ($RemoveLegacy.IsPresent) {
    $legacy = Join-Path $ResRoot 'drawable\zerobook_icon.png'
    if (Test-Path $legacy) {
        Write-Host "Removing legacy file: $legacy"
        Remove-Item $legacy -Force
    } else { Write-Host "No legacy file found at $legacy" }
}

Write-Host "Next: run './gradlew clean assembleDebug' and install the APK to verify launcher and splash behavior. Commit changes when ready." 
