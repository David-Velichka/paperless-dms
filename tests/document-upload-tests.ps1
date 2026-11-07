# PowerShell Test Script: Document Upload REST-API (Sprint 1)
param(
    [string]$BaseUrl = "http://localhost:8081/api/v1"
)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$SamplePdf = Join-Path $ScriptDir "sample-documents\sample.pdf"
$SampleTxt = Join-Path $ScriptDir "sample-documents\sample.txt"

Write-Host "=== Paperless DMS Document Upload API Tests ===" -ForegroundColor Cyan
Write-Host "Target: $BaseUrl`n"

# 1. GET /api/documents
Write-Host "--- 1. Testing GET /api/v1/documents ---" -ForegroundColor Yellow
try {
    $docs = Invoke-RestMethod -Uri "$BaseUrl/documents" -Method Get -ContentType "application/json"
    Write-Host "Found $($docs.Count) documents." -ForegroundColor Green
} catch {
    Write-Host "Request failed: $_" -ForegroundColor Red
}

# 2. POST /api/documents/upload with PDF
Write-Host "`n--- 2. Testing POST /api/v1/documents/upload with PDF ---" -ForegroundColor Yellow
try {
    $upload = curl.exe -s -X POST "$BaseUrl/documents/upload" -F "title=Sample Invoice 2026" -F "file=@$SamplePdf;type=application/pdf"
    Write-Host "Response: $upload" -ForegroundColor Green
} catch {
    Write-Host "Upload failed: $_" -ForegroundColor Red
}

# 3. Error case: non-PDF file
Write-Host "`n--- 3. Testing POST /api/v1/documents/upload with a non-PDF file (expect 415) ---" -ForegroundColor Yellow
$codeTxt = curl.exe -s -o /dev/null -w "%{http_code}" -X POST "$BaseUrl/documents/upload" -F "file=@$SampleTxt;type=text/plain"
Write-Host "HTTP Status (expected 415): $codeTxt" -ForegroundColor Green

# 4. Error case: Empty file
Write-Host "`n--- 4. Testing POST /api/v1/documents/upload with empty file (expect 400) ---" -ForegroundColor Yellow
$emptyFile = Join-Path $ScriptDir "empty_temp.tmp"
New-Item -ItemType File -Path $emptyFile -Force | Out-Null
$code = curl.exe -s -o /dev/null -w "%{http_code}" -X POST "$BaseUrl/documents/upload" -F "file=@$emptyFile"
Remove-Item $emptyFile -Force
Write-Host "HTTP Status (expected 400): $code" -ForegroundColor Green

# 5. GET /api/documents
Write-Host "`n--- 5. Testing GET /api/v1/documents (verification) ---" -ForegroundColor Yellow
$docsAfter = Invoke-RestMethod -Uri "$BaseUrl/documents" -Method Get -ContentType "application/json"
Write-Host "Found $($docsAfter.Count) documents." -ForegroundColor Green
$docsAfter | Format-Table -Property id, title, originalFilename, contentType, fileSize

Write-Host "`n=== Tests Finished ===" -ForegroundColor Cyan
