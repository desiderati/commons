/*
 * Copyright (c) 2023 - Felipe Desiderati
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
package io.herd.common.web.security.jwt.authorization;

import io.herd.common.web.security.UserWithMultiTenantSupport;
import io.herd.common.web.security.jwt.JwtService;
import io.herd.common.web.security.jwt.JwtTokenExtractor;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultJwtTokenExtractor implements JwtTokenExtractor<Authentication> {

    @Override
    @SuppressWarnings("unchecked")
    public Authentication extract(Claims tokenPayload) {
        String username = tokenPayload.getSubject();
        Collection<? extends GrantedAuthority> authorities =
            ((List<String>) tokenPayload.get(JwtService.AUTHORITIES_ATTRIBUTE)).stream()
                .map(SimpleGrantedAuthority::new).collect(Collectors.toList());

        String tenant = (String) tokenPayload.get(JwtService.TENANT_ATTRIBUTE);
        User principal = (tenant != null) ?
            new UserWithMultiTenantSupport(username, "****", authorities, tenant) :
            new User(username, "****", authorities);

        return new UsernamePasswordAuthenticationToken(principal,null, authorities);
    }
}
