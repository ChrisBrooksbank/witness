# Witness Implementation Plan

**Generated:** 2026-05-03
**Status:** Active

## Current Iteration

**Focus:** Hardware capture integration
**Target:** Implement and validate Camera2 video and audio-only recording on an Android device or emulator.

## Task List

### P0: Critical Path

- [x] **TASK-001**: Scaffold Android and Go project foundations - completed 2026-05-03
  - Spec: `specs/08-platform.md`, `specs/10-decisions.md`, `AGENTS.md`
  - Depends: none
  - Estimate: M
  - Acceptance: Android project declares `org.witness.app`, min SDK 29, target SDK 34; backend has a Go module and health endpoint; repo has runnable build entrypoints expected by CI.

- [x] **TASK-002**: Add core Android application shell and navigation surface - completed 2026-05-03
  - Spec: `specs/07-ux.md`, `specs/08-platform.md`
  - Depends: TASK-001
  - Estimate: M
  - Acceptance: App launches to a high-contrast recording surface with accessible record controls and status text.

- [x] **TASK-003**: Add domain models for evidence metadata and capture state - completed 2026-05-03
  - Spec: `specs/01-capture.md`, `specs/04-verification.md`
  - Depends: TASK-001
  - Estimate: S
  - Acceptance: Evidence metadata, device info, location, chunk hash, and recording state models exist with unit tests.

- [x] **TASK-004**: Implement SHA-256 chunk hashing and Merkle root calculation - completed 2026-05-03
  - Spec: `specs/04-verification.md`, `specs/10-decisions.md`
  - Depends: TASK-003
  - Estimate: M
  - Acceptance: Hashing utility generates stable chunk hashes and Merkle roots with tests for empty, single, odd, and even chunk lists.

- [x] **TASK-005**: Implement local key generation abstraction - completed 2026-05-03
  - Spec: `specs/06-security.md`
  - Depends: TASK-003
  - Estimate: M
  - Acceptance: Android Keystore-backed signing key manager interface exists with tests around non-platform canonicalization logic.

- [x] **TASK-006**: Add encrypted cache schema for pending evidence - completed 2026-05-03
  - Spec: `specs/03-safety.md`, `specs/06-security.md`
  - Depends: TASK-003
  - Estimate: L
  - Acceptance: Room schema tracks evidence, chunks, upload status, and 24-hour confirmed-upload deletion deadline.

- [x] **TASK-007**: Implement MVP backend health and version endpoints - completed 2026-05-03
  - Spec: `specs/05-federation.md`, `specs/08-platform.md`, `specs/10-decisions.md`
  - Depends: TASK-001
  - Estimate: S
  - Acceptance: Go server exposes `/health` and `/api/v1/version` with tests.

- [x] **TASK-008**: Define single-node upload API contracts - completed 2026-05-03
  - Spec: `specs/02-upload.md`, `specs/10-decisions.md`
  - Depends: TASK-007
  - Estimate: M
  - Acceptance: Backend request/response DTOs and Android Retrofit interfaces cover hash registration and chunk upload.

- [x] **TASK-009**: Add WorkManager upload queue skeleton - completed 2026-05-03
  - Spec: `specs/02-upload.md`, `specs/03-safety.md`
  - Depends: TASK-006, TASK-008
  - Estimate: M
  - Acceptance: Upload worker reads queued chunks, applies connected-network constraint, and records retryable failure states.

- [x] **TASK-010**: Add capture service skeleton for foreground recording - completed 2026-05-03
  - Spec: `specs/01-capture.md`, `specs/03-safety.md`, `specs/08-platform.md`
  - Depends: TASK-002, TASK-003
  - Estimate: L
  - Acceptance: Foreground service starts/stops cleanly, owns notification channel, and exposes state without recording media yet.

### P1: MVP Features

- [x] **TASK-011**: Implement calculator camouflage launcher - completed 2026-05-03
  - Spec: `specs/03-safety.md`, `specs/07-ux.md`
  - Depends: TASK-002
  - Estimate: M
  - Acceptance: Default launcher appears as Calculator, supports basic arithmetic, and unlocks real UI via secret calculation.

- [ ] **TASK-012**: Implement Camera2 720p H.264 video capture
  - Spec: `specs/01-capture.md`, `specs/10-decisions.md`
  - Depends: TASK-010
  - Estimate: XL
  - Acceptance: App records a playable 720p/30fps H.264 MP4 segment.

- [ ] **TASK-013**: Implement audio-only AAC capture fallback
  - Spec: `specs/01-capture.md`, `specs/10-decisions.md`
  - Depends: TASK-010
  - Estimate: L
  - Acceptance: App records a playable 128 kbps AAC audio-only file.

- [x] **TASK-014**: Implement capture metadata collection - completed 2026-05-03
  - Spec: `specs/01-capture.md`, `specs/04-verification.md`, `specs/10-decisions.md`
  - Depends: TASK-003
  - Estimate: M
  - Acceptance: Metadata records device time, optional NTP time, GPS nullable fallback, orientation, device, and app version.

- [x] **TASK-015**: Implement volume-button witness mode trigger - completed 2026-05-03
  - Spec: `specs/03-safety.md`, `specs/10-decisions.md`
  - Depends: TASK-010
  - Estimate: L
  - Acceptance: Up-up-down-down sequence within 500ms gaps starts witness mode with a 5-second cancel window.

- [x] **TASK-016**: Implement encrypted upload-and-delete lifecycle - completed 2026-05-03
  - Spec: `specs/02-upload.md`, `specs/03-safety.md`, `specs/06-security.md`
  - Depends: TASK-006, TASK-009
  - Estimate: L
  - Acceptance: Evidence remains only in encrypted cache, uploads on any connection by default, and schedules deletion 24 hours after server confirmation.

- [x] **TASK-017**: Add English and Spanish resources for MVP flows - completed 2026-05-03
  - Spec: `specs/07-ux.md`, `specs/10-decisions.md`
  - Depends: TASK-002
  - Estimate: S
  - Acceptance: User-facing strings for onboarding, recording status, errors, legal disclaimer, and calculator unlock exist in English and Spanish.

### P2: Important

- [x] **TASK-018**: Add legal disclaimer and Know Your Rights resource links - completed 2026-05-03
  - Spec: `specs/09-legal.md`, `specs/10-decisions.md`
  - Depends: TASK-002
  - Estimate: S
  - Acceptance: First launch shows US-focused disclaimer, not legal advice notice, and accessible resources link.

- [x] **TASK-019**: Add WiFi-only upload setting - completed 2026-05-03
  - Spec: `specs/02-upload.md`, `specs/10-decisions.md`
  - Depends: TASK-009
  - Estimate: S
  - Acceptance: User can restrict large uploads to WiFi; default remains any connection.

- [x] **TASK-020**: Add low-battery quality policy - completed 2026-05-03
  - Spec: `specs/01-capture.md`, `specs/02-upload.md`, `specs/10-decisions.md`
  - Depends: TASK-010
  - Estimate: M
  - Acceptance: Battery below 15% reduces quality/audio-only; below 5% stops recording gracefully.

### P3: Nice to Have

- [x] **TASK-021**: Research C2PA compatibility path - completed 2026-05-03
  - Spec: `specs/04-verification.md`, `specs/10-decisions.md`
  - Depends: TASK-004
  - Estimate: M
  - Acceptance: Research note compares C2PA adoption cost against MVP hash/signature chain.

- [x] **TASK-022**: Design post-MVP federation replication protocol - completed 2026-05-03
  - Spec: `specs/05-federation.md`, `specs/10-decisions.md`
  - Depends: TASK-008
  - Estimate: L
  - Acceptance: Protocol design covers node auth, replication, and revocation conflict handling for multi-node rollout.

## Completed

- [x] **TASK-001**: Scaffold Android and Go project foundations - completed 2026-05-03
  - Added Android Gradle/Compose project shell with min SDK 29, target SDK 34, package `org.witness.app`, permissions baseline, and a minimal high-contrast launch surface.
  - Added Go backend module with `/health` and `/api/v1/version` endpoints plus unit tests.

- [x] **TASK-002**: Add core Android application shell and navigation surface - completed 2026-05-03
  - Added always-visible recording status bar, large accessible record/stop control, witness-mode activation hint, and upload queue navigation surface.
  - Kept the screen high contrast and one-handed friendly while deferring real capture behavior to `TASK-010`.

- [x] **TASK-003**: Add domain models for evidence metadata and capture state - completed 2026-05-03
  - Added capture mode, media type, quality, recording state, device info, location, chunk hash, and evidence metadata models.
  - Added unit tests for nullable GPS metadata behavior.

- [x] **TASK-004**: Implement SHA-256 chunk hashing and Merkle root calculation - completed 2026-05-03
  - Added stable SHA-256 chunk hashing and Merkle root calculation for empty, single, even, and odd chunk lists.
  - Added JVM unit tests for deterministic hashing behavior.

- [x] **TASK-005**: Implement local key generation abstraction - completed 2026-05-03
  - Added metadata signing abstraction, deterministic metadata canonicalization, and Android Keystore-backed ECDSA signer.
  - Added JVM unit tests for stable canonicalization and location-unavailable serialization.

- [x] **TASK-007**: Implement MVP backend health and version endpoints - completed 2026-05-03
  - Validated existing Go `/health` and `/api/v1/version` endpoints with `go test`, `go build`, and `go vet`.

- [x] **TASK-006**: Add encrypted cache schema for pending evidence - completed 2026-05-03
  - Added Room database, DAO, evidence/chunk entities, and schema export for encrypted pending-evidence cache tracking.
  - Schema tracks upload status, encrypted chunk file paths, confirmation timestamps, and 24-hour deletion deadline.

- [x] **TASK-008**: Define single-node upload API contracts - completed 2026-05-03
  - Added Android Retrofit contract for hash registration and chunk upload.
  - Added Go request/response DTOs for the same MVP single-node upload API and JSON contract tests.

- [x] **TASK-009**: Add WorkManager upload queue skeleton - completed 2026-05-03
  - Added WorkManager upload worker with connected-network constraints, exponential backoff, and unique work naming.
  - Worker reads pending chunks from Room and records retryable evidence/chunk failure states until real upload transfer is implemented.

- [x] **TASK-010**: Add capture service skeleton for foreground recording - completed 2026-05-03
  - Added foreground capture service registration, notification channel, start/stop intents, and observable recording state.
  - Service intentionally does not record media yet; Camera2 capture is tracked separately in `TASK-012`.

- [x] **TASK-011**: Implement calculator camouflage launcher - completed 2026-05-03
  - Added default Calculator launcher alias, calculator activity, basic arithmetic UI, and secret `1312=` unlock into the real recording UI.
  - APK inspection confirms app label `Calculator` and MAIN/LAUNCHER alias targeting `CalculatorActivity`.

- [x] **TASK-017**: Add English and Spanish resources for MVP flows - completed 2026-05-03
  - Added Spanish resources for recording, camouflage, upload queue, settings, and legal disclaimer surfaces.

- [x] **TASK-018**: Add legal disclaimer and Know Your Rights resource links - completed 2026-05-03
  - Added first-launch legal disclaimer dialog to the real Witness UI with US-focused "not legal advice" language.

- [x] **TASK-019**: Add WiFi-only upload setting - completed 2026-05-03
  - Added Settings tab with WiFi-only uploads toggle and explanatory copy.

- [x] **TASK-020**: Add low-battery quality policy - completed 2026-05-03
  - Added battery policy for keep-video, audio-only fallback below 15%, and graceful stop at 5%, with unit tests.

- [x] **TASK-014**: Implement capture metadata collection - completed 2026-05-03
  - Added Android metadata collection for device/app version, device time, optional network time, nullable last-known location, and screen orientation.
  - Metadata collection reports explicit fallback reasons when location permission or last-known location is unavailable.

- [x] **TASK-015**: Implement volume-button witness mode trigger - completed 2026-05-03
  - Added tested up-up-down-down volume sequence detection with 500ms maximum gaps.
  - Wired the real UI to arm witness mode with a 5-second cancel window, subtle vibration feedback, and foreground capture service startup in witness mode.

- [x] **TASK-016**: Implement encrypted upload-and-delete lifecycle - completed 2026-05-03
  - Added AES-GCM encrypted app-private chunk storage backed by Android Keystore keys and `.nomedia` protection.
  - Updated the upload worker to register evidence hashes, upload encrypted chunks over the MVP REST contract, mark confirmed uploads, and schedule 24-hour deletion.
  - Added periodic retention cleanup to delete confirmed encrypted chunk files and purge Room rows after the deletion deadline.
  - Added backend upload endpoints for hash registration, chunk upload with SHA-256 verification, and evidence verification lookup.

- [x] **TASK-021**: Research C2PA compatibility path - completed 2026-05-03
  - Added `docs/research/c2pa-compatibility.md` comparing C2PA adoption against the MVP Witness hash/signature chain.
  - Recommendation: keep Witness capture-time verification canonical, then add server-side C2PA export post-MVP.

- [x] **TASK-022**: Design post-MVP federation replication protocol - completed 2026-05-03
  - Added `docs/design/federation-replication.md` covering node identity, trust bundles, replication manifests, signed receipts, revocation, and conflict handling.

## Blocked

- [ ] **TASK-012**: Implement Camera2 720p H.264 video capture
  - Blocker: Requires Android project foundation and device/emulator validation.
  - Unblocks: end-to-end capture acceptance criteria.

## Notes

- `specs/10-decisions.md` is authoritative where it conflicts with earlier specs.
- MVP excludes live streaming, full federation/discovery, Bluetooth mesh, sentry mode, and panic wipe.
- Local development environment currently needs Java/Gradle/Go installed before full quality gates can run on this machine.
- Android UI shell compiles and builds locally with a temporary JDK/SDK setup; device/emulator visual launch is pending because ADB reports no attached targets.
