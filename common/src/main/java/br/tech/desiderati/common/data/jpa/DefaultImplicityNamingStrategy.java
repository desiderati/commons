/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.data.jpa;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.model.naming.*;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;

import java.util.List;
import java.util.Locale;

/**
 * Neccessário definir a propriedade:
 * <p>
 * spring.jpa.hibernate.naming.implicit-strategy=br.tech.desiderati.common.data.jpa.DefaultImplicityNamingStrategy
 * <p>
 * Dentro do arquivo <b>application.properties</b>.
 */
public class DefaultImplicityNamingStrategy extends SpringImplicitNamingStrategy {

    private interface ColumnNameTransformer {

        String transform(String rawColumnName);

    }

    // TODO Felipe Desiderati: Configurar o tamanho de acordo com o banco de dados.
    private static final int MAX_IDENTIFIER_LENGTH = 63;

    @Override
    public Identifier determineForeignKeyName(ImplicitForeignKeyNameSource source) {
        // Não podemos deixar de usar o nome da tabela, pois caso contrário poderemos
        // gerar FKs com o mesmo nome e por causa de um Bug no Liquibase, apenas umas
        // das FKs seriam criadas. Mesmo as mesmas sendo de diferentes tabelas.
        // Veja mais: https://liquibase.jira.com/browse/CORE-3313
        return getIdentifier(source, "fk_" + source.getTableName());
    }

    @Override
    public Identifier determineIndexName(ImplicitIndexNameSource source) {
        return getIdentifier(source, "ix_" + source.getTableName());
    }

    @Override
    public Identifier determineUniqueKeyName(ImplicitUniqueKeyNameSource source) {
        return getIdentifier(source, "uk_" + source.getTableName());
    }

    private Identifier getIdentifier(ImplicitConstraintNameSource source, String prefix) {
        String identifierName = getIdentifierName(source, prefix, this::getColumnName);
        if (identifierName.length() > MAX_IDENTIFIER_LENGTH) {
            // Maior que o tamanho suportado pelo PostgreSQL.
            // Todas as colunas serão truncadas com três caracteres!
            identifierName =
                getIdentifierName(source, prefix, rawColumnName -> getColumnName(
                    StringUtils.truncate(rawColumnName, 3)));
        }

        // Se mesmo assim continuar maior que o tamanho suportado pelo PostgreSQL.
        // O banco de dados irá truncar por sua conta! Veja explicação abaixo!

        // Postgres warns us of identifiers longer than 63 characters, informing us
        // of what they will be truncated to. It then proceeds to create the identifier.
        //
        // If postgres is trying to generate an identifier for us - say, for a foreign
        // key constraint - and that identifier is longer than 63 characters, postgres
        // will truncate the identifier somewhere in the middle so as to maintain the
        // convention of terminating with, for example, _fkey.
        //
        // The 63 byte limit is not arbitrary. It comes from NAMEDATALEN - 1. By default
        // NAMEDATALEN is 64. If need be, this value can be modified in the Postgres source.
        // Yay, open-source database implementations.
        Identifier userProvidedIdentifier = source.getUserProvidedIdentifier();
        return userProvidedIdentifier != null ? userProvidedIdentifier : toIdentifier(
            identifierName, source.getBuildingContext());
    }

    private String getIdentifierName(ImplicitConstraintNameSource source, String prefix,
                                     ColumnNameTransformer columnNameTransformer) {

        StringBuilder identifierName = new StringBuilder(prefix);
        List<Identifier> columnNames = source.getColumnNames();
        for (Identifier columnName : columnNames) {
            identifierName.append("_").append(columnNameTransformer.transform(columnName.getText()));
        }
        return identifierName.toString();
    }

    private String getColumnName(String columnName) {
        StringBuilder builder = new StringBuilder(columnName.replace('.', '_'));
        int i = 1;
        while (i < builder.length() - 1) {
            if (isUnderscoreRequired(builder.charAt(i - 1), builder.charAt(i), builder.charAt(i + 1))) {
                builder.insert(i++, '_');
            }
            i++;
        }
        return builder.toString().toLowerCase(Locale.ROOT);
    }

    private boolean isUnderscoreRequired(char before, char current, char after) {
        return Character.isLowerCase(before) && Character.isUpperCase(current)
            && Character.isLowerCase(after);
    }
}
