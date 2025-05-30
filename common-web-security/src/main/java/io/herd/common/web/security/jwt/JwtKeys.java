/*
 * Copyright (c) 2025 - Felipe Desiderati
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.herd.common.web.security.jwt;

import lombok.Getter;
import lombok.Setter;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * This class holds the cryptographic keys used for JWT (JSON Web Token) operations.
 * <p>
 * JWT tokens can be signed and verified using either symmetric or asymmetric encryption:
 * <ul>
 *   <li>Symmetric encryption uses a single secret key for both signing and verification</li>
 *   <li>Asymmetric encryption uses a key pair (private key for signing, public key for verification)</li>
 * </ul>
 * <p>
 * The appropriate keys should be configured based on the security requirements of your application.
 * Typically, these keys are loaded from configuration properties or secure key stores.
 */
@Getter
@Setter // Never forget to put the setXXX (...) for configuration files!
public class JwtKeys {

    /**
     * The RSA public key used for verifying JWT tokens when using asymmetric encryption.
     * This key can be distributed to services that need to verify tokens.
     * Typically loaded from a certificate, key file, or configuration.
     */
    private RSAPublicKey publicKey;

    /**
     * The RSA private key used for signing JWT tokens when using asymmetric encryption.
     * This key should be kept secure and not shared publicly.
     * Typically loaded from a keystore or secure configuration.
     */
    private RSAPrivateKey privateKey;

    /**
     * The secret key used for both signing and verifying JWT tokens when using symmetric encryption.
     * This key should be kept secure and shared only with trusted services.
     * For production environments, this should be a strong, randomly generated key.
     */
    private String secretKey;

}
