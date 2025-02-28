/*
 * Copyright (c) 2025 - Felipe Desiderati
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
package io.herd.common.data.jpa;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.model.naming.*;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;

import java.util.List;
import java.util.Locale;

/**
 * You need to set the property:
 * <p>
 * spring.jpa.hibernate.naming.implicit-strategy=io.herd.common.data.jpa.DefaultImplicitNamingStrategy
 * <p>
 * Within the <b>application.properties</b> file.
 */
public class DefaultImplicitNamingStrategy extends SpringImplicitNamingStrategy {

    private interface ColumnNameTransformer {

        String transform(String rawColumnName);

    }

    @SuppressWarnings("squid:S1135")
    // TODO Felipe Desiderati: Configurar o tamanho de acordo com o banco de dados.
    private static final int MAX_IDENTIFIER_LENGTH = 63;

    @Override
    public Identifier determineForeignKeyName(ImplicitForeignKeyNameSource source) {
        // We need to use the table name, because otherwise we can generate FKs with the same name
        // and because of a bug in Liquibase, only one of these FKs will be created. Even though
        // they are from different tables.
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
            // Larger than the size supported by PostgreSQL.
            // All columns will be truncated to three characters!
            identifierName =
                getIdentifierName(source, prefix, rawColumnName -> getColumnName(
                    StringUtils.truncate(rawColumnName, 3)));
        }

        // If it is still larger than the size supported by PostgreSQL, the database
        // will truncate on your own! See the explanation below!

        // Postgres warns us of identifiers longer than 63 characters, informing us
        // of what they will be truncated to. It then proceeds to create the identifier.
        //
        // If postgres is trying to generate an identifier for us - say, for a foreign
        // key constraint - and that identifier is longer than 63 characters, postgres
        // will truncate the identifier somewhere in the middle so as to maintain the
        // convention of terminating with, for example, _fkey.
        //
        // The 63 byte limit is not arbitrary. It comes from NAMEDATALEN - 1. By default,
        // NAMEDATALEN is 64. If need be, this value can be modified in the Postgres source.
        // Yay, open-source database implementations.
        Identifier userProvidedIdentifier = source.getUserProvidedIdentifier();
        return userProvidedIdentifier != null ? userProvidedIdentifier : toIdentifier(
            identifierName, source.getBuildingContext());
    }

    private String getIdentifierName(
        ImplicitConstraintNameSource source,
        String prefix,
        ColumnNameTransformer columnNameTransformer
    ) {
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
