/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.test.annotation;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.lang.annotation.*;

@Sql
@DataJpaTest
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Inherited
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface ServiceTest {

    @AliasFor(annotation = Sql.class, attribute = "scripts")
    String[] sqlScripts() default "";

    @AliasFor(annotation = Sql.class, attribute = "executionPhase")
    Sql.ExecutionPhase sqlExecutionPhase() default Sql.ExecutionPhase.BEFORE_TEST_METHOD;

}

