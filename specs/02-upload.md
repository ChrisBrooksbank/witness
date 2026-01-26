# Upload & Streaming

## Overview

The upload system ensures captured evidence reaches federated nodes reliably, even under adverse conditions. It must handle intermittent connectivity, survive app/device restarts, and support real-time streaming when possible.

## Requirements

### Adaptive Upload Strategy

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| UPL-001 | Live stream when battery OK and connectivity strong | Must | Yes |
| UPL-002 | Background chunked upload when connectivity weak | Must | Yes |
| UPL-003 | Queue locally when offline, upload when connected | Must | Yes |
| UPL-004 | Reduce upload quality when battery low | Should | Yes |
| UPL-005 | Switch to audio-only streaming when bandwidth insufficient | Should | Yes |

### Upload Behavior

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| UPL-010 | Chunk uploads into small segments (30s or 5MB) | Must | Yes |
| UPL-011 | Queue persists across app restart | Must | Yes |
| UPL-012 | Queue persists across device restart | Must | Yes |
| UPL-013 | Attempt upload whenever connectivity available | Must | Yes |
| UPL-014 | Option to defer large uploads to WiFi | Should | Yes |
| UPL-015 | Resume interrupted uploads from last chunk | Must | Yes |
| UPL-016 | Exponential backoff on upload failures | Must | Yes |
| UPL-017 | Upload hash immediately, before full video | Must | Yes |

### Live Streaming

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| UPL-020 | Stream to designated watchers (trusted contacts) | Must | Yes |
| UPL-021 | Stream to federated nodes for archival | Must | Yes |
| UPL-022 | Panic trigger option - alert watchers when stream begins | Should | Yes |
| UPL-023 | Graceful degradation when bandwidth insufficient | Must | Yes |
| UPL-024 | Switch from live to queued seamlessly | Must | Yes |

### Node Selection

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| UPL-030 | Upload to multiple nodes for redundancy | Must | Yes |
| UPL-031 | Automatic failover to backup nodes | Must | Yes |
| UPL-032 | User can configure preferred nodes | Should | No |
| UPL-033 | Node health checking and selection | Should | Yes |

## Acceptance Criteria

- [ ] AC-UPL-1: Evidence uploads successfully to at least one node
- [ ] AC-UPL-2: Queued uploads survive app force-close
- [ ] AC-UPL-3: Queued uploads survive device reboot
- [ ] AC-UPL-4: Interrupted upload resumes from last successful chunk
- [ ] AC-UPL-5: Hash is uploaded within 10 seconds of recording start
- [ ] AC-UPL-6: Upload continues in background when app is minimized
- [ ] AC-UPL-7: Failed uploads retry with exponential backoff
- [ ] AC-UPL-8: Large uploads wait for WiFi when user enables that option
- [ ] AC-UPL-9: Live stream starts within 5 seconds on good connectivity
- [ ] AC-UPL-10: Stream gracefully degrades to lower quality on weak connection

## Technical Notes

### WorkManager for Queue

Use WorkManager for reliable background uploads:

```kotlin
val uploadWork = OneTimeWorkRequestBuilder<UploadWorker>()
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    )
    .setBackoffCriteria(
        BackoffPolicy.EXPONENTIAL,
        30, TimeUnit.SECONDS
    )
    .build()

WorkManager.getInstance(context)
    .enqueueUniqueWork(
        "upload_$evidenceId",
        ExistingWorkPolicy.KEEP,
        uploadWork
    )
```

### Upload Protocol

REST API for chunk upload:

```
POST /api/v1/evidence/{evidenceId}/chunks/{chunkIndex}
Content-Type: application/octet-stream
X-Chunk-Hash: {sha256}
X-Evidence-Hash: {fullEvidenceHash}

[binary chunk data]
```

### Streaming Protocol

Options for live streaming:

1. **WebSocket + binary frames** - Simple, good for archival
2. **RTMP** - Industry standard, but needs server support
3. **HLS** - HTTP-based, easier to cache but higher latency

Recommendation: WebSocket for MVP simplicity, consider RTMP for v2.

### Hash Upload

Hash should be uploaded BEFORE video to establish timestamp:

```
POST /api/v1/evidence/{evidenceId}/hash
{
    "hash": "sha256:abc123...",
    "timestamp": "2026-01-26T10:30:00Z",
    "metadata": { ... }
}
```

### Adaptive Quality

Monitor conditions and adjust:

```kotlin
sealed class UploadStrategy {
    object LiveStream : UploadStrategy()
    object BackgroundUpload : UploadStrategy()
    object QueueOnly : UploadStrategy()
}

fun determineStrategy(
    battery: Int,
    networkType: NetworkType,
    bandwidth: Long
): UploadStrategy {
    return when {
        battery < 15 -> QueueOnly
        networkType == WIFI && bandwidth > 1_000_000 -> LiveStream
        networkType != NONE -> BackgroundUpload
        else -> QueueOnly
    }
}
```

## Open Questions

| Question | Status |
|----------|--------|
| Streaming protocol (WebSocket vs RTMP vs HLS)? | Leaning WebSocket for MVP |
| Maximum queue size before warning user? | Need to determine |
| WiFi-only default or opt-in? | Likely opt-in for safety |
| How many nodes to upload to simultaneously? | Start with 2, make configurable |
