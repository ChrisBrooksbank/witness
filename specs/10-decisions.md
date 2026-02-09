# Spec Gap Decisions — Addendum

This document resolves gaps, open questions, undefined thresholds, and ambiguities found across the Witness specifications (01–09). Each decision references the original spec requirement ID(s) so it can be traced back.

**This file is authoritative.** Where it conflicts with an earlier spec, this file wins.

---

## 1. MVP Scope Decisions

| Decision | Resolution |
|----------|-----------|
| Sentry Mode, Dead Drop, Multi-Witness Bluetooth | All **post-MVP**. Not in scope. |
| Live streaming to trusted contacts | **Post-MVP**. MVP is capture + upload to nodes only. |
| C2PA standard compliance | **Research task** before committing. Own hash+signature chain for MVP. |
| Federation at launch | **Single self-hosted node**. No discovery protocol needed for MVP. |
| Minimum SDK | **Raised to API 29 (Android 10+)** for native TLS 1.3 support. Update PLT-001. |

---

## 2. Capture Parameters

| Parameter | Value | Refs |
|-----------|-------|------|
| Default video | 720p / 30fps / H.264 | CAP-007, CAP-008 |
| Audio-only bitrate | 128 kbps AAC | CAP open question |
| Max recording duration | 4 hours (matches WakeLock timeout) | CAP open question |
| Battery low threshold | 15% — reduce quality, switch to audio-only | CAP-006 |
| Battery critical threshold | 5% — stop recording gracefully | CAP-006 |
| GPS unavailable behavior | Set location to null in metadata. Don't block recording. Note "location unavailable." | CAP-004, VER-008 |
| Camera API | Camera2 throughout (both standard and witness mode) for consistency and low-level control | tech-stack.md |

---

## 3. Witness Mode

| Parameter | Value | Refs |
|-----------|-------|------|
| Volume button timing | 500ms max between presses | SAF-020 |
| Accidental trigger protection | 5-second cancel window after activation, signaled by subtle haptic | SAF-024 |
| Status indicator (screen off) | Haptic heartbeat — subtle vibration every 30 seconds to confirm recording | UX-010, SAF-025 |
| Phone call during recording | Continue video recording. Audio may switch to call audio. Resume normal audio capture after call ends. | PLT-031 |

---

## 4. Safety & Camouflage

| Decision | Resolution | Refs |
|----------|-----------|------|
| Calculator disguise depth | Basic arithmetic only (add, subtract, multiply, divide). Enough to look real at a glance. | SAF-015 |
| Local storage policy | Cache only. Evidence exists locally ONLY as encrypted cache while waiting to upload. Auto-deleted 24 hours after server confirms receipt. No "save locally" option. | SAF-030, SAF-031, SAF-032 |
| Evidence deletion timing | 24-hour grace period after upload confirmation, then auto-delete. | SAF-032 |

---

## 5. Upload & Network

| Parameter | Value | Refs |
|-----------|-------|------|
| Bandwidth threshold: reduce quality | Below 1 Mbps | UPL-005 |
| Bandwidth threshold: audio-only | Below 256 Kbps | UPL-005 |
| Default network policy | Upload on any connection (WiFi + cellular). User can restrict to WiFi-only in settings. | UPL-014 open question |
| Exponential backoff: initial | 30 seconds | UPL-016 |
| Exponential backoff: max | 30 minutes, keep retrying indefinitely | UPL-016 |
| Upload to how many nodes | 1 (single node MVP) | FED open question |

---

## 6. Security & Auth

| Parameter | Value | Refs |
|-----------|-------|------|
| TLS minimum version | TLS 1.3 (native, no backport needed since min SDK is now API 29) | SEC-002 |
| Access token format | JWT, 1-hour expiry | SEC-032 |
| Token refresh | Refresh token issued alongside JWT. Single-use refresh token rotation. | SEC-033 |
| Account model | One account per device install. Deletable. Deletion wipes local keys; evidence on nodes persists but becomes inaccessible to deleted account. | SAF-002 |

---

## 7. Verification & Time

| Parameter | Value | Refs |
|-----------|-------|------|
| Time source | NTP query when online. Device time as fallback. Metadata records which source was used. | VER-007 open question |
| Clock skew tolerance | Record both device and NTP time. Let verifier compare. No enforcement at capture time. | VER-007 |

---

## 8. UX & Localization

| Decision | Resolution | Refs |
|----------|-----------|------|
| Languages | English + Spanish. Auto-detect from system locale, fallback to English for unsupported locales. | UX-030 |
| Legal disclaimer | US recording law info on first launch. "Laws vary by jurisdiction" note. No age gate. | LEG-001, LEG-003 |

---

## 9. Legal & Licensing

| Decision | Resolution | Refs |
|----------|-----------|------|
| Open source license | Apache 2.0 | LEG-020 open question |
| International legal scope | US-only for MVP. Brief disclaimer + link to external Know Your Rights resources. | LEG-001 |

---

## 10. Development Process

| Decision | Resolution | Refs |
|----------|-----------|------|
| Pre-commit hook scope | Lightweight: compile + ktlint only. Detekt runs in CI and `./scripts/check.sh`. | AGENTS.md |
| Federation node auth (MVP) | Not needed — single self-hosted node. Client authenticates with JWT to its configured node URL. | FED-006 |
