# Group Backend Setup Plan

The plan should make "run a Witness backend for my group" feel like setting up a small appliance, not becoming a backend engineer.

## Goal

Enable a non-specialist group operator to set up a Witness backend that their group's Android installations can upload evidence to, with clear commands, safe defaults, and honest security expectations.

The operator should not need to understand Go, Android internals, TLS certificate management, or database administration to get a first working deployment.

## Target Operator Experience

The recommended setup path should feel like this:

1. Rent a small server.
2. Point a domain name at it.
3. Copy one example configuration file.
4. Run one start command.
5. Open a health URL and see `ok`.
6. Configure the Android app with that backend URL.
7. Record one test clip.
8. Confirm the server received and verified it.

Anything more complex should be optional and clearly marked as advanced.

## Non-Goals For The First Version

- No multi-cloud deployment guide.
- No Kubernetes.
- No manual Go installation for operators.
- No requirement to understand reverse proxies.
- No operator-facing evidence viewer until the encryption/viewing model is designed.
- No claim that the backend can decrypt or watch uploaded videos.

## Current Reality

The current MVP backend can receive an evidence hash and encrypted chunk upload, then expose a verification JSON endpoint. It is useful for end-to-end upload testing.

It is not yet ready as a real group backend because:

- uploaded chunk bytes are not durably persisted across backend restarts
- metadata is stored in memory
- there is no operator setup package
- there is no production HTTPS deployment guide
- Android backend URL configuration is not yet operator-friendly
- there is no safe viewer/export workflow

The README should not promise a production-ready group backend until these gaps are closed.

## Recommended Implementation Order

### 1. Durable Single-Node Backend

Make the backend survive restarts before documenting it as a group server.

Minimum implementation:

- SQLite database for evidence records, chunk metadata, upload status, and verification state.
- Local filesystem storage for encrypted chunk bytes.
- Configurable data directory, defaulting to `./data`.
- Startup creates required directories.
- `/health` reports whether storage is writable.
- Existing endpoints continue to work:
  - `POST /api/v1/evidence/{evidenceId}/hash`
  - `POST /api/v1/evidence/{evidenceId}/chunks/{chunkIndex}`
  - `GET /api/v1/evidence/{evidenceId}/verify`

Acceptance checks:

- upload a test clip
- restart backend
- `GET /api/v1/evidence/{id}/verify` still returns the record
- encrypted chunk file still exists on disk

### 2. Backend Docker Package

Operators should not install Go.

Add:

- `backend/Dockerfile`
- root `docker-compose.yml`
- `.env.example`
- persistent `./data` volume
- container health check

The basic operator command should be:

```bash
docker compose up -d
```

Expected local check:

```bash
curl http://localhost:8080/health
```

Expected output:

```json
{"status":"ok"}
```

### 3. HTTPS With Automatic Certificates

Real phone uploads should use HTTPS. Use Caddy because it can manage TLS certificates automatically.

Add:

- `deploy/Caddyfile`
- Docker Compose profile or second compose file for Caddy
- README section explaining required DNS record

Operator-facing final health check:

```bash
curl https://witness.example.org/health
```

The guide should explain that `http://` is only for emulator/local development.

### 4. Android Backend URL Configuration

The app needs a clean way to point group builds at a real backend.

Recommended path:

- Keep debug default as `http://10.0.2.2:8080/`.
- Add release/group backend URL via Gradle property or environment variable.
- Generate `BuildConfig.WITNESS_NODE_BASE_URL`.
- Make the upload worker read the base URL from one app configuration source.
- Fail the release build if the backend URL is missing or still points to emulator localhost.

Example group build command:

```bash
./gradlew assembleRelease -PwitnessNodeBaseUrl=https://witness.example.org/
```

Documentation should make it clear that each group app build must know its backend URL.

### 5. Non-Expert README Guide

Add a top-level README section named `Run A Group Backend`.

Suggested structure:

1. **What You Need**
   - a domain name
   - a small Ubuntu VPS
   - SSH access to the server
   - Docker installed

2. **Set Your Domain**
   - create an `A` record pointing to the server IP
   - example: `witness.example.org`

3. **Copy The Config**
   - copy `.env.example` to `.env`
   - set server name and data directory

4. **Start The Backend**
   - `docker compose up -d`

5. **Check It Works**
   - `curl https://your-domain/health`

6. **Point The Android App At It**
   - set the backend URL
   - build/install the app

7. **Record A Test Clip**
   - use calculator unlock code
   - record a short clip
   - wait for upload

8. **Confirm Receipt**
   - use `/api/v1/evidence/{id}/verify`
   - explain where to find the evidence ID in logs/UI once implemented

9. **Backups**
   - back up `./data`
   - test restoring it

10. **Troubleshooting**
    - health URL does not load
    - app queue stays pending
    - certificate not issued
    - server disk full

### 6. Operator Safety Notes

The guide should be plain and careful:

- The backend stores encrypted evidence chunks.
- The backend operator should not be able to casually watch uploaded clips.
- Losing the backend data directory may lose uploaded evidence.
- Anyone operating a server should protect SSH keys and admin access.
- HTTPS is required for real deployments.
- The current MVP is not a substitute for legal advice or organizational security planning.

### 7. Viewing And Export Policy

Do not add a casual server-side "watch video" feature without a security review.

A safer future design should define:

- who is allowed to decrypt
- where decryption happens
- whether the recording phone can decrypt pending clips
- how access is granted and revoked
- how export logs/audit receipts work
- what happens if a phone or server is seized

Until that is implemented, documentation should say:

> The backend can confirm receipt and verify uploaded encrypted chunks. It does not currently provide a video playback interface.

## First README Promise

Once durable storage and Docker deployment exist, the README should be able to honestly say:

> Witness includes a simple Docker-based backend for group deployments. A group operator can run it on a small server, point Android builds at its HTTPS URL, and confirm encrypted clip uploads through the verification endpoint.

## Definition Of Done

- Backend persists metadata and encrypted chunks after restart.
- Docker Compose starts a healthy backend with one command.
- HTTPS deployment path is documented with Caddy.
- Android release/group builds can be configured with a backend URL.
- README has a non-expert setup guide.
- A fresh emulator recording uploads to the Docker backend.
- A fresh real-device recording uploads to the HTTPS backend.
- `/verify` confirms the received evidence after backend restart.
- Documentation clearly states that playback/viewing is not implemented yet.
