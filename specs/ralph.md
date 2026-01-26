# Ralph Wiggum Development Methodology

## Overview

The Ralph Wiggum technique is an AI-assisted development methodology created by Geoffrey Huntley. It enables autonomous, iterative development through a structured loop of planning and building.

## The Core Loop

```bash
while :; do cat PROMPT.md | claude-code ; done
```

This simple loop drives continuous development, with Claude Code implementing one task per iteration.

## Three Phases, Two Prompts, One Loop

### Phase 1: Define Requirements

Requirements are defined as JTBD (Jobs To Be Done) aligned specifications, one per topic:

```
specs/
├── 01-capture.md      # Video/audio/photo capture
├── 02-upload.md       # Upload & streaming
├── 03-safety.md       # Camouflage, witness mode
├── 04-verification.md # Hashing, metadata
├── 05-federation.md   # Multi-node architecture
├── 06-security.md     # Encryption, keys
├── 07-ux.md           # User experience
├── 08-platform.md     # Android requirements
└── 09-legal.md        # Legal considerations
```

Each spec file follows a consistent format:
- Overview
- Requirements (with IDs, priorities, MVP status)
- Acceptance criteria
- Technical notes
- Open questions

### Phase 2: Planning Mode

**Prompt**: `PROMPT_plan.md`

The planning agent:
1. Reads all specifications
2. Studies existing code
3. Performs gap analysis
4. Prioritizes tasks (P0, P1, P2, P3)
5. Generates `IMPLEMENTATION_PLAN.md`

Output is a structured task list with:
- Task ID
- Description
- Spec reference
- Dependencies
- Acceptance criteria

### Phase 3: Building Mode

**Prompt**: `PROMPT_build.md`

The building agent:
1. Reads the implementation plan
2. Selects next unchecked task
3. Implements the task
4. Validates against quality gates
5. Commits and updates the plan
6. Exits (loop restarts)

## Quality Gates as Backpressure

Quality gates prevent the loop from producing broken code:

| Gate | Command | Requirement |
|------|---------|-------------|
| 1. Type Safety | `./gradlew compileDebugKotlin` | 0 errors |
| 2. Lint | `./gradlew ktlintCheck` | 0 errors, 0 warnings |
| 3. Format | `./gradlew ktlintFormat` | All formatted |
| 4. Unit Tests | `./gradlew testDebugUnitTest` | 100% pass |
| 5. Build | `./gradlew assembleDebug` | Successful |
| 6. Integration | `./gradlew connectedDebugAndroidTest` | 100% pass |

**Rule**: Never commit code that fails any gate.

## Rules Hierarchy

The prompts include numbered rules that override other instructions:

```
99999:    Quality gates are mandatory
999999:   One task per loop
9999999:  Safety-critical code needs extra review
99999999: Never modify hash generation without approval
```

Higher numbers = higher priority.

## Running the Loop

### Planning

```bash
./loop.sh plan
```

Generates/updates `IMPLEMENTATION_PLAN.md` from specs.

### Building

```bash
./loop.sh
```

Continuous loop implementing tasks one at a time.

### Single Iteration

```bash
./loop.sh once
```

Run one building iteration without looping.

## File Structure

```
witness/
├── CLAUDE.md              # Project overview for Claude Code
├── AGENTS.md              # Operational guide (commands, gates)
├── PROMPT_plan.md         # Planning mode prompt
├── PROMPT_build.md        # Building mode prompt
├── IMPLEMENTATION_PLAN.md # Task tracking (generated)
├── loop.sh                # Orchestration script
└── specs/                 # Requirements specifications
    ├── readme.md          # Spec index
    ├── 01-capture.md
    ├── 02-upload.md
    └── ...
```

## Benefits

1. **Autonomous execution**: Loop runs without human intervention
2. **Quality enforcement**: Gates prevent regressions
3. **Clear specifications**: Specs define what to build
4. **Traceable progress**: Plan shows what's done
5. **Reproducible**: Same prompts, same behavior
6. **Resumable**: Can stop and restart anytime

## Anti-Patterns to Avoid

| Anti-Pattern | Why It's Bad |
|--------------|--------------|
| Skipping gates | Leads to broken code accumulation |
| Multiple tasks per loop | Harder to debug, review |
| Vague specs | Agent guesses wrong |
| No acceptance criteria | Can't verify completion |
| Huge tasks | Take multiple iterations |

## When to Re-Plan

Re-run planning mode when:
- Specifications change
- Major blockers discovered
- Architecture decisions change
- Significant progress made (refresh priorities)

## Sources

- [Ralph Wiggum Technique](https://ghuntley.com/ralph/) - Geoffrey Huntley
- [How to Ralph Wiggum](https://github.com/ghuntley/how-to-ralph-wiggum) - Full methodology
- [letshang repo](https://github.com/ChrisBrooksbank/letshang) - Format reference

## Philosophy

> "I'm helping!" - Ralph Wiggum

The methodology embraces the idea that AI can autonomously make progress on well-defined problems. The human role shifts to:
- Defining requirements clearly
- Reviewing output periodically
- Adjusting course when needed

The loop handles the tedious implementation work.
