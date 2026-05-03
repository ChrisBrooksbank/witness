package org.witness.app.data.local.evidence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EvidenceChunkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChunk(chunk: EvidenceChunkEntity)

    @Query("SELECT * FROM evidence_chunks WHERE evidence_id = :evidenceId AND upload_status != :completeStatus")
    suspend fun getPendingChunks(
        evidenceId: String,
        completeStatus: String = UploadStatus.Complete.name,
    ): List<EvidenceChunkEntity>

    @Query(
        """
        UPDATE evidence_chunks
        SET upload_status = :status,
            uploaded_at_epoch_millis = :uploadedAtEpochMillis
        WHERE evidence_id = :evidenceId AND chunk_index = :chunkIndex
        """,
    )
    suspend fun markChunkUploaded(
        evidenceId: String,
        chunkIndex: Int,
        status: String = UploadStatus.Complete.name,
        uploadedAtEpochMillis: Long,
    )

    @Query(
        """
        UPDATE evidence_chunks
        SET upload_status = :status
        WHERE evidence_id = :evidenceId AND chunk_index = :chunkIndex
        """,
    )
    suspend fun markChunkStatus(evidenceId: String, chunkIndex: Int, status: String)
}
