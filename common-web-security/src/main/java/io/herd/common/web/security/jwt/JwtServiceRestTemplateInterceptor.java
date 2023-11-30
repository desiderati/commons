package io.herd.common.web.security.jwt;

import io.herd.common.web.security.jwt.authorization.JwtAuthorizationService;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

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
