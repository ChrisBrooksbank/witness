# Witness - Project Guide for Claude Code

## Project Overview

**Witness** is an open-source, federated video evidence tool for citizen journalists documenting potential misconduct by authorities, with an initial focus on ICE (Immigration and Customs Enforcement) actions.

### Mission

Protect people who document injustice. Safety first, evidence second.

### Core Philosophy

- **Safety first** — The journalist's safety always takes priority over evidence capture
- **Open source** — Full transparency, community trust, not for profit
- **Decentralized** — Federated architecture, no single point of failure
- **Accessible** — Simple enough for anyone to use, regardless of technical skill

## Tech Stack

| Component | Technology | Notes |
|-----------|------------|-------|
| **Android Client** | Kotlin + Jetpack Compose | Native Android, API 26+ |
| **Architecture** | MVVM + Clean Architecture | Hilt for DI |
| **Camera** | Camera2 + CameraX | Full control for witness mode |
| **Background** | WorkManager + ForegroundService | Reliable upload queue |
| **Encryption** | Tink + Android Keystore | Evidence protection |
| **Backend** | Go + Gin | Federation nodes |
| **Database** | PostgreSQL + Room | Server + client storage |
| **Object Storage** | MinIO (S3 API) | Video chunk storage |

## Commands

### Android

```bash
# Build
./gradlew assembleDebug
./gradlew assembleRelease

# Test
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest

# Lint & Format
./gradlew ktlintCheck
./gradlew ktlintFormat
./gradlew detekt

# All checks
./gradlew check
```

### Go Backend

```bash
# Build
go build -o witness-node ./cmd/server

# Test
go test ./...

# Lint
golangci-lint run

# Run
./witness-node
```

## Architecture

```
witness/
├── android/                    # Android application
│   ├── app/                    # Main app module
│   │   ├── src/main/
│   │   │   ├── kotlin/org/witness/app/
│   │   │   │   ├── ui/         # Jetpack Compose UI
│   │   │   │   ├── domain/     # ViewModels, use cases
│   │   │   │   ├── data/       # Repositories, data sources
│   │   │   │   └── service/    # ForegroundService, workers
│   │   │   └── res/            # Resources
│   │   └── build.gradle.kts
│   └── build.gradle.kts
├── backend/                    # Go federation node
│   ├── cmd/server/             # Main entry point
│   ├── internal/
│   │   ├── api/                # HTTP handlers
│   │   ├── federation/         # Node sync protocol
│   │   ├── storage/            # MinIO integration
│   │   └── auth/               # JWT, anonymous accounts
│   └── go.mod
├── specs/                      # Requirements specifications
├── CLAUDE.md                   # This file
├── AGENTS.md                   # Operational guide
├── PROMPT_build.md             # Building mode prompt
├── PROMPT_plan.md              # Planning mode prompt
└── IMPLEMENTATION_PLAN.md      # Current task tracking
```

## Development Workflow

This project uses the **Ralph Wiggum Loop** methodology for AI-assisted development.

### The Loop

```bash
# Planning mode - analyze specs, generate implementation plan
./loop.sh plan

# Building mode - implement one task per iteration
./loop.sh
```

### Workflow

1. **Read specs** in `specs/` directory
2. **Check** `IMPLEMENTATION_PLAN.md` for current task
3. **Implement** one task
4. **Validate** against quality gates (see AGENTS.md)
5. **Commit** and update plan
6. **Loop** until complete

## Quality Gates

All code must pass these gates before commit:

| Gate | Command | Requirement |
|------|---------|-------------|
| Type Safety | `./gradlew compileDebugKotlin` | 0 errors |
| Lint | `./gradlew ktlintCheck` | 0 errors, 0 warnings |
| Format | `./gradlew ktlintFormat` | All files formatted |
| Unit Tests | `./gradlew testDebugUnitTest` | 100% pass |
| Build | `./gradlew assembleDebug` | Successful |

See `AGENTS.md` for complete quality gate definitions.

## Specifications

All requirements are in `specs/`:

| File | Topic |
|------|-------|
| `specs/readme.md` | Index with priorities |
| `specs/01-capture.md` | Content capture |
| `specs/02-upload.md` | Upload & streaming |
| `specs/03-safety.md` | Safety features |
| `specs/04-verification.md` | Verification & credibility |
| `specs/05-federation.md` | Federation architecture |
| `specs/06-security.md` | Security |
| `specs/07-ux.md` | User experience |
| `specs/08-platform.md` | Platform requirements |
| `specs/09-legal.md` | Legal considerations |
| `specs/tech-stack.md` | Tech stack research |
| `specs/competitive.md` | Competitive landscape |
| `specs/ralph.md` | Development methodology |

## Key Concepts

### Witness Mode

Covert recording with screen off, activated via volume button sequence. Uses ForegroundService + WakeLock + Camera2 API.

### Camouflage

App disguised as calculator/weather/notes using Activity aliases. User can switch disguise at runtime.

### Federation

Multiple independent nodes run by trusted organizations. Evidence replicates across nodes automatically. No single point of failure.

### Evidence Hash

SHA-256 hash generated at moment of capture, uploaded immediately to nodes even before full video. Proves footage existed and was unaltered.
