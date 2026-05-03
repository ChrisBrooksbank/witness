# C2PA Compatibility Path

Generated: 2026-05-03

## Summary

C2PA should be a post-MVP export and interoperability layer, not the primary MVP evidence integrity mechanism. Witness should keep the current capture-time SHA-256 chunk hashes, Merkle root, metadata bundle, and Android Keystore signatures as the source of truth, then generate C2PA Content Credentials when packaging evidence for external sharing.

## Current Standard Fit

The C2PA 2.2 specification defines Content Credentials as manifests containing assertions, a signed claim, and content bindings. Hard bindings are cryptographic hashes over the asset or portions of the asset, which maps well to Witness's existing chunk hash and Merkle-root model. The standard also supports embedded manifests and external manifests discoverable by reference or HTTP `Link` headers, which is useful for large video evidence where metadata may need to remain server-side.

For BMFF-style audiovisual assets such as MP4, the C2PA spec describes a `uuid` box approach for timed media compatibility. That matters for Witness because the MVP video target is H.264 MP4, and adding provenance must not break playback.

## Tooling

Official tooling currently centers on the Rust reference implementation, with Node.js, Python, browser, and Java bindings listed by the C2PA/Content Authenticity Initiative tools page. There is not yet a clear first-class Android/Kotlin SDK path in the official list, so the lowest-risk implementation path is server-side C2PA generation using Rust, Node.js, Python, or Java tooling after upload confirmation.

## MVP Comparison

| Option | Pros | Cons | Recommendation |
| --- | --- | --- | --- |
| Keep Witness hash/signature chain only | Small APK, works offline, easy to validate during capture, no certificate trust-list dependency | Not recognized by generic Content Credentials tools | MVP default |
| Generate C2PA on Android at capture time | Strong portability if official Android support matures | Adds binary/tooling complexity, key/certificate management, MP4 box mutation risk during stressful capture | Defer |
| Generate C2PA server-side after upload | Keeps Android small, can use mature official tooling, avoids on-device MP4 mutation | C2PA signature proves server packaging, while Witness signature remains capture proof | Best post-MVP path |

## Recommended Path

1. Preserve Witness's current capture-time evidence model as canonical.
2. Store C2PA-ready metadata fields in backend records: capture assertions, device signer public key, chunk hashes, Merkle root, upload confirmation time, and redaction state.
3. Add a server-side `POST /api/v1/evidence/{id}/content-credential` job that emits a C2PA manifest or C2PA-packaged MP4 after all chunks are verified.
4. Prefer external manifests or HTTP `Link: rel="c2pa-manifest"` for large evidence bundles until MP4 embedding is tested thoroughly.
5. Treat C2PA trust as additive: a missing or stripped C2PA manifest must not invalidate the Witness hash/signature chain.

## References

- Official C2PA 2.2 technical specification: https://spec.c2pa.org/specifications/specifications/2.2/specs/C2PA_Specification.html
- Official tools and SDKs: https://c2pa.wiki/tools/official/
