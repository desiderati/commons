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
package io.herd.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Optional;

public final class LoggerUtils {

    private static final Logger log = LoggerFactory.getLogger("io.herd.common.logging.LoggerUtils");

    public static final boolean IS_RUNNING_ON_AWS = isRunningOnAws();

    private LoggerUtils() {

    }

    public static String replaceNewLineWithCarrierReturn(String text) {
        return text.replaceAll("\r\n", "\r") // DOS
            .replaceAll("\n", "\r"); // Unix
    }

    @SuppressWarnings("unused")
    public static String removeLastChar(String s) {
        return Optional.ofNullable(s)
            .map(str -> str.replaceAll(".$", ""))
            .orElse(s);
    }

    private static boolean isRunningOnAws() {
        return connectTo("EC2", "http://169.254.169.254/latest/meta-data/")
            || connectTo("ECS", "http://169.254.170.2/v2/metadata/");
    }

    private static boolean connectTo(String cloud, String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();
            connection.setConnectTimeout(1000 * 2);
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.connect();
            log.debug("Retrieving AWS metadata information...");
            log.debug("Response Code = {}", connection.getResponseCode());

            try (InputStream inputStream = connection.getInputStream()) {
                StringBuilder buffer = new StringBuilder();
                while (true) {
                    int c = inputStream.read();
                    if (c == -1) {
                        break;
                    }
                    buffer.append((char) c);
                }
                log.debug("Response Body = {}", buffer);
            }
            return connection.getResponseCode() == HttpURLConnection.HTTP_OK;

        } catch (Exception ex) {
            String logMessage =
                "It wasn't possible verify if the application is running inside the AWS " + cloud + " Cloud.";
            if (!log.isDebugEnabled()) {
                log.info(logMessage);
            }
            log.debug(logMessage, ex);
            return false;
        }
    }
}
