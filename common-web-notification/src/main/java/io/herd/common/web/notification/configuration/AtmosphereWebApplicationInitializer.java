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
package io.herd.common.web.notification.configuration;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.atmosphere.cpr.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.WebApplicationInitializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;

import static org.atmosphere.cpr.ApplicationConfig.ANNOTATION_PACKAGE;
import static org.atmosphere.cpr.ApplicationConfig.BROADCASTER_SHARABLE_THREAD_POOLS;

@Slf4j
@Configuration
@ComponentScan(basePackages = "io.herd.common.web.notification",
    // Do not add the auto-configured classes, otherwise the auto-configuration will not work as expected.
    excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class)
)
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
        if (registration != null) { // If it is equal to null, it indicates that the Servlet has already been registered.
            registration.addMapping(atmosphereUrlMapping);
            registration.setLoadOnStartup(0);
            registration.setAsyncSupported(true);

            onStartup(Collections.emptySet(), servletContext);
            atmosphereFramework = (AtmosphereFramework) servletContext.getAttribute("atmosphere");
            atmosphereFramework.addInitParameter(BROADCASTER_SHARABLE_THREAD_POOLS, "true");
            atmosphereFramework.addInitParameter(ANNOTATION_PACKAGE, "io.herd.common.web.notification");
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
