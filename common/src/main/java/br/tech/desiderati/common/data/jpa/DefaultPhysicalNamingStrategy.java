/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.data.jpa;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;

/**
 * Neccessário definir a propriedade:
 * <p>
 * spring.jpa.hibernate.naming.physical-strategy=DefaultPhysicalNamingStrategy
 * <p>
 * Dentro do arquivo <b>application.properties</b>. Esta classe pode ser estendida caso desejemos
 * personalizar o prefixo a ser utilizado.
 */
public class DefaultPhysicalNamingStrategy extends SpringPhysicalNamingStrategy {

    private static final String PREFIX = "app";

    @Override
    public final Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        Identifier identifier = super.toPhysicalTableName(name, jdbcEnvironment);
        return new Identifier(getPrefix() + "_" + identifier.getText(), identifier.isQuoted());
    }

    protected String getPrefix() {
        return PREFIX;
    }
}
