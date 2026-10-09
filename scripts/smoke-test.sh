#!/usr/bin/env sh
# End-to-end check of the launcher. Usage: scripts/smoke-test.sh [path-to-launcher]
set -eu

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LAUNCHER="${1:-$ROOT/credit_simulator}"
FAIL=0

check() {
    name="$1"; expected="$2"; actual="$3"
    case "$actual" in
        *"$expected"*) echo "PASS: $name" ;;
        *) echo "FAIL: $name (expected '$expected')"; echo "$actual"; FAIL=1 ;;
    esac
}

good="$(mktemp)"
printf 'Mobil\nBaru\n%s\n1000000000\n6\n500000000\n' "$(date +%Y)" > "$good"
out="$("$LAUNCHER" "$good" 2>&1)"
rm -f "$good"
check "file input year 1" "tahun 1 : Rp. 7,500,000.00/bln , Suku Bunga : 8%" "$out"
check "file input year 2 rate" "Suku Bunga : 8,1%" "$out"

out="$(printf 'show\nexit\n' | "$LAUNCHER" 2>&1)"
check "show lists commands" "Command yang tersedia" "$out"

out="$(printf 'input\ntruk\nexit\n' | "$LAUNCHER" 2>&1)"
check "invalid input handled" "Error: Jenis kendaraan harus Mobil atau Motor." "$out"

bad="$(mktemp)"
printf 'Mobil\nBaru\n2025\n1000000000\n6\n1\n' > "$bad"
if "$LAUNCHER" "$bad" >/dev/null 2>&1; then
    echo "FAIL: invalid file must exit non-zero"; FAIL=1
else
    echo "PASS: invalid file exits non-zero"
fi
rm -f "$bad"

exit "$FAIL"
