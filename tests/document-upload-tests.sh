#!/bin/bash
# Test Script: Document Upload REST-API (Sprint 1)
# Usage: ./document-upload-tests.sh [BASE_URL]

BASE_URL="${1:-http://localhost:8081/api}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SAMPLE_PDF="${SCRIPT_DIR}/sample-documents/sample.pdf"
SAMPLE_TXT="${SCRIPT_DIR}/sample-documents/sample.txt"

echo "=== Paperless DMS Document Upload API Tests ==="
echo "Target: ${BASE_URL}"
echo ""

echo "--- 1. Testing GET /api/documents ---"
curl -s -X GET "${BASE_URL}/documents" \
  -H "Accept: application/json" | grep -o '.*'
echo ""

echo "--- 2. Testing POST /api/documents/upload with PDF and title ---"
UPLOAD_RESP=$(curl -s -X POST "${BASE_URL}/documents/upload" \
  -F "title=Sample Invoice 2026" \
  -F "file=@${SAMPLE_PDF};type=application/pdf")
echo "Response: ${UPLOAD_RESP}"
echo ""

echo "--- 3. Testing POST /api/documents/upload without title (default filename) ---"
UPLOAD_TXT_RESP=$(curl -s -X POST "${BASE_URL}/documents/upload" \
  -F "file=@${SAMPLE_TXT};type=text/plain")
echo "Response: ${UPLOAD_TXT_RESP}"
echo ""

echo "--- 4. Testing POST /api/documents/upload error case (empty file -> 400 Bad Request) ---"
EMPTY_FILE="${SCRIPT_DIR}/empty_temp.tmp"
touch "${EMPTY_FILE}"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}/documents/upload" \
  -F "file=@${EMPTY_FILE}")
rm -f "${EMPTY_FILE}"
echo "HTTP Status (expected 400): ${HTTP_CODE}"
echo ""

echo "--- 5. Testing GET /api/documents (post-upload verification) ---"
curl -s -X GET "${BASE_URL}/documents" \
  -H "Accept: application/json"
echo ""

echo "=== Tests Finished ==="
