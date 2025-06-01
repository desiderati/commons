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
package dev.springbloom.logging;

import ch.qos.logback.classic.pattern.ExtendedThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.CoreConstants;

/**
 * <a href="https://stackoverflow.com/questions/53233934/aws-streaming-multi-line-log-files-from-cloudwatch-to-elk">
 * AWS streaming multi-line log files from CloudWatch to ELK
 * </a>
 */
public class AwsMessageConverter extends ExtendedThrowableProxyConverter {

    @Override
    public String convert(ILoggingEvent event) {
        String message;
        if (LoggerUtils.IS_RUNNING_ON_AWS) {
            message = LoggerUtils.replaceNewLineWithCarrierReturn(event.getFormattedMessage());
            message = message.endsWith("\r") ? message : message + "\r";

            IThrowableProxy tp = event.getThrowableProxy();
            if (tp != null) {
                String stackTraceWithoutNewLine =
                    LoggerUtils.replaceNewLineWithCarrierReturn(super.throwableProxyToString(tp));
                return message + "\t|\r\t" + stackTraceWithoutNewLine + " " + CoreConstants.LINE_SEPARATOR;
            } else {
                return message;
            }
        } else {
            message = event.getFormattedMessage();

            IThrowableProxy tp = event.getThrowableProxy();
            if (tp != null) {
                String stackTraceWithoutNewLine = super.throwableProxyToString(tp);
                return message + CoreConstants.LINE_SEPARATOR + stackTraceWithoutNewLine;
            } else {
                return message;
            }
        }
    }
}
