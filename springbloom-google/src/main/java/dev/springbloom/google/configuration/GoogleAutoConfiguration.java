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
package dev.springbloom.google.configuration;

import dev.springbloom.google.GoogleCalendarService;
import dev.springbloom.google.GoogleCaptchaService;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.validation.annotation.Validated;

@Configuration
@PropertySource("classpath:application-springbloom-google.properties")
@ComponentScan(basePackages = "dev.springbloom.google",
    // Do not add the auto-configured classes, otherwise the auto-configuration will not work as expected.
    excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class)
)
public class GoogleAutoConfiguration {

    @Bean
    @Validated
    @ConditionalOnProperty(name = "google.calendar.enabled", havingValue = "true")
    @ConfigurationProperties("google.calendar")
    public GoogleCalendarProperties googleCalendarProperties() {
        return new GoogleCalendarProperties();
    }

    @Bean
    @ConditionalOnProperty(name = "google.calendar.enabled", havingValue = "true")
    public GoogleCalendarService googleCalendarService(GoogleCalendarProperties calendarProperties) {
        return new GoogleCalendarService(calendarProperties);
    }

    @Bean
    @Validated
    @ConditionalOnProperty("google.captcha.secret-key")
    @ConfigurationProperties("google.captcha")
    public GoogleCaptchaProperties googleCaptchaProperties() {
        return new GoogleCaptchaProperties();
    }

    @Bean
    @ConditionalOnProperty("google.captcha.secret-key")
    public GoogleCaptchaService googleCaptchaService(GoogleCaptchaProperties captchaProperties) {
        return new GoogleCaptchaService(captchaProperties);
    }
}
