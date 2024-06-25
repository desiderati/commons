/*
 * Copyright (c) 2024 - Felipe Desiderati
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

import io.herd.common.web.configuration.WebAutoConfiguration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import org.atmosphere.cpr.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.*;
import org.springframework.web.WebApplicationInitializer;

import java.util.Collections;

import static org.atmosphere.cpr.ApplicationConfig.*;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ComponentScan(basePackages = "io.herd.common.web.notification",
    // Do not add the auto-configured classes, otherwise the auto-configuration will not work as expected.
    excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class)
)
@Import(WebAutoConfiguration.class) // To be used with @WebMvcTest
public class WebNotificationAutoConfiguration extends ContainerInitializer
    implements WebApplicationInitializer, ServletContextInitializer {

    @Value("${spring.web.atmosphere.url.mapping:/atmosphere}")
    private String atmosphereUrlMapping;

    private AtmosphereFramework atmosphereFramework;

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

            // It will ensure that CORS will be configured for Atmosphere.
            atmosphereFramework.addInitParameter(DROP_ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "false");
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
