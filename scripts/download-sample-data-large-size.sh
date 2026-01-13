#!/usr/bin/env bash

set -e

DOWNLOAD_URL="https://github.com/nn-hieu/mvel/releases/download/sample-data/sample-data-large-size.zip"
TARGET_DIR="sample-data"
OUTPUT_FILE="$TARGET_DIR/sample-data-large-size.zip"

echo "📥 Downloading large sample data..."

mkdir -p "$TARGET_DIR"

if command -v curl >/dev/null 2>&1; then
  curl -L "$DOWNLOAD_URL" -o "$OUTPUT_FILE"
elif command -v wget >/dev/null 2>&1; then
  wget "$DOWNLOAD_URL" -O "$OUTPUT_FILE"
else
  echo "❌ Error: curl or wget is required to download the file."
  exit 1
fi

echo "📦 Download completed: $OUTPUT_FILE"

echo "📂 Extracting zip file..."
unzip -o "$OUTPUT_FILE" -d "$TARGET_DIR"

echo "✅ Large sample data is ready in '$TARGET_DIR'"