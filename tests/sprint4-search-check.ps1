# PowerShell Test Script: Sprint 4 check (upload -> OCR -> Elasticsearch -> search "Hello")
# Run from the repository root after "docker compose up -d --build"
param(
    [string]$BaseUrl = "http://localhost/api/v1",
    [int]$TimeoutSeconds = 90
)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$HelloPdf = Join-Path $ScriptDir "sample-documents\HelloWorld.pdf"

Write-Host "=== Sprint 4: Upload HelloWorld.pdf, wait for OCR, search 'Hello' ===" -ForegroundColor Cyan

$response = curl.exe -s -X POST "$BaseUrl/documents/upload" -F "title=Sprint 4 Search Check" -F "file=@$HelloPdf;type=application/pdf"
$documentId = ($response | ConvertFrom-Json).id
if (-not $documentId) {
    Write-Host "Upload failed: $response" -ForegroundColor Red
    exit 1
}
Write-Host "Uploaded document $documentId" -ForegroundColor Green

# Wait until the OCR result is back (status REVIEW_PENDING)
$status = ""
$deadline = (Get-Date).AddSeconds($TimeoutSeconds)
while ((Get-Date) -lt $deadline) {
    $status = (curl.exe -s "$BaseUrl/documents/$documentId" | ConvertFrom-Json).currentStatus
    if ($status -eq "REVIEW_PENDING") { break }
    Start-Sleep -Seconds 2
}
Write-Host "Status: $status"
if ($status -ne "REVIEW_PENDING") {
    Write-Host "OCR did not finish within $TimeoutSeconds seconds" -ForegroundColor Red
    exit 1
}

# Full-text search and fuzzy search (one typo).
# Elasticsearch makes new entries searchable after its refresh interval (about 1 s), so retry a few times.
$failed = $false
foreach ($query in @("Hello", "Helo")) {
    $ids = @()
    for ($try = 1; $try -le 5; $try++) {
        $ids = @((curl.exe -s "$BaseUrl/documents/search?query=$query" | ConvertFrom-Json).id)
        if ($ids -contains $documentId) { break }
        Start-Sleep -Seconds 1
    }
    if ($ids -contains $documentId) {
        Write-Host "Search '$query': document $documentId found" -ForegroundColor Green
    } else {
        Write-Host "Search '$query': document $documentId NOT found (ids: $($ids -join ', '))" -ForegroundColor Red
        $failed = $true
    }
}

if ($failed) { exit 1 }
Write-Host "Sprint 4 check passed" -ForegroundColor Cyan
