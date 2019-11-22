/*
 * Copyright (c) 2019 - Felipe Desiderati
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
