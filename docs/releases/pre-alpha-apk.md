# Witness Pre-Alpha APK

This APK is an early testing build.

## What Works

- Calculator camouflage launcher.
- Witness recording flow.
- Encrypted local evidence cache.
- Upload queue to the backend URL baked into this build.
- Backend receipt and verification through `/api/v1/evidence/{evidenceId}/verify`.

## Important Limits

- This is not a production safety guarantee.
- Each group build must point to the group's HTTPS backend URL.
- The backend stores encrypted evidence chunks and verification metadata.
- The backend does not currently provide a video playback interface.
- GitHub APK installs do not provide app-store-style update notifications.

## Verification

Download both the APK and `.sha256` file from this release. Verify the checksum before installing when possible:

```bash
sha256sum -c witness-v0.1.0-pre-alpha.1.apk.sha256
```

The checksum filename will match the actual release tag.

The release also includes a `signing-certificate.txt` file with the APK signing certificate fingerprints. Future APKs should use the same signing identity unless a key rotation is explicitly announced.
