# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Witness** is an open-source, federated video evidence tool for citizen journalists documenting potential misconduct by authorities. Initial focus is ICE (Immigration and Customs Enforcement) actions.

**Status:** Early development — specifications complete, no implementation code yet.

**Core Philosophy:** Safety first, evidence second. The journalist's safety always takes priority over evidence capture.

## Tech Stack

| Component | Technology |
|-----------|------------|
| **Android Client** | Kotlin + Jetpack Compose, MVVM + Clean Architecture, Hilt DI |
| **Camera** | Camera2 + CameraX (full control for witness mode) |
| **Background** | WorkManager + ForegroundService |
| **Encryption** | Tink + Android Keystore |
| **Backend** | Go + Gin (federation nodes) |
| **Storage** | PostgreSQL + Room (server + client), MinIO (S3 API) |

## Development Methodology

This project uses the **Ralph Wiggum Loop** for AI-assisted development (see `specs/ralph.md`).

```bash
# Planning mode - analyze specs, generate implementation plan
./loop.sh plan

# Building mode - implement one task per iteration
./loop.sh
```

**Workflow:**
1. Read specs in `specs/` directory
2. Check `IMPLEMENTATION_PLAN.md` for current task
3. Implement one task
4. Validate against quality gates (see `AGENTS.md`)
5. Commit and update plan

## Commands

### Quality Gates (Run Before Committing)

```bash
./scripts/check.sh               # Run ALL quality gates
./scripts/check.sh android       # Android checks only
./scripts/check.sh backend       # Go backend checks only
./scripts/check.sh quick         # Quick checks (skip build)
```

### Android

```bash
cd android
./gradlew assembleDebug          # Build debug APK
./gradlew testDebugUnitTest      # Run unit tests
./gradlew ktlintCheck            # Lint check
./gradlew ktlintFormat           # Auto-format
./gradlew detekt                 # Static analysis
./gradlew check                  # All checks
./gradlew connectedDebugAndroidTest  # Instrumented tests (requires device)
```

### Go Backend

```bash
cd backend
go build -o witness-node ./cmd/server  # Build
go test ./...                          # Test
golangci-lint run                      # Lint
```

## Quality Gates

All gates must pass before committing. See `AGENTS.md` for full details.

| Gate | Command | Requirement |
|------|---------|-------------|
| Type Safety | `./gradlew compileDebugKotlin` | 0 errors |
| Lint | `./gradlew ktlintCheck` | 0 errors, 0 warnings |
| Unit Tests | `./gradlew testDebugUnitTest` | 100% pass |
| Build | `./gradlew assembleDebug` | Successful |
| APK Size | Check output | < 15MB |

## Key Concepts

### Witness Mode
Covert recording with screen off, activated via volume button sequence. Uses ForegroundService + WakeLock + Camera2 API.

### Camouflage
App disguised as calculator/weather/notes using Activity aliases. User can switch disguise at runtime.

### Federation
Multiple independent nodes run by trusted organizations. Evidence replicates across nodes automatically. No single point of failure.

### Evidence Hash
SHA-256 hash generated at moment of capture, uploaded immediately to nodes even before full video. Proves footage existed and was unaltered.

## Specifications

All requirements are in `specs/`:

| Priority | Files |
|----------|-------|
| **P0 (Critical)** | `01-capture.md`, `02-upload.md`, `03-safety.md`, `04-verification.md`, `05-federation.md`, `06-security.md` |
| **P1 (MVP)** | `07-ux.md`, `08-platform.md` |
| **P2 (Post-MVP)** | `09-legal.md` |
| **Reference** | `tech-stack.md`, `competitive.md`, `ralph.md` |

## Project Structure (Planned)

```
witness/
├── android/                    # Android application
│   └── app/src/main/kotlin/org/witness/app/
│       ├── ui/                 # Jetpack Compose UI
│       ├── domain/             # ViewModels, use cases
│       ├── data/               # Repositories, data sources
│       └── service/            # ForegroundService, workers
├── backend/                    # Go federation node
│   ├── cmd/server/             # Main entry point
│   └── internal/               # api/, federation/, storage/, auth/
├── specs/                      # Requirements specifications
├── AGENTS.md                   # Operational guide (commands, quality gates)
├── PROMPT_build.md             # Building mode prompt
├── PROMPT_plan.md              # Planning mode prompt
└── IMPLEMENTATION_PLAN.md      # Current task tracking
```

## Guardrails

### Pre-commit Hook (Local)

Install to run quality checks before every commit:

```bash
./scripts/install-hooks.sh
```

The hook automatically checks staged Kotlin/Go files and blocks commits with:
- Compilation errors
- Lint failures
- Potential secrets in staged files

### CI Pipeline (GitHub Actions)

`.github/workflows/ci.yml` runs on every push/PR:

| Job | Checks |
|-----|--------|
| **Android** | Compile, ktlint, detekt, unit tests, build, APK size |
| **Backend** | Build, vet, golangci-lint, tests |

### Feedback Loop

1. **Write code** -> Kotlin/Go compiler catches type errors
2. **Save file** -> IDE shows lint warnings
3. **Attempt commit** -> Pre-commit hook runs checks
4. **Push to remote** -> CI runs full quality gate suite

## Critical Rules

1. **Quality gates are mandatory** — Never commit code that fails any gate
2. **One task per loop** — Implement exactly one task per loop iteration
3. **Safety-critical code needs extra review** — Encryption, location, network, storage
4. **Never modify hash generation without approval** — Evidence integrity is paramount
5. **Follow the plan** — `IMPLEMENTATION_PLAN.md` is source of truth during building mode
