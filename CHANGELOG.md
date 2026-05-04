# Changelog

## v0.1.0-pre-alpha.1

First pre-alpha APK candidate for trusted testing.

### Added

- Android calculator camouflage launcher and Witness recording flow.
- Encrypted local evidence cache and upload queue.
- Configurable backend URL for release/group builds.
- Durable single-node backend with SQLite metadata and filesystem chunk storage.
- Docker Compose and Caddy deployment path for group backend operators.
- GitHub Releases workflow for signed pre-alpha APK publishing.

### Known Limits

- APKs must be built for a specific HTTPS backend URL.
- The backend can confirm receipt and verify encrypted chunks, but it cannot play uploaded videos.
- Evidence ID discovery in the app UI is still early MVP work.
- Real-device HTTPS upload validation depends on a live group backend deployment.
