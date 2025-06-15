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
package dev.springbloom.web.security.auth.sign;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

/**
 * Represents the configuration properties for the sign request client in a Spring-based application.
 * These properties are retrieved from the application configuration file under the
 * "{@code spring.web.security.sign-request.client} prefix.
 * <p>
 * The class uses validation annotations to enforce constraints on its fields and ensures proper
 * configuration for security-related settings like client ID and secret key. The validation annotations
 * are applied to the getter methods due to the requirement of using {@link Validated} for property
 * validation in Spring applications.
 * <p>
 * The {@link SignValidation} interface is used for grouping validation constraints that are applied
 * when validating the configuration properties.
 */
@Getter
@Setter // Never forget to put the setXXX (...) for configuration files!
@Validated
@Component
@ConfigurationProperties("spring.web.security.sign-request.client")
public class SignRequestClientProperties {

    public interface SignValidation {

    }

    private UUID id;
    private String secretKey;

    /**
     * We had to put the validation annotations in the methods instead of the fields, because, when declaring
     * validation rules for a properties file, we need to annotate the referring class with {@link Validated}.
     * <p>
     * If not annotated, a warning message would be printed in the application log: "The @ConfigurationProperties
     * bean class dev.springbloom.web.security.sign.authorization.SignRequestAuthorizationClientProperties
     * contains validation constraints but had not been annotated with @Validated."
     * <p>
     * This way, when we annotate a class with {@link Validated}, Spring Boot will create a Proxy for this class
     * and thus the validation will not work correctly if the validation annotations are not in the methods.
     */
    @NotNull(groups = SignValidation.class)
    public UUID getId() {
        return id;
    }

    @NotBlank(groups = SignValidation.class)
    public String getSecretKey() {
        return secretKey;
    }
}
