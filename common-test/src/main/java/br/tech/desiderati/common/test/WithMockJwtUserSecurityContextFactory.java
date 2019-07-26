/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.test;

import br.tech.desiderati.common.test.annotation.WithMockJwtUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

/**
 * Adiciona suporte à autenticação durante a execução dos testes. Será necessário anotar a classe
 * contendo os testes da seguinte forma:
 * <pre>
 * &#064;TestExecutionListeners({WithSecurityContextTestExecutionListener.class, ...})
 * public class MyTestClass extends AbstractTestNGSpringContextTests {
 *    ...
 * }</pre>
 *
 * Veja mais em: https://docs.spring.io/spring-security/site/docs/4.0.x/reference/htmlsingle/#test-method-withsecuritycontext
 */
public class WithMockJwtUserSecurityContextFactory implements WithSecurityContextFactory<WithMockJwtUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockJwtUser jwtUser) {
        Authentication auth =
            new UsernamePasswordAuthenticationToken(jwtUser.username(),
                null, AuthorityUtils.createAuthorityList(jwtUser.role()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        return context;
    }
}
