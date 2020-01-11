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
package io.herd.common.configuration.annotation;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
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
@EntityScan

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

    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "value")
    String[] jpaRepositoryScan() default {};

    @AliasFor(annotation = EntityScan.class, attribute = "value")
    String[] entityScan() default {};

    @AliasFor(annotation = PropertySource.class, attribute = "value")
    String[] propertySource() default {"classpath:application.properties"};

    @AliasFor(annotation = ComponentScan.class, attribute = "value")
    String[] componentScan() default {"io.herd.common"};

}

