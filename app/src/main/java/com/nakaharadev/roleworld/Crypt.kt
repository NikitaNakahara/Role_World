package com.nakaharadev.roleworld

import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object Crypt {
    fun createAsymmetricCryptObject(): AsymmetricCrypt {
        val crypt = AsymmetricCrypt()
        crypt.init()

        return crypt
    }

    fun createSymmetricCryptObject(): SymmetricCrypt {
        val crypt = SymmetricCrypt()
        crypt.init()

        return crypt
    }

    class AsymmetricCrypt {
        private var _privateKey: PrivateKey? = null
        private var _publicKey: PublicKey? = null
        private var _cipher: Cipher? = null
        fun setPublicKey(key: PublicKey?) {
            _publicKey = key
        }

        fun init(): PublicKey? {
            createCipher()
            _publicKey = generateKeyPair()
            return _publicKey
        }

        private fun createCipher() {
            _cipher = try {
                Cipher.getInstance("RSA")
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        private fun generateKeyPair(): PublicKey? {
            try {
                val generator = KeyPairGenerator.getInstance("RSA")
                generator.initialize(2048)
                val keyPair = generator.generateKeyPair()
                _privateKey = keyPair.private
                return keyPair.public
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun encryptKey(key: SecretKey): String {
            val keyBytes = keyToBytes(key)
            return try {
                _cipher!!.init(Cipher.ENCRYPT_MODE, _publicKey)
                Base64.getEncoder().encodeToString(_cipher!!.doFinal(keyBytes))
            } catch (e: InvalidKeyException) {
                throw RuntimeException(e)
            } catch (e: IllegalBlockSizeException) {
                throw RuntimeException(e)
            } catch (e: BadPaddingException) {
                throw RuntimeException(e)
            }
        }

        fun decryptKey(str: String?): SecretKey {
            val keyBytes = Base64.getDecoder().decode(str)
            return try {
                _cipher!!.init(Cipher.DECRYPT_MODE, _privateKey)
                bytesToKey(_cipher!!.doFinal(Base64.getDecoder().decode(keyBytes)))
            } catch (e: InvalidKeyException) {
                throw RuntimeException(e)
            } catch (e: IllegalBlockSizeException) {
                throw RuntimeException(e)
            } catch (e: BadPaddingException) {
                throw RuntimeException(e)
            }
        }

        private fun keyToBytes(key: SecretKey): ByteArray {
            return key.encoded
        }

        private fun bytesToKey(data: ByteArray): SecretKey {
            return SecretKeySpec(data, 0, data.size, "AES")
        }
    }

    class SymmetricCrypt {
        private var _key: SecretKey? = null
        private var _cipher: Cipher? = null
        fun setKey(key: SecretKey?) {
            _key = key
        }

        fun init(): SecretKey? {
            createCipher()
            return generateKey()
        }

        private fun createCipher() {
            _cipher = try {
                Cipher.getInstance("AES")
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        private fun generateKey(): SecretKey? {
            return try {
                val generator = KeyGenerator.getInstance("AES")
                generator.init(256, SecureRandom())
                _key = generator.generateKey()
                return _key
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        fun encryptString(text: String): String {
            return try {
                _cipher!!.init(Cipher.ENCRYPT_MODE, _key)
                val result = _cipher!!.doFinal(text.toByteArray(StandardCharsets.UTF_8))
                Base64.getEncoder().encodeToString(result)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        fun decryptString(text: String?): String {
            return try {
                val base64decrypted = Base64.getDecoder().decode(text)
                _cipher!!.init(Cipher.DECRYPT_MODE, _key)
                String(_cipher!!.doFinal(base64decrypted))
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}