/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.validation.constraints;

import br.tech.desiderati.common.validation.CnpjValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Pattern;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Não utilizamos a validação já disponível pelo Hibernate, pois desejamos validar ou com toda formatação,
 * ou sem formatação alguma.
 *
 * @see org.hibernate.validator.constraints.br.CNPJ
 */
@Pattern(regexp = "^([0-9]{2}\\.[0-9]{3}\\.[0-9]{3}/[0-9]{4}-[0-9]{2})$|^([0-9]{14})$")
@ReportAsSingleViolation
@Documented
@Constraint(validatedBy = {CnpjValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(Cnpj.List.class)
@SuppressWarnings("unused")
public @interface Cnpj {

    String message() default "{br.tech.desiderati.common.validation.constraints.Cnpj.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        Cnpj[] value();
    }
}


