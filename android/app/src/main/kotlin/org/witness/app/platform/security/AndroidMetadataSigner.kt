package org.witness.app.platform.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.Base64
import org.witness.app.domain.model.EvidenceMetadata
import org.witness.app.domain.verification.MetadataCanonicalizer
import org.witness.app.domain.verification.MetadataSigner

private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val SIGNING_ALGORITHM = "SHA256withECDSA"
private const val SIGNING_KEY_ALIAS = "witness_signing_key"
private const val EC_CURVE = "secp256r1"

class AndroidMetadataSigner(
    private val canonicalizer: MetadataCanonicalizer = MetadataCanonicalizer(),
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE),
) : MetadataSigner {
    override fun sign(metadata: EvidenceMetadata): String {
        keyStore.load(null)
        ensureSigningKey()

        val privateKey = keyStore.getKey(SIGNING_KEY_ALIAS, null)
        val signature = Signature.getInstance(SIGNING_ALGORITHM)
        signature.initSign(privateKey as java.security.PrivateKey)
        signature.update(canonicalizer.canonicalize(metadata))

        return Base64.getEncoder().encodeToString(signature.sign())
    }

    override fun verify(metadata: EvidenceMetadata, signatureBase64: String): Boolean {
        keyStore.load(null)
        val certificate = keyStore.getCertificate(SIGNING_KEY_ALIAS) ?: return false

        val signature = Signature.getInstance(SIGNING_ALGORITHM)
        signature.initVerify(certificate.publicKey)
        signature.update(canonicalizer.canonicalize(metadata))

        return signature.verify(Base64.getDecoder().decode(signatureBase64))
    }

    private fun ensureSigningKey() {
        if (keyStore.containsAlias(SIGNING_KEY_ALIAS)) return

        val parameterSpec = KeyGenParameterSpec.Builder(
            SIGNING_KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY,
        )
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setAlgorithmParameterSpec(ECGenParameterSpec(EC_CURVE))
            .build()

        KeyPairGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEYSTORE)
            .apply { initialize(parameterSpec) }
            .generateKeyPair()
    }
}
