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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

public class JwtDefaultAuthenticationTokenConfigurer implements JwtAuthenticationTokenConfigurer {

    @Override
    public JwtTokenConfigurer retrieveJwtTokenConfigurer(HttpServletRequest request, Authentication auth) {
        return tokenPayload -> {
            tokenPayload.setSubject(((User) auth.getPrincipal()).getUsername());
            tokenPayload.put(JwtService.CREDENTIALS_ATTRIBUTE, auth.getCredentials());
            tokenPayload.put(JwtService.AUTHORITIES_ATTRIBUTE,
                auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        };
    }
}
