/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.configuration;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Properties;

/**
 * Implementação de {@link org.springframework.context.MessageSource} responsável por buscar
 * todos os arquivos de propriedades (<b>ARQUIVOS COM MESMO NOME</b>) contidos dentro do Classpath.
 */
@Slf4j
public class PathMatchingReloadableResourceBundleMessageSource extends ReloadableResourceBundleMessageSource {

    private static final String PROPERTIES_SUFFIX = ".properties";

    private PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @NotNull
    @Override
    protected PropertiesHolder refreshProperties(@NotNull String filename, PropertiesHolder propHolder) {
        if (filename.startsWith(PathMatchingResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX)) {
            return refreshClassPathProperties(filename, propHolder);
        } else {
            return super.refreshProperties(filename, propHolder);
        }
    }

    private PropertiesHolder refreshClassPathProperties(String filename, PropertiesHolder propHolder) {
        Properties properties = new Properties();
        long lastModified = -1;
        try {
            Resource[] resources = resolver.getResources(filename + PROPERTIES_SUFFIX);
            for (Resource resource : resources) {
                String sourcePath = resource.getURI().toString().replace(PROPERTIES_SUFFIX, "");

                log.info("Loading resource: " + sourcePath);
                PropertiesHolder holder = super.refreshProperties(sourcePath, propHolder);
                if (holder.getProperties() != null) {
                    properties.putAll(holder.getProperties());
                    if (lastModified < resource.lastModified()) {
                        lastModified = resource.lastModified();
                    }
                }
            }
        } catch (IOException ex) {
            // Just ignore any exception!
            log.warn("Error while getting resources: " + filename + PROPERTIES_SUFFIX, ex);
        }
        return new PropertiesHolder(properties, lastModified);
    }
}
