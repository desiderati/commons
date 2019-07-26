/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.exception;

import java.io.Serializable;

/**
 * Não usamos a classe @{@link IllegalArgumentException} pois a mesma não
 * aceita mensagens internacionalizadas.
 * Esta exceção será tratada como um @{@link org.springframework.http.HttpStatus#NOT_ACCEPTABLE}.
 */
@SuppressWarnings("unused")
public class IllegalArgumentApplicationException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public IllegalArgumentApplicationException(String message) {
        super(message);
    }

    public IllegalArgumentApplicationException(String message, Serializable... args) {
        super(message, args);
    }

    public IllegalArgumentApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalArgumentApplicationException(String message, Throwable cause, Serializable... args) {
        super(message, cause, args);
    }
}
