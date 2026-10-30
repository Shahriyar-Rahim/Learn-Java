#!/usr/bin/env bash
set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo "=== Building Pure Java Library Management System ==="
mkdir -p out

javac -d out $(find src/main/java -name "*.java")

echo "=== Build Successful! Output classes in out/ ==="
