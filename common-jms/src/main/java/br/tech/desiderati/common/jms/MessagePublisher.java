/*
 * Copyright (c) 2019 - Felipe Desiderati
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
