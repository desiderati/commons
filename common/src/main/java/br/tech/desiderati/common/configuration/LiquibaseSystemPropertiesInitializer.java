/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.configuration;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Order(Ordered.HIGHEST_PRECEDENCE) // Primeiro a ser executado!
public class LiquibaseSystemPropertiesInitializer implements ApplicationListener<ApplicationStartingEvent> {

    @Override
    public void onApplicationEvent(@NotNull ApplicationStartingEvent event) {
        System.setProperty("liquibase.databaseChangeLogTableName", "liq_db_changelog");
        System.setProperty("liquibase.databaseChangeLogLockTableName", "liq_db_changelog_lock");
    }
}
