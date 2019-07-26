/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@SpringBootConfiguration
@ConfigurationProperties(prefix = "app.datasource")
public class DatabaseProperties {

    private String ddlCreateSchema = "CREATE SCHEMA ${schemaName}";
    private String ddlChangeSchema = "SET SCHEMA '${schemaName}'";

}
