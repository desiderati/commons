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
package io.herd.common.security.jwt.authentication;

import io.herd.common.security.jwt.JwtService;
import io.herd.common.security.jwt.JwtTokenConfigurer;
import io.herd.common.security.MultiTenantSupport;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

public class DefaultJwtAuthenticationTokenConfigurer implements JwtAuthenticationTokenConfigurer {

    @Override
    @SuppressWarnings("squid:S1905") // Unnecessary cast
    public JwtTokenConfigurer retrieveJwtTokenConfigurer(HttpServletRequest request, Authentication authentication) {
        return tokenPayload -> {
            tokenPayload.setSubject(((User) authentication.getPrincipal()).getUsername());
            tokenPayload.put(JwtService.AUTHORITIES_ATTRIBUTE,
                authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

            if (authentication.getPrincipal() instanceof MultiTenantSupport) {
                MultiTenantSupport multiTenantSupport = (MultiTenantSupport) authentication.getPrincipal();
                tokenPayload.put(JwtService.TENANT_ATTRIBUTE, multiTenantSupport.getTenant());
            }
        };
    }
}
