/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.exception;

import java.io.Serializable;

/**
 *  Esta exceção será tratada como um @{@link org.springframework.http.HttpStatus#NOT_FOUND}.
 */
@SuppressWarnings("unused")
public class ResourceNotFoundApplicationException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundApplicationException(String message) {
        super(message);
    }

    public ResourceNotFoundApplicationException(String message, Serializable... args) {
        super(message, args);
    }

    public ResourceNotFoundApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceNotFoundApplicationException(String message, Throwable cause, Serializable... args) {
        super(message, cause, args);
    }
}
