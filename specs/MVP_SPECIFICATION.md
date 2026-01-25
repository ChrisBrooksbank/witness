# Witness — MVP Specification

**Version:** 0.1 (Draft)
**Status:** Requirements Gathering Complete
**Last Updated:** 2026-01-25

---

## 1. Project Overview

### 1.1 Mission
Witness is an open-source, federated video evidence tool designed to protect citizen journalists documenting potential misconduct by authorities, with an initial focus on ICE (Immigration and Customs Enforcement) actions.

### 1.2 Core Philosophy
- **Safety first** — The journalist's safety always takes priority over evidence capture
- **Open source** — Full transparency, community trust, not for profit
- **Decentralized** — No single point of failure, resistant to takedowns and attacks
- **Accessible** — Simple enough for anyone to use, regardless of technical skill

### 1.3 Success Criteria
- Widespread adoption among target communities
- Deterrent effect (authorities aware they are being documented)
- Footage that surfaces and enables accountability

---

## 2. Users & Scenarios

### 2.1 Primary Users
| User Type | Description |
|-----------|-------------|
| Individual citizens | People who witness or encounter enforcement actions |
| Community observers | Organized community members who monitor their neighborhoods |
| Legal observers | Trained individuals documenting for legal purposes |

### 2.2 Target Scenarios
- Street protests and demonstrations
- ICE enforcement actions (raids, checkpoints)
- Workplace enforcement
- Traffic stops and detentions
- Any interaction with authorities where documentation may be needed

---

## 3. Threat Model

### 3.1 Assumed Threats
| Threat | Description | Mitigation Approach |
|--------|-------------|---------------------|
| Device seizure | Authorities confiscate the recording device | Continuous upload, no local storage option |
| Device destruction | Device is physically destroyed | Real-time streaming, immediate hash registration |
| Footage deletion | Authorities delete recordings from device | Federated backup, no single copy |
| Journalist identification | Recorder is identified and faces retaliation | Anonymous accounts, camouflaged app |
| Server takedown | Legal/technical attack on infrastructure | Federated architecture, multiple nodes |
| Network blocking | Connectivity deliberately disrupted | Queue and retry, opportunistic upload |
| Digital surveillance | Tracking of app users | Anonymous accounts, minimal metadata |

### 3.2 Priority Hierarchy
1. **Journalist safety** — Always prioritized
2. **Evidence preservation** — Secondary to safety
3. **Evidence credibility** — Important but not at cost of above

---

## 4. Functional Requirements

### 4.1 Content Capture

#### 4.1.1 Supported Media Types
- **Video** — Primary capture mode
- **Audio** — Standalone or with video
- **Photos** — Single capture and burst mode

#### 4.1.2 Metadata Capture
| Metadata | Purpose | MVP |
|----------|---------|-----|
| GPS coordinates | Location verification | Yes |
| Timestamp | Time verification | Yes |
| Device info | Authenticity signal | Yes |
| Orientation | Context | Yes |
| Network status | Context | Post-MVP |

#### 4.1.3 Capture Modes
| Mode | Description | MVP |
|------|-------------|-----|
| Standard | Normal recording with UI visible | Yes |
| Witness mode | Screen off, subtle activation, covert recording | Yes |
| Low battery mode | Reduced quality, optimized for longevity | Yes |
| Audio-only | Minimal power consumption fallback | Yes |

### 4.2 Upload & Streaming

#### 4.2.1 Adaptive Upload Strategy
```
IF battery OK AND connectivity strong THEN
    Live stream to federated nodes
ELSE IF battery OK AND connectivity weak THEN
    Continuous background upload (chunked)
ELSE IF battery low THEN
    Queue locally, upload when conditions improve
    Consider audio-only mode
END
```

#### 4.2.2 Upload Behavior
- **Chunked upload** — Stream in small segments so partial footage survives interruption
- **Queue persistence** — Queued uploads survive app restart/device restart
- **Opportunistic sync** — Attempt upload whenever connectivity is available
- **Wi-Fi preference** — Option to defer large uploads to Wi-Fi
- **Resume capability** — Interrupted uploads resume from last successful chunk

#### 4.2.3 Live Streaming
- Stream to designated watchers (pre-configured trusted contacts)
- Stream to federated nodes for archival
- Panic trigger option — alert watchers when stream begins
- Graceful degradation when bandwidth insufficient

### 4.3 Safety Features

#### 4.3.1 Anonymity
- **Anonymous accounts only** — No real identity required or stored
- **Burner accounts** — Easy to abandon and create new identity
- **No account option** — Anonymous upload without any account (post-MVP consideration)
- **No PII collection** — App never asks for name, email, phone

#### 4.3.2 Camouflage
- App disguised as innocuous application (calculator, weather, notes)
- Alternate icon and name selectable by user
- No "Witness" branding visible when camouflaged
- Decoy mode — opens fake app if opened normally, real app via secret gesture

#### 4.3.3 Witness Mode (Covert Recording)
- Record with screen off
- Activation methods:
  - Volume button sequence (e.g., up-up-down-down)
  - Shake pattern
  - Widget that appears innocuous
- Audio/haptic confirmation (subtle)
- Continues recording if other apps opened over it

#### 4.3.4 Evidence Protection
- **No local storage option** — Footage uploaded and local copy deleted
- **Encrypted local cache** — If footage must be stored locally, encrypted
- **Panic wipe** — Quick action to clear all local evidence (post-MVP)
- **Plausible deniability** — Camouflage makes app presence non-obvious

### 4.4 Verification & Credibility

#### 4.4.1 MVP Verification
| Feature | Description |
|---------|-------------|
| SHA-256 hash | Generated at moment of capture, proves footage unaltered |
| Metadata bundle | GPS, timestamp, device info packaged with hash |
| Immediate hash upload | Hash sent to nodes immediately, even before full footage |

#### 4.4.2 Post-MVP Verification
| Feature | Description |
|---------|-------------|
| Blockchain timestamping | Immutable proof of when hash existed |
| Device attestation | Cryptographic proof footage came from real device camera |
| Multi-witness corroboration | Algorithm scoring based on multiple recordings of same incident |
| Tamper detection | Analysis to detect manipulation on playback |

### 4.5 Federation & Architecture

#### 4.5.1 Federated Model
- Trusted organizations run independent nodes
- No central authority controls the network
- Nodes can operate independently if isolated
- Footage replicates across multiple nodes automatically

#### 4.5.2 Node Characteristics
| Requirement | Description |
|-------------|-------------|
| Independence | Each node functional without others |
| Replication | Footage automatically copied to multiple nodes |
| Encryption | Footage encrypted, node operators cannot view without permission |
| Open source | Node software fully auditable |

#### 4.5.3 Potential Node Operators
- ACLU chapters
- Immigrant rights organizations
- Legal aid societies
- Press freedom organizations
- Universities
- Faith-based organizations
- Individual activists (self-hosted)

#### 4.5.4 Access Control
- Recorder controls who can view their footage
- Can grant access to specific individuals, organizations, or make public
- Revocable access
- Option to pre-authorize access (e.g., "if I go silent for 24h, release to lawyers")

---

## 5. Non-Functional Requirements

### 5.1 Platforms
- **Android** — Target API level TBD, support older devices
- **iOS** — Target iOS version TBD

### 5.2 Performance
| Metric | Target |
|--------|--------|
| App launch to recording | < 3 seconds |
| Witness mode activation | < 2 seconds |
| Time to first upload chunk | < 10 seconds on good connectivity |
| Battery impact (recording) | Comparable to native camera app |
| Storage footprint | < 50MB app size |

### 5.3 Usability
- **Zero training required** — Install and immediately usable
- **Icon-driven UI** — Minimal text, universal symbols
- **Accessibility** — Screen reader support, high contrast option
- **Offline capable** — Core recording works without connectivity

### 5.4 Localization
| Language | MVP | Post-MVP |
|----------|-----|----------|
| English | Yes | — |
| Spanish | Yes | — |
| Other languages | — | Community translation system |

### 5.5 Reliability
- App must not crash during recording
- Must survive backgrounding, interruptions, phone calls
- Queued uploads must persist across app/device restarts
- Graceful degradation, never silent failure

---

## 6. User Experience

### 6.1 Onboarding
1. Install app (disguised in app store listing or via direct APK/TestFlight)
2. Choose camouflage appearance
3. Set up witness mode activation gesture
4. Optionally add trusted contacts for live stream alerts
5. Ready to record

### 6.2 Primary User Flows

#### 6.2.1 Standard Recording
```
1. Open app (via camouflage or direct)
2. Tap record button
3. Point camera, record
4. Tap stop
5. Footage uploads automatically
6. Confirmation shown
```

#### 6.2.2 Witness Mode Recording
```
1. Perform secret gesture (e.g., volume button sequence)
2. Subtle confirmation (vibration)
3. Screen stays off or shows decoy
4. Recording happens in background
5. Perform stop gesture or recording auto-stops based on settings
6. Footage uploads automatically
```

#### 6.2.3 Emergency / Panic Scenario
```
1. User is recording
2. Situation becomes dangerous
3. User performs panic action (optional feature)
4. App immediately pushes all cached footage
5. Local evidence cleared
6. App shows decoy or closes
```

### 6.3 UI Principles
- Large touch targets (usable with shaking hands, gloves)
- High contrast (visible in bright sunlight)
- Minimal steps for any action
- No confirmation dialogs during recording
- Status always visible (recording, uploading, queued)

---

## 7. Technical Architecture (High-Level)

### 7.1 Client Architecture
```
┌─────────────────────────────────────────┐
│              Witness App                │
├─────────────────────────────────────────┤
│  UI Layer (Camouflage + Real UI)        │
├─────────────────────────────────────────┤
│  Capture Service                        │
│  - Video/Audio/Photo capture            │
│  - Metadata collection                  │
│  - Hash generation                      │
├─────────────────────────────────────────┤
│  Upload Manager                         │
│  - Chunking                             │
│  - Queue management                     │
│  - Retry logic                          │
│  - Encryption                           │
├─────────────────────────────────────────┤
│  Local Storage (Encrypted)              │
│  - Pending uploads                      │
│  - Settings                             │
│  - No long-term footage storage         │
├─────────────────────────────────────────┤
│  Network Layer                          │
│  - Federation protocol                  │
│  - Node discovery                       │
│  - Streaming                            │
└─────────────────────────────────────────┘
```

### 7.2 Federation Architecture
```
┌──────────┐    ┌──────────┐    ┌──────────┐
│  Node A  │◄──►│  Node B  │◄──►│  Node C  │
│ (Org 1)  │    │ (Org 2)  │    │ (Org 3)  │
└────▲─────┘    └────▲─────┘    └────▲─────┘
     │               │               │
     └───────────────┼───────────────┘
                     │
              ┌──────▼──────┐
              │   Clients   │
              │ (Witnesses) │
              └─────────────┘
```

### 7.3 Data Flow
```
Capture → Hash → Encrypt → Chunk → Upload → Replicate
                                      │
                              ┌───────▼───────┐
                              │ Multiple Nodes│
                              └───────────────┘
```

---

## 8. MVP Scope

### 8.1 In Scope (MVP)
| Feature | Priority |
|---------|----------|
| Video capture | Must have |
| Audio capture | Must have |
| Photo capture | Must have |
| GPS metadata | Must have |
| SHA-256 hash at capture | Must have |
| Anonymous accounts | Must have |
| Camouflaged app | Must have |
| Witness mode (screen off recording) | Must have |
| Federated upload to nodes | Must have |
| Live streaming (when conditions allow) | Must have |
| Low battery mode | Must have |
| Queue + retry upload | Must have |
| English + Spanish | Must have |
| Android app | Must have |
| iOS app | Must have |

### 8.2 Out of Scope (Post-MVP)
| Feature | Notes |
|---------|-------|
| Blockchain timestamping | Adds complexity |
| Device attestation | Requires platform-specific work |
| Multi-witness corroboration | Needs significant backend logic |
| Dead man's switch | Safety feature, but adds complexity |
| Panic wipe | Needs careful implementation |
| Tor/VPN integration | Users can use external tools |
| Additional languages | Community translation post-launch |
| No-account anonymous upload | Federation needs some identity model |

---

## 9. Security Considerations

### 9.1 Encryption
- All footage encrypted before leaving device
- TLS for all network communication
- At-rest encryption on federated nodes
- End-to-end encryption option (only recorder + authorized viewers can decrypt)

### 9.2 Key Management
- Keys generated on device
- Option to backup keys (for recovery)
- Keys never transmitted to nodes in plaintext
- Consider key escrow model for organizational use

### 9.3 Threat Mitigations
| Threat | Mitigation |
|--------|------------|
| Man-in-the-middle | Certificate pinning, TLS |
| Node compromise | Encryption, footage distributed across nodes |
| Device forensics | No local storage option, encrypted cache |
| Traffic analysis | Consider traffic padding, Tor integration (post-MVP) |

---

## 10. Legal Considerations

### 10.1 Recording Laws
- Recording laws vary by jurisdiction (one-party vs two-party consent)
- App should inform users of legal considerations
- Not legal advice — users responsible for understanding local laws
- In-app resources or links to legal guidance

### 10.2 Terms of Service
- Clear data handling policies
- No warranties about legal protection
- Open source license (TBD — likely GPL or similar copyleft)

### 10.3 Organizational Structure
- Consider fiscal sponsor or nonprofit structure
- Legal review of federation model
- DMCA/takedown policy for nodes

---

## 11. Open Questions

| Question | Status |
|----------|--------|
| Specific tech stack (React Native? Flutter? Native?) | To be decided |
| Federation protocol details | To be designed |
| Node software architecture | To be designed |
| Key management model | To be designed |
| App store strategy (may be removed for content policies) | To be researched |
| Specific camouflage apps to mimic | To be decided |
| Witness mode activation gestures | To be user tested |
| Funding/sustainability model | Open |
| Partnerships with orgs for node hosting | Open |
| Legal review | Needed |

---

## 12. Appendix

### 12.1 Comparable Tools
| Tool | Strengths | Gaps |
|------|-----------|------|
| ACLU Mobile Justice | Established, legal backing | Centralized, US-state specific |
| ProofMode | Good metadata/verification | Not focused on safety, no streaming |
| Signal | Excellent security | Not purpose-built for evidence |
| Witness.org tools | Mission-aligned | Various |

### 12.2 Glossary
| Term | Definition |
|------|------------|
| Federated | Architecture where independent servers cooperate |
| Witness mode | Covert recording with screen off |
| Camouflage | Disguising the app as another application |
| Hash | Cryptographic fingerprint proving content integrity |
| Node | A server in the federated network run by a trusted org |

---

## 13. Document History

| Version | Date | Changes |
|---------|------|---------|
| 0.1 | 2026-01-25 | Initial requirements gathering complete |

---

*This document is a living specification and will be updated as the project evolves.*
