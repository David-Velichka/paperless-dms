# PowerShell Test Script: Sprint 3 check (upload -> RabbitMQ -> OCR worker log entry)
# Run from the repository root after "docker compose up -d --build"
param(
    [string]$BaseUrl = "http://localhost/api/v1"
)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$SamplePdf = Join-Path $ScriptDir "sample-documents\sample.pdf"

Write-Host "=== Sprint 3: Upload PDF via Nginx and check OCR worker log ===" -ForegroundColor Cyan

$response = curl.exe -s -X POST "$BaseUrl/documents/upload" -F "title=Sprint 3 Queue Check" -F "file=@$SamplePdf;type=application/pdf"
Write-Host "Upload response: $response" -ForegroundColor Green
$documentId = ($response | ConvertFrom-Json).id

Start-Sleep -Seconds 2
Write-Host "`n--- paperless-ocr-worker log for document $documentId ---" -ForegroundColor Yellow
docker compose logs paperless-ocr-worker | Select-String "Received OCR job for document $documentId "
