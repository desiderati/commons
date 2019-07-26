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
import java.util.UUID;

@Getter
@NoArgsConstructor
public class ResponseExceptionDTO implements Serializable {

    /**
     * Apenas para termos total e plena certeza ao deserializar (JSON) esta classe,
     * saberemos que é realmente ela que estamos tratando.
     */
    private String type = ResponseExceptionDTO.class.getName();

    private UUID errorId;

    private String errorCode;

    private String message;

    private int status;

    ResponseExceptionDTO(UUID errorId, String errorCode, String message, int status) {
        this.errorId = errorId;
        this.errorCode = errorCode;
        this.message = message;
        this.status = status;
    }
}
