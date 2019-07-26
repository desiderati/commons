/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.jms;

import br.tech.desiderati.common.jms.configuration.JmsConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;

/**
 * Classe equivalente a uma implementação de {@link javax.jms.MessageListener} a qual é resposável por ler
 * as mensagens enviadas para a fila padrão.
 *
 * @see JmsConfiguration#queue(String)
 */
@Slf4j
@SuppressWarnings("unused")
public abstract class AbstractMessageListener<M extends Message> {

    @JmsListener(destination = "${jms.default-queue.name}")
    public void recieve(M message) throws Exception {
        log.info("Message {} received!", message.getId());
        log.debug("{}", message);
        doReceive(message);
        log.info("Message {} received and processed with success!", message.getId());
    }

    protected abstract void doReceive(M message) throws Exception;
}
