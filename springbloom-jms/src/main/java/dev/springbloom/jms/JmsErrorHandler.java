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

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.ErrorHandler;

@Slf4j
public class JmsErrorHandler implements ErrorHandler {

    private final Integer maxDeliveryAttempts;

    public JmsErrorHandler(Integer maxDeliveryAttempts) {
        this.maxDeliveryAttempts = maxDeliveryAttempts;
    }

    @Override
    public void handleError(@NotNull Throwable throwable) {
        try {
            if (throwable.getCause() != null && throwable.getCause() instanceof JmsApplicationException exception) {
                Message message = exception.getFailedMessage();
                int deliveryCount = message.getIntProperty("JMSXDeliveryCount");
                if (deliveryCount >= maxDeliveryAttempts) {
                    handleLastError(exception.getCause());
                } else {
                    log.warn(
                        "An error occurred while processing JMS message [{}/{}]!",
                        deliveryCount,
                        maxDeliveryAttempts,
                        throwable
                    );
                }
            }
        } catch (JMSException ex) {
            log.error("It was not possible to extract the delivery count from the message!", ex);
        }
    }

    protected void handleLastError(@NotNull Throwable throwable) {
        log.error(
            "All attempts [{}/{}] to process the message have been exhausted!",
            maxDeliveryAttempts,
            maxDeliveryAttempts,
            throwable
        );
    }
}
