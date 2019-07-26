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
import org.atmosphere.config.managed.Encoder;

@Slf4j
public class NotificationJacksonEncoder implements Encoder<Notification, String> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String encode(Notification object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception ex) {
            String errorMsg = "Unable to serialize JSON object: + " + object;
            log.error(errorMsg);
            log.debug(ex.getMessage(), ex);
            throw new ApplicationException(errorMsg);
        }
    }
}
