$ErrorActionPreference = "Stop"

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$classesDirectory = Join-Path $repositoryRoot "out\browser-classes"
$jarPath = Join-Path $repositoryRoot "docs\terminal-simulation.jar"

New-Item -ItemType Directory -Force $classesDirectory | Out-Null
Get-ChildItem -LiteralPath $classesDirectory -Filter "*.class" -Recurse |
    Remove-Item -Force

$sources = (Get-ChildItem (Join-Path $repositoryRoot "src") -Filter "*.java").FullName
javac --release 8 -encoding UTF-8 -d $classesDirectory $sources
if ($LASTEXITCODE -ne 0) {
    throw "Java compilation failed."
}

jar --create --file $jarPath --main-class TerminalSimulation -C $classesDirectory .
if ($LASTEXITCODE -ne 0) {
    throw "JAR packaging failed."
}

Write-Host "Browser JAR created at $jarPath"
