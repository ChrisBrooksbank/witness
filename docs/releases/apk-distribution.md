# APK Distribution

Witness can publish signed APKs through GitHub Releases for early testers and group operators who are not using an app store yet.

## Policy

- GitHub Releases APKs are pre-alpha testing artifacts until the project has a stable viewer/export and distribution model.
- Public APKs must be signed with the Witness release key, never the Android debug key.
- Each APK must publish a SHA-256 checksum next to the APK.
- Release notes must say which backend URL is baked into the APK.
- Group production APKs should be built per group with that group's HTTPS backend URL.
- Do not publish a universal production APK unless it intentionally points to a public production backend.
- Do not claim the backend can play or decrypt uploaded videos.

## GitHub Secrets

The `Android APK Release` workflow requires these repository secrets:

| Secret | Purpose |
| --- | --- |
| `WITNESS_RELEASE_KEYSTORE_BASE64` | Base64-encoded Android release keystore. |
| `WITNESS_RELEASE_STORE_PASSWORD` | Keystore password. |
| `WITNESS_RELEASE_KEY_ALIAS` | Release key alias inside the keystore. |
| `WITNESS_RELEASE_KEY_PASSWORD` | Release key password. |
| `WITNESS_RELEASE_BACKEND_URL` | HTTPS backend URL for tag-triggered releases. Must end with `/`. |

With GitHub CLI, set them with:

```bash
gh secret set WITNESS_RELEASE_KEYSTORE_BASE64
gh secret set WITNESS_RELEASE_STORE_PASSWORD
gh secret set WITNESS_RELEASE_KEY_ALIAS
gh secret set WITNESS_RELEASE_KEY_PASSWORD
gh secret set WITNESS_RELEASE_BACKEND_URL
```

Create the base64 keystore secret from a local release keystore:

```bash
base64 -w 0 witness-release.jks
```

On macOS:

```bash
base64 -i witness-release.jks
```

Never commit the keystore or passwords.

Create a new release keystore with the JDK `keytool` command:

```bash
keytool -genkeypair \
  -v \
  -keystore witness-release.jks \
  -alias witness-release \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000
```

Store the keystore offline after adding it to GitHub Secrets. The release workflow publishes signing certificate fingerprints with each APK so users can compare future releases against the same signing identity.

## First Pre-Alpha Release

Use the first release tag:

```bash
git tag -a v0.1.0-pre-alpha.1 -m "Witness v0.1.0-pre-alpha.1"
git push origin v0.1.0-pre-alpha.1
```

The tag workflow uses `WITNESS_RELEASE_BACKEND_URL` from repository secrets. For a one-off group build, run the workflow manually and provide:

- `tag`: `v0.1.0-pre-alpha.1`
- `backend_url`: the group's HTTPS backend URL, for example `https://witness.example.org/`

## Local Release Build

Local unsigned release builds are useful for compile checks:

```bash
cd android
./gradlew assembleRelease -PwitnessNodeBaseUrl=https://witness.example.org/
```

Local signed release builds require the same signing environment variables used by CI:

```bash
export WITNESS_NODE_BASE_URL=https://witness.example.org/
export WITNESS_RELEASE_STORE_FILE=/path/to/witness-release.jks
export WITNESS_RELEASE_STORE_PASSWORD=...
export WITNESS_RELEASE_KEY_ALIAS=...
export WITNESS_RELEASE_KEY_PASSWORD=...
./gradlew validateWitnessReleaseSigning assembleRelease
```

## User-Facing Install Notes

Tell users:

- Install only APKs attached to the official GitHub Release.
- Check the SHA-256 checksum before installing when possible.
- Android may require enabling installation from the browser or file manager.
- GitHub-installed APKs do not provide app-store-style update management.
- Pre-alpha builds are for testing and trusted group pilots, not broad public safety reliance.
