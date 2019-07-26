/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.tenant;

import java.util.Collections;
import java.util.Set;

/**
 * Identifica quais os clientes (Tenants) serão executadas as regras do Liquibase na inicialização da aplicação.
 * Não é obrigatório a implementação da mesma, entretanto é extremamente recomendado.
 */
public interface MultiTenancyDatabaseMigrationInitializer {

    default Set<String> tenants() {
        return Collections.emptySet();
    }
}
