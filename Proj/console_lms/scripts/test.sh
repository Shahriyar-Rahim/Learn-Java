#!/usr/bin/env bash
set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo "=== Compiling & Running Automated Test Suite ==="
mkdir -p out out_test

javac -d out $(find src/main/java -name "*.java")
javac -d out_test -cp out $(find src/test/java -name "*.java")

java -cp out:out_test library.test.TestRunner
