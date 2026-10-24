#!/bin/bash
# Bash Test Script: Sprint 3 check (upload -> RabbitMQ -> OCR worker log entry)
# Run from the repository root after "docker compose up -d --build"
BASE_URL=${1:-"http://localhost/api/v1"}
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "=== Sprint 3: Upload PDF via Nginx and check OCR worker log ==="

# Relative file path, so it also works with curl on Windows (Git Bash)
RESPONSE=$(cd "$SCRIPT_DIR" && curl -s -X POST "$BASE_URL/documents/upload" -F "title=Sprint 3 Queue Check" -F "file=@sample-documents/sample.pdf;type=application/pdf")
echo "Upload response: $RESPONSE"
DOCUMENT_ID=$(echo "$RESPONSE" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')

sleep 2
echo ""
echo "--- paperless-ocr-worker log for document $DOCUMENT_ID ---"
docker compose logs paperless-ocr-worker | grep "Received OCR job for document $DOCUMENT_ID "
