# scripts/compile.ps1
# Compiles all Java files in src/ and outputs to out/

$srcDir = "src"
$outDir = "out"

if (-not (Test-Path $outDir)) {
    Write-Host "Creating output directory: $outDir"
    New-Item -ItemType Directory -Path $outDir | Out-Null
}

Write-Host "Searching for Java files in $srcDir..."
$javaFiles = Get-ChildItem -Path $srcDir -Filter *.java -Recurse | Select-Object -ExpandProperty FullName

if ($javaFiles) {
    Write-Host "Compiling $($javaFiles.Count) files..."
    # Running javac from the project root to ensure package 'src.xxx' is resolved correctly
    javac -d $outDir -cp . $javaFiles
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Compilation successful! Classes are in $outDir" -ForegroundColor Green
    } else {
        Write-Error "Compilation failed with exit code $LASTEXITCODE."
    }
} else {
    Write-Warning "No .java files found in $srcDir."
}
