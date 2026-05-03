package org.witness.app.data.upload

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EvidenceFileDeleterTest {
    private val deleter = EvidenceFileDeleter()

    @Test
    fun deletesExistingFiles() {
        val file = File.createTempFile("witness-evidence", ".chunk")
        file.writeText("encrypted payload")

        val result = deleter.delete(listOf(file.absolutePath))

        assertTrue(result.succeeded)
        assertFalse(file.exists())
    }

    @Test
    fun missingFilesAreTreatedAsAlreadyDeleted() {
        val file = File(System.getProperty("java.io.tmpdir"), "missing-witness-evidence")

        val result = deleter.delete(listOf(file.absolutePath))

        assertTrue(result.succeeded)
    }
}
