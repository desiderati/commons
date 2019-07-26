/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.data;

import br.tech.desiderati.common.configuration.DatabaseProperties;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class DatabaseService {

    private DatabaseProperties databaseProperties;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseService(DatabaseProperties databaseProperties, JdbcTemplate jdbcTemplate) {
        this.databaseProperties = databaseProperties;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void createSchema(String name) {
        Map<String, String> values = new HashMap<>();
        values.put("schemaName", name);

        StringSubstitutor strSubstitutor = new StringSubstitutor(values);
        String createQuery = strSubstitutor.replace(databaseProperties.getDdlCreateSchema());

        jdbcTemplate.execute(createQuery);
    }
}
