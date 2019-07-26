/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.configuration;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.io.File;
import java.nio.file.Paths;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE) // Primeiro a ser executado!
public class ExternalPropertiesFileValidationInitializer implements ApplicationContextInitializer {

    @Override
    public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
        String externalProperties = applicationContext.getEnvironment().getProperty("spring.config.location");
        if (externalProperties != null) {
            externalProperties = externalProperties.replace("file:", "");
            File externalPropertiesFile = Paths.get(externalProperties).toFile();
            if (externalPropertiesFile.exists() && externalPropertiesFile.isFile()) {
                log.info("\n \nLoading external properties file:" + externalPropertiesFile + "\n");
            }
        }
    }
}
