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
package dev.springbloom.web.security.auth.jwt;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.graphql.server.support.BearerTokenAuthenticationExtractor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

/**
 * A strategy for configure/resolving <a href="https://tools.ietf.org/html/rfc6750#section-1.2" target="_blank">Bearer Token</a>s
 * from the authorization header.
 * <p>
 * We have defined this strategy because with this, the user can define how it would configure/resolve
 * the header authorization.
 * For instance, he could use PROXY_AUTHORIZATION instead of {@link HttpHeaders#AUTHORIZATION} as
 * the authorization header name.
 *
 * @see BearerTokenAuthenticationExtractor
 * @see DefaultBearerTokenResolver
 */
public interface JwtAuthenticationHeaderConfigurer {

    /**
     * @return The authentication header name.
     */
    @SuppressWarnings("unused")
    String getAuthorizationHeaderName();

    /**
     * @param jwtToken The JWT Token encoded as String.
     * @return The Bearer Token value.
     */
    String configureBearerToken(String jwtToken);

    /**
     * Resolve the <a href="https://tools.ietf.org/html/rfc6750#section-1.2" target="_blank">Bearer Token</a>
     * value from the authentication header.
     *
     * @return the Bearer Token value or {@code null} if none found
     */
    String resolveBearerToken(String authorizationHeader);

    /**
     * Configure the authorization header in an HttpRequest object with the provided value.
     *
     * @param request             The HttpRequest object to configure the authorization header for.
     * @param authorizationHeader The value to set as the authorization header.
     */
    void configureAuthorizationHeader(HttpRequest request, String authorizationHeader);

    /**
     * Configures the authorization header in a HttpServletResponse object with the provided value.
     *
     * @param response            The HttpServletResponse object to configure the authorization header for.
     * @param authorizationHeader The value to set as the authorization header.
     */
    void configureAuthorizationHeader(HttpServletResponse response, String authorizationHeader);

    /**
     * Resolves the authorization header from the HttpHeaders object.
     *
     * @param headers The HttpHeaders object containing the authorization header.
     * @return The authorization header value extracted from the HttpHeaders object, or null if none found.
     */
    String resolveAuthorizationHeader(HttpHeaders headers);

}
