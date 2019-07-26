/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.jms;

import br.tech.desiderati.common.jms.configuration.JmsConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Queue;

/**
 * Classe resposável por publicar as mensagens na fila padrão.
 * <p>
 * O Spring consegue fazer a injeção de dependências corretamente,
 * pelo fato do nome do parâmetro ser igual ao nome da fila sendo configurada.
 *
 * @see JmsConfiguration#queue(String)
 */
@Slf4j
@Component
public class MessagePublisher {

    private final JmsTemplate jmsTemplate;

    private final Queue queue;

    @Autowired
    public MessagePublisher(JmsTemplate jmsTemplate, Queue queue) {
        this.jmsTemplate = jmsTemplate;
        this.queue = queue;
    }

    @SuppressWarnings("unused")
    public <M extends Message> void publish(M message) {
        log.info("Publishing message {} ...", message.getId());
        log.debug("{}", message);
        jmsTemplate.convertAndSend(queue, message);
        log.info("Message {} published!", message.getId());
    }
}
