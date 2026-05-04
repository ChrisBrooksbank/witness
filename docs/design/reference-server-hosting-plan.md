# Reference Server Hosting Plan

## Goal

Stand up a public Witness reference server that demonstrates the current single-node upload API end to end:

- Android builds can upload encrypted evidence hashes and chunks to a stable HTTPS URL.
- Testers can check `/health`, `/api/v1/version`, and `/api/v1/evidence/{evidenceId}/verify`.
- Server data survives restarts.
- The deployment is documented well enough that another operator can reproduce it.

This server is a reference deployment for development and trusted pre-alpha testing. It is not yet a production-safe public evidence node.

## Recommendation

Use the existing Go backend as the reference server and host it on a container-capable platform.

Your Netlify account still helps, but mostly around the server rather than as the main Go host:

- Host a small Witness reference landing/status page on Netlify.
- Optionally add Netlify Functions for lightweight operator tools later.
- Optionally use Netlify DNS for the reference domain if you already manage DNS there.

Do not make Netlify the first-choice host for the current Go backend. The backend is already a long-running Dockerized Go HTTP service with SQLite plus filesystem chunk storage. Netlify Functions can run Go functions, but the current service would need to be reshaped into serverless handlers, and Netlify Blobs cannot be accessed from Go functions. Netlify Blobs is also optimized for frequent reads and infrequent writes, while Witness evidence intake is write-heavy during testing.

Relevant Netlify docs:

- [Netlify Functions overview](https://docs.netlify.com/build/functions/overview/)
- [Netlify Blobs overview](https://docs.netlify.com/storage/blobs/overview/)
- [Netlify CLI deployment workflow](https://docs.netlify.com/cli/get-started/)

## Hosting Options

### Option A: Small VPS With Docker And Caddy

Best first reference-server path.

Pros:

- Matches the existing `docker-compose.yml`, `backend/Dockerfile`, and `deploy/Caddyfile`.
- Persistent local disk is simple to reason about.
- Caddy can manage HTTPS certificates.
- Closest to the documented group backend setup.

Cons:

- Requires basic server patching, firewall, backups, and SSH hygiene.
- Scaling and monitoring are manual.

Good candidates:

- Hetzner
- DigitalOcean
- Linode/Akamai
- Fly.io volume-backed VM, if we want a more managed feel

### Option B: Managed Container Host

Good if we want less server administration.

Pros:

- Runs the existing Docker image with fewer operations chores.
- Easier deploy previews and logs.
- Usually straightforward environment variables.

Cons:

- Persistent disk support varies by provider and plan.
- Some platforms sleep free instances, which is bad for upload reliability.
- HTTPS and custom domain setup differs per provider.

Good candidates:

- Fly.io with a volume
- Render with persistent disk
- Railway with persistent volume

### Option C: Netlify Serverless Rewrite

Use only after the reference server is working elsewhere.

Pros:

- Fits your existing Netlify account.
- Good for status pages, small JSON APIs, dashboards, and operator tooling.
- Netlify Blobs supports large individual objects and site-scoped stores.

Cons:

- Requires rewriting the Go HTTP service into serverless functions or adding a Node API layer.
- Go functions cannot access Netlify Blobs.
- SQLite plus local filesystem persistence does not map cleanly to serverless runtime storage.
- Concurrency and upload consistency need a redesign.

This path is useful for a future "Netlify reference facade", not the fastest route to a trustworthy reference node.

## Target Architecture

```text
Android pre-alpha build
        |
        | HTTPS
        v
reference.witness.example
        |
        v
Caddy or platform TLS router
        |
        v
Go witness-node container
        |
        +--> SQLite metadata database
        |
        +--> encrypted chunk files
```

Optional Netlify companion:

```text
status.witness.example
        |
        v
Netlify static site
        |
        +--> links to health/version endpoints
        +--> deployment notes
        +--> tester instructions
```

## Implementation Steps

### 1. Choose Hosting Target

Decision: start with a Docker-capable host.

Acceptance:

- Host has HTTPS support or supports Caddy on ports `80` and `443`.
- Host has at least 20 GB persistent disk for pre-alpha testing.
- Host can run the current backend image without changing application code.
- We know how backups will be taken before accepting real test footage.

### 2. Reserve Domain

Use a clear reference hostname, for example:

```text
reference.witness.example
```

Acceptance:

- DNS `A` or `CNAME` record points to the host.
- `/health` will be publicly reachable over HTTPS.
- Any Netlify-hosted status site uses a separate hostname, such as `status.witness.example`.

### 3. Harden Reference Configuration

Add a deployment-specific `.env` on the host, never committed:

```bash
WITNESS_SERVER_NAME=reference.witness.example
WITNESS_DATA_DIR=./data
COMPOSE_PROFILES=https
```

Acceptance:

- Backend binds only behind Caddy or the platform router.
- Data directory is persistent.
- Server logs do not include evidence payload bytes or sensitive metadata dumps.

### 4. Deploy The Current Backend

For VPS/Caddy:

```bash
docker compose up -d --build
curl https://reference.witness.example/health
curl https://reference.witness.example/api/v1/version
```

Acceptance:

- `/health` returns `{"status":"ok"}`.
- `/api/v1/version` returns the expected backend version.
- Container restarts automatically after reboot.

### 5. Add Backup And Restore Procedure

Back up the data directory containing:

- `witness.db`
- encrypted chunk files

Acceptance:

- A documented backup command exists.
- A documented restore command exists.
- Restore is tested on a second local or temporary instance.
- Backup notes explain that losing `./data` may lose uploaded evidence.

### 6. Add Reference Build Configuration For Android

Create a repeatable reference APK build command:

```bash
cd android
./gradlew assembleRelease -PwitnessNodeBaseUrl=https://reference.witness.example/
```

Acceptance:

- Release build refuses localhost/emulator URLs.
- The generated APK uploads to the reference URL.
- Tester notes include the exact backend URL baked into the APK.

### 7. Run End-To-End Smoke Test

Test flow:

1. Install reference APK.
2. Open Calculator.
3. Enter `1312=`.
4. Record a short clip.
5. Wait for upload.
6. Query `/api/v1/evidence/{evidenceId}/verify`.
7. Restart backend.
8. Query verification again.

Acceptance:

- Hash upload is recorded.
- Encrypted chunk upload is recorded.
- `encryptedBytesStored` remains `true` after restart.
- Android upload queue marks the evidence confirmed.

### 8. Publish Netlify Companion Page

Use Netlify for a small static status and tester page.

Content:

- Current reference backend URL.
- Current APK/release link.
- Health endpoint link.
- Safety note that the server stores encrypted chunks and has no playback interface.
- Warning that this is a pre-alpha reference server, not legal advice or production evidence infrastructure.

Acceptance:

- Netlify deploy is connected to the repo or a small static folder.
- Page links to the current health/version endpoints.
- No secrets are stored in the static site.

### 9. Add Minimal Monitoring

Minimum:

- External HTTPS uptime check against `/health`.
- Disk usage alert.
- Weekly manual verification that backups restore.

Acceptance:

- Health monitor alerts the operator.
- Disk alert fires before the volume is full.
- Monitoring does not expose evidence IDs publicly.

## Security Boundaries

The reference server must be honest about what it does and does not protect:

- It stores encrypted chunks, not plaintext video.
- It verifies hashes and chunk receipt.
- It does not provide playback.
- It does not yet implement federation replication.
- It does not yet implement viewer access grants, revocation, or public evidence publishing.
- Operators must protect SSH, hosting account access, backups, and DNS.

## Future Netlify Path

After the Docker reference server is stable, evaluate a Netlify companion architecture:

- Netlify static status site.
- Netlify Function that checks backend health and formats public status JSON.
- Netlify Blobs for public release metadata, not primary evidence storage.
- Netlify Forms for tester feedback.

Only consider Netlify as the primary evidence API if we first redesign storage and concurrency around serverless constraints.

## Definition Of Done

- Reference host selected and documented.
- HTTPS domain is live.
- Current Go backend deploys without code rewrites.
- Data survives container restart and host reboot.
- Backup and restore are documented and tested.
- Android reference APK uploads to the hosted server.
- Verification endpoint confirms uploaded encrypted chunks.
- Netlify companion page is published or explicitly deferred.
- README links to this plan once the reference server is live.
