# Witness - Operational Guide

This document defines build commands, quality gates, and coding standards for AI agents working on the Witness project.

## Build & Run Commands

### Android Client

```bash
# Compile Kotlin (type check)
./gradlew compileDebugKotlin

# Lint check
./gradlew ktlintCheck

# Auto-format
./gradlew ktlintFormat

# Detekt static analysis
./gradlew detekt

# Unit tests
./gradlew testDebugUnitTest

# Unit tests with coverage
./gradlew testDebugUnitTest jacocoTestReport

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Instrumented tests (requires emulator/device)
./gradlew connectedDebugAndroidTest

# All checks
./gradlew check

# Clean build
./gradlew clean assembleDebug
```

### Go Backend

```bash
# Build
go build -o witness-node ./cmd/server

# Run tests
go test ./...

# Run tests with coverage
go test -cover ./...

# Lint
golangci-lint run

# Format
gofmt -w .

# Vet
go vet ./...

# Run server
./witness-node

# Run with hot reload (air)
air
```

## Quality Gates

**IMPORTANT**: All gates must pass before committing. These gates create backpressure to maintain code quality.

### Gate 1: Type Safety

**Command**: `./gradlew compileDebugKotlin`
**Requirement**: 0 errors
**Purpose**: Kotlin compiler catches type errors, null safety violations

```bash
# Android
./gradlew compileDebugKotlin
# Must exit 0

# Go
go build ./...
# Must exit 0
```

### Gate 2: Linting

**Command**: `./gradlew ktlintCheck` or `./gradlew detekt`
**Requirement**: 0 errors, 0 warnings
**Purpose**: Enforce consistent code style, catch common issues

```bash
# Android - ktlint
./gradlew ktlintCheck

# Android - detekt
./gradlew detekt

# Go
golangci-lint run
```

### Gate 3: Formatting

**Command**: `./gradlew ktlintFormat`
**Requirement**: All files properly formatted
**Purpose**: Consistent code formatting

```bash
# Android
./gradlew ktlintFormat

# Go
gofmt -w .
```

### Gate 4: Unit Tests

**Command**: `./gradlew testDebugUnitTest`
**Requirement**: 100% pass, 80% coverage target
**Purpose**: Verify business logic works correctly

```bash
# Android
./gradlew testDebugUnitTest

# Go
go test ./...
```

### Gate 5: Build

**Command**: `./gradlew assembleDebug`
**Requirement**: Successful build
**Purpose**: Ensure app compiles and packages correctly

```bash
# Android
./gradlew assembleDebug

# Go
go build -o witness-node ./cmd/server
```

### Gate 6: Instrumented Tests

**Command**: `./gradlew connectedDebugAndroidTest`
**Requirement**: 100% pass (when UI changes)
**Purpose**: Verify UI and integration works on device
**Note**: Run when UI or Android-specific code changes

```bash
./gradlew connectedDebugAndroidTest
```

### Gate 7: APK Size

**Target**: < 15MB
**Purpose**: Keep app lightweight for target users with limited storage

```bash
# Check APK size after build
ls -lh android/app/build/outputs/apk/debug/app-debug.apk
```

### Gate 8: Dead Code Detection

**Command**: `./gradlew detekt` with unused rules
**Requirement**: 0 unused declarations
**Purpose**: Keep codebase clean

### Gate 9: Dependency Check

**Command**: Gradle dependency analysis
**Requirement**: No unused dependencies
**Purpose**: Minimize APK size and attack surface

## Code Quality Rules

### DRY (Don't Repeat Yourself)

- Extract repeated code into functions
- Use shared modules for common logic
- Parameterize similar functions

### No Magic Numbers

```kotlin
// BAD
if (battery < 15) { ... }

// GOOD
private const val LOW_BATTERY_THRESHOLD = 15
if (battery < LOW_BATTERY_THRESHOLD) { ... }
```

### Error Handling

- Always handle errors explicitly
- Log errors with context
- Provide user-friendly error messages
- Never swallow exceptions silently

```kotlin
// BAD
try { upload() } catch (e: Exception) { }

// GOOD
try {
    upload()
} catch (e: IOException) {
    Log.e(TAG, "Upload failed: ${e.message}", e)
    showError(R.string.upload_failed)
}
```

### Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Classes | PascalCase | `UploadWorker` |
| Functions | camelCase | `startRecording()` |
| Constants | SCREAMING_SNAKE | `MAX_RETRY_COUNT` |
| Variables | camelCase | `uploadQueue` |
| Packages | lowercase | `org.witness.app.data` |
| XML IDs | snake_case | `btn_record` |

### Architecture Patterns

**MVVM + Clean Architecture**

```
UI Layer (Compose)
    ↓
ViewModel (StateFlow)
    ↓
Use Cases (optional)
    ↓
Repository (interface)
    ↓
Data Sources (Room, Retrofit, etc.)
```

**Dependency Injection (Hilt)**

- All dependencies injected via constructor
- Use `@Inject` for classes
- Use `@Module` + `@Provides` for third-party dependencies

## Security Requirements

### Encryption

- All footage encrypted before upload (Tink AES-GCM)
- Keys stored in Android Keystore
- TLS 1.3 for all network communication
- No plaintext credentials anywhere

### No Secrets in Code

- No API keys in source
- No hardcoded passwords
- Use BuildConfig for environment-specific values
- Use EncryptedSharedPreferences for local secrets

### Evidence Integrity

- SHA-256 hash at moment of capture
- Hash uploaded immediately (before full video)
- Metadata bundle signed with device key

## Codebase Patterns

### Kotlin Coroutines

```kotlin
// Use viewModelScope for ViewModel operations
viewModelScope.launch {
    _state.value = State.Loading
    try {
        val result = repository.getData()
        _state.value = State.Success(result)
    } catch (e: Exception) {
        _state.value = State.Error(e.message)
    }
}
```

### StateFlow for UI State

```kotlin
private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
val uiState: StateFlow<UiState> = _uiState.asStateFlow()
```

### Room Database

```kotlin
@Entity(tableName = "evidence")
data class EvidenceEntity(
    @PrimaryKey val id: String,
    val hash: String,
    val timestamp: Long,
    val uploaded: Boolean
)

@Dao
interface EvidenceDao {
    @Query("SELECT * FROM evidence WHERE uploaded = 0")
    fun getPendingUploads(): Flow<List<EvidenceEntity>>
}
```

### WorkManager for Uploads

```kotlin
val uploadWork = OneTimeWorkRequestBuilder<UploadWorker>()
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    )
    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
    .build()

WorkManager.getInstance(context)
    .enqueueUniqueWork("upload_$evidenceId", ExistingWorkPolicy.KEEP, uploadWork)
```

## Definition of Done

A task is complete when:

- [ ] Code compiles without errors (Gate 1)
- [ ] Linting passes (Gate 2)
- [ ] Code is formatted (Gate 3)
- [ ] Unit tests pass (Gate 4)
- [ ] Build succeeds (Gate 5)
- [ ] Instrumented tests pass if UI changed (Gate 6)
- [ ] APK size < 15MB (Gate 7)
- [ ] No dead code introduced (Gate 8)
- [ ] No unused dependencies (Gate 9)
- [ ] Code reviewed (self-review checklist)
- [ ] Committed with descriptive message
- [ ] IMPLEMENTATION_PLAN.md updated

## Self-Review Checklist

Before committing, verify:

- [ ] No TODO comments without issue references
- [ ] No commented-out code
- [ ] No debug logging in production code
- [ ] Error messages are user-friendly
- [ ] Sensitive data is encrypted
- [ ] New code has test coverage
- [ ] Documentation updated if API changed
