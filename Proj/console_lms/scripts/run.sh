#!/usr/bin/env bash
set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

if [ ! -d "out" ] || [ -z "$(ls -A out)" ]; then
    echo "=== Compiling Pure Java Library Management System ==="
    mkdir -p out
    javac -d out $(find src/main/java -name "*.java")
fi

echo "=== Launching Library Management System CLI ==="
java -cp out library.Main "$@"
