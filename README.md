# Witness

An open-source, federated video evidence tool for citizen journalists documenting potential misconduct by authorities.

## Mission

Protect people who document injustice. Safety first, evidence second.

## Core Principles

- **Safety first** — The journalist's safety always takes priority over evidence capture
- **Open source** — Full transparency, community trust, not for profit
- **Decentralized** — Federated architecture with no single point of failure
- **Accessible** — Simple enough for anyone to use

## Key Features (MVP)

- Video, audio, and photo capture with GPS metadata
- **Witness mode** — Covert recording with screen off
- **Camouflaged app** — Disguised as innocuous application
- **Federated upload** — Footage distributed across trusted nodes
- **Live streaming** — When connectivity allows
- **Offline resilient** — Queue and upload when connection restored
- **Anonymous accounts** — No identity required
- **Verification** — SHA-256 hash at capture for tamper-proof evidence
- **Low battery mode** — Adaptive quality for extended use

## Threat Model

Designed to protect against:
- Device seizure and destruction
- Footage deletion
- Server takedowns
- Network blocking
- Journalist identification and retaliation

## Status

Early development — requirements gathering complete.

See [specs/readme.md](specs/readme.md) for the full specification index.

## Development

This project uses the **Ralph Wiggum Loop** methodology for AI-assisted development.

```bash
# Generate implementation plan from specs
./loop.sh plan

# Run building mode (implements one task per iteration)
./loop.sh
```

See [specs/ralph.md](specs/ralph.md) for methodology details.

## Contributing

This project is in early stages. Contributions, feedback, and ideas welcome.

## License

TBD — Will be open source (likely GPL or similar copyleft license).

---

*Witness is a public good, not for profit.*
