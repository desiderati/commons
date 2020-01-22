/*
 * Copyright (c) 2020 - Felipe Desiderati
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
package io.herd.common.security.jwt.authorization;

import io.herd.common.security.jwt.JwtService;
import io.herd.common.security.jwt.JwtTokenExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Service
@ConditionalOnProperty(name = "security.jwt.authorization.enabled", havingValue = "true")
public class JwtAuthorizationService {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String TOKEN_BEARER = "Bearer ";

    private final JwtService jwtService;
    private final JwtTokenExtractor<Authentication> jwtTokenExtractor;

    @Autowired
    public JwtAuthorizationService(JwtService jwtService,
                                   JwtTokenExtractor<Authentication> jwtTokenExtractor) {
        this.jwtService = jwtService;
        this.jwtTokenExtractor = jwtTokenExtractor;
    }

    /**
     * Checks whether the current request has the authentication Token (JWT). If so, the authenticated user
     * (contained inside the TOKEN) is added to the current request with the appropriate permissions.
     */
    public Authentication verifyAuthentication(HttpServletRequest request) {
        try {
            return getAuthenticationFromTokenPayload(request);
        } catch (Exception e) {
            throw new AuthenticationServiceException("Unable to authorize the request due to: " + e.getMessage(), e);
        }
    }

    /**
     * Extract the authenticated user (contained inside the TOKEN).
     */
    private Authentication getAuthenticationFromTokenPayload(HttpServletRequest request) {
        String auth = request.getHeader(HEADER_AUTHORIZATION);
        if (auth != null && auth.startsWith(TOKEN_BEARER)) {
            return jwtService.extractTokenPayload(
                auth.replace(TOKEN_BEARER, ""), jwtTokenExtractor);
        }
        return null;
    }
}
