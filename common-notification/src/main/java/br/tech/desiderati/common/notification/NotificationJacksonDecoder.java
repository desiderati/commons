/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.notification;

import br.tech.desiderati.common.exception.ApplicationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.atmosphere.config.managed.Decoder;

@Slf4j
public class NotificationJacksonDecoder implements Decoder<String, Notification> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Notification decode(String string) {
        try {
            return mapper.readValue(string, Notification.class);
        } catch (Exception ex) {
            String errorMsg = "Unable to deserialize JSON object: + " + string;
            log.error(errorMsg);
            log.debug(ex.getMessage(), ex);
            throw new ApplicationException(errorMsg);
        }
    }
}
