/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.security.jwt;

import io.jsonwebtoken.Claims;

public interface JwtTokenPayloadExtractor<T> {

    T extract(Claims tokenPayload);

}
