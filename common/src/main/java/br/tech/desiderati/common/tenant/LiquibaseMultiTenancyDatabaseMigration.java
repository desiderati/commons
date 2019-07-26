/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.tenant;

import br.tech.desiderati.common.configuration.MultiTenancySchemaConfiguration;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Set;

/**
 * Classe responsável por aplicar as regras do Liquibase.
 */
@Slf4j
@Component
@ConditionalOnBean(MultiTenancySchemaConfiguration.class)
public class LiquibaseMultiTenancyDatabaseMigration {

    private final LiquibaseProperties liquibaseProperties;
    private final ResourceLoader resourceLoader;
    private final DataSource dataSource;

    /**
     * Cache local para garantir que as regras sejam aplicadas apenas uma vez por cliente (Tenant).
     */
    private final Set<String> databases;

    @Autowired
    public LiquibaseMultiTenancyDatabaseMigration(DataSource dataSource,
                                                  LiquibaseProperties liquibaseProperties,
                                                  ResourceLoader resourceLoader) {

        this.liquibaseProperties = liquibaseProperties;
        this.resourceLoader = resourceLoader;
        this.dataSource = dataSource;
        databases = new HashSet<>();
    }

    public void migrateDatabases(Set<String> tenantIds) {
        tenantIds.forEach(this::migrateDatabase);
    }

    void migrateDatabase(String tenantId) {
        if (databases.contains(tenantId)) {
            return;
        }

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(liquibaseProperties.getChangeLog());
        liquibase.setContexts(liquibaseProperties.getContexts());
        liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
        liquibase.setDropFirst(liquibaseProperties.isDropFirst());
        liquibase.setShouldRun(liquibaseProperties.isEnabled());
        liquibase.setLabels(liquibaseProperties.getLabels());
        liquibase.setChangeLogParameters(liquibaseProperties.getParameters());
        liquibase.setRollbackFile(liquibaseProperties.getRollbackFile());
        liquibase.setResourceLoader(resourceLoader);
        liquibase.setDefaultSchema(tenantId);

        try {
            liquibase.afterPropertiesSet();
        } catch (LiquibaseException e) {
            log.error(String.format("Problem running liquibase with tenantId: %s", tenantId), e);
        }

        databases.add(tenantId);
    }
}
