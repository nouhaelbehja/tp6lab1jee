$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$projectRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent

function Export-LogImage([string]$Title, [string[]]$Lines, [string]$FileName) {
    $font = [System.Drawing.Font]::new('Consolas', 14)
    $titleFont = [System.Drawing.Font]::new('Segoe UI', 18, [System.Drawing.FontStyle]::Bold)
    $width = 1500
    $height = 125 + 29 * $Lines.Count
    $bitmap = [System.Drawing.Bitmap]::new($width, $height)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    try {
        $graphics.Clear([System.Drawing.Color]::FromArgb(20, 26, 36))
        $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
        $graphics.DrawString($Title, $titleFont, [System.Drawing.Brushes]::White, 28, 20)
        $graphics.DrawString('Export du journal reel - TP6 / Maven', $font, [System.Drawing.Brushes]::LightGray, 28, 62)
        $y = 108
        foreach ($line in $Lines) {
            $brush = [System.Drawing.Brushes]::Gainsboro
            if ($line -match 'BUILD SUCCESS|Failures: 0, Errors: 0') { $brush = [System.Drawing.Brushes]::LightGreen }
            $graphics.DrawString($line, $font, $brush, 28, $y)
            $y += 29
        }
        $bitmap.Save((Join-Path $PSScriptRoot $FileName), [System.Drawing.Imaging.ImageFormat]::Png)
    } finally {
        $graphics.Dispose()
        $bitmap.Dispose()
        $font.Dispose()
        $titleFont.Dispose()
    }
}

$report = Get-Content (Join-Path $projectRoot 'target/surefire-reports/org.example.PersistenceTest.txt')
Export-LogImage 'Tests JPA / Hibernate - H2' $report '01.png'
$buildLog = Get-Content (Join-Path $projectRoot 'target/build-execution.log')
$start = -1
for ($i = 0; $i -lt $buildLog.Count; $i++) {
    if ($buildLog[$i] -match '^\[INFO\] Results:') { $start = $i; break }
}
if ($start -lt 0) { throw 'Resume Maven absent du journal.' }
$summary = $buildLog[$start..($buildLog.Count - 1)] | ForEach-Object {
    $_.Replace($projectRoot + '\', '')
}
Export-LogImage 'Compilation et generation du WAR' $summary '02.png'
