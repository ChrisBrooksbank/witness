package org.witness.app.data.local.evidence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

private const val DELETE_GRACE_PERIOD_MILLIS = 24L * 60L * 60L * 1_000L

enum class UploadStatus {
    Pending,
    Uploading,
    Complete,
    FailedRetryable,
}

@Entity(tableName = "evidence")
data class EvidenceEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "media_type") val mediaType: String,
    @ColumnInfo(name = "capture_mode") val captureMode: String,
    @ColumnInfo(name = "merkle_root") val merkleRoot: String?,
    @ColumnInfo(name = "captured_at_epoch_millis") val capturedAtEpochMillis: Long,
    @ColumnInfo(name = "device_manufacturer") val deviceManufacturer: String,
    @ColumnInfo(name = "device_model") val deviceModel: String,
    @ColumnInfo(name = "android_version") val androidVersion: String,
    @ColumnInfo(name = "app_version") val appVersion: String,
    @ColumnInfo(name = "latitude") val latitude: Double?,
    @ColumnInfo(name = "longitude") val longitude: Double?,
    @ColumnInfo(name = "location_accuracy_meters") val locationAccuracyMeters: Float?,
    @ColumnInfo(name = "location_unavailable_reason") val locationUnavailableReason: String?,
    @ColumnInfo(name = "upload_status") val uploadStatus: String = UploadStatus.Pending.name,
    @ColumnInfo(name = "confirmed_uploaded_at_epoch_millis") val confirmedUploadedAtEpochMillis: Long? = null,
    @ColumnInfo(name = "delete_after_epoch_millis") val deleteAfterEpochMillis: Long? = null,
) {
    fun withUploadConfirmed(confirmedAtEpochMillis: Long): EvidenceEntity {
        return copy(
            uploadStatus = UploadStatus.Complete.name,
            confirmedUploadedAtEpochMillis = confirmedAtEpochMillis,
            deleteAfterEpochMillis = deletionDeadline(confirmedAtEpochMillis),
        )
    }

    companion object {
        fun deletionDeadline(confirmedAtEpochMillis: Long): Long {
            return confirmedAtEpochMillis + DELETE_GRACE_PERIOD_MILLIS
        }
    }
}

@Entity(
    tableName = "evidence_chunks",
    foreignKeys = [
        ForeignKey(
            entity = EvidenceEntity::class,
            parentColumns = ["id"],
            childColumns = ["evidence_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["evidence_id", "chunk_index"], unique = true),
    ],
)
data class EvidenceChunkEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "evidence_id") val evidenceId: String,
    @ColumnInfo(name = "chunk_index") val chunkIndex: Int,
    @ColumnInfo(name = "sha256") val sha256: String,
    @ColumnInfo(name = "encrypted_file_path") val encryptedFilePath: String,
    @ColumnInfo(name = "size_bytes") val sizeBytes: Long,
    @ColumnInfo(name = "captured_at_epoch_millis") val capturedAtEpochMillis: Long,
    @ColumnInfo(name = "upload_status") val uploadStatus: String = UploadStatus.Pending.name,
    @ColumnInfo(name = "uploaded_at_epoch_millis") val uploadedAtEpochMillis: Long? = null,
)
