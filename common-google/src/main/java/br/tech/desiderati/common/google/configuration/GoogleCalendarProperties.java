/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.google.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Getter
@Setter // Nunca esquecer de colocar os setXXX(..) para arquivos de configuração!
@Component
@Validated
@PropertySource("classpath:google-calendar.properties")
@ConfigurationProperties(prefix = "google.calendar")
public class GoogleCalendarProperties {

    @NotBlank
    private String applicationName;

    @NotBlank
    private String credentialsFolder;

    @NotBlank
    private String apiSecretJson;

}
