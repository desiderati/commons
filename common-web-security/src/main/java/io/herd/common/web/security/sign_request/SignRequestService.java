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
package io.herd.common.web.security.sign_request;

import io.herd.common.exception.ApplicationException;
import io.herd.common.validation.ValidationUtils;
import io.herd.common.web.security.sign_request.SignRequestClientProperties.SignValidation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static io.herd.common.web.security.sign_request.SignRequestSigner.HEADER_DATE_FORMAT;

/**
 * Service class responsible for handling the signing and verification of requests.
 * This is typically used for ensuring secure communication between clients and
 * APIs by signing requests with a secret key and validating these signatures.
 * <p>
 * The service can handle signing and verification for HTTP requests using both custom
 * and Spring's request models.
 * <p>
 * It provides mechanisms to validate authorization, ensure request integrity,
 * and add the necessary headers for secure communication.
 */
@Slf4j
@Service
public class SignRequestService {

    private static final String HEADER_AUTHORIZATION = HttpHeaders.AUTHORIZATION;
    private static final String SIGNED_REQUEST_TOKEN = "SignedRequest";

    private static final int VALID_TIME_WINDOW = 15; // Minutes
    private static final String DEFAULT_AUTHORIZATION_ERROR_MSG = "Unable to authorize the request due to: ";

    private final SignRequestClientProperties signRequestAuthorizationClientProperties;

    private final SignRequestAuthorizationClientRepository signRequestAuthorizedClientRepository;

    public SignRequestService(
        SignRequestClientProperties signRequestAuthorizationClientProperties,
        @Autowired(required = false) SignRequestAuthorizationClientRepository signRequestAuthorizedClientRepository
    ) {
        this.signRequestAuthorizationClientProperties = signRequestAuthorizationClientProperties;
        this.signRequestAuthorizedClientRepository = signRequestAuthorizedClientRepository;
    }

    /**
     * Method responsible for signing the request using API Secret and API ID. Used commonly by the Open API Clients.
     */
    @SuppressWarnings("unused")
    public Request sign(Request request) {
        try {
            ValidationUtils.Companion.validate(signRequestAuthorizationClientProperties, SignValidation.class);

            SimpleDateFormat dateFormat = new SimpleDateFormat(HEADER_DATE_FORMAT, Locale.US);
            Request newRequest = request.newBuilder()
                .addHeader(HttpHeaders.DATE, dateFormat.format(new Date()))
                .build();

            String sign = SignRequestSigner.builder()
                .request(newRequest)
                .secret(signRequestAuthorizationClientProperties.getSecretKey())
                .build()
                .sign();

            newRequest = newRequest.newBuilder()
                .addHeader(
                    HEADER_AUTHORIZATION,
                    SIGNED_REQUEST_TOKEN + " " + signRequestAuthorizationClientProperties.getId() + ":" + sign
                )
                .build();
            return newRequest;
        } catch (Exception e) {
            throw new AuthenticationServiceException("Unable to sign the request due to: " + e.getMessage(), e);
        }
    }

    /**
     * Method responsible for signing the request using API Secret and API ID.
     * This version works with Spring's HttpRequest.
     */
    @SuppressWarnings("unused")
    public HttpRequest sign(HttpRequest httpRequest, byte[] body) {
        try {
            ValidationUtils.Companion.validate(signRequestAuthorizationClientProperties, SignValidation.class);

            SimpleDateFormat dateFormat = new SimpleDateFormat(HEADER_DATE_FORMAT, Locale.US);
            httpRequest.getHeaders().add(HttpHeaders.DATE, dateFormat.format(new Date()));

            String sign = SignRequestSigner.builder()
                .httpRequest(httpRequest)
                .secret(signRequestAuthorizationClientProperties.getSecretKey())
                .build()
                .sign(body);

            httpRequest.getHeaders().add(
                HEADER_AUTHORIZATION,
                SIGNED_REQUEST_TOKEN + " " + signRequestAuthorizationClientProperties.getId() + ":" + sign
            );

            return httpRequest;
        } catch (Exception e) {
            throw new AuthenticationServiceException("Unable to sign the request due to: " + e.getMessage(), e);
        }
    }

    /**
     * Checks whether the current requisition is authorized to be executed. If so, the application (client)
     * accessing this API is added to the current request with the appropriate permissions.
     */
    public Authentication verifySignature(HttpServletRequest request) {
        try {
            final String auth = request.getHeader(HEADER_AUTHORIZATION);
            if (auth != null && auth.startsWith(SIGNED_REQUEST_TOKEN) && verifyDate(request)) {
                String[] authArr = auth.replace(SIGNED_REQUEST_TOKEN + " ", "").split(":");

                final String keyId = authArr[0];
                final String sign = authArr[1];
                final SignRequestAuthorizationClient authorizedClient =
                    signRequestAuthorizedClientRepository.findById(UUID.fromString(keyId))
                        .orElseThrow(() -> new ApplicationException("API Key not registered for Id: " + keyId));

                final String verifySign = SignRequestSigner.builder().httpServletRequest(request)
                    .secret(authorizedClient.getSecretKey()).build().sign();
                if (verifySign.equals(sign)) {
                    List<GrantedAuthority> grantedAuthorities =
                        AuthorityUtils.createAuthorityList(
                            authorizedClient.getRoles().stream().map(role -> "ROLE_" + role).toArray(String[]::new));

                    return new UsernamePasswordAuthenticationToken(
                        authorizedClient, null, grantedAuthorities
                    );
                } else {
                    log.warn(DEFAULT_AUTHORIZATION_ERROR_MSG + "Invalid signed request!");
                }
            }
            return null;
        } catch (Exception e) {
            throw new AuthenticationServiceException(DEFAULT_AUTHORIZATION_ERROR_MSG + e.getMessage(), e);
        }
    }

    private boolean verifyDate(HttpServletRequest request) {
        if (request.getHeader(HttpHeaders.DATE) == null) {
            log.warn(DEFAULT_AUTHORIZATION_ERROR_MSG + "Request date header not informed!");
            return false;
        }

        ZonedDateTime currentDate = ZonedDateTime.now();
        ZonedDateTime requestDate =
            ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(request.getDateHeader(HttpHeaders.DATE)), ZoneId.of("UTC")
            );

        boolean dateOutOfRange = requestDate.isBefore(currentDate.minusMinutes(VALID_TIME_WINDOW))
            || requestDate.isAfter(currentDate.plusMinutes(VALID_TIME_WINDOW));
        if (dateOutOfRange) {
            log.warn(DEFAULT_AUTHORIZATION_ERROR_MSG + "Request out of date! Current time: {}, Request Date: {}",
                currentDate,
                requestDate
            );
        }
        return !dateOutOfRange;
    }
}
