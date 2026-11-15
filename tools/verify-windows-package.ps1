param(
    [string]$ArchivePath = 'app/build/distributions/Tetris-Windows-x64.zip'
)
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
if (-not [IO.Path]::IsPathRooted($ArchivePath)) {
    $ArchivePath = Join-Path $taskRoot $ArchivePath
}
$taskArchive = (Resolve-Path -LiteralPath $ArchivePath).Path
$taskExtract = Join-Path $taskRoot ('app/build/package-smoke/launch test-' + [guid]::NewGuid().ToString('N'))
Add-Type -AssemblyName System.IO.Compression.FileSystem
[IO.Compression.ZipFile]::ExtractToDirectory($taskArchive, $taskExtract)
$taskApp = Join-Path $taskExtract 'Tetris'
$taskExe = Join-Path $taskApp 'Tetris.exe'
foreach ($taskRequired in @('Tetris.exe', 'runtime/bin/server/jvm.dll', 'app/Tetris.cfg')) {
    if (-not (Test-Path -LiteralPath (Join-Path $taskApp $taskRequired) -PathType Leaf)) {
        throw "Missing packaged file: $taskRequired"
    }
}
$taskBytes = (Get-ChildItem -LiteralPath $taskApp -Recurse -File | Measure-Object -Property Length -Sum).Sum
if ($taskBytes -gt 500000000) { throw "Unpacked distribution exceeds 500 MB (500,000,000 bytes): $taskBytes bytes" }

# The native EXE must embed the project's purple/cyan icon, rather than a generic Java icon.
Add-Type -AssemblyName System.Drawing
$taskIcon = [Drawing.Icon]::ExtractAssociatedIcon($taskExe)
if ($null -eq $taskIcon) { throw 'EXE has no extractable icon' }
$taskBitmap = $taskIcon.ToBitmap()
try {
    $taskPurple = $false; $taskCyan = $false
    for ($taskY = 0; $taskY -lt $taskBitmap.Height; $taskY++) {
        for ($taskX = 0; $taskX -lt $taskBitmap.Width; $taskX++) {
            $taskColor = $taskBitmap.GetPixel($taskX, $taskY)
            if ($taskColor.A -gt 128 -and $taskColor.R -gt 130 -and $taskColor.B -gt 170 -and $taskColor.G -lt 140) { $taskPurple = $true }
            if ($taskColor.A -gt 128 -and $taskColor.R -lt 90 -and $taskColor.G -gt 150 -and $taskColor.B -gt 150) { $taskCyan = $true }
        }
    }
    if (-not ($taskPurple -and $taskCyan)) { throw 'EXE icon does not contain the project icon colors' }
} finally { $taskBitmap.Dispose(); $taskIcon.Dispose() }

$taskProcess = $null
$taskWindowProcess = $null
$taskChildIds = [Collections.Generic.HashSet[int]]::new()
try {
    $taskProcess = Start-Process -FilePath $taskExe -WorkingDirectory $taskApp -WindowStyle Hidden -PassThru `
        -RedirectStandardOutput (Join-Path $taskExtract 'stdout.log') `
        -RedirectStandardError (Join-Path $taskExtract 'stderr.log')
    # Retain a native handle before exit so Windows PowerShell can read ExitCode reliably.
    $null = $taskProcess.Handle
    $taskDeadline = [DateTime]::UtcNow.AddSeconds(30)
    $taskStarted = $false
    while ([DateTime]::UtcNow -lt $taskDeadline) {
        $taskProcess.Refresh()
        if ($taskProcess.HasExited) { throw "Packaged app exited before startup: $($taskProcess.ExitCode)" }
        # jpackage on Windows may host JavaFX in a child launcher process.
        $taskChildren = @(Get-CimInstance Win32_Process -Filter "ParentProcessId = $($taskProcess.Id)")
        $taskCandidates = @($taskProcess)
        foreach ($taskChild in $taskChildren) {
            if ($taskChild.ExecutablePath -eq $taskExe) {
                [void]$taskChildIds.Add([int]$taskChild.ProcessId)
                $taskCandidate = Get-Process -Id $taskChild.ProcessId -ErrorAction SilentlyContinue
                if ($null -ne $taskCandidate) { $taskCandidates += $taskCandidate }
            }
        }
        foreach ($taskCandidate in $taskCandidates) {
            $taskCandidate.Refresh()
            if (-not $taskCandidate.HasExited -and $taskCandidate.MainWindowHandle -ne 0 -and $taskCandidate.MainWindowTitle -eq 'Tetris') {
                $taskWindowProcess = $taskCandidate; $taskStarted = $true; break
            }
        }
        if ($taskStarted) { break }
        Start-Sleep -Milliseconds 250
    }
    if (-not $taskStarted) { throw 'Packaged app did not display its Tetris window within 30 seconds' }
    [void]$taskWindowProcess.CloseMainWindow()
    if (-not $taskProcess.WaitForExit(10000)) { throw 'Packaged app did not close normally' }
    if ($taskProcess.ExitCode -ne 0) { throw "Packaged app exit code: $($taskProcess.ExitCode)" }
} finally {
    foreach ($taskChildId in $taskChildIds) {
        $taskChildProcess = Get-Process -Id $taskChildId -ErrorAction SilentlyContinue
        if ($null -ne $taskChildProcess -and $taskChildProcess.Path -eq $taskExe) {
            Stop-Process -Id $taskChildId -Force
        }
    }
    if ($null -ne $taskProcess -and -not $taskProcess.HasExited) {
        Stop-Process -Id $taskProcess.Id -Force
    }
}
$taskReport = [ordered]@{
    archive = $taskArchive
    extractedBytes = $taskBytes
    extractedMiB = [math]::Round($taskBytes / 1MB, 2)
    extractedMB = [math]::Round($taskBytes / 1000000, 2)
    within500MB = $true
    embeddedIcon = $true
    bundledRuntime = $true
    startupAndNormalExit = $true
    executable = $taskExe
}
$taskReportDirectory = Join-Path $taskRoot 'app/build/reports/packaging'
[void][IO.Directory]::CreateDirectory($taskReportDirectory)
$taskReport | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $taskReportDirectory 'windows.json') -Encoding UTF8
$taskReport | ConvertTo-Json
