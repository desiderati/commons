/*
 * Copyright (c) 2022 - Felipe Desiderati
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

import io.jsonwebtoken.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Service
public class JwtService {

    public static final String AUTHORITIES_ATTRIBUTE = "authorities";
    public static final String TENANT_ATTRIBUTE = "tenant";

    private final JwtProperties jwtProperties;
    private final JwtEncryptionMethod jwtEncryptionMethod;
    private final int expirationPeriod;

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final PrivateKey privateKey = loadPrivateKey();

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final PublicKey publicKey = loadPublicKey();

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final SecretKey secretKey = loadSecretKey();

    /**
     * The parameters "jwtEncryptionMethod" and "expirationPeriod" where defined using
     * the constructor instead of the direct wired, because we may create the JwtService
     * without using the Spring Framework auto-injection.
     */
    @Autowired
    public JwtService(
        JwtProperties jwtProperties,
        @Value("${spring.web.security.jwt.authentication.encryption-method:asymmetric}") JwtEncryptionMethod jwtEncryptionMethod,
        @Value("${spring.web.security.jwt.authentication.expiration-period:1}") int expirationPeriod
    ) {
        this.jwtProperties = jwtProperties;
        this.jwtEncryptionMethod = jwtEncryptionMethod;
        this.expirationPeriod = expirationPeriod;
    }

    private PrivateKey loadPrivateKey() {
        assert jwtProperties != null;
        try {
            if (StringUtils.isBlank(jwtProperties.getPrivateKey())) {
                throw new IllegalStateException("Empty private key.");
            }
            byte[] encoded = Base64.getDecoder().decode(jwtProperties.getPrivateKey());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new JwtException("Unable to load private key.", e);
        }
    }

    private PublicKey loadPublicKey() {
        assert jwtProperties != null;
        try {
            if (StringUtils.isBlank(jwtProperties.getPublicKey())) {
                throw new IllegalStateException("Empty public key.");
            }
            byte[] encodedPublicKey = Base64.getDecoder().decode(jwtProperties.getPublicKey());
            X509EncodedKeySpec pkeySpec = new X509EncodedKeySpec(encodedPublicKey);
            return KeyFactory.getInstance("RSA").generatePublic(pkeySpec);
        } catch (Exception e) {
            throw new JwtException("Unable to load public key.", e);
        }
    }

    private SecretKey loadSecretKey() {
        assert jwtProperties != null;
        try {
            if (StringUtils.isBlank(jwtProperties.getSecretKey())) {
                throw new IllegalStateException("Empty secret key.");
            }
            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
            return new SecretKeySpec(jwtProperties.getSecretKey().getBytes(), signatureAlgorithm.getJcaName());
        } catch (Exception e) {
            throw new JwtException("Unable to load public key.", e);
        }
    }

    public String generateToken(JwtTokenConfigurer configurer) {
        Claims tokenPayload = Jwts.claims();
        configurer.configure(tokenPayload);
        tokenPayload.setExpiration(DateTime.now().plusHours(expirationPeriod).toDate());

        JwtBuilder builder = Jwts.builder().setClaims(tokenPayload);
        if (jwtEncryptionMethod == JwtEncryptionMethod.ASYMMETRIC) {
            builder.signWith(SignatureAlgorithm.RS256, getPrivateKey());
        } else if (jwtEncryptionMethod == JwtEncryptionMethod.SYMMETRIC) {
            builder.signWith(SignatureAlgorithm.HS256, getSecretKey());
        } else {
            throw new IllegalStateException("Invalid encryption method! This exception should not happen at all.");
        }
        return builder.compact();
    }

    public <T> T extractTokenPayload(String token, JwtTokenExtractor<T> extractor) {
        JwtParser parser = Jwts.parser();
        if (jwtEncryptionMethod == JwtEncryptionMethod.ASYMMETRIC) {
            parser.setSigningKey(getPublicKey());
        } else if (jwtEncryptionMethod == JwtEncryptionMethod.SYMMETRIC) {
            parser.setSigningKey(getSecretKey());
        } else {
            throw new IllegalStateException("Invalid encryption method! This exception should not happen at all.");
        }

        Claims tokenPayload = parser.parseClaimsJws(token).getBody();
        return extractor.extract(tokenPayload);
    }
}
