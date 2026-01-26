#!/bin/bash
#
# Witness - Ralph Wiggum Loop Orchestrator
#
# Usage:
#   ./loop.sh        # Run building mode (default)
#   ./loop.sh plan   # Run planning mode
#   ./loop.sh build  # Run building mode explicitly
#   ./loop.sh once   # Run one iteration only (no loop)
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if claude-code (or claude) is available
check_claude() {
    if command -v claude &> /dev/null; then
        CLAUDE_CMD="claude"
    elif command -v claude-code &> /dev/null; then
        CLAUDE_CMD="claude-code"
    else
        log_error "claude or claude-code not found in PATH"
        log_info "Install Claude Code: https://github.com/anthropics/claude-code"
        exit 1
    fi
    log_info "Using: $CLAUDE_CMD"
}

# Run planning mode
run_plan() {
    log_info "Starting planning mode..."
    check_claude

    cat PROMPT_plan.md | $CLAUDE_CMD

    log_success "Planning complete. Check IMPLEMENTATION_PLAN.md"
}

# Run building mode (single iteration)
run_build_once() {
    log_info "Starting building mode (single iteration)..."
    check_claude

    cat PROMPT_build.md | $CLAUDE_CMD

    log_success "Iteration complete."
}

# Run building mode (continuous loop)
run_build_loop() {
    log_info "Starting building mode (continuous loop)..."
    log_info "Press Ctrl+C to stop"
    check_claude

    iteration=1
    while true; do
        echo ""
        log_info "=========================================="
        log_info "Iteration $iteration"
        log_info "=========================================="
        echo ""

        cat PROMPT_build.md | $CLAUDE_CMD

        # Check if plan is complete (all tasks checked)
        if grep -q "^- \[ \]" IMPLEMENTATION_PLAN.md; then
            log_info "Tasks remaining. Starting next iteration in 5 seconds..."
            log_info "Press Ctrl+C to stop"
            sleep 5
            ((iteration++))
        else
            log_success "All tasks complete!"
            break
        fi
    done
}

# Show usage
show_usage() {
    echo "Witness - Ralph Wiggum Loop Orchestrator"
    echo ""
    echo "Usage:"
    echo "  ./loop.sh        Run building mode (continuous loop)"
    echo "  ./loop.sh plan   Run planning mode"
    echo "  ./loop.sh build  Run building mode (continuous loop)"
    echo "  ./loop.sh once   Run one building iteration only"
    echo "  ./loop.sh help   Show this help message"
    echo ""
    echo "The Ralph Wiggum Loop:"
    echo "  1. Plan: Analyze specs, generate IMPLEMENTATION_PLAN.md"
    echo "  2. Build: Implement one task per iteration, validate, commit"
    echo "  3. Repeat until all tasks complete"
    echo ""
    echo "For more info, see specs/ralph.md"
}

# Main
case "${1:-build}" in
    plan)
        run_plan
        ;;
    build)
        run_build_loop
        ;;
    once)
        run_build_once
        ;;
    help|--help|-h)
        show_usage
        ;;
    *)
        log_error "Unknown command: $1"
        show_usage
        exit 1
        ;;
esac
