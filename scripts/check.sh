#!/bin/bash
#
# Witness - Quality Gate Runner
#
# Runs all quality gates defined in AGENTS.md.
# Use before committing to catch issues early.
#
# Usage:
#   ./scripts/check.sh           # Run all checks
#   ./scripts/check.sh android   # Android checks only
#   ./scripts/check.sh backend   # Go backend checks only
#   ./scripts/check.sh quick     # Quick checks (no build)
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[PASS]${NC} $1"; }
log_error() { echo -e "${RED}[FAIL]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }

FAILED=0

run_check() {
    local name="$1"
    local cmd="$2"

    echo ""
    log_info "Gate: $name"
    if eval "$cmd"; then
        log_success "$name"
    else
        log_error "$name"
        FAILED=1
    fi
}

check_android() {
    if [ ! -d "android" ]; then
        log_warn "android/ directory not found, skipping Android checks"
        return 0
    fi

    cd android

    run_check "Kotlin compile (type check)" "./gradlew compileDebugKotlin --quiet"
    run_check "ktlint (lint)" "./gradlew ktlintCheck --quiet"
    run_check "detekt (static analysis)" "./gradlew detekt --quiet"
    run_check "Unit tests" "./gradlew testDebugUnitTest --quiet"

    if [ "${QUICK:-}" != "1" ]; then
        run_check "Build debug APK" "./gradlew assembleDebug --quiet"
    fi

    cd "$PROJECT_ROOT"
}

check_backend() {
    if [ ! -d "backend" ]; then
        log_warn "backend/ directory not found, skipping Go checks"
        return 0
    fi

    cd backend

    run_check "Go build" "go build ./..."
    run_check "Go vet" "go vet ./..."

    if command -v golangci-lint &> /dev/null; then
        run_check "golangci-lint" "golangci-lint run"
    else
        log_warn "golangci-lint not installed, skipping"
    fi

    run_check "Go tests" "go test ./..."

    cd "$PROJECT_ROOT"
}

show_summary() {
    echo ""
    echo "=========================================="
    if [ $FAILED -eq 0 ]; then
        log_success "All quality gates passed!"
    else
        log_error "Some quality gates failed"
        exit 1
    fi
}

# Main
case "${1:-all}" in
    android)
        check_android
        ;;
    backend|go)
        check_backend
        ;;
    quick)
        QUICK=1
        check_android
        check_backend
        ;;
    all)
        check_android
        check_backend
        ;;
    *)
        echo "Usage: ./scripts/check.sh [android|backend|quick|all]"
        exit 1
        ;;
esac

show_summary
