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
package io.herd.common.web.security.sign_request.authorization;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SignRequestSigner {

    static final String HEADER_DATE = "Date";
    static final String HEADER_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";

    private final Request request;
    private final HttpServletRequest httpServletRequest;
    private final String secret;

    public String sign() {
        try {
            if (request != null && StringUtils.isNotBlank(secret)) {
                return signString(generateStringToSign(request));
            } else if (httpServletRequest != null && StringUtils.isNotBlank(secret)) {
                return signString(generateStringToSign(httpServletRequest));
            }
        } catch (Exception ex) {
            throw new SignRequestAuthorizationException(
                "An error occurred while signing request: " + ex.getMessage(), ex);
        }

        throw new IllegalStateException(
            "Neither 'request' or 'httpServletRequest' were configured. Or 'secret' was blank!");
    }

    @SneakyThrows(IOException.class)
    private String generateStringToSign(Request request) {
        try (Buffer buffer = new Buffer()) {
            RequestBody body = request.body();
            if (body != null) {
                body.writeTo(buffer);
            }
            return request.method() + "\n" +
                computeRequestBodyHash(buffer.inputStream()) + "\n" +
                request.header(HEADER_DATE);
        }
    }

    @SneakyThrows(IOException.class)
    private String generateStringToSign(HttpServletRequest request) {
        return request.getMethod() + "\n" +
            computeRequestBodyHash(request.getInputStream()) + "\n" +
            request.getHeader(HEADER_DATE);
    }

    /**
     * Instead of signing the whole request content directly, first we need to generate the hash.
     */
    @SneakyThrows({IOException.class, NoSuchAlgorithmException.class})
    private String computeRequestBodyHash(InputStream fis) {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        int n = 0;
        byte[] buffer = new byte[8192];
        while (n != -1) {
            n = fis.read(buffer);
            if (n > 0) {
                digest.update(buffer, 0, n);
            }
        }
        return Base64.getEncoder().encodeToString(digest.digest());
    }

    @SneakyThrows({InvalidKeyException.class, NoSuchAlgorithmException.class})
    private String signString(String stringToSign) {
        SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signingKey);
        return Base64.getEncoder().encodeToString(mac.doFinal(stringToSign.getBytes()));
    }
}
