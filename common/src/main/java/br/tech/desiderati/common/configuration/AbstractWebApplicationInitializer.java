/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.configuration;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Precisamos definir este {@link SpringBootServletInitializer} pois o Spring Boot
 * somente configura automaticamente o contexto de inicialização do Spring caso usemos a classe
 * {@link org.springframework.boot.SpringApplication} para a execução da aplicação.
 * <p>
 * Utilizar esta configuração sempre que formos usar um servidor de aplicação para distribuição da
 * aplicação. Por exemplo, WildFly.
 * <p>
 * Retirado da documentação: <i>Note that a WebApplicationInitializer is only needed if you are
 * building a war file and deploying it. If you prefer to run an embedded container then you won't
 * need this at all.</i>
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public abstract class AbstractWebApplicationInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder) {
        String externalPropertiesFile = getExternalPropertiesFile();
        if (externalPropertiesFile != null) {
            applicationBuilder.properties("spring.config.location=file:" + externalPropertiesFile);
        }
        return applicationBuilder.sources(getWebApplicationClass());
    }

    protected String getExternalPropertiesFile() {
        return null;
    }

    protected abstract Class<?> getWebApplicationClass();
}
