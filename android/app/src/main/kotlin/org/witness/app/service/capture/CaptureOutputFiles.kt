package org.witness.app.service.capture

import android.content.Context
import java.io.File
import org.witness.app.domain.model.MediaType

private const val CAPTURE_DIRECTORY = ".capture"
private const val NO_MEDIA_FILE_NAME = ".nomedia"
private const val MP4_EXTENSION = "mp4"
private const val M4A_EXTENSION = "m4a"

object CaptureOutputFiles {
    fun create(context: Context, evidenceId: String, mediaType: MediaType, timestampMillis: Long): File {
        val directory = File(context.filesDir, CAPTURE_DIRECTORY)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        File(directory, NO_MEDIA_FILE_NAME).createNewFile()
        return File(directory, fileName(evidenceId, mediaType, timestampMillis))
    }

    fun latestFor(context: Context, evidenceId: String, mediaType: MediaType): File? {
        val directory = File(context.filesDir, CAPTURE_DIRECTORY)
        val prefix = "${sanitize(evidenceId)}-"
        val suffix = ".${extensionFor(mediaType)}"
        return directory
            .listFiles { file -> file.isFile && file.name.startsWith(prefix) && file.name.endsWith(suffix) }
            ?.maxByOrNull { file -> file.lastModified() }
    }

    fun fileName(evidenceId: String, mediaType: MediaType, timestampMillis: Long): String {
        return "${sanitize(evidenceId)}-$timestampMillis.${extensionFor(mediaType)}"
    }

    private fun sanitize(evidenceId: String): String {
        return evidenceId.replace(Regex("[^A-Za-z0-9_-]"), "_")
    }

    private fun extensionFor(mediaType: MediaType): String {
        return when (mediaType) {
            MediaType.Video -> MP4_EXTENSION
            MediaType.Audio -> M4A_EXTENSION
            MediaType.Photo -> MP4_EXTENSION
        }
    }
}
