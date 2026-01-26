# Content Capture

## Overview

The capture system is the core of Witness, responsible for recording video, audio, and photos with associated metadata. It must work reliably in high-stress situations, support covert recording, and generate verification hashes at the moment of capture.

## Requirements

### Media Types

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| CAP-001 | Support video recording | Must | Yes |
| CAP-002 | Support audio recording (standalone) | Must | Yes |
| CAP-003 | Support photo capture | Must | Yes |
| CAP-004 | Support burst mode photos | Should | No |
| CAP-005 | Support video with audio | Must | Yes |
| CAP-006 | Support audio-only fallback when battery low | Must | Yes |

### Metadata Capture

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| CAP-010 | Capture GPS coordinates at start of recording | Must | Yes |
| CAP-011 | Capture GPS coordinates periodically during recording | Should | Yes |
| CAP-012 | Capture timestamp (device + network time if available) | Must | Yes |
| CAP-013 | Capture device info (manufacturer, model, OS version) | Must | Yes |
| CAP-014 | Capture device orientation | Must | Yes |
| CAP-015 | Capture network status (WiFi/cellular/none) | Could | No |
| CAP-016 | Capture cell tower info for location corroboration | Could | No |
| CAP-017 | Capture accelerometer data for handling context | Could | No |

### Capture Modes

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| CAP-020 | Standard mode with UI visible | Must | Yes |
| CAP-021 | Witness mode with screen off | Must | Yes |
| CAP-022 | Low battery mode with reduced quality | Must | Yes |
| CAP-023 | Audio-only mode for minimal power | Must | Yes |
| CAP-024 | Continue recording when other apps overlay | Must | Yes |

### Quality & Format

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| CAP-030 | H.264 video encoding (hardware) | Must | Yes |
| CAP-031 | H.265/HEVC option for smaller files | Should | No |
| CAP-032 | AAC audio encoding | Must | Yes |
| CAP-033 | Configurable video resolution (720p default) | Should | Yes |
| CAP-034 | Configurable video bitrate | Should | Yes |
| CAP-035 | Automatic quality reduction when storage low | Should | Yes |

### Reliability

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| CAP-040 | Recording must survive app backgrounding | Must | Yes |
| CAP-041 | Recording must survive incoming phone calls | Must | Yes |
| CAP-042 | Recording must not crash under any circumstances | Must | Yes |
| CAP-043 | Partial footage must be preserved on interruption | Must | Yes |
| CAP-044 | Generate segments/chunks during recording for resilience | Must | Yes |

## Acceptance Criteria

- [ ] AC-CAP-1: Can start video recording and save valid MP4 file
- [ ] AC-CAP-2: Can start audio-only recording and save valid M4A file
- [ ] AC-CAP-3: Can take photos and save valid JPEG files
- [ ] AC-CAP-4: GPS coordinates are embedded in metadata within 10 seconds of recording start
- [ ] AC-CAP-5: Device timestamp is embedded in all captures
- [ ] AC-CAP-6: Recording continues when screen is turned off (witness mode)
- [ ] AC-CAP-7: Recording continues when another app is opened over Witness
- [ ] AC-CAP-8: Recording continues during incoming phone call
- [ ] AC-CAP-9: If recording is interrupted, at least partial footage is recoverable
- [ ] AC-CAP-10: Video is encoded using hardware encoder (MediaCodec)

## Technical Notes

### Camera API

Use Camera2 API (not CameraX alone) for full control needed for witness mode:

```kotlin
// Camera2 provides:
// - Background operation without preview surface
// - Full control over encoding
// - Wake lock compatibility
```

CameraX can be used for the standard UI mode where it simplifies preview handling.

### ForegroundService

Recording must use a ForegroundService to survive backgrounding:

```kotlin
class CaptureService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        // Start recording...
        return START_STICKY
    }
}
```

### Chunked Recording

Split recording into chunks for upload resilience:

- Chunk size: 30 seconds or 5MB, whichever comes first
- Each chunk is independently playable
- Each chunk gets its own hash
- Upload can start while recording continues

### WakeLock

Witness mode requires PARTIAL_WAKE_LOCK to keep CPU running:

```kotlin
val wakeLock = powerManager.newWakeLock(
    PowerManager.PARTIAL_WAKE_LOCK,
    "Witness::Recording"
)
wakeLock.acquire(TimeUnit.HOURS.toMillis(4))
```

### Hash Generation

SHA-256 hash generated for each chunk immediately:

```kotlin
val digest = MessageDigest.getInstance("SHA-256")
// Stream chunk through digest
val hash = digest.digest().toHexString()
```

## Open Questions

| Question | Status |
|----------|--------|
| Maximum recording duration limit? | Need to decide - storage vs use case |
| Default video resolution? | Leaning toward 720p for balance |
| Audio bitrate for audio-only mode? | Need to test battery impact |
| Chunk size optimization? | Need real-world testing |
