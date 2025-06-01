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
package dev.springbloom.core.configuration;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Configuration used to load all application internationalization files in the same {@link java.util.ResourceBundle}.
 */
@Slf4j
@Setter
@Configuration(proxyBeanMethods = false)
@PropertySource("classpath:i18n.properties")
@Import({
    // Spring Cloud uses RefreshAutoConfiguration to add the refresh scope to your application.
    // By default, this auto-configuration isn't included in the auto-configuration
    // enabled by @WebMvcTest. You can enable extra auto-configuration by adding
    // @ImportAutoConfiguration(RefreshAutoConfiguration.class) to your tests.
    RefreshAutoConfiguration.class
})
public class I18nConfiguration {

    private static final String[] i18nDefaultFiles =
        new String[]{"classpath*:i18n/exceptions", "classpath*:i18n/http-exceptions", "classpath*:i18n/templates",
            "classpath*:i18n/default-validation-messages", "classpath*:i18n/validation-messages"};

    @Bean
    @RefreshScope
    public MessageSource messageSource(
        @Value("${i18n.files}") String i18nFilesProperty,
        @Value("${i18n.files.encoding:ISO-8859-1}") String i18nFilesEncoding
    ) {
        String[] i18nFiles = null;
        if (StringUtils.isNotBlank(i18nFilesProperty)) {
            i18nFiles = i18nFilesProperty.split(",");
        }
        String[] i18nAllFiles = ArrayUtils.addAll(i18nDefaultFiles, i18nFiles);

        log.info("Loading i18n files: {}", Arrays.toString(i18nAllFiles));
        PathMatchingReloadableResourceBundleMessageSource source =
            new PathMatchingReloadableResourceBundleMessageSource();
        source.setCacheSeconds(300); // Reload messages every 5 minutes
        source.setBasenames(i18nAllFiles);

        return switch (i18nFilesEncoding) {
            case "ISO-8859-1" -> {
                source.setDefaultEncoding(StandardCharsets.ISO_8859_1.toString());
                yield source;
            }
            case "UTF-8" -> {
                source.setDefaultEncoding(StandardCharsets.UTF_8.toString());
                yield source;
            }
            default -> {
                log.warn("Invalid i18n file encoding!");
                yield source;
            }
        };
    }
}
