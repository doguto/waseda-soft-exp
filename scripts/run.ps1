# scripts/run.ps1
# Runs JabberServer or GUIClient

param (
    [Parameter(Mandatory=$true)]
    [ValidateSet("server", "client")]
    [string]$Target
)

$outDir = "out"
$libDir = "lib"

if (-not (Test-Path $outDir)) {
    Write-Error "Output directory '$outDir' not found. Please run scripts/compile.ps1 first."
    exit 1
}

$cp = "$outDir;$libDir/*"

if ($Target -eq "server") {
    Write-Host "Starting JabberServer..." -ForegroundColor Cyan
    java -cp $cp src.server.JabberServer
} else {
    Write-Host "Starting GUIClient..." -ForegroundColor Magenta
    java -cp $cp src.client.GUIClient
}
