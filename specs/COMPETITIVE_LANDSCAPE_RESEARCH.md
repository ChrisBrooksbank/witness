# Competitive Landscape Research: Video Evidence & Documentation Tools

**Research Date:** January 2026
**Project:** Witness - Federated Video Evidence Tool for Citizen Journalists
**Researcher:** Claude Code (AI-Assisted Research)

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Market Overview](#market-overview)
3. [Product Deep Dives](#product-deep-dives)
   - [ACLU Mobile Justice](#1-aclu-mobile-justice-discontinued)
   - [ProofMode](#2-proofmode-guardian-project)
   - [eyeWitness to Atrocities](#3-eyewitness-to-atrocities)
   - [Tella](#4-tella-horizontal)
   - [Save by OpenArchive](#5-save-by-openarchive)
   - [Starling Lab Framework](#6-starling-lab-framework)
   - [Signal](#7-signal)
   - [Matrix/Element](#8-matrixelement)
   - [Briar](#9-briar)
   - [Mesh Networking Apps](#10-mesh-networking-apps-bridgefy-firechat-bitchat)
4. [Standards & Protocols](#standards--protocols)
   - [C2PA Content Authenticity](#c2pa--content-authenticity-initiative)
   - [IPFS/Filecoin Decentralized Storage](#ipfsfilecoin)
5. [Historical/Archived Tools](#historicalarchived-tools)
6. [Advanced Deployment Scenarios](#advanced-deployment-scenarios)
   - [Abandoned Device / Dead Drop Mode](#abandoned-device--dead-drop-mode)
   - [Multi-Witness Scenarios](#multi-witness-scenarios)
7. [Hardware Considerations](#hardware-considerations)
   - [Standalone Hidden Cameras](#standalone-hidden-cameras)
   - [Body-Worn Cameras](#body-worn-cameras)
   - [DIY Options (Raspberry Pi)](#diy-options-raspberry-pi)
   - [Repurposed Smartphones](#repurposed-smartphones)
8. [Comparative Analysis](#comparative-analysis)
9. [Gap Analysis](#gap-analysis)
10. [Recommendations for Witness](#recommendations-for-witness)
11. [Witness Value Proposition](#witness-value-proposition)
12. [Brief Mentions: Other Relevant Products](#brief-mentions-other-relevant-products)
13. [Sources](#sources)

---

## Executive Summary

The market for secure video documentation tools serving activists, human rights defenders, and citizen journalists has evolved significantly since 2015. Our analysis of 15+ tools reveals a fragmented landscape where **no single solution combines all three pillars that Witness targets**:

1. **Federated architecture** with multi-node replication
2. **Covert recording with camouflage** for user safety
3. **Real-time streaming to distributed storage**

### Key Findings

| Finding | Implication for Witness |
|---------|------------------------|
| ACLU Mobile Justice discontinued (Feb 2025) | Centralized models are unsustainable; validates federation approach |
| eyeWitness has 85k+ files, first ICC conviction | Legal pathway partnerships are critical for credibility |
| Tella has best camouflage but no covert recording | Opportunity to exceed with screen-off + camouflage |
| ProofMode leads verification but lacks safety features | Integrate ProofMode's approach, add safety layer |
| Mesh networking saw 4000% growth in Hong Kong protests | Multi-witness Bluetooth sync is validated use case |
| C2PA adoption still low despite industry backing | Don't over-invest in standards; focus on core UX |

### Market Opportunity

Witness occupies a unique position: **safety-first evidence capture with federated resilience**. The discontinuation of ACLU Mobile Justice leaves a gap in the US market, particularly for ICE documentation where no purpose-built tool exists.

---

## Market Overview

### Problem Space

Citizen journalists and human rights documenters face interconnected challenges:

```
┌─────────────────────────────────────────────────────────────────┐
│                        THREAT LANDSCAPE                         │
├─────────────────┬─────────────────┬─────────────────────────────┤
│ Device Seizure  │ Network Blocks  │ Identity Exposure           │
│ Evidence Delete │ Server Takedown │ Physical Harm               │
│ Footage Tamper  │ Surveillance    │ Legal Retaliation           │
└─────────────────┴─────────────────┴─────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     REQUIRED CAPABILITIES                       │
├─────────────────┬─────────────────┬─────────────────────────────┤
│ Secure Capture  │ Resilient Store │ Verifiable Evidence         │
│ Covert Mode     │ Decentralized   │ Chain of Custody            │
│ Camouflage      │ Redundant       │ Legal Admissibility         │
└─────────────────┴─────────────────┴─────────────────────────────┘
```

### Market Segments

| Segment | Primary Need | Current Solutions | Gap |
|---------|--------------|-------------------|-----|
| War crimes documentation | Legal admissibility | eyeWitness | Centralized storage |
| Protest documentation | Safety + immediacy | Tella, Signal | No federation |
| Immigration enforcement | Anonymity + speed | *None purpose-built* | **Major gap** |
| Police accountability | Quick capture + backup | *ACLU discontinued* | **Major gap** |
| Election monitoring | Verification + scale | Tella | Limited streaming |

---

## Product Deep Dives

### 1. ACLU Mobile Justice (DISCONTINUED)

**Status:** Discontinued February 2025
**Operator:** ACLU State Chapters
**Platforms:** iOS, Android (no longer available)

#### Summary

ACLU Mobile Justice was a video recording app that automatically uploaded footage to local ACLU chapters for legal review. It represented the most direct precedent for Witness's mission but failed due to architectural and sustainability issues.

#### Features

| Feature | Description |
|---------|-------------|
| One-tap recording | Immediate video capture |
| Automatic upload | Videos sent to local ACLU affiliate |
| Witness alerts | Notify nearby users of incidents |
| Know Your Rights | In-app legal information |
| Trusted contacts | Designate 3 people to receive videos |

#### Architecture

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   User App   │────▶│  ACLU State  │────▶│  Legal Team  │
│              │     │    Server    │     │    Review    │
└──────────────┘     └──────────────┘     └──────────────┘
        │                    │
        │            Single Point of
        │               Failure
        ▼
   Local Copy
   (Vulnerable)
```

#### Strengths

- **Institutional backing**: Direct pathway to ACLU legal resources
- **Simple UX**: One-tap recording for high-stress situations
- **Community feature**: Witness alerts created solidarity network
- **Bilingual**: English and Spanish support
- **Proven demand**: Deployed across 18+ states

#### Weaknesses

- **Centralized architecture**: Each state had independent server
- **Maintenance burden**: 18+ separate deployments to maintain
- **No sustainability model**: Depended on chapter funding
- **No camouflage**: App clearly identified as "Mobile Justice"
- **No covert recording**: Required visible app interaction
- **State fragmentation**: Different versions for different states

#### Why It Failed

1. **Unsustainable architecture**: Each ACLU chapter maintained separate infrastructure
2. **Funding dependency**: No revenue model beyond donations
3. **Technical debt**: Multiple codebases diverged over time
4. **No federation**: Couldn't share resources across chapters

#### Lessons for Witness

| Lesson | Application |
|--------|-------------|
| Centralized = fragile | Federation is essential |
| Chapter model failed | Single codebase, multiple nodes |
| Legal backing valuable | Partner with orgs, don't replicate |
| Simple UX critical | One-tap recording non-negotiable |

**Sources:** [ACLU Mobile Justice Wikipedia](https://en.wikipedia.org/wiki/ACLU_Mobile_Justice), [ACLU Minnesota](https://www.aclu-mn.org/en/mobile-justice-app), [ACLU Pennsylvania](https://www.aclupa.org/press-releases/aclu-pa-releases-new-app-hold-law-enforcement-accountable/)

---

### 2. ProofMode (Guardian Project)

**Status:** Active - v2.6.0 (2025)
**Operator:** Guardian Project + WITNESS partnership
**Platforms:** Android, iOS
**License:** Open Source

#### Summary

ProofMode is the gold standard for evidence verification on mobile. It automatically generates cryptographic proofs for photos and videos, creating a verifiable chain of custody. However, it prioritizes verification over user safety and lacks streaming capabilities.

#### Features

| Feature | Description |
|---------|-------------|
| OpenPGP signing | Automatic cryptographic signature of all media |
| Sensor metadata | GPS, accelerometer, compass, light, network data |
| C2PA support | Content Authenticity Initiative compliance |
| IPFS/Filecoin | Decentralized hash notarization |
| ProofCheck | Verification tool for recipients |
| Self-signed certs | No centralized authority required |

#### Technical Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      PROOFMODE FLOW                         │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐  │
│  │ Capture │───▶│  Sign   │───▶│ Bundle  │───▶│ Notarize│  │
│  │  Media  │    │ OpenPGP │    │ + CSV   │    │  IPFS   │  │
│  └─────────┘    └─────────┘    └─────────┘    └─────────┘  │
│       │              │              │              │        │
│       ▼              ▼              ▼              ▼        │
│   Original      Private Key    Metadata       Filecoin     │
│    Media        Generated      Snapshot        Hash        │
│                 On-Device                    Timestamp      │
└─────────────────────────────────────────────────────────────┘
```

#### Verification Process

```bash
# Verify hash matches media file
sha256sum media_file.jpg
# Compare to hash in proof.csv

# Verify OpenPGP signature
gpg --verify proof.csv.asc proof.csv

# Check Filecoin notarization
# CID provides immutable timestamp
```

#### Metadata Captured

| Sensor | Data | Purpose |
|--------|------|---------|
| GPS | Lat/Long/Alt/Accuracy | Location verification |
| Accelerometer | X/Y/Z movement | Context/handling |
| Compass | Bearing | Orientation |
| Light meter | Lux reading | Environmental context |
| Cell towers | Tower IDs | Location corroboration |
| Wi-Fi | Network signatures | Location corroboration |
| Bluetooth | Device signatures | Environmental context |

#### Strengths

- **Best-in-class verification**: Most comprehensive proof system
- **Open source**: Fully auditable, community trusted
- **Decentralized notarization**: IPFS/Filecoin integration
- **C2PA compliant**: Future-proof standards alignment
- **No account required**: Privacy-respecting design
- **Lightweight**: Runs in background, minimal battery
- **Self-signed support**: No centralized CA dependency

#### Weaknesses

- **No safety features**: No camouflage or covert mode
- **No streaming**: Files must be manually shared
- **No immediate upload**: User must initiate sharing
- **Technical complexity**: Verification requires expertise
- **No encrypted storage**: Media visible in gallery
- **No panic features**: No quick delete or wipe

#### Adoption

- Integrated into WITNESS training programs globally
- Used by journalists in conflict zones
- Recommended by digital security trainers
- Active GitHub community

#### Carry Forward to Witness

| ProofMode Feature | Witness Integration |
|-------------------|---------------------|
| SHA-256 hashing | Implement at capture |
| Sensor metadata bundle | Replicate approach |
| C2PA support | Consider for v2 |
| Self-signed certificates | Enable for privacy |
| IPFS integration | Use for hash notarization |

**Sources:** [Guardian Project ProofMode](https://guardianproject.info/apps/org.witness.proofmode/), [ProofMode.org](https://proofmode.org/about), [ProofMode C2PA](https://proofmode.org/c2pa), [Filecoin Foundation](https://fil.org/ecosystem-explorer/proofmode-by-guardian-project)

---

### 3. eyeWitness to Atrocities

**Status:** Active (Since 2015)
**Operator:** International Bar Association (IBA)
**Storage Partner:** LexisNexis Rule of Law Foundation
**Platforms:** Android, iOS

#### Summary

eyeWitness to Atrocities is the most legally credible evidence documentation app, with a proven track record of footage admitted to international courts including the ICC. It achieved the first-ever conviction using app-captured video evidence (DRC, 2018).

#### Features

| Feature | Description |
|---------|-------------|
| Embedded metadata | GPS, timestamp, device info at capture |
| Digital fingerprinting | Cryptographic hash at moment of capture |
| Secure vault | LexisNexis-hosted encrypted storage |
| Chain of custody | Full audit trail of all access |
| Legal pathway | Direct submission to ICC and courts |
| Encrypted local storage | PIN-protected within app |
| Icon selection | Can change app icon (limited camouflage) |

#### Technical Architecture

```
┌───────────────────────────────────────────────────────────────────┐
│                   eyeWITNESS ARCHITECTURE                         │
├───────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────┐                                                  │
│  │   Mobile    │                                                  │
│  │    App      │                                                  │
│  └──────┬──────┘                                                  │
│         │                                                         │
│         │ Capture + Embed Metadata + Generate Hash                │
│         │                                                         │
│         ▼                                                         │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────┐   │
│  │  Encrypted  │───▶│  LexisNexis │───▶│  eyeWitness Legal   │   │
│  │   Upload    │    │   Secure    │    │    Analysis Team    │   │
│  └─────────────┘    │    Vault    │    └─────────────────────┘   │
│                     └──────┬──────┘              │                │
│                            │                     │                │
│                            │                     ▼                │
│                            │         ┌─────────────────────┐     │
│                            │         │  Dossier Creation   │     │
│                            │         │  for ICC/Courts     │     │
│                            │         └─────────────────────┘     │
│                            │                                      │
│                     Chain of Custody                              │
│                     Documentation                                 │
└───────────────────────────────────────────────────────────────────┘
```

#### Legal Track Record

| Milestone | Date | Significance |
|-----------|------|--------------|
| App launch | 2015 | First legal-grade evidence app |
| First ICC submission | 2017 | Established credibility |
| **First conviction** | Sept 2018 | DRC military tribunal, crimes against humanity |
| Ukraine documentation | 2022+ | 55,000+ items from conflict zone |
| 85+ dossiers submitted | Ongoing | Multiple international courts |

#### Adoption Statistics

| Metric | Value |
|--------|-------|
| Total captures | 85,000+ photos/videos/audio |
| Downloads | 10,000+ |
| Countries accessed | 130 |
| Ukraine items | 55,000+ (since 2022) |
| Training sessions | 900+ |
| Dossiers submitted | 85+ |

#### Strengths

- **Proven legal admissibility**: Only app with conviction record
- **Institutional backing**: IBA provides legal expertise
- **Professional storage**: LexisNexis enterprise-grade security
- **Direct court pathway**: Established ICC relationships
- **Training program**: 900+ sessions delivered
- **Chain of custody**: Full audit trail

#### Weaknesses

- **Centralized storage**: Single point of failure (LexisNexis)
- **Limited camouflage**: Can change icon but app name visible
- **No covert recording**: Requires app interaction
- **No live streaming**: Upload only after capture
- **Closed source**: Cannot audit security
- **Organization controlled**: Users don't control footage

#### Carry Forward to Witness

| eyeWitness Approach | Witness Application |
|---------------------|---------------------|
| Immediate hash generation | Implement identically |
| Chain of custody documentation | Replicate for federation |
| Legal org partnerships | Build similar relationships |
| Metadata embedding | Match their standards |
| Training program model | Partner with orgs for training |

**Sources:** [eyeWitness Global](https://www.eyewitness.global/), [LexisNexis Foundation](https://www.lexisnexisrolfoundation.org/projects/eyewitness.aspx), [IBA Press Release](https://www.ibanet.org/eyeWitness-to-atrocities-app-surpasses-collection-of-20000-verifiable-items-of-potential-human-rights-violations-in-Ukraine-and-group-submits-evidence-to-UN-COI), [NBC News](https://www.nbcnews.com/tech/tech-news/eyewitness-app-aims-expose-war-criminals-n411801)

---

### 4. Tella (Horizontal)

**Status:** Active
**Operator:** Horizontal (nonprofit)
**Platforms:** Android, iOS, F-Droid
**License:** Open Source (FOSS version available)

#### Summary

Tella is the closest existing product to Witness in philosophy, with a strong focus on user safety through camouflage and encryption. However, it lacks covert (screen-off) recording and federated architecture.

#### Features

| Feature | Description |
|---------|-------------|
| **Camouflage** | Change app icon/name to calculator, weather, etc. |
| **Calculator disguise** | Fully functional calculator; PIN opens Tella |
| **Encrypted vault** | All media encrypted, invisible outside app |
| **Quick delete** | Panic-style rapid deletion |
| **Offline mode** | Queue uploads for later |
| **Server integrations** | Uwazi, ODK, Tella Web, Google Drive, Nextcloud |
| **FOSS version** | 100% tracker-free variant |

#### Camouflage Implementation (Technical Deep Dive)

```
┌─────────────────────────────────────────────────────────────┐
│                  TELLA CAMOUFLAGE MODES                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  MODE 1: Icon Disguise                                      │
│  ┌─────────────┐                                            │
│  │   Weather   │ ◄── User sees weather icon                 │
│  │    Icon     │     Tap opens Tella directly               │
│  └─────────────┘                                            │
│                                                             │
│  MODE 2: Calculator Camouflage (More Secure)                │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐     │
│  │ Calculator  │───▶│  Enter PIN  │───▶│   Tella     │     │
│  │   (Real!)   │    │  as calc #  │    │   Opens     │     │
│  └─────────────┘    └─────────────┘    └─────────────┘     │
│        │                                                    │
│        ▼                                                    │
│   Works as actual                                           │
│   calculator if wrong                                       │
│   numbers entered                                           │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

#### Limitations of Camouflage

| Limitation | Description |
|------------|-------------|
| Android Settings visible | App still shows as "Tella" in Settings > Apps |
| Storage size reveals | Large files (videos) make app storage suspicious |
| iOS not supported | Apple App Store restrictions prevent icon changes |
| Calculator + PIN conflict | Cannot use with "restrict unlock attempts" |

#### Server Integrations

| Backend | Use Case |
|---------|----------|
| Tella Web | Horizontal's own server solution |
| Uwazi | Human rights documentation database |
| Open Data Kit (ODK) | Survey/forms collection |
| Google Drive | Consumer cloud storage |
| Nextcloud | Self-hosted cloud |

#### Real-World Deployments

| Context | Year | Scale |
|---------|------|-------|
| Nigerian elections | 2019 | Thousands of observers |
| Belarus elections | 2020-21 | Multiple deployments |
| Gender-based violence (Cuba) | Ongoing | Cubalex partnership |
| Human rights (Myanmar) | Ongoing | Active documentation |
| Human rights (West Papua) | Ongoing | Active documentation |
| Indigenous defenders (Brazil) | Ongoing | Active documentation |

#### Strengths

- **Best camouflage implementation**: Calculator disguise is innovative
- **Strong encryption**: Files invisible outside app
- **Open source**: Fully auditable (FOSS version available)
- **Flexible backends**: Multiple server options
- **Free forever**: Commitment to accessibility
- **Active development**: Tella Web launching

#### Weaknesses

- **No covert recording**: Cannot record with screen off
- **No live streaming**: Upload only, not real-time
- **No federation**: Single server destination
- **No hash verification**: Limited evidence verification
- **iOS camouflage missing**: Apple restrictions
- **Server setup required**: Organizations must configure backend

#### Carry Forward to Witness

| Tella Innovation | Witness Implementation |
|------------------|------------------------|
| Calculator camouflage | Replicate for Android |
| Icon disguise system | Extend with more options |
| Encrypted vault | Match encryption standard |
| Quick delete | Implement panic wipe |
| FOSS commitment | Open source from day one |

**Sources:** [Tella App](https://tella-app.org/), [Tella Camouflage Docs](https://docs.tella-app.org/features/camouflage), [Horizontal GitHub](https://github.com/Horizontal-org/Tella-Android), [World Justice Project](https://worldjusticeproject.org/world-justice-challenge-2021/tella), [Open Tech Fund](https://www.opentech.fund/news/tella-transforms-activist-reporting-from-the-field/)

---

### 5. Save by OpenArchive

**Status:** Active
**Operator:** OpenArchive (nonprofit collective)
**Platforms:** Android, iOS, F-Droid
**License:** Open Source

#### Summary

Save (Share, Archive, Verify, Encrypt) focuses on secure archival rather than capture. It's designed to safely transfer media from device to organizational servers or the Internet Archive.

#### Features

| Feature | Description |
|---------|-------------|
| TLS encryption | Encrypted connection to servers |
| Metadata editing | Add notes, location, flags |
| Batch operations | Process multiple files |
| Project albums | Organize by campaign |
| Internet Archive | Direct upload to IA |
| Nextcloud | Self-hosted server support |

#### Use Cases

Save is deployed with Decentralized Archival Communities (DACs):
- Iraq
- Sudan
- Ukraine (Kharkiv Human Rights Protection Group)
- United States (People's Media Record)
- Ecuador (ALDEA Foundation)
- Cuba (Cubalex)
- Mexico (Data Civica)

#### Strengths

- Community-driven development
- Internet Archive integration
- Open source
- DAC network model

#### Weaknesses

- Not a capture tool (archival focus)
- No camouflage
- No covert features
- Limited verification

**Sources:** [OpenArchive](https://www.open-archive.org/), [Save App](https://www.open-archive.org/save), [FFDW Blog](https://ffdweb.org/blog/decentralized-technology-for-human-rights-archiving-and-activism/)

---

### 6. Starling Lab Framework

**Status:** Active (Research/Academic)
**Operator:** USC Shoah Foundation + Stanford University
**Funding:** Protocol Labs, Filecoin Foundation ($2M+)

#### Summary

Starling Lab is an academic research initiative developing protocols for authenticated digital evidence. While not a consumer app, their framework informs best practices for evidence capture, storage, and verification.

#### The Starling Framework

```
┌─────────────────────────────────────────────────────────────────┐
│                    STARLING FRAMEWORK                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐         │
│  │   CAPTURE   │───▶│    STORE    │───▶│   VERIFY    │         │
│  │             │    │             │    │             │         │
│  │ • Mobile    │    │ • IPFS      │    │ • Hedera    │         │
│  │ • Camera    │    │ • Filecoin  │    │ • Audit     │         │
│  │ • Firmware  │    │ • Hyperledger│   │ • Timestamp │         │
│  │ • Metadata  │    │ • Encrypted │    │ • Provenance│         │
│  └─────────────┘    └─────────────┘    └─────────────┘         │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

#### Technology Stack

| Layer | Technologies |
|-------|--------------|
| Capture | Mobile prototypes, camera firmware |
| Storage | IPFS, Filecoin, Hyperledger Fabric |
| Verification | Hedera Hashgraph, public ledgers |
| Standards | C2PA integration |

#### Impact

| Project | Description |
|---------|-------------|
| Ukraine ICC submission | First cryptographic dossier to ICC (2022) |
| Holocaust testimonies | USC Shoah Foundation archive to Filecoin |
| Presidential transition | Trump → Biden documentation |
| Reuters partnership | Photo authentication prototype |

#### Carry Forward to Witness

| Starling Approach | Witness Application |
|-------------------|---------------------|
| Capture-Store-Verify model | Adopt as mental framework |
| Multi-tech redundancy | Use multiple verification methods |
| IPFS for storage | Consider for hash notarization |
| Academic rigor | Reference their research |

**Sources:** [Starling Lab](https://www.starlinglab.org/), [USC Shoah Foundation](https://sfi.usc.edu/news/2022/06/33571-starling-lab-and-hala-systems-file-cryptographic-submission-evidence-war-crimes), [Filecoin Blog](https://filecoin.io/blog/posts/starling-lab-establishing-trust-for-humanity-s-data/), [Fast Company](https://www.fastcompany.com/90731729/inside-starling-lab-a-moonshot-project-to-preserve-the-worlds-most-important-information)

---

### 7. Signal

**Status:** Active - Industry standard for secure messaging
**Operator:** Signal Foundation (nonprofit)
**Platforms:** Android, iOS, Desktop
**License:** Open Source

#### Summary

Signal is the gold standard for encrypted messaging but is not purpose-built for evidence documentation. Many activists use Signal to share footage, but it lacks capture, verification, and safety features.

#### Relevance to Witness

| Aspect | Signal Approach | Witness Opportunity |
|--------|-----------------|---------------------|
| Encryption | E2E encryption standard (Signal Protocol) | Match or integrate |
| Video calls | Encrypted real-time video | Model for streaming |
| Trust | Nonprofit, open source, audited | Follow same model |
| Phone requirement | Requires phone number | Improve with anonymous accounts |

#### Strengths

- Industry-leading encryption
- Edward Snowden endorsement
- Nonprofit foundation
- Open source protocol
- Audited security

#### Weaknesses

- **Requires phone number** (identity risk)
- Not built for evidence
- No metadata capture
- No verification features
- Centralized infrastructure
- File size limits

**Sources:** [Signal Wikipedia](https://en.wikipedia.org/wiki/Signal_(software)), [Signal.org](https://signal.org/)

---

### 8. Matrix/Element

**Status:** Active
**Operator:** Matrix.org Foundation + Element (company)
**Platforms:** Web, Android, iOS, Desktop
**License:** Open Source (Apache 2.0)

#### Summary

Matrix is a federated communication protocol (like email for messaging). Element is the primary client. The protocol demonstrates that secure federation at scale is technically feasible.

#### Federation Model

```
┌─────────────────────────────────────────────────────────────────┐
│                    MATRIX FEDERATION                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐                  │
│  │Homeserver│◄──▶│Homeserver│◄──▶│Homeserver│                  │
│  │   Org A  │    │   Org B  │    │   Org C  │                  │
│  └────┬─────┘    └────┬─────┘    └────┬─────┘                  │
│       │               │               │                         │
│   ┌───▼───┐       ┌───▼───┐       ┌───▼───┐                    │
│   │Clients│       │Clients│       │Clients│                    │
│   └───────┘       └───────┘       └───────┘                    │
│                                                                 │
│  • Rooms replicated across participating servers               │
│  • No single server controls the network                       │
│  • Users choose their homeserver                               │
│  • Servers can operate independently if isolated               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

#### Key Concepts for Witness

| Matrix Concept | Witness Application |
|----------------|---------------------|
| Event graph | Model for evidence replication |
| Eventual consistency | Accept for federation |
| Server signatures | Prevent tampering |
| Room-based sync | Evidence "rooms" per incident |
| No single authority | Core federation principle |

#### Technical Details

- **Server-Server API**: HTTPS with public key signatures
- **Event signing**: Git-style signatures for tamper detection
- **Encryption**: Olm (1:1) and Megolm (group), based on Signal Protocol
- **CAP theorem**: Prioritizes Availability + Partition over Consistency

**Sources:** [Matrix Specification](https://spec.matrix.org/latest/), [Matrix Wikipedia](https://en.wikipedia.org/wiki/Matrix_(protocol)), [Element.io](https://element.io/en)

---

### 9. Briar

**Status:** Active
**Operator:** Briar Project
**Platforms:** Android only (no iOS planned)
**License:** Open Source (GPL)

#### Summary

Briar is a peer-to-peer messaging app designed for activists that works without internet via Bluetooth mesh networking. Highly relevant for protest scenarios with multiple nearby witnesses.

#### How It Works

```
┌─────────────────────────────────────────────────────────────────┐
│                    BRIAR MESH NETWORKING                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Internet Available:                                            │
│  ┌──────┐         ┌─────┐         ┌──────┐                     │
│  │Phone │◄───────▶│ Tor │◄───────▶│Phone │                     │
│  │  A   │         │     │         │  B   │                     │
│  └──────┘         └─────┘         └──────┘                     │
│                                                                 │
│  Internet Blocked:                                              │
│  ┌──────┐  BT/WiFi  ┌──────┐  BT/WiFi  ┌──────┐               │
│  │Phone │◄─────────▶│Phone │◄─────────▶│Phone │               │
│  │  A   │   ~100m   │  B   │   ~100m   │  C   │               │
│  └──────┘           └──────┘           └──────┘               │
│                                                                 │
│  Transport Options:                                             │
│  • Bluetooth (~10 meters, ~100m with relay)                    │
│  • Wi-Fi Direct (up to 500 feet)                               │
│  • Tor (when internet available)                               │
│  • USB drives (sneakernet)                                     │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

#### Security

- End-to-end encryption
- No central servers
- Contacts stored only on device (encrypted)
- Audited: "Crypto implementation exceptionally clear and sound"

#### Use Cases

- Protests with internet shutdowns
- Dense neighborhoods during outages
- High-security communications
- Journalist coordination

#### Limitations

- **No iOS** (developers have no plans)
- High battery usage
- Text only (no video over Bluetooth bandwidth)
- Requires density of users

#### Carry Forward to Witness

| Briar Approach | Witness Application |
|----------------|---------------------|
| Bluetooth mesh | Multi-witness sync in protests |
| Works offline | Queue + mesh hybrid |
| Tor integration | Optional for anonymity |
| Peer-to-peer model | Evidence sharing between nearby witnesses |

**Sources:** [Briar Project](https://briarproject.org/), [Briar Wikipedia](https://en.wikipedia.org/wiki/Briar_(software)), [Open Tech Fund](https://www.opentech.fund/projects-we-support/supported-projects/briar/)

---

### 10. Mesh Networking Apps (Bridgefy, FireChat, Bitchat)

**Status:** Various (Bridgefy active, FireChat discontinued)
**Relevance:** Demonstrates demand for offline communication in protests

#### Protest Usage Statistics

| Event | App | Impact |
|-------|-----|--------|
| Hong Kong 2019 | Bridgefy | 4000% download increase in 60 days |
| Hong Kong 2014 | FireChat | Massive adoption during Umbrella Movement |
| Belarus 2020 | Bridgefy | Election protest coordination |
| Nigeria 2020 | Bridgefy | EndSARS protest coordination |
| Thailand 2020 | Bridgefy | Pro-democracy protests |
| Myanmar 2021 | Bridgefy | 1M+ downloads after coup |
| US 2020 | Bridgefy | BLM protest coordination |

#### Security Concerns

Critical: A 2021 security analysis of Bridgefy found severe vulnerabilities:
- Users could be **tracked**
- **No authenticity** verification
- **No effective encryption** (at time of analysis)
- Network could be **shut down** with single malicious message

Bridgefy has since added Signal-protocol-based E2E encryption.

#### Bitchat (Newer Entry)

- Bluetooth LE mesh with multi-hop relay
- Nostr protocol fallback for internet
- No accounts, no phone numbers
- Noise Protocol encryption for mesh
- Up to 300 meters with relay

#### Carry Forward to Witness

| Mesh Learning | Witness Application |
|---------------|---------------------|
| High demand proven | Build mesh capability for protests |
| Security failures | Use proven encryption from start |
| Bluetooth range limits | Design for ~100m effective range |
| Battery concerns | Optimize for low power |
| Density requirement | Target protest scenarios specifically |

**Sources:** [Bridgefy](https://bridgefy.me/), [FireChat Wikipedia](https://en.wikipedia.org/wiki/FireChat), [Breaking Bridgefy Paper](https://eprint.iacr.org/2021/214.pdf), [TechCrunch](https://techcrunch.com/2020/11/02/bridgefy-launches-end-to-end-encrypted-messaging-for-the-app-used-during-protests-and-disasters/)

---

## Standards & Protocols

### C2PA / Content Authenticity Initiative

**Status:** Becoming ISO standard (2025)
**Backers:** Adobe, Microsoft, Intel, BBC, Arm, Truepic

#### What It Is

C2PA (Coalition for Content Provenance and Authenticity) is a technical standard for certifying the source and history of media content—like a "nutrition label" for digital content.

#### How It Works

```
┌─────────────────────────────────────────────────────────────┐
│                    C2PA MANIFEST                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │                    ASSERTIONS                          │ │
│  │  • Camera info (make, model)                          │ │
│  │  • Capture timestamp                                   │ │
│  │  • GPS coordinates                                     │ │
│  │  • Thumbnail                                           │ │
│  │  • Hash of content                                     │ │
│  └───────────────────────────────────────────────────────┘ │
│                          │                                  │
│                          ▼                                  │
│  ┌───────────────────────────────────────────────────────┐ │
│  │                      CLAIM                             │ │
│  │  Lists all assertions                                  │ │
│  │  Digitally signed                                      │ │
│  └───────────────────────────────────────────────────────┘ │
│                          │                                  │
│                          ▼                                  │
│  ┌───────────────────────────────────────────────────────┐ │
│  │              EMBEDDED IN MEDIA FILE                   │ │
│  │  (JPEG, PNG, video, etc.)                             │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

#### Current Status (2025)

- Specification v2 published May 2025
- Conformance program launched June 2025
- Becoming ISO standard
- **Adoption still very low** in practice
- Can be bypassed by determined attackers

#### Recommendation for Witness

**Priority: Medium** - Implement C2PA support for interoperability, but don't depend on it. Focus on simpler hash verification for MVP.

**Sources:** [C2PA.org](https://c2pa.org/), [C2PA Specification](https://spec.c2pa.org/), [Library of Congress](https://blogs.loc.gov/thesignal/2025/07/c2pa-glam/), [Content Authenticity Initiative](https://contentauthenticity.org/how-it-works)

---

### IPFS/Filecoin

**Status:** Active, growing adoption in human rights space

#### What They Are

- **IPFS**: Decentralized content storage using content identifiers (CIDs)
- **Filecoin**: Incentive layer for IPFS with proof-of-storage

#### Human Rights Applications

| Project | Use |
|---------|-----|
| Starling Lab | Evidence preservation |
| ProofMode | Hash notarization |
| OpenArchive | Decentralized archival |
| HRDAG | Human rights data backup |

#### Benefits

- Content-addressed (CID = hash of content)
- Redundant storage across providers
- Tamper-evident by design
- No single point of failure
- Cryptographic verification built-in

#### Recommendation for Witness

**Priority: High for hash notarization, Medium for full storage**

Use IPFS/Filecoin for:
- Immediate hash registration (lightweight)
- Backup storage layer (secondary to federation)

**Sources:** [IPFS.tech](https://ipfs.tech/), [Filecoin Docs](https://docs.filecoin.io/basics/what-is-filecoin), [FFDW Blog](https://ffdweb.org/blog/decentralized-technology-for-human-rights-archiving-and-activism/)

---

## Historical/Archived Tools

### CameraV / InformaCam (Guardian Project + WITNESS)

**Status:** Discontinued (Succeeded by ProofMode)

CameraV was the predecessor to ProofMode, capturing extensive sensor metadata alongside media. Key innovation: treating the phone as a "sensor bundle" capturing environmental context.

**Legacy:** Sensor metadata approach carried forward to ProofMode.

### ObscuraCam (Guardian Project + WITNESS)

**Status:** Maintained but legacy

Focuses on **removing** identifying information (face blurring, metadata stripping). Inverse of ProofMode's approach.

**Relevance:** Face anonymization may be useful for Witness when sharing publicly.

**Sources:** [CameraV Guide](http://guardianproject.github.io/informacam-guide/en/InformacamGuide.html), [InformaCam Archive](https://guardianproject.info/archive/informacam/), [ObscuraCam](https://guardianproject.info/apps/org.witness.sscphase1/)

---

## Advanced Deployment Scenarios

### Abandoned Device / Dead Drop Mode

An important use case for Witness involves **leaving a device in place for extended unattended recording** with later retrieval—either physical retrieval or remote data extraction. This "dead drop" scenario has several applications:

#### Use Cases

| Scenario | Description | Technical Requirements |
|----------|-------------|------------------------|
| Workplace monitoring | Device left in break room during expected enforcement action | Long battery, triggered recording, secure storage |
| Community checkpoint | Phone positioned to document recurring ICE checkpoint | Solar power, cellular upload, camouflage |
| Safe house documentation | Record potential raids at known target locations | Motion activation, immediate upload, panic wipe |
| Event documentation | Device left at protest location | Mesh sync with nearby witnesses, batch upload |

#### Technical Architecture for Dead Drop Mode

```
┌─────────────────────────────────────────────────────────────────┐
│                    ABANDONED DEVICE MODE                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  SETUP PHASE:                                                   │
│  ┌──────────────┐                                               │
│  │   Configure  │  • Set activation triggers (motion, sound)   │
│  │   & Deploy   │  • Configure upload schedule                 │
│  │              │  • Set auto-delete policies                  │
│  │              │  • Enable device attestation                 │
│  └──────┬───────┘                                               │
│         │                                                       │
│  MONITORING PHASE:                                              │
│         ▼                                                       │
│  ┌──────────────┐    Trigger     ┌──────────────┐              │
│  │    Idle /    │───────────────▶│   Record +   │              │
│  │   Watching   │    Detected    │   Hash       │              │
│  └──────────────┘                └──────┬───────┘              │
│         ▲                               │                       │
│         │                               ▼                       │
│         │                        ┌──────────────┐              │
│         │   No Connection        │   Queue for  │              │
│         └────────────────────────│    Upload    │              │
│                                  └──────┬───────┘              │
│                                         │                       │
│  UPLOAD PHASE:                          │ Connection Available  │
│                                         ▼                       │
│                                  ┌──────────────┐              │
│                                  │   Upload to  │──▶ Federation│
│                                  │   Nodes      │              │
│                                  └──────┬───────┘              │
│                                         │                       │
│                                         ▼                       │
│                                  ┌──────────────┐              │
│                                  │  Delete Local│              │
│                                  │  (Optional)  │              │
│                                  └──────────────┘              │
│                                                                 │
│  RETRIEVAL OPTIONS:                                             │
│  • Physical: Recover device, export encrypted archive          │
│  • Remote: Device uploads when network available               │
│  • Mesh: Nearby Witness user syncs via Bluetooth               │
│  • Destruction: Device seized → local data already uploaded    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

#### Key Design Considerations

| Consideration | Requirement | Implementation |
|---------------|-------------|----------------|
| **Power management** | Days to weeks of operation | Aggressive sleep, motion/sound activation, solar support |
| **Storage management** | Limited local space | Prioritize hash upload, compress video, delete after upload |
| **Tamper resistance** | Device may be discovered | Full disk encryption, no external indicators of app |
| **Retrieval security** | Extraction without exposing operator | Bluetooth range pickup, timed WiFi enable, physical dead drop |
| **Legal deniability** | Device owner may be questioned | Camouflage as generic phone, decoy content |
| **Activation triggers** | Conserve resources | PIR motion, audio level threshold, scheduled times |
| **Network opportunism** | Intermittent connectivity | Queue all data, upload when any connection available |

#### Prior Art: Off-Grid Surveillance Systems

Commercial systems like **GrizCam** demonstrate that intelligent off-grid surveillance is technically feasible:
- 360° video + directional audio + environmental sensors
- AI-based motion detection (eliminates false positives from wind/shadows)
- Works in areas with no power or cell coverage
- Multi-sensor fusion for meaningful activity detection

**Key insight:** Consumer smartphone sensors (accelerometer, microphone, camera) can implement similar detection logic.

#### Implementation Recommendations for Witness

| Priority | Feature | Rationale |
|----------|---------|-----------|
| P1 | Motion-activated recording | Conserve battery and storage |
| P1 | Encrypted local queue | Protect evidence if device seized before upload |
| P1 | Opportunistic upload | Use any network when available |
| P2 | Audio activation | Start recording on loud noises (shouting, doors) |
| P2 | Scheduled recording windows | Record during expected enforcement times |
| P2 | Bluetooth mesh sync | Allow nearby Witness user to retrieve data |
| P3 | Solar/external power support | Extended deployment duration |
| P3 | Timed self-wipe | Delete evidence after X hours if not retrieved |

---

### Multi-Witness Scenarios

When multiple observers are present (protests, workplace actions, community monitoring), Witness can leverage proximity for enhanced evidence collection and corroboration.

#### Scenario: Protest Documentation

```
┌─────────────────────────────────────────────────────────────────┐
│                  MULTI-WITNESS PROTEST SCENARIO                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│        ┌───────┐           ┌───────┐           ┌───────┐       │
│        │Witness│           │Witness│           │Witness│       │
│        │   A   │◄─────────▶│   B   │◄─────────▶│   C   │       │
│        └───┬───┘  Bluetooth └───┬───┘  Bluetooth└───┬───┘       │
│            │       ~100m        │       ~100m       │           │
│            │                    │                   │           │
│            ▼                    ▼                   ▼           │
│     ┌──────────┐         ┌──────────┐        ┌──────────┐      │
│     │  Record  │         │  Record  │        │  Record  │      │
│     │ + Hash   │         │ + Hash   │        │ + Hash   │      │
│     └────┬─────┘         └────┬─────┘        └────┬─────┘      │
│          │                    │                   │            │
│          └────────────────────┼───────────────────┘            │
│                               │                                 │
│                        BLUETOOTH MESH                           │
│                     Exchange: Hashes + Metadata                 │
│                     (NOT full video - bandwidth)                │
│                               │                                 │
│                               ▼                                 │
│                    ┌───────────────────┐                       │
│                    │   CORROBORATION   │                       │
│                    │   • Timestamp sync│                       │
│                    │   • Location proof│                       │
│                    │   • Multi-angle   │                       │
│                    └───────────────────┘                       │
│                                                                 │
│  WHEN INTERNET AVAILABLE:                                       │
│  Each device uploads to federation independently               │
│  Hashes already shared = automatic corroboration linking       │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

#### What Gets Shared Over Bluetooth

Given Bluetooth bandwidth limitations (~1-2 Mbps practical, high latency), full video sharing is impractical. Instead, share:

| Data Type | Size | Purpose |
|-----------|------|---------|
| Video hash (SHA-256) | 32 bytes | Proves video existed at time |
| Timestamp | 8 bytes | Synchronize clocks |
| GPS coordinates | 16 bytes | Location corroboration |
| Device fingerprint | 64 bytes | Identity (pseudonymous) |
| Incident ID | 32 bytes | Link related recordings |

**Total per exchange:** ~150 bytes — easily exchanged over Bluetooth LE

#### Benefits of Multi-Witness Mode

| Benefit | Description |
|---------|-------------|
| **Timestamp corroboration** | Multiple independent timestamps prove timing |
| **Location verification** | Multiple GPS readings confirm location |
| **Multi-angle evidence** | Different perspectives of same incident |
| **Redundancy** | If one device seized, others have evidence |
| **Network resilience** | Mesh enables data movement without internet |
| **Deterrent effect** | Visible multi-person documentation |

#### Lessons from Hong Kong/Bridgefy

The 4000% growth in Bridgefy downloads during Hong Kong protests proves demand. However, Bridgefy's security failures are instructive:

| Bridgefy Failure | Witness Mitigation |
|------------------|-------------------|
| Users could be tracked | Use ephemeral Bluetooth identities |
| No authentication | Sign all mesh messages |
| No encryption | E2E encrypt all exchanges |
| Network could be crashed | Rate limiting, message validation |

---

## Hardware Considerations

Beyond smartphones, Witness could potentially support or integrate with dedicated hardware for specialized deployment scenarios.

### Standalone Hidden Cameras

#### Market Overview

The hidden camera market offers various form factors with WiFi/cellular upload capability:

| Form Factor | Battery Life | Resolution | Connectivity | Price Range |
|-------------|--------------|------------|--------------|-------------|
| Button camera | 6-8 hours | 1080p | WiFi | $50-150 |
| Glasses camera | 2-4 hours | 720p-1080p | Local/WiFi | $100-300 |
| Pen camera | 2-4 hours | 720p-1080p | Local only | $30-80 |
| Smoke detector | Hardwired | 1080p-4K | WiFi | $100-200 |
| Clock camera | Hardwired | 1080p-4K | WiFi | $50-150 |
| USB charger | Hardwired | 1080p | WiFi | $30-100 |

#### Key Features (2025 Generation)

- AI motion detection
- 4K resolution available
- 5G connectivity emerging
- Cloud upload (proprietary)
- Night vision
- Time/date stamping

#### Challenges for Witness Integration

| Challenge | Description |
|-----------|-------------|
| **Proprietary protocols** | Most use closed cloud services |
| **No verification** | No hash/signing capability |
| **Limited customization** | Firmware is locked |
| **Trust** | Unknown supply chain (often Chinese OEM) |
| **Legal complexity** | Recording laws vary by jurisdiction |

#### Potential Integration Approach

Rather than building hardware, Witness could:

1. **Define an open protocol** for evidence upload
2. **Partner with ethical hardware vendor** (Purism, Pine64)
3. **Provide receiver endpoint** for WiFi cameras to upload to
4. **Add verification layer** on receipt (hash, timestamp, log)

```
┌────────────────┐      ┌────────────────┐      ┌────────────────┐
│  Third-party   │      │   Witness      │      │   Federation   │
│  WiFi Camera   │─────▶│   Bridge/App   │─────▶│     Nodes      │
│  (commodity)   │      │  (adds proofs) │      │                │
└────────────────┘      └────────────────┘      └────────────────┘
```

---

### Body-Worn Cameras

#### Professional Market

Body-worn cameras are well-established in law enforcement with robust evidence management:

| Vendor | Notable Features | Market |
|--------|------------------|--------|
| Axon | Auto-activation triggers, Evidence.com cloud, encrypted | Law enforcement |
| Motorola | Integration with radio systems | Law enforcement |
| Reveal | GDPR-compliant, European focus | Law enforcement/security |
| Wolfcom | Budget-friendly, long battery | Security guards |

#### Consumer/Activist Options

| Product Type | Features | Limitations |
|--------------|----------|-------------|
| WiFi button cameras | 1080p, 6hr battery, smartphone app | Proprietary apps, no verification |
| Clip-on cameras | Night vision, motion detect | Limited connectivity |
| Glasses cameras | Hands-free, natural angle | Conspicuous, short battery |

#### Key Insight

Professional body cameras have solved many technical problems:
- Auto-activation based on triggers (gun drawn, vehicle door opens)
- Tamper-evident storage
- Chain of custody documentation
- Pre-event buffering (captures 30-60 seconds before activation)

**Witness opportunity:** Implement similar features on smartphones:
- Pre-event buffer in Witness mode
- Auto-activation on motion/sound
- Signed video frames

---

### DIY Options (Raspberry Pi)

#### Why Raspberry Pi?

Raspberry Pi offers a **fully customizable, open-source** platform for evidence capture:

| Advantage | Description |
|-----------|-------------|
| Open source | Full control over software stack |
| Cheap | ~$35-75 depending on model |
| Flexible | Add any sensors, cameras, connectivity |
| Auditable | Can verify no backdoors |
| Programmable | Run Witness protocol natively |

#### Existing Projects

| Project | Features |
|---------|----------|
| **MotionEyeOS** | NVR-style motion detection, web interface |
| **RPi Cam Web Interface** | Simple streaming, easy setup |
| **Custom (GitHub: Ruud14/SecurityCamera)** | H.264, pre-motion recording, remote storage, offline support |

#### Academic Research

An IEEE paper proposes encrypted surveillance using Raspberry Pi + PIR sensor:
- Records only when motion detected
- Encrypts data before transmission
- Provides secured data transmission

#### Witness + Raspberry Pi Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                   WITNESS RASPBERRY PI NODE                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐         │
│  │   Camera    │    │  PIR Sensor │    │    Mic      │         │
│  │  Module     │    │  (Motion)   │    │  (Audio)    │         │
│  └──────┬──────┘    └──────┬──────┘    └──────┬──────┘         │
│         │                  │                  │                 │
│         └──────────────────┼──────────────────┘                 │
│                            │                                    │
│                            ▼                                    │
│                   ┌────────────────┐                           │
│                   │  Witness Core  │                           │
│                   │  (Python/Go)   │                           │
│                   │  • Capture     │                           │
│                   │  • Hash/Sign   │                           │
│                   │  • Queue       │                           │
│                   └────────┬───────┘                           │
│                            │                                    │
│              ┌─────────────┼─────────────┐                     │
│              ▼             ▼             ▼                      │
│       ┌──────────┐  ┌──────────┐  ┌──────────┐                │
│       │  WiFi    │  │ Cellular │  │  Local   │                │
│       │  Upload  │  │  Modem   │  │  Storage │                │
│       └──────────┘  └──────────┘  └──────────┘                │
│                                                                 │
│  Power Options:                                                 │
│  • USB (5V, 2.5A minimum)                                      │
│  • Battery pack + solar                                        │
│  • PoE HAT (power over ethernet)                               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

#### Recommended Pi Setup for Witness

| Component | Recommendation | Cost |
|-----------|----------------|------|
| Board | Raspberry Pi 4 (2GB) or Zero 2 W | $35-55 |
| Camera | Pi Camera Module 3 (12MP, HDR) | $25 |
| Storage | 64GB+ microSD (high endurance) | $15 |
| Case | Weatherproof if outdoor | $15-30 |
| Power | USB-C adapter or battery | $10-50 |
| Connectivity | Built-in WiFi or LTE HAT | $0-50 |

**Total: ~$100-200** for a complete Witness capture node

---

### Repurposed Smartphones

#### The Opportunity

Millions of old smartphones sit unused in drawers. These can become dedicated Witness capture devices:

| Advantage | Description |
|-----------|-------------|
| **Free hardware** | Users already own them |
| **Full sensor suite** | Camera, mic, GPS, accelerometer |
| **Battery included** | Self-powered for hours |
| **Connectivity** | WiFi + potentially cellular |
| **Familiar form factor** | Easy to position, camouflage |

#### Existing Apps for Phone-to-Camera Conversion

| App | Downloads | Key Features |
|-----|-----------|--------------|
| **AlfredCamera** | 70M+ | 8-hour continuous loop, motion alerts, free tier |
| **Manything** | Popular | Motion zones, cloud storage |
| **AtHome Camera** | Established | Two-way audio, scheduling |
| **Haven** | Open source | Sensors as intrusion detection (by Guardian Project) |

#### Limitations of Existing Apps

| Limitation | Description |
|------------|-------------|
| Proprietary | Closed source, unverifiable |
| Cloud-dependent | Footage goes to commercial servers |
| No verification | No hashing or signing |
| Privacy concerns | Companies have access to footage |
| Subscription models | Best features require payment |

#### Haven (Guardian Project) — Closest Precedent

Haven, by Guardian Project (creators of ProofMode), uses smartphone sensors for security:
- Motion detection via accelerometer
- Sound detection via microphone
- Light detection via light sensor
- Camera capture on trigger
- Sends alerts via Signal/Tor

**Key insight:** Haven proves smartphones can be effective unattended monitoring devices.

#### Witness "Sentry Mode" Concept

Repurpose old phones as dedicated Witness capture devices:

```
┌─────────────────────────────────────────────────────────────────┐
│                    WITNESS SENTRY MODE                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  SETUP (on old phone):                                          │
│  1. Install Witness app                                         │
│  2. Configure as "Sentry" device                                │
│  3. Set activation triggers                                     │
│  4. Configure upload destination (federation node)              │
│  5. Position device, plug in power                              │
│                                                                 │
│  OPERATION:                                                     │
│  ┌───────────────┐                                              │
│  │ Motion/Sound  │──▶ Threshold exceeded?                       │
│  │   Monitoring  │           │                                  │
│  └───────────────┘           │ Yes                              │
│                              ▼                                  │
│                    ┌───────────────┐                           │
│                    │    Record     │                           │
│                    │  + Hash/Sign  │                           │
│                    └───────┬───────┘                           │
│                            │                                    │
│                            ▼                                    │
│                    ┌───────────────┐                           │
│                    │   Upload to   │──▶ Federation             │
│                    │   Witness     │                           │
│                    └───────────────┘                           │
│                                                                 │
│  ADVANTAGES OVER COMMERCIAL APPS:                               │
│  • Hash verification at capture                                │
│  • Federation (no commercial cloud)                            │
│  • Open source (auditable)                                     │
│  • Community controlled                                         │
│  • No subscription fees                                         │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

#### Implementation Priority

| Priority | Feature | Rationale |
|----------|---------|-----------|
| P2 | Basic sentry mode | Significant value, reuses existing hardware |
| P2 | Motion/sound triggers | Essential for battery/storage efficiency |
| P3 | Pre-event buffer | Capture context before trigger |
| P3 | Multiple sentry coordination | Mesh awareness between devices |

---

## Comparative Analysis

### Feature Matrix

| Feature | Mobile Justice | ProofMode | eyeWitness | Tella | OpenArchive | Briar | Witness (Planned) |
|---------|---------------|-----------|------------|-------|-------------|-------|-------------------|
| **Status** | Discontinued | Active | Active | Active | Active | Active | Planned |
| **Open Source** | No | Yes | No | Yes | Yes | Yes | Yes |
| **Camouflage** | No | No | Limited | Yes | No | No | **Yes** |
| **Covert Recording** | No | No | No | No | No | No | **Yes** |
| **Live Streaming** | No | No | No | No | No | No | **Yes** |
| **Federated** | No | Partial | No | No | No | P2P | **Yes** |
| **Hash Verification** | No | Yes | Yes | No | No | No | **Yes** |
| **Mesh/Bluetooth** | No | No | No | No | No | Yes | **Yes** |
| **Anonymous Accounts** | No | Yes | No | No | No | Yes | **Yes** |
| **Encrypted Storage** | No | Partial | Yes | Yes | Yes | Yes | **Yes** |
| **Legal Pathway** | ACLU | No | ICC | No | No | No | Partnerships |
| **iOS Support** | Was | Yes | Yes | Yes | Yes | No | **Yes** |
| **Offline Capable** | No | Yes | Partial | Yes | No | Yes | **Yes** |

### Safety Feature Comparison

| Safety Feature | Tella | eyeWitness | Witness (Planned) |
|----------------|-------|------------|-------------------|
| App camouflage | Calculator + icons | Icon only | Calculator + icons + decoy |
| Screen-off recording | No | No | **Yes** |
| Panic delete | Yes | Yes | **Yes** |
| Encrypted local storage | Yes | Yes | **Yes** |
| No local storage option | No | No | **Yes** |
| Decoy mode | No | No | **Yes** |
| Volume button activation | No | No | **Yes** |

### Verification Feature Comparison

| Verification Feature | ProofMode | eyeWitness | Starling | Witness (Planned) |
|---------------------|-----------|------------|----------|-------------------|
| SHA-256 hash | Yes | Yes | Yes | **Yes** |
| GPS metadata | Yes | Yes | Yes | **Yes** |
| Sensor metadata | Extensive | Basic | Extensive | **Yes** |
| OpenPGP signing | Yes | No | No | Consider |
| C2PA support | Yes | No | Yes | Post-MVP |
| Blockchain timestamp | IPFS | No | Yes | Consider |
| Chain of custody | Yes | Yes | Yes | **Yes** |

---

## Gap Analysis

### Unmet Needs in Current Market

| Capability | Best Current Solution | Gap | Witness Opportunity |
|------------|----------------------|-----|---------------------|
| ICE documentation | None | **Critical** | First-mover |
| Federated evidence storage | None | **Critical** | First-mover |
| Covert + camouflage combo | None (Tella has camouflage only) | **Significant** | Differentiation |
| Live streaming to distributed storage | None | **Significant** | Innovation |
| Multi-witness Bluetooth sync | Briar (text only) | **Moderate** | Extend to video metadata |
| US police accountability | ACLU discontinued | **Critical** | Fill vacuum |

### User Segment Gaps

| User Segment | Current Options | Gap Assessment |
|--------------|-----------------|----------------|
| ICE witnesses | No purpose-built tool | **Severe gap** |
| Protest documenters | Tella (no covert), Briar (no video) | **Moderate gap** |
| Legal observers | eyeWitness (centralized) | **Moderate gap** |
| Community monitors | ACLU gone, Signal inadequate | **Significant gap** |
| Workplace enforcement | No tools | **Severe gap** |

---

## Recommendations for Witness

### Technical Implementation Priorities

#### 1. Camouflage System (Learn from Tella)

**Source:** Tella open-source implementation

```
IMPLEMENTATION APPROACH:
├── Android: Calculator camouflage (replicate Tella)
├── Android: Multiple icon options
├── iOS: Submit as innocuous app (workaround Apple restrictions)
└── Decoy mode: Fake app opens, secret gesture reveals real app
```

**Key insight from Tella:** Calculator disguise provides both concealment AND plausible deniability. User can claim they were just using calculator.

**Improvement over Tella:** Add decoy content (fake notes, fake weather data) for inspection resistance.

#### 2. Verification System (Learn from ProofMode)

**Source:** ProofMode open-source implementation

```
IMPLEMENTATION APPROACH:
├── Capture: Generate SHA-256 hash immediately
├── Metadata: Replicate ProofMode sensor bundle
│   ├── GPS (lat/long/alt/accuracy)
│   ├── Timestamp (device + network time)
│   ├── Device info
│   ├── Orientation
│   └── Network context (cell towers, WiFi signatures)
├── Signing: Consider OpenPGP or simpler approach
└── Notarization: IPFS hash registration (lightweight)
```

**Key insight from ProofMode:** Self-signed certificates enable verification without centralized authority—critical for anonymity.

#### 3. Federation Protocol (Learn from Matrix)

**Source:** Matrix protocol specification

```
IMPLEMENTATION APPROACH:
├── Model: Event-based replication (like Matrix rooms)
├── Consistency: Eventual consistency (accept trade-off)
├── Security: Server-to-server authentication with public keys
├── Independence: Each node fully functional alone
└── Replication: Evidence automatically copied to multiple nodes
```

**Key insight from Matrix:** Federation is technically proven at scale. Don't reinvent—adapt their patterns.

#### 4. Multi-Witness Mesh (Learn from Briar + Bridgefy)

**Source:** Briar (security), Bridgefy (adoption proof)

```
IMPLEMENTATION APPROACH:
├── Transport: Bluetooth LE for metadata/hashes
│   └── Full video via internet only (bandwidth limit)
├── Discovery: Automatic peer discovery
├── Security: Use proven encryption (not Bridgefy's early mistakes)
├── Use case: Protests, demonstrations, workplace enforcement
└── Limitation: ~100m range, requires density
```

**Key insight from Hong Kong:** Mesh demand is proven (4000% growth). Security is critical (Bridgefy was broken).

**Recommended scope for MVP:** Exchange hashes and metadata over Bluetooth, not full video. Proves corroboration without bandwidth issues.

#### 5. Legal Pathway (Learn from eyeWitness + WITNESS.org)

**Strategy:** Partner, don't build

```
PARTNERSHIP APPROACH:
├── Training: Partner with WITNESS.org for curriculum
├── Legal review: Engage immigration lawyers
├── Node operators: Immigrant rights orgs (ACLU chapters, etc.)
└── Chain of custody: Match eyeWitness documentation standards
```

**Key insight from eyeWitness:** Legal credibility comes from institutional relationships, not just technology.

### Feature Prioritization

| Priority | Feature | Rationale |
|----------|---------|-----------|
| **P0 (MVP)** | Video capture + hash | Core functionality |
| **P0 (MVP)** | Federated upload | Key differentiator |
| **P0 (MVP)** | Witness mode (screen-off) | Safety requirement |
| **P0 (MVP)** | Camouflage | Safety requirement |
| **P0 (MVP)** | Anonymous accounts | Safety requirement |
| **P1** | Live streaming | When conditions allow |
| **P1** | Metadata bundle | Verification layer |
| **P1** | Low battery mode | Resilience |
| **P2** | Multi-witness Bluetooth | Protest scenarios |
| **P2** | C2PA support | Interoperability |
| **P2** | Panic wipe | Safety enhancement |
| **Post-MVP** | Blockchain timestamp | Adds complexity |
| **Post-MVP** | Device attestation | Platform-specific |

---

## Witness Value Proposition

### Why Witness Is Worthwhile Despite Existing Tools

#### 1. No Tool Covers ICE Documentation

The US immigration enforcement documentation space has **no purpose-built tool**:
- ACLU Mobile Justice: Discontinued
- eyeWitness: Focused on war crimes, international courts
- Tella: General human rights, no US focus
- ProofMode: Verification only, no safety features

**Witness fills a critical gap for a vulnerable community.**

#### 2. Federation Is Unproven but Essential

No existing tool offers true federated architecture for evidence:
- eyeWitness: Centralized (LexisNexis)
- Tella: Single server destination
- ProofMode: No storage solution
- ACLU: Was state-fragmented, not federated

**Witness would be the first federated evidence platform.**

#### 3. Covert Recording + Camouflage Is Unique

| Tool | Camouflage | Covert Recording |
|------|------------|------------------|
| Tella | Yes | No |
| eyeWitness | Limited | No |
| Consumer spy apps | No | Yes (unsafe) |
| **Witness** | **Yes** | **Yes** |

**Witness uniquely combines both safety features with ethical framing.**

#### 4. Live Streaming to Federation Is New

No tool streams directly to decentralized storage:
- Signal: Encrypted calls, but centralized, not evidence-focused
- All others: Upload after capture only

**Witness would pioneer real-time federated evidence capture.**

#### 5. Timing Is Right

- ACLU Mobile Justice just discontinued (Feb 2025)
- ICE enforcement increasing
- Community need is acute
- Technical components are proven (just not combined)

### Competitive Positioning

```
                    HIGH SAFETY FOCUS
                           │
                           │
              Witness ◄────┼──── Tella
              (planned)    │
                           │
    LOW ───────────────────┼─────────────────── HIGH
    VERIFICATION           │            VERIFICATION
                           │
              Consumer     │      ProofMode
              spy apps     │      eyeWitness
                           │
                           │
                    LOW SAFETY FOCUS
```

### Risk Assessment

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Technical complexity | High | Incremental development, proven components |
| Adoption challenges | Medium | Partner with trusted orgs for distribution |
| App store rejection | Medium | Alternative distribution (APK, TestFlight) |
| Sustainability | Medium | Open source, org-operated nodes |
| Security vulnerabilities | Medium | Security audit, open source review |
| Legal liability | Low | Clear terms, no warranties |

---

## Brief Mentions: Other Relevant Products

The following products were identified during research but did not warrant full deep dives:

| Product | Category | Relevance | Why Not Full Analysis |
|---------|----------|-----------|----------------------|
| **SecureDrop** | Whistleblower platform | Source protection | Server-focused, not mobile capture |
| **OnionShare** | Anonymous file sharing | Secure transfer | Not evidence-specific |
| **Orbot** | Tor for Android | Anonymity layer | Infrastructure, not app |
| **F-Droid** | FOSS app store | Distribution | Channel, not product |
| **Nextcloud** | Self-hosted cloud | Storage backend | Generic, not evidence-specific |
| **Uwazi** | Human rights database | Documentation backend | Tella integration exists |
| **Ushahidi** | Crisis mapping | Crowdsourced reports | Different focus (mapping) |
| **Martus** | Human rights documentation | Secure records | Desktop-focused, older |
| **KoBoToolbox** | Survey/data collection | Field data | Forms-focused, not video |
| **goTenna Mesh** | Hardware mesh networking | Offline comms | Hardware, not software |
| **Meshtastic** | Long-range mesh | Offline comms | LoRa hardware required |
| **Rumble/Odysee** | Alt video platforms | Video hosting | Not evidence-focused |
| **Periscope (X)** | Live streaming | Real-time broadcast | Corporate, not secure |
| **Bambuser** | Live streaming for media | Professional streaming | Enterprise-focused |
| **Citizen** | Safety/incident alerts | Community awareness | US-only, different model |

---

## Sources

### Primary Sources

- [ACLU Mobile Justice - Wikipedia](https://en.wikipedia.org/wiki/ACLU_Mobile_Justice)
- [ACLU Minnesota - Mobile Justice](https://www.aclu-mn.org/en/mobile-justice-app)
- [ProofMode - Guardian Project](https://guardianproject.info/apps/org.witness.proofmode/)
- [ProofMode.org](https://proofmode.org/about)
- [ProofMode C2PA Support](https://proofmode.org/c2pa)
- [eyeWitness to Atrocities](https://www.eyewitness.global/)
- [LexisNexis Rule of Law Foundation](https://www.lexisnexisrolfoundation.org/projects/eyewitness.aspx)
- [IBA eyeWitness Press Releases](https://www.ibanet.org/)
- [Tella App](https://tella-app.org/)
- [Tella Camouflage Documentation](https://docs.tella-app.org/features/camouflage)
- [Horizontal GitHub - Tella Android](https://github.com/Horizontal-org/Tella-Android)
- [OpenArchive](https://www.open-archive.org/)
- [Save by OpenArchive](https://www.open-archive.org/save)
- [Starling Lab](https://www.starlinglab.org/)
- [USC Shoah Foundation - Starling](https://sfi.usc.edu/news/2022/06/33571-starling-lab-and-hala-systems-file-cryptographic-submission-evidence-war-crimes)
- [Matrix Specification](https://spec.matrix.org/latest/)
- [Matrix Wikipedia](https://en.wikipedia.org/wiki/Matrix_(protocol))
- [Element.io](https://element.io/en)
- [Briar Project](https://briarproject.org/)
- [Briar Wikipedia](https://en.wikipedia.org/wiki/Briar_(software))
- [Bridgefy](https://bridgefy.me/)
- [FireChat Wikipedia](https://en.wikipedia.org/wiki/FireChat)
- [C2PA.org](https://c2pa.org/)
- [C2PA Specification](https://spec.c2pa.org/)
- [IPFS](https://ipfs.tech/)
- [Filecoin Documentation](https://docs.filecoin.io/)
- [WITNESS.org](https://www.witness.org/)
- [WITNESS Library](https://library.witness.org/)
- [Video as Evidence Field Guide](https://vae.witness.org/video-as-evidence-field-guide/)
- [Guardian Project - InformaCam Archive](https://guardianproject.info/archive/informacam/)
- [Guardian Project - CameraV Archive](https://guardianproject.info/archive/camerav/)
- [Guardian Project - ObscuraCam](https://guardianproject.info/apps/org.witness.sscphase1/)
- [Filecoin Foundation - FFDW](https://ffdweb.org/)
- [Open Tech Fund](https://www.opentech.fund/)
- [World Justice Project - Tella](https://worldjusticeproject.org/world-justice-challenge-2021/tella)

### Research Papers

- [Mesh Messaging in Large-Scale Protests: Breaking Bridgefy](https://eprint.iacr.org/2021/214.pdf)
- [Amigo: Secure Group Mesh Messaging in Realistic Protest Settings](https://eprint.iacr.org/2024/1872.pdf)

### News Sources

- [Fast Company - Starling Lab](https://www.fastcompany.com/90731729/inside-starling-lab-a-moonshot-project-to-preserve-the-worlds-most-important-information)
- [TechCrunch - Bridgefy E2E Encryption](https://techcrunch.com/2020/11/02/bridgefy-launches-end-to-end-encrypted-messaging-for-the-app-used-during-protests-and-disasters/)
- [NBC News - eyeWitness Launch](https://www.nbcnews.com/tech/tech-news/eyewitness-app-aims-expose-war-criminals-n411801)
- [South China Morning Post - Hong Kong Mesh Apps](https://www.scmp.com/abacus/culture/article/3026527/we-tested-messaging-app-used-hong-kong-protesters-works-without)

---

*Document generated January 2026. Information current as of research date.*
