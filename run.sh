#!/bin/bash
# run.sh - Launch the Subnet Calculator GUI
#
# Must be run after build.sh has compiled the project.

OUT_DIR="out"

if [ ! -d "$OUT_DIR" ]; then
    echo "[ERROR] Build output not found. Run ./build.sh first."
    exit 1
fi

# Try to find JavaFX automatically
if [ -d "/usr/share/openjfx/lib" ]; then
    JAVAFX_PATH="/usr/share/openjfx/lib"
elif [ -n "$JAVAFX_HOME" ]; then
    JAVAFX_PATH="$JAVAFX_HOME/lib"
else
    echo "[ERROR] JavaFX not found."
    echo "Install with: sudo apt-get install openjfx"
    exit 1
fi

echo "[INFO] Launching Subnet Calculator..."
java --module-path "$JAVAFX_PATH" \
     --add-modules javafx.controls \
     -cp "$OUT_DIR" \
     com.subnetcalc.gui.MainApp
