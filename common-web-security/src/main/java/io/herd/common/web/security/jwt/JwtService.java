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

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * It will be responsible for encoding and decoding JWT tokens.
 */
@Slf4j
public class JwtService {

    private NimbusJwtEncoder jwtEncoder;
    private final NimbusJwtDecoder jwtDecoder;

    private final JwtKeys jwtKeys;
    private final Converter<Jwt, ?> jwtConverter;
    private final JwtEncryptionMethod jwtEncryptionMethod;
    private final int expirationPeriod;

    public JwtService(
        JwtKeys jwtKeys,
        Converter<Jwt, ?> jwtConverter,
        JwtEncryptionMethod jwtEncryptionMethod,
        int expirationPeriod
    ) {
        assert jwtKeys != null;
        this.jwtKeys = jwtKeys;
        this.jwtConverter = jwtConverter;

        if (jwtEncryptionMethod == JwtEncryptionMethod.ASYMMETRIC) {
            RSAPublicKey publicKey = loadPublicKey();
            this.jwtDecoder = NimbusJwtDecoder.withPublicKey(loadPublicKey())
                // TODO Felipe Desiderati: Pegar os algoritmos da configuração do Resource Server.
                .signatureAlgorithm(SignatureAlgorithm.RS512)
                .build();
            jwtDecoder.setJwtValidator(JwtValidators.createDefault());

            RSAPrivateKey privateKey = loadPrivateKey();
            if (privateKey != null) {
                RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
                this.jwtEncoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
            }
        } else if (jwtEncryptionMethod == JwtEncryptionMethod.SYMMETRIC) {
            SecretKey secretKey = loadSecretKey();
            this.jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey).build();
            this.jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));

        } else {
            throw new IllegalStateException("Invalid authentication method! This exception should not happen at all.");
        }

        this.jwtEncryptionMethod = jwtEncryptionMethod;
        this.expirationPeriod = expirationPeriod;
    }

    private RSAPrivateKey loadPrivateKey() {
        return jwtKeys.getPrivateKey();
    }

    private RSAPublicKey loadPublicKey() {
        if (jwtKeys.getPublicKey() == null) {
            throw new IllegalStateException("Empty public key.");
        }
        return jwtKeys.getPublicKey();
    }

    private SecretKey loadSecretKey() {
        try {
            if (StringUtils.isBlank(jwtKeys.getSecretKey())) {
                throw new IllegalStateException("Empty secret key.");
            }
            return Keys.hmacShaKeyFor(jwtKeys.getSecretKey().getBytes());
        } catch (Exception e) {
            throw new JwtException("Unable to load public key.", e);
        }
    }

    private MacAlgorithm getSecretKetMacAlgorithm(byte[] bytes) {
        int bitLength = bytes.length * 8;

        // Purposefully ordered higher to lower to ensure the strongest key possible can be generated.
        if (bitLength >= 512) {
            return MacAlgorithm.HS512;
        } else if (bitLength >= 384) {
            return MacAlgorithm.HS384;
        } else if (bitLength >= 256) {
            return MacAlgorithm.HS256;
        }

        throw new IllegalStateException("The specified key byte array is " + bitLength + " bits which " +
            "is not secure enough for any JWT HMAC-SHA algorithm! " +
            "This exception should not happen at all.");
    }

    public String generateToken(JwtClaimsConfigurer configurer) {
        Instant now = Instant.now();
        JwtClaimsSet claims = configurer.configure(
            JwtClaimsSet.builder()
                .issuedAt(Instant.now())
                .expiresAt(now.plus(expirationPeriod > 0 ? expirationPeriod : Integer.MAX_VALUE, ChronoUnit.HOURS))
        );

        if (jwtEncryptionMethod == JwtEncryptionMethod.ASYMMETRIC) {
            if (jwtEncoder == null) {
                throw new IllegalStateException(
                    "JWT Encoder not configured! Probably because JWT private key is empty."
                );
            }

            return jwtEncoder.encode(
                JwtEncoderParameters.from(
                    // TODO Felipe Desiderati: Pegar os algoritmos da configuração do Resource Server.
                    JwsHeader.with(SignatureAlgorithm.RS512).build(),
                    claims
                )
            ).getTokenValue();
        } else if (jwtEncryptionMethod == JwtEncryptionMethod.SYMMETRIC) {
            return jwtEncoder.encode(
                JwtEncoderParameters.from(
                    JwsHeader.with(getSecretKetMacAlgorithm(jwtKeys.getSecretKey().getBytes())).build(),
                    claims
                )
            ).getTokenValue();
        } else {
            throw new IllegalStateException("Invalid authentication method! This exception should not happen at all.");
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T extractFromToken(String token) {
        return (T) jwtConverter.convert(getJwt(token));
    }

    private Jwt getJwt(String token) {
        try {
            return this.jwtDecoder.decode(token);
        } catch (Exception failed) {
            throw new JwtException(failed.getMessage(), failed);
        }
    }
}
