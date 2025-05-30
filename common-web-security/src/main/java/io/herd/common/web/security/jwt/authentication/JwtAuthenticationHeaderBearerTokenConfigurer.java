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
package io.herd.common.web.security.jwt.authentication;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class JwtAuthenticationHeaderBearerTokenConfigurer implements JwtAuthenticationHeaderConfigurer {

    private static final String BEARER_TOKEN = "Bearer";

    private static final Pattern authorizationPattern =
        Pattern.compile("^" + BEARER_TOKEN + " (?<jwtToken>[a-zA-Z0-9-._~+/]+=*)$", Pattern.CASE_INSENSITIVE);

    private String authorizationHeaderName = HttpHeaders.AUTHORIZATION;

    @Override
    public String configureBearerToken(String jwtToken) {
        return BEARER_TOKEN + " " + jwtToken;
    }

    @Override
    public String resolveBearerToken(String authorizationHeader) {
        if (!StringUtils.startsWithIgnoreCase(authorizationHeader, BEARER_TOKEN.toLowerCase())) {
            return null;
        }

        Matcher matcher = authorizationPattern.matcher(authorizationHeader);
        if (!matcher.matches()) {
            throw new InvalidBearerTokenException("Bearer token is malformed!");
        }
        return matcher.group("jwtToken");
    }

    @Override
    public void configureAuthorizationHeader(HttpServletResponse response, String authorizationHeader) {
        response.addHeader(this.authorizationHeaderName, authorizationHeader);
    }

    @Override
    public void configureAuthorizationHeader(HttpRequest request, String authorizationHeader) {
        request.getHeaders().add(this.authorizationHeaderName, authorizationHeader);
    }

    @Override
    public String resolveAuthorizationHeader(HttpHeaders headers) {
        List<String> authorizationHeader = headers.get(this.authorizationHeaderName);
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return null;
        }
        return authorizationHeader.getFirst();
    }
}
