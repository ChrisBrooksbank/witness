# Verification & Credibility

## Overview

Evidence verification establishes that footage is authentic and unaltered. This is critical for legal admissibility and public credibility. Witness uses cryptographic hashing and metadata bundles to create a verifiable chain of custody.

## Requirements

### MVP Verification

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| VER-001 | SHA-256 hash generated at moment of capture | Must | Yes |
| VER-002 | Hash generated for each chunk independently | Must | Yes |
| VER-003 | Final hash of complete evidence (Merkle root) | Must | Yes |
| VER-004 | Metadata bundle packaged with hash | Must | Yes |
| VER-005 | Hash uploaded immediately, before full footage | Must | Yes |
| VER-006 | Timestamp from device clock | Must | Yes |
| VER-007 | Timestamp from network time if available | Should | Yes |
| VER-008 | GPS coordinates at capture | Must | Yes |
| VER-009 | Device info (manufacturer, model, OS) | Must | Yes |

### Post-MVP Verification

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| VER-010 | Blockchain timestamping (proof of existence) | Could | No |
| VER-011 | Device attestation (cryptographic proof from device) | Could | No |
| VER-012 | Multi-witness corroboration scoring | Could | No |
| VER-013 | Tamper detection on playback | Could | No |
| VER-014 | C2PA content authenticity support | Could | No |
| VER-015 | IPFS/Filecoin hash notarization | Should | No |

### Metadata Bundle

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| VER-020 | Bundle includes evidence hash | Must | Yes |
| VER-021 | Bundle includes capture timestamp | Must | Yes |
| VER-022 | Bundle includes GPS coordinates | Must | Yes |
| VER-023 | Bundle includes device fingerprint | Must | Yes |
| VER-024 | Bundle includes app version | Must | Yes |
| VER-025 | Bundle is signed with device key | Should | Yes |
| VER-026 | Bundle transmitted with evidence | Must | Yes |

## Acceptance Criteria

- [ ] AC-VER-1: SHA-256 hash generated within 1 second of capture start
- [ ] AC-VER-2: Hash is uploaded to node before full video upload completes
- [ ] AC-VER-3: Metadata bundle includes timestamp accurate to 1 second
- [ ] AC-VER-4: Metadata bundle includes GPS accurate to 10 meters (when available)
- [ ] AC-VER-5: Each video chunk has its own hash
- [ ] AC-VER-6: Complete evidence has Merkle root hash of all chunks
- [ ] AC-VER-7: Hash verification tool confirms unchanged evidence
- [ ] AC-VER-8: Metadata bundle is cryptographically signed

## Technical Notes

### Hash Generation

```kotlin
class EvidenceHasher {

    fun hashChunk(chunk: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(chunk).toHexString()
    }

    fun merkleRoot(chunkHashes: List<String>): String {
        if (chunkHashes.isEmpty()) return ""
        if (chunkHashes.size == 1) return chunkHashes[0]

        val pairs = chunkHashes.chunked(2).map { pair ->
            if (pair.size == 2) {
                hashPair(pair[0], pair[1])
            } else {
                pair[0]
            }
        }
        return merkleRoot(pairs)
    }

    private fun hashPair(a: String, b: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(a.hexToByteArray())
        digest.update(b.hexToByteArray())
        return digest.digest().toHexString()
    }
}
```

### Metadata Bundle

```kotlin
data class EvidenceMetadata(
    val evidenceId: String,
    val hash: String,
    val chunkHashes: List<String>,
    val timestamp: Instant,
    val networkTimestamp: Instant?,
    val location: Location?,
    val device: DeviceInfo,
    val appVersion: String,
    val signature: String? // Signed with device key
)

data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val fingerprint: String
)

data class Location(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val accuracy: Float,
    val provider: String
)
```

### Signing with Device Key

```kotlin
class MetadataSigner(
    private val keyStore: KeyStore
) {
    private val keyAlias = "witness_signing_key"

    fun sign(metadata: EvidenceMetadata): String {
        val privateKey = keyStore.getKey(keyAlias, null) as PrivateKey
        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(privateKey)
        signature.update(metadata.toCanonicalJson().toByteArray())
        return signature.sign().toBase64()
    }

    fun verify(metadata: EvidenceMetadata, signatureBase64: String): Boolean {
        val publicKey = keyStore.getCertificate(keyAlias).publicKey
        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initVerify(publicKey)
        signature.update(metadata.toCanonicalJson().toByteArray())
        return signature.verify(signatureBase64.base64ToByteArray())
    }
}
```

### Hash Upload Timing

Hash MUST be uploaded before full video to establish timestamp:

```
Timeline:
[0s]     Recording starts
[0.1s]   First chunk hash generated
[0.5s]   Hash + metadata uploaded to node
[30s]    First chunk complete, uploaded
[60s]    Recording ends
[61s]    Final chunk uploaded
[62s]    Merkle root calculated and uploaded
```

### Verification API

```
GET /api/v1/evidence/{evidenceId}/verify
Response:
{
    "evidenceId": "abc123",
    "hash": "sha256:...",
    "hashReceivedAt": "2026-01-26T10:30:00Z",
    "videoReceivedAt": "2026-01-26T10:32:00Z",
    "verified": true,
    "metadata": { ... }
}
```

## Open Questions

| Question | Status |
|----------|--------|
| Include accelerometer data in metadata? | Nice to have, defer |
| Blockchain integration for MVP? | No - post-MVP |
| C2PA support timeline? | Post-MVP |
| Network time source? | NTP or HTTPS time header |
