/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.scanner;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.UUID;

@Getter
@EqualsAndHashCode(of = "id")
@ToString(exclude = "image")
@Document(collection = "qrCodes")
public class QRCode implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private UUID id;

    private String content;

    @Setter(AccessLevel.PACKAGE)
    @Transient
    private byte[] image;

    /**
     * Generates a QRCode with content as UUID.
     */
    QRCode() {
        id = UUID.randomUUID();
        content = id.toString();
    }

    QRCode(String content) {
        id = UUID.randomUUID();
        this.content = content;
    }
}
