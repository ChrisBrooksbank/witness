# Safety Features

## Overview

Safety is Witness's primary differentiator. The app must protect users from identification, device seizure, and retaliation. These features are not optional enhancements - they are core to the mission.

## Requirements

### Anonymity

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| SAF-001 | Anonymous accounts only - no real identity required | Must | Yes |
| SAF-002 | Burner accounts - easy to abandon and create new | Must | Yes |
| SAF-003 | No PII collection - never ask for name, email, phone | Must | Yes |
| SAF-004 | Account creation without internet (local key generation) | Should | Yes |
| SAF-005 | No-account mode for one-time anonymous uploads | Could | No |

### Camouflage

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| SAF-010 | App disguised as innocuous application | Must | Yes |
| SAF-011 | Calculator disguise with working calculator | Must | Yes |
| SAF-012 | Weather app disguise | Should | Yes |
| SAF-013 | Notes app disguise | Should | Yes |
| SAF-014 | User can select disguise | Must | Yes |
| SAF-015 | No "Witness" branding visible when camouflaged | Must | Yes |
| SAF-016 | Decoy mode - fake app content on normal open | Must | Yes |
| SAF-017 | Secret gesture to reveal real app from decoy | Must | Yes |

### Witness Mode (Covert Recording)

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| SAF-020 | Record with screen off | Must | Yes |
| SAF-021 | Activation via volume button sequence | Must | Yes |
| SAF-022 | Activation via shake pattern | Should | No |
| SAF-023 | Activation via innocuous widget | Should | No |
| SAF-024 | Subtle audio/haptic confirmation of start | Must | Yes |
| SAF-025 | Subtle confirmation of stop | Must | Yes |
| SAF-026 | Continue recording if other apps opened | Must | Yes |
| SAF-027 | Configurable activation gesture | Should | Yes |

### Evidence Protection

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| SAF-030 | No local storage option - upload and delete | Must | Yes |
| SAF-031 | Encrypted local cache for pending uploads | Must | Yes |
| SAF-032 | Automatic deletion after confirmed upload | Must | Yes |
| SAF-033 | Panic wipe - quick action to clear all local evidence | Should | No |
| SAF-034 | Plausible deniability via camouflage | Must | Yes |
| SAF-035 | No evidence visible in system gallery | Must | Yes |
| SAF-036 | No evidence visible in recent files | Must | Yes |

## Acceptance Criteria

- [ ] AC-SAF-1: Account can be created without providing any personal information
- [ ] AC-SAF-2: App appears as "Calculator" (or other disguise) in launcher
- [ ] AC-SAF-3: Opening disguised app shows working calculator
- [ ] AC-SAF-4: Secret gesture (PIN as calculation) opens real Witness UI
- [ ] AC-SAF-5: Volume button sequence (up-up-down-down) starts recording with screen off
- [ ] AC-SAF-6: Subtle vibration confirms recording start/stop
- [ ] AC-SAF-7: Recording continues with screen off
- [ ] AC-SAF-8: No Witness files visible in Android gallery
- [ ] AC-SAF-9: Local evidence deleted after upload confirmation
- [ ] AC-SAF-10: App shows no "Witness" text or branding when camouflaged

## Technical Notes

### Activity Aliases for Camouflage

```xml
<!-- Calculator disguise (default) -->
<activity-alias
    android:name=".CalculatorLauncher"
    android:icon="@mipmap/ic_calculator"
    android:label="Calculator"
    android:targetActivity=".ui.camouflage.CalculatorActivity"
    android:enabled="true"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity-alias>
```

### Disguise Switching

```kotlin
object CamouflageManager {
    fun switchDisguise(context: Context, disguise: Disguise) {
        val pm = context.packageManager

        // Disable all aliases
        Disguise.entries.forEach { d ->
            pm.setComponentEnabledSetting(
                ComponentName(context, d.componentName),
                COMPONENT_ENABLED_STATE_DISABLED,
                DONT_KILL_APP
            )
        }

        // Enable selected
        pm.setComponentEnabledSetting(
            ComponentName(context, disguise.componentName),
            COMPONENT_ENABLED_STATE_ENABLED,
            DONT_KILL_APP
        )
    }
}
```

### Volume Button Detection

```kotlin
class VolumeButtonService : Service() {
    private val pattern = listOf(UP, UP, DOWN, DOWN)
    private val presses = mutableListOf<Int>()
    private var lastPress = 0L

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != ACTION_DOWN) return false

        val now = System.currentTimeMillis()
        if (now - lastPress > 500) presses.clear()

        presses.add(event.keyCode)
        lastPress = now

        if (presses.takeLast(4) == pattern) {
            presses.clear()
            startWitnessMode()
            return true
        }
        return false
    }
}
```

### Calculator Decoy

The calculator must be a fully functional calculator:

```kotlin
@Composable
fun CalculatorScreen(
    onSecretEntered: () -> Unit
) {
    var display by remember { mutableStateOf("0") }
    val secretPin = "1312=" // Example: entering "1312=" unlocks

    // Real calculator logic
    // If input matches secretPin, call onSecretEntered()
}
```

### Hidden Storage

Use app-private directory with encryption:

```kotlin
val evidenceDir = File(context.filesDir, ".evidence")
// This directory is not visible to other apps
// Not indexed by media scanner
// Encrypted with Tink
```

## Open Questions

| Question | Status |
|----------|--------|
| Default activation gesture? | Volume up-up-down-down, configurable |
| Panic wipe implementation for MVP? | Defer to post-MVP |
| Which disguises to include at launch? | Calculator (required), Weather, Notes |
| How to handle "Calculator" in Settings > Apps? | Documented limitation |
