/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class ValidationResponseExceptionDTO extends ResponseExceptionDTO {

    private String[] validationMessages;

    ValidationResponseExceptionDTO(UUID errorId, String errorCode, String message, int status,
                                   String[] validationMessages) {

        super(errorId, errorCode, message, status);
        this.validationMessages = validationMessages;
    }
}
