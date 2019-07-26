/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serializable;

@SuppressWarnings("unused")
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictRestApiException extends RestApiException {

    private static final long serialVersionUID = 1L;

    public ConflictRestApiException() {
    }

    public ConflictRestApiException(String message) {
        super(message);
    }

    public ConflictRestApiException(String message, Serializable... args) {
        super(message, args);
    }

    public ConflictRestApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConflictRestApiException(String message, Throwable cause, Serializable... args) {
        super(message, cause, args);
    }
}
