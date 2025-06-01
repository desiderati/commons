/*
 * Copyright (c) 2025 - Felipe Desiderati
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
package dev.springbloom.jms;

import dev.springbloom.jms.configuration.JmsAutoConfiguration;
import jakarta.jms.Queue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Class responsible for publishing messages to the standard queue.
 * <p>
 * Spring is able to do the dependency injection correctly, because the parameter name
 * is the same as the name of the queue being configured.
 *
 * @see JmsAutoConfiguration#queue(String)
 */
@Slf4j
@Component
public class AsyncMessagePublisher {

    private final JmsTemplate jmsTemplate;

    private final Queue queue;

    @Autowired
    public AsyncMessagePublisher(JmsTemplate jmsTemplate, Queue queue) {
        this.jmsTemplate = jmsTemplate;
        this.queue = queue;
    }

    @SuppressWarnings("unused")
    public <M extends AsyncMessage> void publish(M message) {
        log.info("Publishing message '{}' ...", message.getMsgId());
        log.debug("{}", message);
        jmsTemplate.convertAndSend(queue, message);
        log.info("Message '{}' published!", message.getMsgId());
    }
}
