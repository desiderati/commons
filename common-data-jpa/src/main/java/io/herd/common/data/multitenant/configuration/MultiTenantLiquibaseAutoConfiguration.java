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
package io.herd.common.data.multitenant.configuration;

import io.herd.common.data.multitenant.LiquibaseSchemaRetriever;
import io.herd.common.data.multitenant.LiquibaseSchemaUpdater;
import io.herd.common.data.multitenant.MultiTenantConnectionProvider;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import java.util.Collections;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(LiquibaseAutoConfiguration.class)
@EnableConfigurationProperties(LiquibaseProperties.class)
@ConditionalOnBean(MultiTenantConnectionProvider.class)
@ConditionalOnProperty(name = "spring.liquibase.enabled", havingValue = "true")
@ComponentScan(basePackages = "io.herd.common.data.multitenant",
    // Do not add the auto-configured classes, otherwise the auto-configuration will not work as expected.
    excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class)
)
public class MultiTenantLiquibaseAutoConfiguration {

    private final LiquibaseSchemaUpdater liquibaseSchemaUpdater;
    private final LiquibaseSchemaRetriever liquibaseTenantRetriever;

    @Autowired
    public MultiTenantLiquibaseAutoConfiguration(
        LiquibaseSchemaUpdater liquibaseSchemaUpdater,
        ObjectProvider<LiquibaseSchemaRetriever> liquibaseTenantRetriever
    ) {
        this.liquibaseSchemaUpdater = liquibaseSchemaUpdater;
        this.liquibaseTenantRetriever = liquibaseTenantRetriever.getIfAvailable(() -> Collections::emptySet);
    }

    /**
     * It guarantees that when starting an application, all Liquibase rules will be executed for each schema (Tenant).
     */
    @PostConstruct
    public void applyChanges() {
        liquibaseTenantRetriever.schemas().forEach(liquibaseSchemaUpdater::update);
    }
}
