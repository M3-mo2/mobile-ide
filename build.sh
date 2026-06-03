#!/bin/bash

# Build script for Mobile IDE
# This script builds the project and runs tests

set -e

echo "=========================================="
echo "Mobile IDE - Build Script"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    print_error "gradlew not found. Please run this script from the project root."
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

# Parse arguments
TASK=""
while [[ $# -gt 0 ]]; do
    case $1 in
        --clean)
            TASK="clean"
            shift
            ;;
        --build)
            TASK="build"
            shift
            ;;
        --test)
            TASK="test"
            shift
            ;;
        --lint)
            TASK="lint"
            shift
            ;;
        --debug)
            TASK="debug"
            shift
            ;;
        --release)
            TASK="release"
            shift
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --clean     Clean build artifacts"
            echo "  --build     Build debug APK"
            echo "  --test      Run all tests"
            echo "  --lint      Run lint checks"
            echo "  --debug     Build and install debug APK"
            echo "  --release   Build release APK"
            echo "  --help      Show this help message"
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Default task is build
if [ -z "$TASK" ]; then
    TASK="build"
fi

case $TASK in
    clean)
        print_status "Cleaning build artifacts..."
        ./gradlew clean
        print_status "Clean complete!"
        ;;
    build)
        print_status "Building debug APK..."
        ./gradlew assembleDebug
        print_status "Build complete!"
        print_status "APK location: app/build/outputs/apk/debug/app-debug.apk"
        ;;
    test)
        print_status "Running tests..."
        ./gradlew test
        print_status "Tests complete!"
        ;;
    lint)
        print_status "Running lint checks..."
        ./gradlew ktlintCheck detekt
        print_status "Lint checks complete!"
        ;;
    debug)
        print_status "Building debug APK..."
        ./gradlew assembleDebug
        print_status "Build complete!"
        print_status "APK location: app/build/outputs/apk/debug/app-debug.apk"
        
        # Check if adb is available
        if command -v adb &> /dev/null; then
            print_status "Installing debug APK..."
            adb install -r app/build/outputs/apk/debug/app-debug.apk
            print_status "Installation complete!"
        else
            print_warning "adb not found. APK built but not installed."
        fi
        ;;
    release)
        print_status "Building release APK..."
        ./gradlew assembleRelease
        print_status "Build complete!"
        print_status "APK location: app/build/outputs/apk/release/app-release-unsigned.apk"
        ;;
esac

echo "=========================================="
echo "Done!"
echo "=========================================="
