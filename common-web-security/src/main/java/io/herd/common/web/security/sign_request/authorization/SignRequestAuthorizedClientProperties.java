/*
 * Copyright (c) 2023 - Felipe Desiderati
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
package io.herd.common.web.security.sign_request.authorization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.map.HashedMap;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter // Never forget to put the setXXX (..) for configuration files!
@Component
@ConfigurationProperties(prefix = "spring.web.security.sign-request.authorization")
@org.springframework.context.annotation.PropertySource(
    value = "classpath:sign-request-authorized-clients.json",
    factory = SignRequestAuthorizedClientProperties.JsonPropertySourceFactory.class)
@ConditionalOnProperty(name = "spring.web.security.sign-request.authorization.enabled", havingValue = "true")
public class SignRequestAuthorizedClientProperties implements SignRequestAuthorizedClientRepository {

    public static class JsonPropertySourceFactory implements PropertySourceFactory {

        @NotNull
        @Override
        public PropertySource<?> createPropertySource(String name,
                                                      EncodedResource resource) throws IOException {
            List<SignRequestAuthorizedClient> authorizedClients =
                new ObjectMapper().readValue(resource.getInputStream(), new TypeReference<>(){});
            Map<String, Object> authorizedClientsProperties = new HashedMap<>();
            authorizedClientsProperties.put("spring.web.security.sign-request.authorization.authorized-clients", authorizedClients);
            return new MapPropertySource("sign-request-authorized-clients", authorizedClientsProperties);
        }
    }

    private List<SignRequestAuthorizedClient> authorizedClients;

    @Override
    public Optional<SignRequestAuthorizedClient> findById(UUID id) {
        return authorizedClients.stream().filter(authorizedClient -> authorizedClient.getId().equals(id)).findAny();
    }
}