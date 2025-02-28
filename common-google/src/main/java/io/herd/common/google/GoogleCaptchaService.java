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
package io.herd.common.google;

import io.herd.common.google.configuration.GoogleCaptchaProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Slf4j
public class GoogleCaptchaService {

    private final GoogleCaptchaProperties captchaProperties;

    public GoogleCaptchaService(GoogleCaptchaProperties captchaProperties) {
        this.captchaProperties = captchaProperties;
    }

    @SuppressWarnings("unused")
    public boolean isCaptchaValid(String recaptchaResponse) {
        recaptchaResponse = StringUtils.isBlank(recaptchaResponse)
            ? ""
            : new String(recaptchaResponse.getBytes(StandardCharsets.ISO_8859_1));

        return verify(recaptchaResponse);
    }

    private boolean verify(String recaptchaResponse) {
        if (recaptchaResponse == null || recaptchaResponse.isEmpty()) {
            return false;
        }

        for (int retry = 0; retry < captchaProperties.getMaxRetries(); retry++) {
            try {
                return innerVerify(recaptchaResponse);
            } catch (Exception ex) {
                log.warn(
                    "Error while calling Google Recaptcha! Retrying ({}/{})...",
                    retry + 1,
                    captchaProperties.getMaxRetries(),
                    ex
                );
            }
        }

        log.error("Error while calling Google Recaptcha! Max retries achieved!");
        return false;
    }

    private boolean innerVerify(String recaptchaResponse) throws IOException, JSONException, URISyntaxException {
        HttpsURLConnection conn = callWebService(recaptchaResponse, getUrlConnection());
        try (
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(conn.getInputStream()))
        ) {
            log.info("Retrieving response...");
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
            JSONObject jsonObject = new JSONObject(responseStrBuilder.toString());

            log.info("Google Recaptcha called with success!");
            return (Boolean) jsonObject.get("success");
        }
    }

    @NotNull
    private HttpsURLConnection getUrlConnection() throws IOException, URISyntaxException {
        URL verifyUrl = new URI(captchaProperties.getSiteVerifyUrl()).toURL();
        HttpsURLConnection conn = (HttpsURLConnection) verifyUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", captchaProperties.getUserAgent());
        conn.setRequestProperty("Accept-Language", captchaProperties.getAcceptLanguage());
        conn.setConnectTimeout(captchaProperties.getTimeout());
        conn.setDoOutput(true);
        return conn;
    }

    private HttpsURLConnection callWebService(
        String recaptchaResponse,
        HttpsURLConnection urlConnection
    ) throws IOException {
        try (OutputStream outStream = urlConnection.getOutputStream()) {
            log.info("Calling Google Recaptcha...");
            String postParams = "secret=" + captchaProperties.getSecretKey() + "&response=" + recaptchaResponse;
            outStream.write(postParams.getBytes());
            outStream.flush();
        }
        return urlConnection;
    }
}
