package org.witness.app.data.remote.evidence

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface EvidenceUploadApi {
    @POST("api/v1/evidence/{evidenceId}/hash")
    suspend fun registerHash(
        @Path("evidenceId") evidenceId: String,
        @Body request: RegisterHashRequest,
    ): RegisterHashResponse

    @POST("api/v1/evidence/{evidenceId}/chunks/{chunkIndex}")
    suspend fun uploadChunk(
        @Path("evidenceId") evidenceId: String,
        @Path("chunkIndex") chunkIndex: Int,
        @Header("X-Chunk-Hash") chunkHash: String,
        @Header("X-Evidence-Hash") evidenceHash: String,
        @Body chunkBody: RequestBody,
    ): UploadChunkResponse
}
