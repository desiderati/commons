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
package br.tech.desiderati.common.configuration;

import br.tech.desiderati.common.tenant.LiquibaseMultiTenancyDatabaseMigration;
import br.tech.desiderati.common.tenant.MultiTenancyConnectionProvider;
import br.tech.desiderati.common.tenant.MultiTenancyDatabaseMigrationInitializer;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cfg.Environment;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;

@Slf4j
@SpringBootConfiguration
@AutoConfigureBefore({DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    LiquibaseAutoConfiguration.class})
@EnableConfigurationProperties({MultiTenancyProperties.class, LiquibaseProperties.class})
@ConditionalOnProperty(prefix = "app.multitenancy", name = "type", havingValue = "schema")
public class MultiTenancySchemaConfiguration {

    private final LiquibaseMultiTenancyDatabaseMigration liquibaseMultiTenancyDatabaseMigration;
    private final MultiTenancyDatabaseMigrationInitializer multiTenancyDatabaseMigrationInitializer;

    @Autowired
    public MultiTenancySchemaConfiguration(LiquibaseMultiTenancyDatabaseMigration liquibaseMultiTenancyDatabaseMigration,
                                           MultiTenancyDatabaseMigrationInitializer multiTenancyDatabaseMigrationInitializer) {

        this.liquibaseMultiTenancyDatabaseMigration = liquibaseMultiTenancyDatabaseMigration;
        this.multiTenancyDatabaseMigrationInitializer = multiTenancyDatabaseMigrationInitializer;
    }

    /**
     * Garante que ao inicializar uma aplicação, todas as regras do Liquibase serão executadas
     * para cada cliente (Tenant).
     */
    @Autowired
    @PostConstruct
    public void migrateDatabases() {
        liquibaseMultiTenancyDatabaseMigration.migrateDatabases(multiTenancyDatabaseMigrationInitializer.tenants());
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(MultiTenancyConnectionProvider multiTenancyConnectionProvider,
                                                                       CurrentTenantIdentifierResolver tenantIdentifierResolver) {
        return hibernateProperties -> {
            hibernateProperties.put(Environment.MULTI_TENANT, MultiTenancyStrategy.SCHEMA);
            hibernateProperties.put(Environment.MULTI_TENANT_CONNECTION_PROVIDER, multiTenancyConnectionProvider);
            hibernateProperties.put(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantIdentifierResolver);
        };
    }
}
