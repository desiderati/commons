/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.jms;

import lombok.Getter;

import java.util.UUID;

@Getter
public abstract class Message {

    private UUID id;

    public Message() {
        id = UUID.randomUUID();
    }
}
