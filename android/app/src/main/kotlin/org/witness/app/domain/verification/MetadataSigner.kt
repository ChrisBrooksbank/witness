package org.witness.app.domain.verification

import org.witness.app.domain.model.EvidenceMetadata

interface MetadataSigner {
    fun sign(metadata: EvidenceMetadata): String

    fun verify(metadata: EvidenceMetadata, signatureBase64: String): Boolean
}
