/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
public class RestApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Serializable[] args;

    public RestApiException(String message) {
        super(message);
    }

    public RestApiException(String message, Serializable...args) {
        super(message);
        this.args = args;
    }

    public RestApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public RestApiException(String message, Throwable cause, Serializable...args) {
        super(message, cause);
        this.args = args;
    }
}
