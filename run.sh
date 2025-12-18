#!/bin/bash
echo "Running Water Meter Monitoring System (CLI)..."
# Add lib/* to classpath. separator is : on linux
java -cp "bin:lib/*" br.com.monitoring.Main
