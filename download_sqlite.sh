#!/bin/bash

# Script to download SQLite JDBC driver and SLF4J dependencies
# Downloads from Maven Central

LIB_DIR="lib"

# SQLite JDBC driver (using version that works without SLF4J or we'll add SLF4J)
SQLITE_VERSION="3.44.1.0"
SQLITE_JAR="sqlite-jdbc-${SQLITE_VERSION}.jar"
SQLITE_URL="https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/${SQLITE_VERSION}/${SQLITE_JAR}"

# SLF4J API and Simple implementation (required by SQLite JDBC)
SLF4J_API_VERSION="2.0.9"
SLF4J_API_JAR="slf4j-api-${SLF4J_API_VERSION}.jar"
SLF4J_API_URL="https://repo1.maven.org/maven2/org/slf4j/slf4j-api/${SLF4J_API_VERSION}/${SLF4J_API_JAR}"

SLF4J_SIMPLE_VERSION="2.0.9"
SLF4J_SIMPLE_JAR="slf4j-simple-${SLF4J_SIMPLE_VERSION}.jar"
SLF4J_SIMPLE_URL="https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/${SLF4J_SIMPLE_VERSION}/${SLF4J_SIMPLE_JAR}"

echo "Downloading SQLite JDBC driver and dependencies..."
echo ""

mkdir -p "${LIB_DIR}"

download_file() {
    local url=$1
    local output=$2
    local name=$3
    
    echo "Downloading ${name}..."
    if command -v curl &> /dev/null; then
        curl -L -o "${output}" "${url}" 2>/dev/null
    elif command -v wget &> /dev/null; then
        wget -q -O "${output}" "${url}"
    else
        echo "Error: Neither curl nor wget is available."
        return 1
    fi
    
    if [ -f "${output}" ]; then
        echo "✓ Successfully downloaded ${name}"
        return 0
    else
        echo "✗ Failed to download ${name}"
        return 1
    fi
}

# Download SQLite JDBC
download_file "${SQLITE_URL}" "${LIB_DIR}/${SQLITE_JAR}" "SQLite JDBC Driver"

# Download SLF4J API
download_file "${SLF4J_API_URL}" "${LIB_DIR}/${SLF4J_API_JAR}" "SLF4J API"

# Download SLF4J Simple
download_file "${SLF4J_SIMPLE_URL}" "${LIB_DIR}/${SLF4J_SIMPLE_JAR}" "SLF4J Simple"

echo ""
echo "All dependencies downloaded successfully!"
echo "Location: ${LIB_DIR}/"

