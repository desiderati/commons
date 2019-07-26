/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.test.annotation;

import br.tech.desiderati.common.test.WithMockJwtUserSecurityContextFactory;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Adiciona suporte à autenticação durante a execução dos testes. Será necessário anotar a classe ou métodos
 * contendo os testes, indicando assim o usuário que está sendo autenticado.
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockJwtUserSecurityContextFactory.class)
public @interface WithMockJwtUser {

    String username() default "admin";

    /**
     * Sempre com o prefixo "ROLE_".
     */
    String role() default "ROLE_ADMIN";

}
