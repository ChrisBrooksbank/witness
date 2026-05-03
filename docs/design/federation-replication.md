# Post-MVP Federation Replication Protocol

Generated: 2026-05-03

## Scope

MVP remains a single self-hosted node. Federation starts after the single-node upload path is stable and should replicate verified evidence metadata and encrypted chunks across trusted nodes without exposing user identity.

## Goals

- Replicate confirmed evidence to multiple operator-controlled nodes.
- Keep node identities explicit and revocable.
- Preserve the Witness hash/signature chain across replication.
- Avoid central discovery in the first federated release.

## Node Identity

Each node owns a long-lived Ed25519 signing key and publishes:

- `nodeId`: hash of public key.
- `baseUrl`: HTTPS endpoint.
- `publicKey`: signing public key.
- `operatorLabel`: human-readable operator name.
- `trustBundleVersion`: monotonically increasing trust bundle version.

Users or operators configure a signed trust bundle out of band. Nodes reject replication peers that are absent, expired, or revoked in the active trust bundle.

## Replication Flow

1. Source node verifies local upload: registered hash exists, chunk hashes match uploaded bytes, and metadata signature is present.
2. Source node creates a replication manifest containing evidence ID, Merkle root, chunk list, source node ID, timestamps, and retention policy.
3. Source node signs the manifest with its node key.
4. Destination node validates the source node against its trust bundle.
5. Destination node pulls missing chunks by hash, verifies each chunk, stores the replication manifest, and returns a signed receipt.
6. Source node records receipts for audit and eventual user-visible redundancy status.

## API Sketch

```http
POST /api/v1/replication/manifests
Content-Type: application/json
X-Node-Id: {sourceNodeId}
X-Node-Signature: {signature}
```

```json
{
  "evidenceId": "evidence-123",
  "merkleRoot": "sha256:...",
  "chunks": [
    {
      "chunkIndex": 0,
      "sha256": "sha256:...",
      "sizeBytes": 5242880
    }
  ],
  "sourceNodeId": "node-abc",
  "replicatedAt": "2026-05-03T12:00:00Z",
  "deleteAfter": "2026-05-04T12:00:00Z"
}
```

```http
GET /api/v1/replication/evidence/{evidenceId}/chunks/{chunkIndex}
X-Node-Id: {destinationNodeId}
X-Node-Signature: {signature}
```

## Revocation and Conflict Handling

- Trust bundle revocation blocks new replication from revoked nodes immediately.
- Already replicated evidence remains stored unless a local operator retention rule says otherwise.
- If two nodes present conflicting metadata for the same evidence ID, the Merkle root wins as the content identity. Different Merkle roots under the same evidence ID are stored as a conflict set and require operator review.
- Signed receipts are append-only audit records and are not deleted when a peer is later revoked.

## Privacy Notes

- Replication never requires user names, emails, phone numbers, or account identifiers.
- Nodes replicate evidence IDs, hashes, encrypted chunks, capture metadata, and signatures only.
- Federation discovery should be explicit trust-bundle configuration for the first release; public peer discovery can wait until abuse controls exist.
