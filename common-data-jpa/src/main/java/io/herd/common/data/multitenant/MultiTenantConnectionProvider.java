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
package io.herd.common.data.multitenant;

import io.herd.common.data.DatabaseProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Class responsible for applying the rules of Multi Tenant whenever a connection is created by the Hibernate.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.database.multitenant.strategy", havingValue = "schema")
public class MultiTenantConnectionProvider extends AbstractMultiTenantConnectionProvider {

    private final transient DatabaseProperties databaseProperties;
    private final transient HikariDatasourceConnectionProvider connectionProvider;
    private final transient LiquibaseSchemaUpdater liquibaseSchemaUpdater;

    @Autowired
    public MultiTenantConnectionProvider(DatabaseProperties databaseProperties,
                                         HikariDatasourceConnectionProvider connectionProvider,
                                         ObjectProvider<LiquibaseSchemaUpdater> liquibaseSchemaUpdater) {

        this.databaseProperties = databaseProperties;
        this.connectionProvider = connectionProvider;
        this.liquibaseSchemaUpdater = liquibaseSchemaUpdater.getIfAvailable();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = super.getConnection(tenantIdentifier);
        try (Statement statement = connection.createStatement()) {
            StringSubstitutor stringSubstitutor = getStringSubstitutor(tenantIdentifier);
            statement.execute(stringSubstitutor.replace(databaseProperties.getDdlCreateSchema()));

            if (liquibaseSchemaUpdater != null) {
                // Ensure that Liquibase rules are being applied to the new clients.
                liquibaseSchemaUpdater.update(tenantIdentifier);
            }

            statement.execute(stringSubstitutor.replace(databaseProperties.getDdlChangeSchema()));
        } catch (Exception e) {
            log.error("Failed to create/select schema to '{}'", tenantIdentifier, e);
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
        return connectionProvider;
    }

    @Override
    protected ConnectionProvider selectConnectionProvider(String tenantIdentifier) {
        return connectionProvider;
    }
}
