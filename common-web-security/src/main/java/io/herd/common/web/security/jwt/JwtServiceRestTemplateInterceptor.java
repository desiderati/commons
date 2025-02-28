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
package io.herd.common.web.security.jwt;

import io.herd.common.web.security.jwt.authorization.JwtAuthorizationService;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

/**
 * We had to use a {@link RestTemplate} instead of a {@link RestClient}, because with {@link RestTemplate}
 * we can configure the interceptors after the object creation.
 */
public class JwtServiceRestTemplateInterceptor implements JwtServiceInterceptor {

    private final RestTemplate restTemplate;

    public JwtServiceRestTemplateInterceptor(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void onBeforeExtractPayload(String token) {
        final Map<String, String> headers = new HashMap<>();
        headers.put(JwtAuthorizationService.HEADER_AUTHORIZATION, JwtAuthorizationService.TOKEN_BEARER + token);

        restTemplate.getInterceptors().clear();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                request.getHeaders().add(header.getKey(), header.getValue());
            }
            return execution.execute(request, body);
        });
    }
}
