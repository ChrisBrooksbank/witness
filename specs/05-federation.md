# Federation Architecture

## Overview

Federation is Witness's key architectural differentiator. Multiple independent nodes, operated by trusted organizations, store and replicate evidence. No single entity controls the network, making it resistant to takedowns and attacks.

## Requirements

### Federated Model

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| FED-001 | Trusted organizations run independent nodes | Must | Yes |
| FED-002 | No central authority controls the network | Must | Yes |
| FED-003 | Nodes can operate independently if isolated | Must | Yes |
| FED-004 | Evidence replicates across multiple nodes | Must | Yes |
| FED-005 | Client can upload to any available node | Must | Yes |
| FED-006 | Nodes sync evidence between themselves | Must | Yes |

### Node Characteristics

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| FED-010 | Each node fully functional without others | Must | Yes |
| FED-011 | Evidence automatically copied to peer nodes | Must | Yes |
| FED-012 | Evidence encrypted - operators cannot view | Must | Yes |
| FED-013 | Node software fully open source | Must | Yes |
| FED-014 | Node health monitoring and status | Should | Yes |
| FED-015 | Node can refuse replication (storage limits) | Should | Yes |

### Access Control

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| FED-020 | Recorder controls who can view footage | Must | Yes |
| FED-021 | Grant access to specific accounts | Must | Yes |
| FED-022 | Grant access to organizations | Should | Yes |
| FED-023 | Make evidence public | Must | Yes |
| FED-024 | Revoke access | Must | Yes |
| FED-025 | Pre-authorized access (dead man's switch) | Could | No |

### Node Discovery

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| FED-030 | App ships with default node list | Must | Yes |
| FED-031 | Nodes can be added manually | Should | Yes |
| FED-032 | Nodes can announce themselves to peers | Should | No |
| FED-033 | Node list can be updated from trusted source | Should | Yes |

## Acceptance Criteria

- [ ] AC-FED-1: Evidence uploaded to Node A appears on Node B within 5 minutes
- [ ] AC-FED-2: Client can upload when only one node is reachable
- [ ] AC-FED-3: Evidence remains accessible if one node goes offline
- [ ] AC-FED-4: Node operator cannot view encrypted evidence
- [ ] AC-FED-5: User can grant access to another account and they can view
- [ ] AC-FED-6: User can revoke access and viewer can no longer see
- [ ] AC-FED-7: Public evidence is viewable without authentication

## Technical Notes

### Federation Protocol

Nodes communicate via authenticated HTTPS:

```
POST /federation/v1/evidence
Authorization: Bearer {node-to-node-token}
{
    "evidenceId": "abc123",
    "originNode": "node-a.aclu.org",
    "hash": "sha256:...",
    "encryptedKey": "...",  // Key encrypted for each node's public key
    "metadata": { ... }
}
```

### Replication Strategy

```
                 ┌──────────┐
    Upload ──────│  Node A  │
                 │ (Origin) │
                 └────┬─────┘
                      │
           ┌──────────┴──────────┐
           │                     │
      ┌────▼─────┐         ┌────▼─────┐
      │  Node B  │         │  Node C  │
      │ (Replica)│         │ (Replica)│
      └──────────┘         └──────────┘
```

1. Client uploads to Node A (origin)
2. Node A stores evidence
3. Node A notifies peers (B, C) of new evidence
4. Peers request replication
5. Evidence + encrypted key transferred
6. Acknowledgment sent to Node A

### Encryption Model

Evidence is encrypted with a unique key. That key is then encrypted for each authorized party:

```
Evidence ──► AES-256-GCM ──► Encrypted Evidence
                │
                ▼
        Evidence Key
                │
    ┌───────────┼───────────┐
    ▼           ▼           ▼
RSA(owner)  RSA(node_a)  RSA(node_b)
```

### Access Control

```kotlin
data class AccessGrant(
    val evidenceId: String,
    val grantedTo: String,  // Account ID or organization
    val grantedBy: String,  // Owner account ID
    val encryptedKey: String,  // Evidence key encrypted for grantee
    val expiresAt: Instant?,
    val revoked: Boolean
)
```

### Node API

```
# Upload evidence
POST /api/v1/evidence
Authorization: Bearer {user-token}

# Get evidence (if authorized)
GET /api/v1/evidence/{id}
Authorization: Bearer {user-token}

# Grant access
POST /api/v1/evidence/{id}/access
{
    "grantTo": "account-id-or-org",
    "expiresAt": "2026-12-31T23:59:59Z"
}

# Revoke access
DELETE /api/v1/evidence/{id}/access/{grantId}

# Make public
POST /api/v1/evidence/{id}/publish

# Node health
GET /health
```

### Potential Node Operators

- ACLU chapters
- Immigrant rights organizations (RAICES, etc.)
- Legal aid societies
- Press freedom organizations (EFF, Freedom of the Press)
- Universities
- Faith-based organizations
- Individual activists (self-hosted)

### Node Requirements

| Requirement | Minimum | Recommended |
|-------------|---------|-------------|
| Storage | 100GB | 1TB+ |
| Bandwidth | 10 Mbps | 100 Mbps |
| Availability | 95% | 99.9% |
| HTTPS | Required | Required |
| Location | Varies | US + International |

## Open Questions

| Question | Status |
|----------|--------|
| Minimum nodes for MVP launch? | 2-3 operated by trusted partners |
| Node-to-node authentication? | mTLS with pre-shared certs |
| Conflict resolution for access revocation? | Last-write-wins for MVP |
| Storage quotas per user? | Need to determine |
| How to handle node permanent shutdown? | Migration protocol needed |
