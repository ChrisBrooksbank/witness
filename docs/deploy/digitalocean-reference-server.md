# DigitalOcean Reference Server

This guide provisions the Witness reference server on a small DigitalOcean Droplet.

The goal is a public HTTPS endpoint that runs the existing Dockerized Go backend and persists encrypted chunks plus SQLite metadata under `/opt/witness/data`.

## Recommendation

Start with the lowest-cost Droplet only for pre-alpha testing:

- Image: Ubuntu LTS
- Size: `$4/month` basic Droplet
- Region: nearest to the testers
- Backups: weekly at minimum
- Domain: `reference.<your-domain>`

The bootstrap enables 1 GB swap because the smallest Droplet is tight for Docker image builds.

## Prerequisites

- DigitalOcean account
- `doctl` installed and authenticated, or `DIGITALOCEAN_ACCESS_TOKEN` set
- SSH key added to DigitalOcean
- A domain you can point at the Droplet

Install/authenticate `doctl`:

```powershell
winget install DigitalOcean.Doctl
doctl auth init
```

Or use a token for the current terminal:

```powershell
$env:DIGITALOCEAN_ACCESS_TOKEN = "dop_v1_..."
```

## Create The Droplet

From the repo root:

```powershell
.\scripts\provision-digitalocean-reference.ps1 `
  -Domain "reference.example.org" `
  -SshKeyName "your-digitalocean-ssh-key-name"
```

The script will:

- create a Droplet named `witness-reference`
- pass `deploy/digitalocean-cloud-init.yml` as cloud-init user data
- open only SSH, HTTP, and HTTPS through a DigitalOcean firewall
- print the Droplet IP address

If your DNS zone is managed in DigitalOcean, add `-ManageDns`:

```powershell
.\scripts\provision-digitalocean-reference.ps1 `
  -Domain "reference.example.org" `
  -SshKeyName "your-digitalocean-ssh-key-name" `
  -ManageDns
```

If DNS is not managed in DigitalOcean, create an `A` record manually:

```text
reference.example.org -> <droplet-ip>
```

## Check Bootstrap Progress

SSH into the server:

```powershell
ssh root@<droplet-ip>
```

Watch bootstrap logs:

```bash
tail -f /var/log/cloud-init-output.log
```

Check containers:

```bash
cd /opt/witness
docker compose ps
```

## Verify The Server

Once DNS points to the Droplet:

```bash
curl https://reference.example.org/health
curl https://reference.example.org/api/v1/version
```

Expected health response:

```json
{"status":"ok"}
```

## Build A Reference APK

Build the Android release with the hosted backend URL:

```bash
cd android
./gradlew assembleRelease -PwitnessNodeBaseUrl=https://reference.example.org/
```

Release builds intentionally reject localhost, emulator URLs, missing trailing slashes, and non-HTTPS URLs.

## Update The Server

SSH into the Droplet:

```bash
ssh root@<droplet-ip>
```

Then run:

```bash
systemctl start witness-reference-update.service
```

## Back Up And Restore

The important server data lives in:

```text
/opt/witness/data
```

Enable DigitalOcean backups on the Droplet. Before relying on the reference server for real tester data, also test a manual restore on a temporary Droplet.

Manual backup example:

```bash
cd /opt/witness
tar -czf /root/witness-data-$(date -u +%Y%m%dT%H%M%SZ).tar.gz data
```

Manual restore example:

```bash
cd /opt/witness
docker compose down
tar -xzf /root/witness-data-backup.tar.gz
docker compose up -d
```

## Safety Notes

- This server stores encrypted chunks, not plaintext video.
- It has no playback interface.
- It is a reference server for pre-alpha testing, not production evidence infrastructure.
- Protect the DigitalOcean account, SSH keys, DNS account, and backups.
- Losing `/opt/witness/data` may lose uploaded evidence.
