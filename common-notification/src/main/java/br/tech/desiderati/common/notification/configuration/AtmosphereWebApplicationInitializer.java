/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.notification.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.atmosphere.cpr.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;

import static org.atmosphere.cpr.ApplicationConfig.ANNOTATION_PACKAGE;
import static org.atmosphere.cpr.ApplicationConfig.BROADCASTER_SHARABLE_THREAD_POOLS;

@Slf4j
@EnableAutoConfiguration
@SpringBootConfiguration
@ComponentScan("br.tech.desiderati.common.notification")
public class AtmosphereWebApplicationInitializer extends ContainerInitializer
        implements WebApplicationInitializer, ServletContextInitializer {

    private static final String DEFAULT_URL_MAPPING = "/notification/*";

    private static final String URL_MAPPING_PROPERTY_NAME = "atmosphere.url.mapping";

    private String atmosphereUrlMapping;

    private AtmosphereFramework atmosphereFramework;

    public AtmosphereWebApplicationInitializer() {
        String filename = "atmosphere.properties";
        try (InputStream input =
                 AtmosphereWebApplicationInitializer.class.getClassLoader().getResourceAsStream(filename)) {
            if (input == null) {
                throw new IOException("Unable to read file '" + filename + "'");
            }

            Properties prop = new Properties();
            prop.load(input);
            String atmosphereUrlMappingTmp = prop.getProperty(URL_MAPPING_PROPERTY_NAME);
            this.atmosphereUrlMapping = StringUtils.defaultIfBlank(atmosphereUrlMappingTmp, DEFAULT_URL_MAPPING);
        } catch (IOException ex) {
            log.debug(ex.getMessage(), ex);
            log.warn("Unable to read property [" + URL_MAPPING_PROPERTY_NAME + "] from file '" + filename + "'! " +
                "Using default URL mapping instead: " + DEFAULT_URL_MAPPING);
            this.atmosphereUrlMapping = DEFAULT_URL_MAPPING;
        }
    }

    @Override
    public void onStartup(@NotNull ServletContext servletContext) throws ServletException {
        AtmosphereServlet servlet = servletContext.createServlet(AtmosphereServlet.class);
        ServletRegistration.Dynamic registration = servletContext.addServlet("atmosphere", servlet);
        if (registration != null) { // Se for igual a null, indica que o Servlet já foi registrado.
            registration.addMapping(atmosphereUrlMapping);
            registration.setLoadOnStartup(0);
            registration.setAsyncSupported(true);

            onStartup(Collections.emptySet(), servletContext);
            atmosphereFramework = (AtmosphereFramework) servletContext.getAttribute("atmosphere");
            atmosphereFramework.addInitParameter(BROADCASTER_SHARABLE_THREAD_POOLS, "true");
            atmosphereFramework.addInitParameter(ANNOTATION_PACKAGE, "br.tech.desiderati.common.notification");
        }
    }

    @Bean
    public BroadcasterFactory atmosphereBroadcasterFactory() {
        return atmosphereFramework.getBroadcasterFactory();
    }

    @Bean
    public AtmosphereResourceFactory atmosphereResourceFactory() {
        return atmosphereFramework.atmosphereFactory();
    }
}
