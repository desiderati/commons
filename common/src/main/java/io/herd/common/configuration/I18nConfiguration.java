/*
 * Copyright (c) 2020 - Felipe Desiderati
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
package io.herd.common.configuration;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Configuração usada para carregar todos os arquivos de internacionalização de uma aplicação
 * em um mesmo @{@link java.util.ResourceBundle}.
 */
@Slf4j
@Configuration
@PropertySource("classpath:i18n.properties")
public class I18nConfiguration implements EnvironmentAware {

    private static final String[] i18nDefaultFiles =
        new String[]{"classpath*:i18n/exceptions", "classpath*:i18n/http-exceptions",
            "classpath*:i18n/default-validation-messages", "classpath*:i18n/validation-messages"};

    @Setter
    private Environment environment;

    @Bean
    @RefreshScope
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
