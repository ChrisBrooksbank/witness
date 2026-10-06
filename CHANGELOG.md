# Changelog

## Unreleased

### Fixed

- Backend: registered evidence hashes and stored chunks can no longer be overwritten; conflicting re-registration returns `409`.
- Backend: concurrent chunk uploads no longer fail with SQLite "database is locked".
- Backend: the Docker container starts on a fresh `./data` directory instead of failing with permission denied.
- Android: starting a capture while already recording no longer loses the clip in progress.
- Android: a clip is queued for upload when the camera fails mid-recording.
- Android: evidence IDs are random UUIDs so two devices can't collide on a shared node.
- Android: the upload queue count updates live, and interrupted uploads are confirmed on the next run.
- Android: the calculator no longer unlocks when `=` is pressed again on a computed `1312`.
- Backend: concurrent uploads of different bytes to the same chunk index can no longer both be accepted; exactly one wins and the rest get `409`.
- Backend: hash registration bodies are capped at 64 KiB instead of being read without limit.
- Android: capture errors (low battery, missing permission, failed queueing) are shown on the home screen instead of being silently reset to "Ready".
- Android: denying the camera permission no longer crashes the capture service on Android 14; recording without microphone permission shows an error instead of starting.
- Android: a clip's evidence row and chunk rows are saved in one transaction, so a failed queue no longer leaves a stuck upload entry or orphaned encrypted files.
- Android: pending chunks upload in index order, and a malformed metadata signature fails verification instead of crashing.

### Docs

- README: demo GIF, component and upload-sequence diagrams.
- README: rewritten with hero art, an animated app walkthrough, a pipeline graphic, a history of citizen witnessing, and an honest works-today/roadmap table.

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
