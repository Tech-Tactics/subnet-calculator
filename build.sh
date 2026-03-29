#!/bin/bash
# build.sh - Compile the Subnet Calculator project
#
# Detects JavaFX location automatically on most Linux systems.
# If JavaFX is installed via apt (openjfx), it should be found
# in /usr/share/openjfx/lib. If installed manually, set the
# JAVAFX_PATH variable below.

set -e

# Output directory for compiled classes
OUT_DIR="out"

# Try to find JavaFX automatically
if [ -d "/usr/share/openjfx/lib" ]; then
    JAVAFX_PATH="/usr/share/openjfx/lib"
elif [ -n "$JAVAFX_HOME" ]; then
    JAVAFX_PATH="$JAVAFX_HOME/lib"
else
    echo "[ERROR] JavaFX not found."
    echo "Install with: sudo apt-get install openjfx"
    echo "Or set JAVAFX_HOME to your JavaFX SDK directory."
    exit 1
fi

echo "[INFO] Using JavaFX from: $JAVAFX_PATH"

# Clean previous build
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

# Find all Java source files
SOURCES=$(find src -name "*.java")

echo "[INFO] Compiling..."
javac --module-path "$JAVAFX_PATH" \
      --add-modules javafx.controls \
      -d "$OUT_DIR" \
      $SOURCES

echo "[INFO] Build complete."
echo "[INFO] Run with: ./run.sh"
