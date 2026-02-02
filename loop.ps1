#
# Witness - Ralph Wiggum Loop Orchestrator (PowerShell)
#
# Usage:
#   .\loop.ps1           # Run building mode (default)
#   .\loop.ps1 plan      # Run planning mode
#   .\loop.ps1 build     # Run building mode explicitly
#   .\loop.ps1 once      # Run one iteration only (no loop)
#   .\loop.ps1 help      # Show help
#

param(
    [Parameter(Position = 0)]
    [ValidateSet("plan", "build", "once", "help")]
    [string]$Mode = "build"
)

$ErrorActionPreference = "Stop"

# Change to script directory
Set-Location $PSScriptRoot

# Logging functions
function Log-Info {
    param([string]$Message)
    Write-Host "[INFO] " -ForegroundColor Blue -NoNewline
    Write-Host $Message
}

function Log-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] " -ForegroundColor Green -NoNewline
    Write-Host $Message
}

function Log-Warn {
    param([string]$Message)
    Write-Host "[WARN] " -ForegroundColor Yellow -NoNewline
    Write-Host $Message
}

function Log-Error {
    param([string]$Message)
    Write-Host "[ERROR] " -ForegroundColor Red -NoNewline
    Write-Host $Message
}

# Check if claude is available
$script:ClaudeCmd = $null

function Check-Claude {
    if (Get-Command "claude" -ErrorAction SilentlyContinue) {
        $script:ClaudeCmd = "claude"
    }
    elseif (Get-Command "claude-code" -ErrorAction SilentlyContinue) {
        $script:ClaudeCmd = "claude-code"
    }
    else {
        Log-Error "claude or claude-code not found in PATH"
        Log-Info "Install Claude Code: https://github.com/anthropics/claude-code"
        exit 1
    }
    Log-Info "Using: $script:ClaudeCmd"
}

# Run planning mode
function Run-Plan {
    Log-Info "Starting planning mode..."
    Check-Claude

    Get-Content "PROMPT_plan.md" -Raw | & $script:ClaudeCmd

    Log-Success "Planning complete. Check IMPLEMENTATION_PLAN.md"
}

# Run building mode (single iteration)
function Run-BuildOnce {
    Log-Info "Starting building mode (single iteration)..."
    Check-Claude

    Get-Content "PROMPT_build.md" -Raw | & $script:ClaudeCmd

    Log-Success "Iteration complete."
}

# Run building mode (continuous loop)
function Run-BuildLoop {
    Log-Info "Starting building mode (continuous loop)..."
    Log-Info "Press Ctrl+C to stop"
    Check-Claude

    $iteration = 1
    while ($true) {
        Write-Host ""
        Log-Info "=========================================="
        Log-Info "Iteration $iteration"
        Log-Info "=========================================="
        Write-Host ""

        Get-Content "PROMPT_build.md" -Raw | & $script:ClaudeCmd

        # Check if plan is complete (all tasks checked)
        $planContent = Get-Content "IMPLEMENTATION_PLAN.md" -Raw
        if ($planContent -match "^- \[ \]") {
            Log-Info "Tasks remaining. Starting next iteration in 5 seconds..."
            Log-Info "Press Ctrl+C to stop"
            Start-Sleep -Seconds 5
            $iteration++
        }
        else {
            Log-Success "All tasks complete!"
            break
        }
    }
}

# Show usage
function Show-Usage {
    Write-Host "Witness - Ralph Wiggum Loop Orchestrator"
    Write-Host ""
    Write-Host "Usage:"
    Write-Host "  .\loop.ps1           Run building mode (continuous loop)"
    Write-Host "  .\loop.ps1 plan      Run planning mode"
    Write-Host "  .\loop.ps1 build     Run building mode (continuous loop)"
    Write-Host "  .\loop.ps1 once      Run one building iteration only"
    Write-Host "  .\loop.ps1 help      Show this help message"
    Write-Host ""
    Write-Host "The Ralph Wiggum Loop:"
    Write-Host "  1. Plan: Analyze specs, generate IMPLEMENTATION_PLAN.md"
    Write-Host "  2. Build: Implement one task per iteration, validate, commit"
    Write-Host "  3. Repeat until all tasks complete"
    Write-Host ""
    Write-Host "For more info, see specs/ralph.md"
}

# Main
switch ($Mode) {
    "plan" {
        Run-Plan
    }
    "build" {
        Run-BuildLoop
    }
    "once" {
        Run-BuildOnce
    }
    "help" {
        Show-Usage
    }
    default {
        Log-Error "Unknown command: $Mode"
        Show-Usage
        exit 1
    }
}
