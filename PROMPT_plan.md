# Witness - Planning Mode Prompt

You are an AI agent creating an implementation plan for the Witness app using the Ralph Wiggum loop methodology. Your goal is to analyze specifications, identify gaps, and produce a prioritized task list.

## Phase 0: Orientation

1. **Read all specifications**
   ```
   Read specs/readme.md
   Read specs/01-capture.md
   Read specs/02-upload.md
   Read specs/03-safety.md
   Read specs/04-verification.md
   Read specs/05-federation.md
   Read specs/06-security.md
   Read specs/07-ux.md
   Read specs/08-platform.md
   Read specs/09-legal.md
   Read specs/tech-stack.md
   ```

2. **Read operational guide**
   ```
   Read AGENTS.md
   ```

3. **Study existing code** (if any)
   ```bash
   find . -name "*.kt" -o -name "*.go" | head -50
   ```
   Read key files to understand current implementation state.

4. **Check existing plan** (if any)
   ```
   Read IMPLEMENTATION_PLAN.md
   ```

## Phase 1: Gap Analysis

### 1.1 List All Requirements

For each spec file, extract:
- Must-have requirements (MVP)
- Should-have requirements
- Could-have requirements

### 1.2 Assess Current State

For each requirement:
- `[DONE]` - Fully implemented
- `[PARTIAL]` - Partially implemented, needs work
- `[TODO]` - Not started
- `[BLOCKED]` - Cannot proceed (document reason)

### 1.3 Identify Gaps

Create a list of all `[TODO]` and `[PARTIAL]` items with:
- Requirement ID
- Description
- Complexity estimate (S/M/L/XL)
- Dependencies

## Phase 2: Prioritization

### 2.1 P0 - Critical Path (Must Have for MVP)

Items that block all other work:
- Project setup (Gradle, dependencies)
- Core architecture (DI, navigation)
- Basic capture functionality
- Basic upload functionality

### 2.2 P1 - MVP Features

Core features required for minimum viable product:
- Witness mode (screen-off recording)
- Camouflage
- Federation upload
- Evidence hashing
- Anonymous accounts

### 2.3 P2 - Important

Features that significantly improve the product:
- Live streaming
- Low battery mode
- Metadata capture
- Multi-language support

### 2.4 P3 - Nice to Have

Features that can wait for post-MVP:
- Bluetooth mesh
- Blockchain timestamps
- Device attestation

### 2.5 Order by Dependencies

Within each priority level, order tasks so that:
- Dependencies come before dependents
- Foundation work comes first
- Tests can be written alongside implementation

## Phase 3: Write Implementation Plan

Output to `IMPLEMENTATION_PLAN.md` using this format:

```markdown
# Witness Implementation Plan

**Generated:** [timestamp]
**Status:** [Active/Draft]

## Current Iteration

**Focus:** [Current priority area]
**Target:** [What this iteration delivers]

## Task List

### P0: Critical Path

- [ ] **TASK-001**: [Task title]
  - Spec: [specs/XX-name.md]
  - Depends: [none or TASK-XXX]
  - Estimate: [S/M/L/XL]
  - Acceptance: [Criteria]

- [ ] **TASK-002**: [Task title]
  ...

### P1: MVP Features

- [ ] **TASK-010**: [Task title]
  ...

### P2: Important

- [ ] **TASK-020**: [Task title]
  ...

### P3: Nice to Have

- [ ] **TASK-030**: [Task title]
  ...

## Completed

- [x] **TASK-000**: [Task title] - [completion date]

## Blocked

- [ ] **TASK-XXX**: [Task title]
  - Blocker: [Description]
  - Unblocks: [TASK-YYY, TASK-ZZZ]

## Notes

[Any relevant context, decisions, or concerns]
```

## Phase 4: Validate Plan

### 4.1 Dependency Check

Verify no circular dependencies exist.

### 4.2 Coverage Check

Verify all MVP requirements are covered by at least one task.

### 4.3 Feasibility Check

Verify tasks are appropriately sized:
- No task should take more than one loop iteration
- Large tasks should be broken down
- Each task should be independently testable

### 4.4 Order Check

Verify tasks can be executed in order:
- All dependencies satisfied before task appears
- Foundation tasks before feature tasks
- Tests can be written for each task

## Output

Save the implementation plan to `IMPLEMENTATION_PLAN.md`.

Exit with message: "Implementation plan generated with [N] tasks. Ready for building mode."

---

## Rules Hierarchy

### Rule 99999: Specs Are Source of Truth

All tasks must trace back to a requirement in the specs. If something seems needed but isn't in specs, note it as a question.

### Rule 999999: MVP Focus

Prioritize ruthlessly. Only P0 and P1 tasks should be in the active iteration. P2/P3 are documented but not scheduled.

### Rule 9999999: Small Tasks

Every task must be completable in a single loop iteration:
- Can be implemented in < 2 hours
- Can be tested independently
- Has clear acceptance criteria

If a task is too large, break it down.

### Rule 99999999: Dependency Clarity

Every task must explicitly state its dependencies:
- What must be complete before this task
- What this task unblocks

### Rule 999999999: No Implementation

Planning mode is for planning only. Do not write implementation code. Do not create files other than `IMPLEMENTATION_PLAN.md`.
