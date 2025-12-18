#!/bin/bash
mkdir -p bin
echo "Compiling GUI..."
find src -name "*.java" > sources.txt
javac -d bin -cp "lib/*" @sources.txt
rm sources.txt
echo "Compilation complete."
echo ""
echo "Note: Make sure sqlite-jdbc.jar is in the lib/ directory."
echo "If not, run: ./download_sqlite.sh"
