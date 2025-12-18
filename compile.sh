#!/bin/bash
mkdir -p bin
echo "Compiling..."
javac -d bin -cp "lib/*" -sourcepath src src/br/com/monitoring/Main.java
echo "Compilation complete."
echo ""
echo "Note: Make sure sqlite-jdbc.jar is in the lib/ directory."
echo "If not, run: ./download_sqlite.sh"
