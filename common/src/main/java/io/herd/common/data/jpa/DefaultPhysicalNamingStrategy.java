/*
 * Copyright (c) 2020 - Felipe Desiderati
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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.InputStream;
import java.util.Properties;

/**
 * Neccessário definir a propriedade:
 * <p>
 * spring.jpa.hibernate.naming.physical-strategy=io.herd.common.data.jpa.DefaultPhysicalNamingStrategy
 * <p>
 * Dentro do arquivo <b>application.properties</b>. Esta classe pode ser estendida caso desejemos
 * personalizar o prefixo a ser utilizado.
 */
@Slf4j
public class DefaultPhysicalNamingStrategy extends SpringPhysicalNamingStrategy {

    private static final String DEFAULT_PREFIX = "app";

    private String prefix;

    public DefaultPhysicalNamingStrategy() {
        prefix = getTablePrefix("application.properties");
        if (prefix == null) {
            prefix = getTablePrefix("application-common.properties");
        }
        if (prefix == null) {
            prefix = DEFAULT_PREFIX;
            log.warn("Property 'app.database.table-prefix' not configured! Using default value: " + prefix);
        }
    }

    private String getTablePrefix(String filename) {
        try {
            Properties prop = new Properties();
            PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver =
                new PathMatchingResourcePatternResolver();

            Resource[] resources =
                pathMatchingResourcePatternResolver.getResources("classpath*:" + filename);
            for (Resource resource : resources) {
                InputStream is = resource.getInputStream();
                prop.load(is);
                String prefixTmp = prop.getProperty("app.database.table-prefix");
                if (StringUtils.isNotBlank(prefixTmp)) {
                    return prefixTmp;
                }
            }
            return null;
        } catch (Exception ex) {
            String errorMsg =
                "It wasn't possible to load property 'app.database.table-prefix' from file: " + filename + "!";
            log.info(errorMsg);
            log.debug(errorMsg, ex);
            return null;
        }
    }

    @Override
    public final Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        Identifier identifier = super.toPhysicalTableName(name, jdbcEnvironment);
        return new Identifier(getPrefix() + "_" + identifier.getText(), identifier.isQuoted());
    }

    protected String getPrefix() {
        return prefix;
    }
}
