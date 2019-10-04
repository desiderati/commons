/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.configuration.annotation;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.AliasFor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.lang.annotation.*;

@Inherited
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@EnableAspectJAutoProxy
@EnableJpaRepositories

// NOTE Felipe Desiderati: Não definir a anotação abaixo senão o Spring Boot não irá
// configurar os Proxies corretamente. O gerenciamento de transação é configurado
// automaticamente através do application.properties.
// See: org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration
//@EnableTransactionManagement
@ComponentScan
@PropertySource("")
@SpringBootApplication
@SuppressWarnings("unused")
public @interface CustomSpringBootApplication {

    @AliasFor(annotation = PropertySource.class, attribute = "value")
    String[] propertySource() default {"classpath:application.properties"};

    @AliasFor(annotation = ComponentScan.class, attribute = "value")
    String[] componentScan() default {"br.tech.desiderati.common", "br.tech.desiderati.sample", "br.tech.desiderati.ms"};

}

