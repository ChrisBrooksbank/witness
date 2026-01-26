# Security

## Overview

Security is foundational to Witness. All evidence must be encrypted, keys must be protected, and the system must defend against a sophisticated adversary who may have physical access to devices or network infrastructure.

## Requirements

### Encryption

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| SEC-001 | All footage encrypted before leaving device | Must | Yes |
| SEC-002 | TLS 1.3 for all network communication | Must | Yes |
| SEC-003 | At-rest encryption on federated nodes | Must | Yes |
| SEC-004 | End-to-end encryption (only recorder + authorized can decrypt) | Must | Yes |
| SEC-005 | Certificate pinning for API connections | Should | Yes |
| SEC-006 | Perfect forward secrecy for streaming | Should | Yes |

### Key Management

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| SEC-010 | Keys generated on device | Must | Yes |
| SEC-011 | Keys stored in Android Keystore | Must | Yes |
| SEC-012 | Keys never transmitted in plaintext | Must | Yes |
| SEC-013 | Option to backup keys (encrypted) | Should | No |
| SEC-014 | Key rotation support | Should | No |
| SEC-015 | Account recovery without key (not possible by design) | Must | Yes |

### Threat Mitigations

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| SEC-020 | Man-in-the-middle: Certificate pinning, TLS | Must | Yes |
| SEC-021 | Node compromise: E2E encryption | Must | Yes |
| SEC-022 | Device forensics: No local storage option | Must | Yes |
| SEC-023 | Device forensics: Encrypted cache | Must | Yes |
| SEC-024 | Traffic analysis: Consider padding | Could | No |
| SEC-025 | Malicious node: Data integrity via hashes | Must | Yes |

### Authentication

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| SEC-030 | Anonymous account creation | Must | Yes |
| SEC-031 | No password - key-based auth | Must | Yes |
| SEC-032 | Short-lived access tokens | Must | Yes |
| SEC-033 | Token refresh mechanism | Must | Yes |
| SEC-034 | No session stored on server | Should | Yes |

## Acceptance Criteria

- [ ] AC-SEC-1: Evidence cannot be decrypted without user's key
- [ ] AC-SEC-2: All API traffic uses TLS 1.3
- [ ] AC-SEC-3: Certificate pinning prevents MITM in testing
- [ ] AC-SEC-4: Local cache is encrypted and not readable by file browser
- [ ] AC-SEC-5: Node operator cannot decrypt evidence
- [ ] AC-SEC-6: Account works without email/phone/password
- [ ] AC-SEC-7: Keys are stored in Android Keystore
- [ ] AC-SEC-8: Loss of device means loss of access (no recovery by design)

## Technical Notes

### Encryption Stack

```
┌─────────────────────────────────────────────────────┐
│                   APPLICATION                        │
├─────────────────────────────────────────────────────┤
│  Tink (Google's crypto library)                     │
│  - AES-256-GCM for evidence encryption              │
│  - ECDSA P-256 for signing                          │
│  - Hybrid encryption for key sharing                │
├─────────────────────────────────────────────────────┤
│  Android Keystore                                    │
│  - Hardware-backed key storage (when available)     │
│  - Key generation inside secure element             │
│  - Keys cannot be extracted                         │
├─────────────────────────────────────────────────────┤
│  TLS 1.3                                            │
│  - All network communication                        │
│  - Certificate pinning                              │
└─────────────────────────────────────────────────────┘
```

### Evidence Encryption

```kotlin
class EvidenceEncryptor(
    private val keysetHandle: KeysetHandle
) {
    private val aead = keysetHandle.getPrimitive(Aead::class.java)

    fun encrypt(plaintext: ByteArray, metadata: ByteArray): ByteArray {
        // metadata is associated data (authenticated but not encrypted)
        return aead.encrypt(plaintext, metadata)
    }

    fun decrypt(ciphertext: ByteArray, metadata: ByteArray): ByteArray {
        return aead.decrypt(ciphertext, metadata)
    }
}
```

### Key Generation

```kotlin
class KeyManager(private val context: Context) {

    fun generateIdentityKey(): KeysetHandle {
        // Generate in Android Keystore
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            "witness_identity",
            PURPOSE_SIGN or PURPOSE_VERIFY
        )
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            .build()

        // Use Tink with Android Keystore
        return AndroidKeysetManager.Builder()
            .withSharedPref(context, "witness_keyset", "witness_prefs")
            .withKeyTemplate(EcdsaSignKeyManager.ecdsaP256Template())
            .withMasterKeyUri("android-keystore://witness_master")
            .build()
            .keysetHandle
    }
}
```

### Authentication Flow

```
┌────────┐                              ┌────────┐
│ Client │                              │  Node  │
└────┬───┘                              └────┬───┘
     │                                       │
     │  1. Generate keypair locally          │
     │  ──────────────────────────►          │
     │                                       │
     │  2. Register public key               │
     │  ──────────────────────────►          │
     │                                       │
     │  3. Challenge (nonce)                 │
     │  ◄──────────────────────────          │
     │                                       │
     │  4. Sign challenge                    │
     │  ──────────────────────────►          │
     │                                       │
     │  5. Access token (short-lived)        │
     │  ◄──────────────────────────          │
     │                                       │
```

### Certificate Pinning

```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("node1.witness.org", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
    .add("node2.witness.org", "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=")
    .build()

val client = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

### Threat Model

| Threat | Mitigation |
|--------|------------|
| Device seizure | No local storage, encrypted cache, camouflage |
| Device forensics | Encryption, no plaintext evidence, wipe on panic |
| Network MITM | TLS 1.3, certificate pinning |
| Node compromise | E2E encryption, node cannot decrypt |
| Traffic analysis | Consider traffic padding (post-MVP) |
| Malicious app | Android sandboxing, Keystore protection |
| Account compromise | Key-based auth, no password to steal |
| Server-side breach | E2E encryption, minimal metadata |

## Open Questions

| Question | Status |
|----------|--------|
| Key backup for account recovery? | By design, no recovery possible |
| Hardware attestation for Keystore? | Nice to have, not all devices |
| Traffic padding implementation? | Post-MVP |
| Audit log of access? | Yes, on node side |
