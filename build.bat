@echo off

REM Build script for Mobile IDE on Windows
REM This script builds the project and runs tests

echo ==========================================
echo Mobile IDE - Build Script
echo ==========================================

REM Check if gradlew exists
if not exist "gradlew.bat" (
    echo [ERROR] gradlew.bat not found. Please run this script from the project root.
    exit /b 1
)

REM Parse arguments
set TASK=build

:parse_args
if "%~1"=="" goto execute
if "%~1"=="--clean" set TASK=clean
if "%~1"=="--build" set TASK=build
if "%~1"=="--test" set TASK=test
if "%~1"=="--lint" set TASK=lint
if "%~1"=="--debug" set TASK=debug
if "%~1"=="--release" set TASK=release
if "%~1"=="--help" goto help
shift
goto parse_args

:help
echo Usage: %0 [OPTIONS]
echo.
echo Options:
echo   --clean     Clean build artifacts
echo   --build     Build debug APK
echo   --test      Run all tests
echo   --lint      Run lint checks
echo   --debug     Build and install debug APK
echo   --release   Build release APK
echo   --help      Show this help message
exit /b 0

:execute
echo [INFO] Task: %TASK%

if "%TASK%"=="clean" (
    echo [INFO] Cleaning build artifacts...
    call gradlew.bat clean
    echo [INFO] Clean complete!
)

if "%TASK%"=="build" (
    echo [INFO] Building debug APK...
    call gradlew.bat assembleDebug
    echo [INFO] Build complete!
    echo [INFO] APK location: app\build\outputs\apk\debug\app-debug.apk
)

if "%TASK%"=="test" (
    echo [INFO] Running tests...
    call gradlew.bat test
    echo [INFO] Tests complete!
)

if "%TASK%"=="lint" (
    echo [INFO] Running lint checks...
    call gradlew.bat ktlintCheck detekt
    echo [INFO] Lint checks complete!
)

if "%TASK%"=="debug" (
    echo [INFO] Building debug APK...
    call gradlew.bat assembleDebug
    echo [INFO] Build complete!
    echo [INFO] APK location: app\build\outputs\apk\debug\app-debug.apk
)

if "%TASK%"=="release" (
    echo [INFO] Building release APK...
    call gradlew.bat assembleRelease
    echo [INFO] Build complete!
    echo [INFO] APK location: app\build\outputs\apk\release\app-release-unsigned.apk
)

echo ==========================================
echo Done!
echo ==========================================
