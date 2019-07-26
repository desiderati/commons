/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Collections;

@Slf4j
@SpringBootConfiguration
public class ThymeleafConfiguration {

    @Bean
    public SpringTemplateEngine templateEngine(ITemplateResolver defaultResolver) {
        // Default Resolver.
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        ((AbstractConfigurableTemplateResolver) defaultResolver).setOrder(Ordered.LOWEST_PRECEDENCE - 1);
        templateEngine.addTemplateResolver(defaultResolver);

        // Resolver for TEXT templates.
        templateEngine.addTemplateResolver(textTemplateResolver());

        // Resolver for HTML templates (except the editable one).
        templateEngine.addTemplateResolver(htmlTemplateResolver());

        // Resolver for HTML editable templates (which will be treated as a String).
        templateEngine.addTemplateResolver(stringTemplateResolver());

        // Message source, internationalization specific to templates.
        templateEngine.setTemplateEngineMessageSource(templateEngineMessageSource());
        return templateEngine;
    }

    private ITemplateResolver textTemplateResolver() {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setOrder(1);
        templateResolver.setResolvablePatterns(Collections.singleton("text/*"));
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".txt");
        templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    private ITemplateResolver htmlTemplateResolver() {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setOrder(2);
        templateResolver.setResolvablePatterns(Collections.singleton("html/*"));
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    private ITemplateResolver stringTemplateResolver() {
        final StringTemplateResolver templateResolver = new StringTemplateResolver();
        templateResolver.setOrder(Ordered.LOWEST_PRECEDENCE);
        // No resolvable pattern, will simply process as a String template everything not previously matched!
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    private MessageSource templateEngineMessageSource() {
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/templates");
        return messageSource;
    }
}
