#!/usr/bin/env bash
set -euo pipefail

if ! command -v adb >/dev/null 2>&1; then
  echo "::error::adb was not found on PATH. Install Android SDK platform-tools on the runner."
  exit 1
fi

adb start-server >/dev/null
adb devices -l

# macOS ships Bash 3.2; avoid mapfile/readarray.
CONNECTED="$(adb devices | awk 'NR > 1 && NF >= 2 { print $1 " " $2 }')"
TOTAL="$(printf '%s\n' "$CONNECTED" | awk 'NF { n++ } END { print n+0 }')"
READY="$(printf '%s\n' "$CONNECTED" | awk '$2 == "device" { n++ } END { print n+0 }')"

if [[ "$TOTAL" -ne 1 || "$READY" -ne 1 ]]; then
  echo "::error::Expected exactly one authorized USB Android device, got $TOTAL connected and $READY authorized."
  echo "Unlock the phone, enable USB debugging, and accept the runner host RSA prompt."
  exit 1
fi

SERIAL="$(printf '%s\n' "$CONNECTED" | awk '$2 == "device" { print $1 }')"
if [[ "$SERIAL" == emulator-* || "$SERIAL" == *:* ]]; then
  echo "::error::Expected a USB-connected physical device, not an emulator or TCP/IP ADB endpoint: $SERIAL"
  exit 1
fi

QEMU="$(adb -s "$SERIAL" shell getprop ro.kernel.qemu | tr -d '\r')"
if [[ "$QEMU" == "1" ]]; then
  echo "::error::Connected device reports itself as an emulator."
  exit 1
fi

API="$(adb -s "$SERIAL" shell getprop ro.build.version.sdk | tr -d '\r')"
BOOTED="$(adb -s "$SERIAL" shell getprop sys.boot_completed | tr -d '\r')"
if ! [[ "$API" =~ ^[0-9]+$ ]] || [[ "$API" -lt 33 ]]; then
  echo "::error::Android API 33+ is required for a non-rooted physical profile generator. Found: $API"
  exit 1
fi
if [[ "$BOOTED" != "1" ]]; then
  echo "::error::The physical device has not finished booting."
  exit 1
fi

MODEL="$(adb -s "$SERIAL" shell getprop ro.product.model | tr -d '\r')"
echo "Ready to profile on $MODEL (Android API $API, ADB serial $SERIAL)."
