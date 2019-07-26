/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.configuration;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Configuração usada para carregar todos os arquivos de internacionalização de uma aplicação
 * em um mesmo @{@link java.util.ResourceBundle}.
 */
@Slf4j
@SpringBootConfiguration
@PropertySource("classpath:i18n.properties")
public class I18NConfiguration implements EnvironmentAware {

    private static final String[] i18nDefaultFiles =
        new String[]{"classpath*:i18n/exceptions", "classpath*:i18n/http-exceptions",
            "classpath*:i18n/default-validation-messages", "classpath*:i18n/validation-messages"};

    @Setter
    private Environment environment;

    @Bean
    public MessageSource messageSource() {
        String i18nFilesProperty = environment.getRequiredProperty("i18n.files");
        String[] i18nFiles = null;
        if (StringUtils.isNotBlank(i18nFilesProperty)) {
            i18nFiles = i18nFilesProperty.split(",");
        }
        String[] i18nAllFiles = ArrayUtils.addAll(i18nDefaultFiles, i18nFiles);

        log.info("Loading i18n files: " + Arrays.toString(i18nAllFiles));
        PathMatchingReloadableResourceBundleMessageSource source =
            new PathMatchingReloadableResourceBundleMessageSource();
        //source.setCacheSeconds(300); // Reload messages every 5 minutes
        source.setBasenames(i18nAllFiles);
        source.setDefaultEncoding(StandardCharsets.UTF_8.toString());
        return source;
    }
}
