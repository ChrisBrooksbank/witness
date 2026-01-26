# Witness Specifications

This directory contains the requirements specifications for the Witness project, organized by topic.

## Specification Index

| File | Topic | Priority |
|------|-------|----------|
| [01-capture.md](01-capture.md) | Content Capture | P0 |
| [02-upload.md](02-upload.md) | Upload & Streaming | P0 |
| [03-safety.md](03-safety.md) | Safety Features | P0 |
| [04-verification.md](04-verification.md) | Verification & Credibility | P0 |
| [05-federation.md](05-federation.md) | Federation Architecture | P0 |
| [06-security.md](06-security.md) | Security | P0 |
| [07-ux.md](07-ux.md) | User Experience | P1 |
| [08-platform.md](08-platform.md) | Platform Requirements | P1 |
| [09-legal.md](09-legal.md) | Legal Considerations | P2 |
| [tech-stack.md](tech-stack.md) | Tech Stack Research | Reference |
| [competitive.md](competitive.md) | Competitive Landscape | Reference |
| [ralph.md](ralph.md) | Development Methodology | Reference |

## Priority Definitions

| Priority | Meaning | MVP? |
|----------|---------|------|
| **P0** | Critical path - must have for any release | Yes |
| **P1** | Core MVP feature | Yes |
| **P2** | Important but can wait for v1.1 | No |
| **P3** | Nice to have, post-MVP | No |

## MVP Feature Summary

### P0 - Critical Path

These features are required for the app to function at all:

- Basic video/audio/photo capture
- Local encrypted storage (temporary)
- Upload to at least one node
- Evidence hash generation
- Basic app structure and navigation

### P1 - MVP Features

These features differentiate Witness from alternatives:

- **Witness Mode**: Screen-off recording with volume button activation
- **Camouflage**: Calculator disguise with decoy mode
- **Federation**: Upload to multiple nodes with replication
- **Anonymous Accounts**: No identity required
- **Live Streaming**: When connectivity allows
- **Metadata Capture**: GPS, timestamp, device info

### P2 - Post-MVP

Features that add significant value but can wait:

- Multi-language support (beyond English/Spanish)
- Bluetooth mesh for multi-witness sync
- Advanced power management
- Panic wipe

### P3 - Future

Features for later versions:

- Blockchain timestamp integration
- Device attestation
- Multi-witness corroboration algorithm
- iOS version (limited capabilities)

## Reading the Specs

Each spec file follows this structure:

```markdown
# [Topic Name]

## Overview
Brief description of this area of concern

## Requirements
Tables of requirements with ID, description, priority, and MVP status

## Acceptance Criteria
Testable criteria for completion

## Technical Notes
Implementation guidance and constraints

## Open Questions
Unresolved items needing decisions
```

## How Specs Relate to Implementation

```
specs/                          Implementation
├── 01-capture.md        →      CaptureService, Camera2, MediaCodec
├── 02-upload.md         →      UploadWorker, WorkManager, Retrofit
├── 03-safety.md         →      Camouflage, WitnessModeService
├── 04-verification.md   →      EvidenceHasher, MetadataCollector
├── 05-federation.md     →      NodeRepository, FederationProtocol
├── 06-security.md       →      Tink encryption, Keystore
├── 07-ux.md             →      Compose UI, Navigation
└── 08-platform.md       →      Build config, performance tuning
```

## Updating Specs

When requirements change:

1. Update the relevant spec file
2. Run `./loop.sh plan` to regenerate implementation plan
3. Review task changes
4. Continue with building mode

## Related Documents

- [../CLAUDE.md](../CLAUDE.md) - Project overview for Claude Code
- [../AGENTS.md](../AGENTS.md) - Operational guide with quality gates
- [../IMPLEMENTATION_PLAN.md](../IMPLEMENTATION_PLAN.md) - Current task tracking
- [ralph.md](ralph.md) - Development methodology explanation
