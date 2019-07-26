/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.tenant;

import br.tech.desiderati.common.configuration.DatabaseProperties;
import br.tech.desiderati.common.configuration.MultiTenancySchemaConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe responsável por aplicar as regras de Multi Tenancy sempre que uma conexão for criada pelo Hibernate.
 */
@Slf4j
@Component
@ConditionalOnBean(MultiTenancySchemaConfiguration.class)
public class MultiTenancyConnectionProvider extends AbstractMultiTenantConnectionProvider {

    private static final long serialVersionUID = -2940366997865297000L;

    private final transient DatabaseProperties databaseProperties;
    private final transient HikariDatasourceConnectionProvider datasourceConnectionProvider;
    private final transient LiquibaseMultiTenancyDatabaseMigration liquibaseMultiTenancyDatabaseMigration;

    @Autowired
    public MultiTenancyConnectionProvider(DatabaseProperties databaseProperties,
                                          HikariDatasourceConnectionProvider datasourceConnectionProvider,
                                          LiquibaseMultiTenancyDatabaseMigration liquibaseMultiTenancyDatabaseMigration) {

        this.databaseProperties = databaseProperties;
        this.datasourceConnectionProvider = datasourceConnectionProvider;
        this.liquibaseMultiTenancyDatabaseMigration = liquibaseMultiTenancyDatabaseMigration;
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        // Garantir que as regras do Liquibase estão sendo aplicadas a novos clientes.
        liquibaseMultiTenancyDatabaseMigration.migrateDatabase(tenantIdentifier);

        Connection connection = super.getConnection(tenantIdentifier);
        try (Statement statement = connection.createStatement()) {
            StringSubstitutor stringSubstitutor = getStringSubstitutor(tenantIdentifier);
            statement.execute(stringSubstitutor.replace(databaseProperties.getDdlChangeSchema()));
        } catch (Exception e) {
            log.error("Failed to change schema to '{}'", tenantIdentifier);
        }

        return connection;
    }

    @NotNull
    private StringSubstitutor getStringSubstitutor(String tenantIdentifier) {
        Map<String, String> props = new HashMap<>();
        props.put("schemaName", tenantIdentifier);
        return new StringSubstitutor(props);
    }

    @Override
    protected ConnectionProvider getAnyConnectionProvider() {
        return datasourceConnectionProvider;
    }

    @Override
    protected ConnectionProvider selectConnectionProvider(String tenantIdentifier) {
        return datasourceConnectionProvider;
    }
}
