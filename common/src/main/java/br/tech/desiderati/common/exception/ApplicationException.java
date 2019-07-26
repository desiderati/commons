/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.exception;

import lombok.Getter;
import org.springframework.core.NestedRuntimeException;

import java.io.Serializable;

@Getter
public class ApplicationException extends NestedRuntimeException {

    private static final long serialVersionUID = 5046859219775779815L;

    private Serializable[] args = null;

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(String message, Serializable...args) {
        super(message);
        this.args = args;
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationException(String message, Throwable cause, Serializable...args) {
        super(message, cause);
        this.args = args;
    }
}
