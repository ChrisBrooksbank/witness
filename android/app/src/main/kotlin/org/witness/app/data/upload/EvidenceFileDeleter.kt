package org.witness.app.data.upload

import java.io.File

class EvidenceFileDeleter {
    fun delete(paths: List<String>): DeletionResult {
        val failedPaths = paths.filter { path ->
            val file = File(path)
            file.exists() && !file.delete()
        }
        return DeletionResult(
            requestedCount = paths.size,
            failedPaths = failedPaths,
        )
    }
}

data class DeletionResult(
    val requestedCount: Int,
    val failedPaths: List<String>,
) {
    val succeeded: Boolean
        get() = failedPaths.isEmpty()
}
