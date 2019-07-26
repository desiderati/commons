/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.security.jwt;

import io.jsonwebtoken.Claims;

@FunctionalInterface
public interface JwtTokenPayloadConfigurer {

    void configure(Claims tokenPayload);

}
