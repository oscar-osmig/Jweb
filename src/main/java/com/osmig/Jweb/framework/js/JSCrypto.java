package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

import java.util.ArrayList;
import java.util.List;

/**
 * Web Crypto API DSL for cryptographic operations.
 *
 * <p>The Web Crypto API provides cryptographic primitives for encryption, decryption,
 * hashing, signing, and key generation in a secure and performant way.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSCrypto.*;
 * import static com.osmig.Jweb.framework.js.JS.*;
 *
 * // Generate random values
 * randomUUID()  // crypto.randomUUID()
 * randomValues(array(32))  // crypto.getRandomValues(new Uint8Array(32))
 *
 * // Hash data with SHA-256
 * digest().sha256().data("Hello World").build()
 *
 * // Generate AES-GCM key
 * generateKey()
 *     .aesGcm(256)
 *     .extractable(true)
 *     .keyUsages("encrypt", "decrypt")
 *     .build()
 *
 * // Encrypt data
 * encrypt()
 *     .aesGcm(variable("key"), variable("iv"))
 *     .data(variable("plaintext"))
 *     .build()
 *
 * // Sign with HMAC
 * sign()
 *     .hmac(variable("key"))
 *     .data(variable("message"))
 *     .build()
 * </pre>
 */
public final class JSCrypto {
    private JSCrypto() {}

    // ==================== Random Values ====================

    /**
     * Generates a random UUID: crypto.randomUUID()
     *
     * @return a Val representing the UUID string
     */
    public static Val randomUUID() {
        return new Val("crypto.randomUUID()");
    }

    /**
     * Fills a typed array with random values: crypto.getRandomValues(array)
     *
     * @param array the typed array to fill
     * @return a Val representing the filled array
     */
    public static Val randomValues(Val array) {
        return new Val("crypto.getRandomValues(" + array.js() + ")");
    }

    /**
     * Creates a new Uint8Array with random bytes.
     *
     * @param length number of random bytes
     * @return a Val representing the random byte array
     */
    public static Val randomBytes(int length) {
        return new Val("crypto.getRandomValues(new Uint8Array(" + length + "))");
    }

    // ==================== Digest (Hashing) ====================

    /**
     * Creates a digest (hash) builder.
     *
     * @return a DigestBuilder for configuring the hash operation
     */
    public static DigestBuilder digest() {
        return new DigestBuilder();
    }

    /**
     * Quick SHA-256 hash of text data.
     *
     * @param text the text to hash
     * @return a Val representing the hash promise
     */
    public static Val sha256(String text) {
        return digest().sha256().text(text).build();
    }

    /**
     * Quick SHA-256 hash of data.
     *
     * @param data the data to hash
     * @return a Val representing the hash promise
     */
    public static Val sha256(Val data) {
        return digest().sha256().data(data).build();
    }

    /**
     * Builder for crypto.subtle.digest() operations.
     */
    public static class DigestBuilder {
        private String algorithm;
        private String data;

        DigestBuilder() {}

        /** Sets algorithm to SHA-1 (not recommended for security) */
        public DigestBuilder sha1() {
            this.algorithm = "'SHA-1'";
            return this;
        }

        /** Sets algorithm to SHA-256 */
        public DigestBuilder sha256() {
            this.algorithm = "'SHA-256'";
            return this;
        }

        /** Sets algorithm to SHA-384 */
        public DigestBuilder sha384() {
            this.algorithm = "'SHA-384'";
            return this;
        }

        /** Sets algorithm to SHA-512 */
        public DigestBuilder sha512() {
            this.algorithm = "'SHA-512'";
            return this;
        }

        /** Sets algorithm dynamically */
        public DigestBuilder algorithm(String algorithm) {
            this.algorithm = "'" + JS.esc(algorithm) + "'";
            return this;
        }

        /** Sets data from a Val expression */
        public DigestBuilder data(Val data) {
            this.data = data.js();
            return this;
        }

        /** Sets data from a string (will be encoded to UTF-8) */
        public DigestBuilder text(String text) {
            this.data = "new TextEncoder().encode('" + JS.esc(text) + "')";
            return this;
        }

        /** Sets data from a variable containing text (will be encoded) */
        public DigestBuilder textVar(Val text) {
            this.data = "new TextEncoder().encode(" + text.js() + ")";
            return this;
        }

        /**
         * Builds the digest operation.
         * Returns a Promise that resolves to an ArrayBuffer.
         */
        public Val build() {
            if (algorithm == null) throw new IllegalStateException("Algorithm not set");
            if (data == null) throw new IllegalStateException("Data not set");
            return new Val("crypto.subtle.digest(" + algorithm + "," + data + ")");
        }
    }

    // ==================== Encryption/Decryption ====================

    /**
     * Creates an encrypt builder.
     *
     * @return an EncryptBuilder for configuring encryption
     */
    public static EncryptBuilder encrypt() {
        return new EncryptBuilder();
    }

    /**
     * Creates a decrypt builder.
     *
     * @return a DecryptBuilder for configuring decryption
     */
    public static DecryptBuilder decrypt() {
        return new DecryptBuilder();
    }

    /**
     * Builder for crypto.subtle.encrypt() operations.
     */
    public static class EncryptBuilder {
        private String algorithm;
        private String key;
        private String data;

        EncryptBuilder() {}

        /** AES-GCM encryption with key and IV */
        public EncryptBuilder aesGcm(Val key, Val iv) {
            this.algorithm = "{name:'AES-GCM',iv:" + iv.js() + "}";
            this.key = key.js();
            return this;
        }

        /** AES-GCM encryption with key, IV, and additional data */
        public EncryptBuilder aesGcm(Val key, Val iv, Val additionalData) {
            this.algorithm = "{name:'AES-GCM',iv:" + iv.js() + ",additionalData:" + additionalData.js() + "}";
            this.key = key.js();
            return this;
        }

        /** AES-GCM encryption with tag length */
        public EncryptBuilder aesGcm(Val key, Val iv, Val additionalData, int tagLength) {
            this.algorithm = "{name:'AES-GCM',iv:" + iv.js() + ",additionalData:" + additionalData.js() + ",tagLength:" + tagLength + "}";
            this.key = key.js();
            return this;
        }

        /** AES-CBC encryption with key and IV */
        public EncryptBuilder aesCbc(Val key, Val iv) {
            this.algorithm = "{name:'AES-CBC',iv:" + iv.js() + "}";
            this.key = key.js();
            return this;
        }

        /** AES-CTR encryption with key and counter */
        public EncryptBuilder aesCtr(Val key, Val counter, int length) {
            this.algorithm = "{name:'AES-CTR',counter:" + counter.js() + ",length:" + length + "}";
            this.key = key.js();
            return this;
        }

        /** RSA-OAEP encryption with key */
        public EncryptBuilder rsaOaep(Val key) {
            this.algorithm = "{name:'RSA-OAEP'}";
            this.key = key.js();
            return this;
        }

        /** RSA-OAEP encryption with key and label */
        public EncryptBuilder rsaOaep(Val key, Val label) {
            this.algorithm = "{name:'RSA-OAEP',label:" + label.js() + "}";
            this.key = key.js();
            return this;
        }

        /** Sets data to encrypt */
        public EncryptBuilder data(Val data) {
            this.data = data.js();
            return this;
        }

        /** Sets text data (will be encoded to UTF-8) */
        public EncryptBuilder text(String text) {
            this.data = "new TextEncoder().encode('" + JS.esc(text) + "')";
            return this;
        }

        /** Sets text data from variable (will be encoded) */
        public EncryptBuilder textVar(Val text) {
            this.data = "new TextEncoder().encode(" + text.js() + ")";
            return this;
        }

        /**
         * Builds the encrypt operation.
         * Returns a Promise that resolves to an ArrayBuffer.
         */
        public Val build() {
            if (algorithm == null) throw new IllegalStateException("Algorithm not set");
            if (key == null) throw new IllegalStateException("Key not set");
            if (data == null) throw new IllegalStateException("Data not set");
            return new Val("crypto.subtle.encrypt(" + algorithm + "," + key + "," + data + ")");
        }
    }

    /**
     * Builder for crypto.subtle.decrypt() operations.
     */
    public static class DecryptBuilder {
        private String algorithm;
        private String key;
        private String data;

        DecryptBuilder() {}

        /** AES-GCM decryption with key and IV */
        public DecryptBuilder aesGcm(Val key, Val iv) {
            this.algorithm = "{name:'AES-GCM',iv:" + iv.js() + "}";
            this.key = key.js();
            return this;
        }

        /** AES-GCM decryption with key, IV, and additional data */
        public DecryptBuilder aesGcm(Val key, Val iv, Val additionalData) {
            this.algorithm = "{name:'AES-GCM',iv:" + iv.js() + ",additionalData:" + additionalData.js() + "}";
            this.key = key.js();
            return this;
        }

        /** AES-GCM decryption with tag length */
        public DecryptBuilder aesGcm(Val key, Val iv, Val additionalData, int tagLength) {
            this.algorithm = "{name:'AES-GCM',iv:" + iv.js() + ",additionalData:" + additionalData.js() + ",tagLength:" + tagLength + "}";
            this.key = key.js();
            return this;
        }

        /** AES-CBC decryption with key and IV */
        public DecryptBuilder aesCbc(Val key, Val iv) {
            this.algorithm = "{name:'AES-CBC',iv:" + iv.js() + "}";
            this.key = key.js();
            return this;
        }

        /** AES-CTR decryption with key and counter */
        public DecryptBuilder aesCtr(Val key, Val counter, int length) {
            this.algorithm = "{name:'AES-CTR',counter:" + counter.js() + ",length:" + length + "}";
            this.key = key.js();
            return this;
        }

        /** RSA-OAEP decryption with key */
        public DecryptBuilder rsaOaep(Val key) {
            this.algorithm = "{name:'RSA-OAEP'}";
            this.key = key.js();
            return this;
        }

        /** RSA-OAEP decryption with key and label */
        public DecryptBuilder rsaOaep(Val key, Val label) {
            this.algorithm = "{name:'RSA-OAEP',label:" + label.js() + "}";
            this.key = key.js();
            return this;
        }

        /** Sets data to decrypt */
        public DecryptBuilder data(Val data) {
            this.data = data.js();
            return this;
        }

        /**
         * Builds the decrypt operation.
         * Returns a Promise that resolves to an ArrayBuffer.
         */
        public Val build() {
            if (algorithm == null) throw new IllegalStateException("Algorithm not set");
            if (key == null) throw new IllegalStateException("Key not set");
            if (data == null) throw new IllegalStateException("Data not set");
            return new Val("crypto.subtle.decrypt(" + algorithm + "," + key + "," + data + ")");
        }
    }

    // ==================== Sign/Verify ====================

    /**
     * Creates a sign builder.
     *
     * @return a SignBuilder for configuring signing
     */
    public static SignBuilder sign() {
        return new SignBuilder();
    }

    /**
     * Creates a verify builder.
     *
     * @return a VerifyBuilder for configuring signature verification
     */
    public static VerifyBuilder verify() {
        return new VerifyBuilder();
    }

    /**
     * Builder for crypto.subtle.sign() operations.
     */
    public static class SignBuilder {
        private String algorithm;
        private String key;
        private String data;

        SignBuilder() {}

        /** HMAC signing */
        public SignBuilder hmac(Val key) {
            this.algorithm = "{name:'HMAC'}";
            this.key = key.js();
            return this;
        }

        /** RSA-PSS signing */
        public SignBuilder rsaPss(Val key, int saltLength) {
            this.algorithm = "{name:'RSA-PSS',saltLength:" + saltLength + "}";
            this.key = key.js();
            return this;
        }

        /** RSASSA-PKCS1-v1_5 signing */
        public SignBuilder rsaSsaPkcs1(Val key) {
            this.algorithm = "{name:'RSASSA-PKCS1-v1_5'}";
            this.key = key.js();
            return this;
        }

        /** ECDSA signing with hash algorithm */
        public SignBuilder ecdsa(Val key, String hash) {
            this.algorithm = "{name:'ECDSA',hash:'" + JS.esc(hash) + "'}";
            this.key = key.js();
            return this;
        }

        /** ECDSA signing with SHA-256 */
        public SignBuilder ecdsaSha256(Val key) {
            return ecdsa(key, "SHA-256");
        }

        /** ECDSA signing with SHA-384 */
        public SignBuilder ecdsaSha384(Val key) {
            return ecdsa(key, "SHA-384");
        }

        /** ECDSA signing with SHA-512 */
        public SignBuilder ecdsaSha512(Val key) {
            return ecdsa(key, "SHA-512");
        }

        /** Sets data to sign */
        public SignBuilder data(Val data) {
            this.data = data.js();
            return this;
        }

        /** Sets text data (will be encoded to UTF-8) */
        public SignBuilder text(String text) {
            this.data = "new TextEncoder().encode('" + JS.esc(text) + "')";
            return this;
        }

        /** Sets text data from variable (will be encoded) */
        public SignBuilder textVar(Val text) {
            this.data = "new TextEncoder().encode(" + text.js() + ")";
            return this;
        }

        /**
         * Builds the sign operation.
         * Returns a Promise that resolves to an ArrayBuffer (the signature).
         */
        public Val build() {
            if (algorithm == null) throw new IllegalStateException("Algorithm not set");
            if (key == null) throw new IllegalStateException("Key not set");
            if (data == null) throw new IllegalStateException("Data not set");
            return new Val("crypto.subtle.sign(" + algorithm + "," + key + "," + data + ")");
        }
    }

    /**
     * Builder for crypto.subtle.verify() operations.
     */
    public static class VerifyBuilder {
        private String algorithm;
        private String key;
        private String signature;
        private String data;

        VerifyBuilder() {}

        /** HMAC verification */
        public VerifyBuilder hmac(Val key) {
            this.algorithm = "{name:'HMAC'}";
            this.key = key.js();
            return this;
        }

        /** RSA-PSS verification */
        public VerifyBuilder rsaPss(Val key, int saltLength) {
            this.algorithm = "{name:'RSA-PSS',saltLength:" + saltLength + "}";
            this.key = key.js();
            return this;
        }

        /** RSASSA-PKCS1-v1_5 verification */
        public VerifyBuilder rsaSsaPkcs1(Val key) {
            this.algorithm = "{name:'RSASSA-PKCS1-v1_5'}";
            this.key = key.js();
            return this;
        }

        /** ECDSA verification with hash algorithm */
        public VerifyBuilder ecdsa(Val key, String hash) {
            this.algorithm = "{name:'ECDSA',hash:'" + JS.esc(hash) + "'}";
            this.key = key.js();
            return this;
        }

        /** ECDSA verification with SHA-256 */
        public VerifyBuilder ecdsaSha256(Val key) {
            return ecdsa(key, "SHA-256");
        }

        /** Sets the signature to verify */
        public VerifyBuilder signature(Val signature) {
            this.signature = signature.js();
            return this;
        }

        /** Sets data that was signed */
        public VerifyBuilder data(Val data) {
            this.data = data.js();
            return this;
        }

        /** Sets text data (will be encoded to UTF-8) */
        public VerifyBuilder text(String text) {
            this.data = "new TextEncoder().encode('" + JS.esc(text) + "')";
            return this;
        }

        /** Sets text data from variable (will be encoded) */
        public VerifyBuilder textVar(Val text) {
            this.data = "new TextEncoder().encode(" + text.js() + ")";
            return this;
        }

        /**
         * Builds the verify operation.
         * Returns a Promise that resolves to a boolean.
         */
        public Val build() {
            if (algorithm == null) throw new IllegalStateException("Algorithm not set");
            if (key == null) throw new IllegalStateException("Key not set");
            if (signature == null) throw new IllegalStateException("Signature not set");
            if (data == null) throw new IllegalStateException("Data not set");
            return new Val("crypto.subtle.verify(" + algorithm + "," + key + "," + signature + "," + data + ")");
        }
    }

    // ==================== Key Generation ====================

    /**
     * Creates a key generation builder.
     *
     * @return a GenerateKeyBuilder for configuring key generation
     */
    public static GenerateKeyBuilder generateKey() {
        return new GenerateKeyBuilder();
    }

    /**
     * Builder for crypto.subtle.generateKey() operations.
     */
    public static class GenerateKeyBuilder {
        private String algorithm;
        private boolean extractable;
        private final List<String> keyUsages = new ArrayList<>();

        GenerateKeyBuilder() {}

        /** AES-GCM key generation */
        public GenerateKeyBuilder aesGcm(int length) {
            this.algorithm = "{name:'AES-GCM',length:" + length + "}";
            return this;
        }

        /** AES-CBC key generation */
        public GenerateKeyBuilder aesCbc(int length) {
            this.algorithm = "{name:'AES-CBC',length:" + length + "}";
            return this;
        }

        /** AES-CTR key generation */
        public GenerateKeyBuilder aesCtr(int length) {
            this.algorithm = "{name:'AES-CTR',length:" + length + "}";
            return this;
        }

        /** HMAC key generation */
        public GenerateKeyBuilder hmac(String hash, int length) {
            this.algorithm = "{name:'HMAC',hash:'" + JS.esc(hash) + "',length:" + length + "}";
            return this;
        }

        /** HMAC key generation with SHA-256 */
        public GenerateKeyBuilder hmacSha256(int length) {
            return hmac("SHA-256", length);
        }

        /** HMAC key generation with SHA-512 */
        public GenerateKeyBuilder hmacSha512(int length) {
            return hmac("SHA-512", length);
        }

        /** RSA-OAEP key pair generation */
        public GenerateKeyBuilder rsaOaep(int modulusLength, int publicExponent, String hash) {
            this.algorithm = "{name:'RSA-OAEP',modulusLength:" + modulusLength +
                ",publicExponent:new Uint8Array([" + publicExponent + "]),hash:'" + JS.esc(hash) + "'}";
            return this;
        }

        /** RSA-OAEP key pair with common settings (2048-bit, 65537, SHA-256) */
        public GenerateKeyBuilder rsaOaep2048() {
            return rsaOaep(2048, 65537, "SHA-256");
        }

        /** RSA-PSS key pair generation */
        public GenerateKeyBuilder rsaPss(int modulusLength, int publicExponent, String hash) {
            this.algorithm = "{name:'RSA-PSS',modulusLength:" + modulusLength +
                ",publicExponent:new Uint8Array([" + publicExponent + "]),hash:'" + JS.esc(hash) + "'}";
            return this;
        }

        /** RSASSA-PKCS1-v1_5 key pair generation */
        public GenerateKeyBuilder rsaSsaPkcs1(int modulusLength, int publicExponent, String hash) {
            this.algorithm = "{name:'RSASSA-PKCS1-v1_5',modulusLength:" + modulusLength +
                ",publicExponent:new Uint8Array([" + publicExponent + "]),hash:'" + JS.esc(hash) + "'}";
            return this;
        }

        /** ECDSA key pair generation */
        public GenerateKeyBuilder ecdsa(String namedCurve) {
            this.algorithm = "{name:'ECDSA',namedCurve:'" + JS.esc(namedCurve) + "'}";
            return this;
        }

        /** ECDSA with P-256 curve */
        public GenerateKeyBuilder ecdsaP256() {
            return ecdsa("P-256");
        }

        /** ECDSA with P-384 curve */
        public GenerateKeyBuilder ecdsaP384() {
            return ecdsa("P-384");
        }

        /** ECDSA with P-521 curve */
        public GenerateKeyBuilder ecdsaP521() {
            return ecdsa("P-521");
        }

        /** ECDH key pair generation */
        public GenerateKeyBuilder ecdh(String namedCurve) {
            this.algorithm = "{name:'ECDH',namedCurve:'" + JS.esc(namedCurve) + "'}";
            return this;
        }

        /** ECDH with P-256 curve */
        public GenerateKeyBuilder ecdhP256() {
            return ecdh("P-256");
        }

        /** Sets whether the key can be exported */
        public GenerateKeyBuilder extractable(boolean extractable) {
            this.extractable = extractable;
            return this;
        }

        /** Adds key usages */
        public GenerateKeyBuilder keyUsages(String... usages) {
            for (String usage : usages) {
                keyUsages.add("'" + JS.esc(usage) + "'");
            }
            return this;
        }

        /** Adds encrypt usage */
        public GenerateKeyBuilder forEncrypt() {
            return keyUsages("encrypt", "decrypt");
        }

        /** Adds sign usage */
        public GenerateKeyBuilder forSign() {
            return keyUsages("sign", "verify");
        }

        /** Adds wrap usage */
        public GenerateKeyBuilder forWrap() {
            return keyUsages("wrapKey", "unwrapKey");
        }

        /** Adds derive usage */
        public GenerateKeyBuilder forDerive() {
            return keyUsages("deriveKey", "deriveBits");
        }

        /**
         * Builds the generateKey operation.
         * Returns a Promise that resolves to a CryptoKey or CryptoKeyPair.
         */
        public Val build() {
            if (algorithm == null) throw new IllegalStateException("Algorithm not set");
            if (keyUsages.isEmpty()) throw new IllegalStateException("Key usages not set");
            return new Val("crypto.subtle.generateKey(" + algorithm + "," + extractable + ",[" + String.join(",", keyUsages) + "])");
        }
    }

    // ==================== Key Derivation ====================

    /**
     * Creates a key derivation builder.
     *
     * @return a DeriveKeyBuilder for configuring key derivation
     */
    public static DeriveKeyBuilder deriveKey() {
        return new DeriveKeyBuilder();
    }

    /**
     * Creates a derive bits builder.
     *
     * @return a DeriveBitsBuilder for configuring bit derivation
     */
    public static DeriveBitsBuilder deriveBits() {
        return new DeriveBitsBuilder();
    }

    /**
     * Builder for crypto.subtle.deriveKey() operations.
     */
    public static class DeriveKeyBuilder {
        private String algorithm;
        private String baseKey;
        private String derivedKeyAlgorithm;
        private boolean extractable;
        private final List<String> keyUsages = new ArrayList<>();

        DeriveKeyBuilder() {}

        /** PBKDF2 key derivation */
        public DeriveKeyBuilder pbkdf2(Val baseKey, Val salt, int iterations, String hash) {
            this.algorithm = "{name:'PBKDF2',salt:" + salt.js() + ",iterations:" + iterations + ",hash:'" + JS.esc(hash) + "'}";
            this.baseKey = baseKey.js();
            return this;
        }

        /** PBKDF2 with SHA-256 */
        public DeriveKeyBuilder pbkdf2Sha256(Val baseKey, Val salt, int iterations) {
            return pbkdf2(baseKey, salt, iterations, "SHA-256");
        }

        /** HKDF key derivation */
        public DeriveKeyBuilder hkdf(Val baseKey, Val salt, Val info, String hash) {
            this.algorithm = "{name:'HKDF',salt:" + salt.js() + ",info:" + info.js() + ",hash:'" + JS.esc(hash) + "'}";
            this.baseKey = baseKey.js();
            return this;
        }

        /** ECDH key derivation */
        public DeriveKeyBuilder ecdh(Val baseKey, Val publicKey) {
            this.algorithm = "{name:'ECDH',public:" + publicKey.js() + "}";
            this.baseKey = baseKey.js();
            return this;
        }

        /** Sets the derived key algorithm (e.g., AES-GCM) */
        public DeriveKeyBuilder derivedAlgorithm(String name, int length) {
            this.derivedKeyAlgorithm = "{name:'" + JS.esc(name) + "',length:" + length + "}";
            return this;
        }

        /** Derived key will be AES-GCM */
        public DeriveKeyBuilder deriveAesGcm(int length) {
            return derivedAlgorithm("AES-GCM", length);
        }

        /** Derived key will be AES-CBC */
        public DeriveKeyBuilder deriveAesCbc(int length) {
            return derivedAlgorithm("AES-CBC", length);
        }

        /** Sets whether the derived key can be exported */
        public DeriveKeyBuilder extractable(boolean extractable) {
            this.extractable = extractable;
            return this;
        }

        /** Adds key usages */
        public DeriveKeyBuilder keyUsages(String... usages) {
            for (String usage : usages) {
                keyUsages.add("'" + JS.esc(usage) + "'");
            }
            return this;
        }

        /**
         * Builds the deriveKey operation.
         * Returns a Promise that resolves to a CryptoKey.
         */
        public Val build() {
            if (algorithm == null) throw new IllegalStateException("Algorithm not set");
            if (baseKey == null) throw new IllegalStateException("Base key not set");
            if (derivedKeyAlgorithm == null) throw new IllegalStateException("Derived key algorithm not set");
            if (keyUsages.isEmpty()) throw new IllegalStateException("Key usages not set");
            return new Val("crypto.subtle.deriveKey(" + algorithm + "," + baseKey + "," +
                derivedKeyAlgorithm + "," + extractable + ",[" + String.join(",", keyUsages) + "])");
        }
    }

    /**
     * Builder for crypto.subtle.deriveBits() operations.
     */
    public static class DeriveBitsBuilder {
        private String algorithm;
        private String baseKey;
        private int length;

        DeriveBitsBuilder() {}

        /** PBKDF2 bit derivation */
        public DeriveBitsBuilder pbkdf2(Val baseKey, Val salt, int iterations, String hash) {
            this.algorithm = "{name:'PBKDF2',salt:" + salt.js() + ",iterations:" + iterations + ",hash:'" + JS.esc(hash) + "'}";
            this.baseKey = baseKey.js();
            return this;
        }

        /** HKDF bit derivation */
        public DeriveBitsBuilder hkdf(Val baseKey, Val salt, Val info, String hash) {
            this.algorithm = "{name:'HKDF',salt:" + salt.js() + ",info:" + info.js() + ",hash:'" + JS.esc(hash) + "'}";
            this.baseKey = baseKey.js();
            return this;
        }

        /** ECDH bit derivation */
        public DeriveBitsBuilder ecdh(Val baseKey, Val publicKey) {
            this.algorithm = "{name:'ECDH',public:" + publicKey.js() + "}";
            this.baseKey = baseKey.js();
            return this;
        }

        /** Sets the number of bits to derive */
        public DeriveBitsBuilder length(int length) {
            this.length = length;
            return this;
        }

        /**
         * Builds the deriveBits operation.
         * Returns a Promise that resolves to an ArrayBuffer.
         */
        public Val build() {
            if (algorithm == null) throw new IllegalStateException("Algorithm not set");
            if (baseKey == null) throw new IllegalStateException("Base key not set");
            if (length == 0) throw new IllegalStateException("Length not set");
            return new Val("crypto.subtle.deriveBits(" + algorithm + "," + baseKey + "," + length + ")");
        }
    }

    // ==================== Import/Export Keys ====================

    /**
     * Creates a key import builder.
     *
     * @return an ImportKeyBuilder for importing keys
     */
    public static ImportKeyBuilder importKey() {
        return new ImportKeyBuilder();
    }

    /**
     * Creates a key export builder.
     *
     * @return an ExportKeyBuilder for exporting keys
     */
    public static ExportKeyBuilder exportKey() {
        return new ExportKeyBuilder();
    }

    /**
     * Builder for crypto.subtle.importKey() operations.
     */
    public static class ImportKeyBuilder {
        private String format;
        private String keyData;
        private String algorithm;
        private boolean extractable;
        private final List<String> keyUsages = new ArrayList<>();

        ImportKeyBuilder() {}

        /** Import from raw format */
        public ImportKeyBuilder raw(Val keyData) {
            this.format = "'raw'";
            this.keyData = keyData.js();
            return this;
        }

        /** Import from PKCS8 format */
        public ImportKeyBuilder pkcs8(Val keyData) {
            this.format = "'pkcs8'";
            this.keyData = keyData.js();
            return this;
        }

        /** Import from SPKI format */
        public ImportKeyBuilder spki(Val keyData) {
            this.format = "'spki'";
            this.keyData = keyData.js();
            return this;
        }

        /** Import from JWK format */
        public ImportKeyBuilder jwk(Val keyData) {
            this.format = "'jwk'";
            this.keyData = keyData.js();
            return this;
        }

        /** Sets the algorithm for the imported key */
        public ImportKeyBuilder algorithm(String name) {
            this.algorithm = "{name:'" + JS.esc(name) + "'}";
            return this;
        }

        /** Algorithm is AES-GCM */
        public ImportKeyBuilder aesGcm() {
            this.algorithm = "{name:'AES-GCM'}";
            return this;
        }

        /** Algorithm is AES-CBC */
        public ImportKeyBuilder aesCbc() {
            this.algorithm = "{name:'AES-CBC'}";
            return this;
        }

        /** Algorithm is HMAC with hash */
        public ImportKeyBuilder hmac(String hash) {
            this.algorithm = "{name:'HMAC',hash:'" + JS.esc(hash) + "'}";
            return this;
        }

        /** Algorithm is RSA-OAEP with hash */
        public ImportKeyBuilder rsaOaep(String hash) {
            this.algorithm = "{name:'RSA-OAEP',hash:'" + JS.esc(hash) + "'}";
            return this;
        }

        /** Sets whether the key can be exported */
        public ImportKeyBuilder extractable(boolean extractable) {
            this.extractable = extractable;
            return this;
        }

        /** Adds key usages */
        public ImportKeyBuilder keyUsages(String... usages) {
            for (String usage : usages) {
                keyUsages.add("'" + JS.esc(usage) + "'");
            }
            return this;
        }

        /**
         * Builds the importKey operation.
         * Returns a Promise that resolves to a CryptoKey.
         */
        public Val build() {
            if (format == null) throw new IllegalStateException("Format not set");
            if (keyData == null) throw new IllegalStateException("Key data not set");
            if (algorithm == null) throw new IllegalStateException("Algorithm not set");
            if (keyUsages.isEmpty()) throw new IllegalStateException("Key usages not set");
            return new Val("crypto.subtle.importKey(" + format + "," + keyData + "," +
                algorithm + "," + extractable + ",[" + String.join(",", keyUsages) + "])");
        }
    }

    /**
     * Builder for crypto.subtle.exportKey() operations.
     */
    public static class ExportKeyBuilder {
        private String format;
        private String key;

        ExportKeyBuilder() {}

        /** Export to raw format */
        public ExportKeyBuilder raw(Val key) {
            this.format = "'raw'";
            this.key = key.js();
            return this;
        }

        /** Export to PKCS8 format */
        public ExportKeyBuilder pkcs8(Val key) {
            this.format = "'pkcs8'";
            this.key = key.js();
            return this;
        }

        /** Export to SPKI format */
        public ExportKeyBuilder spki(Val key) {
            this.format = "'spki'";
            this.key = key.js();
            return this;
        }

        /** Export to JWK format */
        public ExportKeyBuilder jwk(Val key) {
            this.format = "'jwk'";
            this.key = key.js();
            return this;
        }

        /**
         * Builds the exportKey operation.
         * Returns a Promise that resolves to ArrayBuffer or JWK object.
         */
        public Val build() {
            if (format == null) throw new IllegalStateException("Format not set");
            if (key == null) throw new IllegalStateException("Key not set");
            return new Val("crypto.subtle.exportKey(" + format + "," + key + ")");
        }
    }

    // ==================== Wrap/Unwrap Keys ====================

    /**
     * Creates a wrap key builder.
     *
     * @return a WrapKeyBuilder for wrapping keys
     */
    public static WrapKeyBuilder wrapKey() {
        return new WrapKeyBuilder();
    }

    /**
     * Creates an unwrap key builder.
     *
     * @return an UnwrapKeyBuilder for unwrapping keys
     */
    public static UnwrapKeyBuilder unwrapKey() {
        return new UnwrapKeyBuilder();
    }

    /**
     * Builder for crypto.subtle.wrapKey() operations.
     */
    public static class WrapKeyBuilder {
        private String format;
        private String key;
        private String wrappingKey;
        private String wrapAlgorithm;

        WrapKeyBuilder() {}

        /** Sets the format for the wrapped key */
        public WrapKeyBuilder format(String format) {
            this.format = "'" + JS.esc(format) + "'";
            return this;
        }

        /** Wrap in raw format */
        public WrapKeyBuilder raw() {
            return format("raw");
        }

        /** Wrap in PKCS8 format */
        public WrapKeyBuilder pkcs8() {
            return format("pkcs8");
        }

        /** Wrap in SPKI format */
        public WrapKeyBuilder spki() {
            return format("spki");
        }

        /** Wrap in JWK format */
        public WrapKeyBuilder jwk() {
            return format("jwk");
        }

        /** Sets the key to wrap */
        public WrapKeyBuilder key(Val key) {
            this.key = key.js();
            return this;
        }

        /** Sets the wrapping key */
        public WrapKeyBuilder wrappingKey(Val wrappingKey) {
            this.wrappingKey = wrappingKey.js();
            return this;
        }

        /** Wraps using AES-GCM */
        public WrapKeyBuilder withAesGcm(Val iv) {
            this.wrapAlgorithm = "{name:'AES-GCM',iv:" + iv.js() + "}";
            return this;
        }

        /** Wraps using AES-KW */
        public WrapKeyBuilder withAesKw() {
            this.wrapAlgorithm = "{name:'AES-KW'}";
            return this;
        }

        /** Wraps using RSA-OAEP */
        public WrapKeyBuilder withRsaOaep() {
            this.wrapAlgorithm = "{name:'RSA-OAEP'}";
            return this;
        }

        /**
         * Builds the wrapKey operation.
         * Returns a Promise that resolves to an ArrayBuffer.
         */
        public Val build() {
            if (format == null) throw new IllegalStateException("Format not set");
            if (key == null) throw new IllegalStateException("Key not set");
            if (wrappingKey == null) throw new IllegalStateException("Wrapping key not set");
            if (wrapAlgorithm == null) throw new IllegalStateException("Wrap algorithm not set");
            return new Val("crypto.subtle.wrapKey(" + format + "," + key + "," + wrappingKey + "," + wrapAlgorithm + ")");
        }
    }

    /**
     * Builder for crypto.subtle.unwrapKey() operations.
     */
    public static class UnwrapKeyBuilder {
        private String format;
        private String wrappedKey;
        private String unwrappingKey;
        private String unwrapAlgorithm;
        private String unwrappedKeyAlgorithm;
        private boolean extractable;
        private final List<String> keyUsages = new ArrayList<>();

        UnwrapKeyBuilder() {}

        /** Sets the format of the wrapped key */
        public UnwrapKeyBuilder format(String format) {
            this.format = "'" + JS.esc(format) + "'";
            return this;
        }

        /** Unwrap from raw format */
        public UnwrapKeyBuilder raw() {
            return format("raw");
        }

        /** Unwrap from PKCS8 format */
        public UnwrapKeyBuilder pkcs8() {
            return format("pkcs8");
        }

        /** Unwrap from JWK format */
        public UnwrapKeyBuilder jwk() {
            return format("jwk");
        }

        /** Sets the wrapped key data */
        public UnwrapKeyBuilder wrappedKey(Val wrappedKey) {
            this.wrappedKey = wrappedKey.js();
            return this;
        }

        /** Sets the unwrapping key */
        public UnwrapKeyBuilder unwrappingKey(Val unwrappingKey) {
            this.unwrappingKey = unwrappingKey.js();
            return this;
        }

        /** Unwraps using AES-GCM */
        public UnwrapKeyBuilder withAesGcm(Val iv) {
            this.unwrapAlgorithm = "{name:'AES-GCM',iv:" + iv.js() + "}";
            return this;
        }

        /** Unwraps using AES-KW */
        public UnwrapKeyBuilder withAesKw() {
            this.unwrapAlgorithm = "{name:'AES-KW'}";
            return this;
        }

        /** Sets the unwrapped key algorithm */
        public UnwrapKeyBuilder unwrappedAlgorithm(String name) {
            this.unwrappedKeyAlgorithm = "{name:'" + JS.esc(name) + "'}";
            return this;
        }

        /** Unwrapped key will be AES-GCM */
        public UnwrapKeyBuilder unwrappedAesGcm() {
            return unwrappedAlgorithm("AES-GCM");
        }

        /** Sets whether the unwrapped key can be exported */
        public UnwrapKeyBuilder extractable(boolean extractable) {
            this.extractable = extractable;
            return this;
        }

        /** Adds key usages */
        public UnwrapKeyBuilder keyUsages(String... usages) {
            for (String usage : usages) {
                keyUsages.add("'" + JS.esc(usage) + "'");
            }
            return this;
        }

        /**
         * Builds the unwrapKey operation.
         * Returns a Promise that resolves to a CryptoKey.
         */
        public Val build() {
            if (format == null) throw new IllegalStateException("Format not set");
            if (wrappedKey == null) throw new IllegalStateException("Wrapped key not set");
            if (unwrappingKey == null) throw new IllegalStateException("Unwrapping key not set");
            if (unwrapAlgorithm == null) throw new IllegalStateException("Unwrap algorithm not set");
            if (unwrappedKeyAlgorithm == null) throw new IllegalStateException("Unwrapped key algorithm not set");
            if (keyUsages.isEmpty()) throw new IllegalStateException("Key usages not set");
            return new Val("crypto.subtle.unwrapKey(" + format + "," + wrappedKey + "," + unwrappingKey + "," +
                unwrapAlgorithm + "," + unwrappedKeyAlgorithm + "," + extractable + ",[" + String.join(",", keyUsages) + "])");
        }
    }

    // ==================== Encoding Helpers ====================

    /**
     * Encodes text to UTF-8 bytes: new TextEncoder().encode(text)
     *
     * @param text the text to encode
     * @return a Val representing the Uint8Array
     */
    public static Val textEncode(String text) {
        return new Val("new TextEncoder().encode('" + JS.esc(text) + "')");
    }

    /**
     * Encodes text from variable to UTF-8 bytes.
     *
     * @param text the text variable
     * @return a Val representing the Uint8Array
     */
    public static Val textEncode(Val text) {
        return new Val("new TextEncoder().encode(" + text.js() + ")");
    }

    /**
     * Decodes UTF-8 bytes to text: new TextDecoder().decode(bytes)
     *
     * @param bytes the bytes to decode
     * @return a Val representing the decoded string
     */
    public static Val textDecode(Val bytes) {
        return new Val("new TextDecoder().decode(" + bytes.js() + ")");
    }

    /**
     * Decodes bytes with specific encoding.
     *
     * @param bytes the bytes to decode
     * @param encoding the encoding (e.g., "utf-8", "utf-16")
     * @return a Val representing the decoded string
     */
    public static Val textDecode(Val bytes, String encoding) {
        return new Val("new TextDecoder('" + JS.esc(encoding) + "').decode(" + bytes.js() + ")");
    }

    /**
     * Converts ArrayBuffer to Base64 string.
     *
     * @param buffer the ArrayBuffer
     * @return a Val representing the Base64 string
     */
    public static Val arrayBufferToBase64(Val buffer) {
        return new Val("btoa(String.fromCharCode.apply(null,new Uint8Array(" + buffer.js() + ")))");
    }

    /**
     * Converts Base64 string to ArrayBuffer.
     *
     * @param base64 the Base64 string
     * @return a Val representing the ArrayBuffer
     */
    public static Val base64ToArrayBuffer(Val base64) {
        return new Val("Uint8Array.from(atob(" + base64.js() + "),function(c){return c.charCodeAt(0)}).buffer");
    }

    /**
     * Converts hex string to ArrayBuffer.
     *
     * @param hex the hex string
     * @return a Val representing the ArrayBuffer
     */
    public static Val hexToArrayBuffer(Val hex) {
        return new Val("new Uint8Array(" + hex.js() + ".match(/.{1,2}/g).map(function(b){return parseInt(b,16)})).buffer");
    }

    /**
     * Converts ArrayBuffer to hex string.
     *
     * @param buffer the ArrayBuffer
     * @return a Val representing the hex string
     */
    public static Val arrayBufferToHex(Val buffer) {
        return new Val("Array.from(new Uint8Array(" + buffer.js() + ")).map(function(b){return b.toString(16).padStart(2,'0')}).join('')");
    }

    // ==================== Typed Arrays ====================

    /**
     * Creates a new Uint8Array.
     *
     * @param length the array length
     * @return a Val representing the Uint8Array
     */
    public static Val uint8Array(int length) {
        return new Val("new Uint8Array(" + length + ")");
    }

    /**
     * Creates a new Uint8Array from data.
     *
     * @param data the data
     * @return a Val representing the Uint8Array
     */
    public static Val uint8Array(Val data) {
        return new Val("new Uint8Array(" + data.js() + ")");
    }

    /**
     * Creates a new Uint16Array.
     *
     * @param length the array length
     * @return a Val representing the Uint16Array
     */
    public static Val uint16Array(int length) {
        return new Val("new Uint16Array(" + length + ")");
    }

    /**
     * Creates a new Uint32Array.
     *
     * @param length the array length
     * @return a Val representing the Uint32Array
     */
    public static Val uint32Array(int length) {
        return new Val("new Uint32Array(" + length + ")");
    }

    /**
     * Creates a new ArrayBuffer.
     *
     * @param byteLength the buffer length in bytes
     * @return a Val representing the ArrayBuffer
     */
    public static Val arrayBuffer(int byteLength) {
        return new Val("new ArrayBuffer(" + byteLength + ")");
    }
}
