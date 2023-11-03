package io.herd.common.web.security.jwt.authentication;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * This class should be used when we define a {@link AuthenticationProvider} which is responsible for calling
 * another system responsible for the authentication.
 */
public class JwtDelegateAuthenticationToken extends UsernamePasswordAuthenticationToken {

    public String authorizationHeader;

    public JwtDelegateAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public JwtDelegateAuthenticationToken(
        Object principal,
        Object credentials,
        Collection<? extends GrantedAuthority> authorities
    ) {
        super(principal, credentials, authorities);
    }
}
