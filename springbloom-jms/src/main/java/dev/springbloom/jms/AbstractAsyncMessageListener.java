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

import dev.springbloom.core.exception.ApplicationException;
import dev.springbloom.jms.configuration.JmsAutoConfiguration;
import jakarta.jms.Message;
import jakarta.validation.ConstraintViolationException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;

/**
 * {@link jakarta.jms.MessageListener} responsible for reading messages sent to the standard queue.
 *
 * @see JmsAutoConfiguration#queue(String)
 */
@Slf4j
public abstract class AbstractAsyncMessageListener<M extends AsyncMessage> {

    @SneakyThrows
    @JmsListener(destination = "${jms.default-queue.name}")
    public void onMessage(Message jmsMessage, M message) {
        try {
            log.info("Message '{}' received!", message.getMsgId());
            log.debug("{}", jmsMessage);
            receive(message);
            log.info("Message '{}' received and processed with success!", message.getMsgId());
            jmsMessage.acknowledge();

        } catch (ApplicationException applicationException) {
            // It means is an application (business) exception and there's nothing else to do about it.
            handleApplicationException(jmsMessage, message, applicationException);

        } catch (ConstraintViolationException constraintViolationException) {
            // Thrown when used the @{link jakarta.validation.Valid} annotation.
            StringBuilder validationErrorsMsg = new StringBuilder("Constraint Violations:");
            constraintViolationException.getConstraintViolations().forEach(
                constraintViolation -> validationErrorsMsg
                    .append(System.lineSeparator())
                    .append(constraintViolation.getPropertyPath())
                    .append(":")
                    .append(constraintViolation.getMessage())
            );
            handleApplicationException(jmsMessage, message, new ApplicationException(validationErrorsMsg.toString()));

        } catch (Throwable throwable) {
            throw new JmsApplicationException(jmsMessage, throwable.getMessage(), throwable);
        }
    }

    /**
     * Method responsible for processing the message received from the queue.
     *
     * @param message The message received from the queue.
     * @throws Exception If any error occurs during the processing of the message.
     */
    protected abstract void receive(M message) throws Exception;

    /**
     * Handles an application (business) exception that occurred while processing a message.
     *
     * @param jmsMessage           The JMS message that triggered the exception.
     * @param message              The message being processed when the exception occurred.
     * @param applicationException The application exception that occurred.
     */
    @SuppressWarnings("unused")
    protected void handleApplicationException(
        Message jmsMessage,
        M message,
        ApplicationException applicationException
    ) {
        log.error("Error while processing message '{}': ", message.getMsgId(), applicationException);
    }
}
