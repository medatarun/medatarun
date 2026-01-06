package io.medatarun.auth.bootstrap.internal

import io.medatarun.auth.bootstrap.AuthEmbeddedKeyRegistry
import io.medatarun.auth.bootstrap.JwtKeyMaterial
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

class AuthEmbeddedKeyRegistryImpl(private val keystorePath: Path): AuthEmbeddedKeyRegistry {

    enum class KeyAudience(val label: String) { PRIVATE("PRIVATE KEY"), PUBLIC("PUBLIC KEY") }

    override fun loadOrCreateKeys(): JwtKeyMaterial {

        Files.createDirectories(keystorePath)

        val privPath = keystorePath.resolve(PRIVATE_KEY_FILENAME)
        val pubPath = keystorePath.resolve(PUBLIC_KEY_FILENAME)
        val kidPath = keystorePath.resolve(IDENTIFIER_KEY_FILENAME)

        if (Files.exists(privPath) && Files.exists(pubPath) && Files.exists(kidPath)) {
            return loadKeys(privPath, pubPath, kidPath)
        }

        return generateKeys(privPath, pubPath, kidPath)
    }

    private fun generateKeys(
        privPath: Path,
        pubPath: Path,
        kidPath: Path?
    ): JwtKeyMaterial {
        val material = generateJwtKeyMaterial()

        writePem(privPath, KeyAudience.PRIVATE, material.privateKey.encoded)
        writePem(pubPath, KeyAudience.PUBLIC, material.publicKey.encoded)
        Files.writeString(kidPath, material.kid, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)


        return material
    }

    private fun loadKeys(
        privPath: Path,
        pubPath: Path,
        kidPath: Path
    ): JwtKeyMaterial {
        val kf = KeyFactory.getInstance(ENCRYPTION_ALGORITHM)
        val privateKey = kf.generatePrivate(PKCS8EncodedKeySpec(readPem(privPath))) as RSAPrivateKey
        val publicKey = kf.generatePublic(X509EncodedKeySpec(readPem(pubPath))) as RSAPublicKey
        val kid = Files.readString(kidPath).trim()
        return JwtKeyMaterial(privateKey, publicKey, kid)
    }

    private fun writePem(path: Path, type: KeyAudience, bytes: ByteArray) {
        val b64 = Base64.getEncoder().encodeToString(bytes)
        val content = buildString {
            appendLine("-----BEGIN ${type.label}-----")
            b64.chunked(64).forEach { appendLine(it) }
            appendLine("-----END ${type.label}-----")
        }
        Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }

    private fun readPem(path: Path): ByteArray {
        val body = Files.readAllLines(path)
            .asSequence()
            .filterNot { it.startsWith("-----") }
            .joinToString("")
        return Base64.getDecoder().decode(body)
    }

    companion object {
        const val PRIVATE_KEY_FILENAME = "private.pem"
        const val PUBLIC_KEY_FILENAME = "public.pem"
        const val IDENTIFIER_KEY_FILENAME = "kid.txt"
        const val ENCRYPTION_ALGORITHM = "RSA"

        fun generateJwtKeyMaterial(): JwtKeyMaterial {
            val gen = KeyPairGenerator.getInstance(ENCRYPTION_ALGORITHM)
            gen.initialize(2048)
            val pair = gen.generateKeyPair()
            val kid = UUID.randomUUID().toString()
            val material = JwtKeyMaterial(pair.private as RSAPrivateKey, pair.public as RSAPublicKey, kid)
            return material
        }

    }

}