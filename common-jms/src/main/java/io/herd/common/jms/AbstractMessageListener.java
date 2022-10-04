/*
 * Copyright (c) 2022 - Felipe Desiderati
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
package io.herd.common.jms;

import io.herd.common.jms.configuration.JmsAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;

/**
 * {@link javax.jms.MessageListener} responsible for reading messages sent to the standard queue.
 *
 * @see JmsAutoConfiguration#queue(String)
 */
@Slf4j
public abstract class AbstractMessageListener<M extends Message> {

    @JmsListener(destination = "${jms.default-queue.name}")
    public void receive(M message) throws Exception {
        log.info("Message {} received!", message.getId());
        log.debug("{}", message);
        doReceive(message);
        log.info("Message {} received and processed with success!", message.getId());
    }

    @SuppressWarnings({"squid:S00112"}) // Must thrown a generic exception.
    protected abstract void doReceive(M message) throws Exception;
}
