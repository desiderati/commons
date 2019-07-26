/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.validation.constraints;

import br.tech.desiderati.common.validation.CpfValidator;

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
 * @see org.hibernate.validator.constraints.br.CPF
 */
@Pattern(regexp = "^([0-9]{3}\\.[0-9]{3}\\.[0-9]{3}-[0-9]{2})$|^([0-9]{11})$")

// Precisamos excluir os casos abaixo pois estes passam pela validação do módulo.
@Pattern(regexp = "^(?:(?!000\\.?000\\.?000-?00).)*$")
@Pattern(regexp = "^(?:(?!111\\.?111\\.?111-?11).)*$")
@Pattern(regexp = "^(?:(?!222\\.?222\\.?222-?22).)*$")
@Pattern(regexp = "^(?:(?!333\\.?333\\.?333-?33).)*$")
@Pattern(regexp = "^(?:(?!444\\.?444\\.?444-?44).)*$")
@Pattern(regexp = "^(?:(?!555\\.?555\\.?555-?55).)*$")
@Pattern(regexp = "^(?:(?!666\\.?666\\.?666-?66).)*$")
@Pattern(regexp = "^(?:(?!777\\.?777\\.?777-?77).)*$")
@Pattern(regexp = "^(?:(?!888\\.?888\\.?888-?88).)*$")
@Pattern(regexp = "^(?:(?!999\\.?999\\.?999-?99).)*$")
@ReportAsSingleViolation
@Documented
@Constraint(validatedBy = {CpfValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(Cpf.List.class)
@SuppressWarnings("unused")
public @interface Cpf {

    String message() default "{Cpf.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        Cpf[] value();
    }
}
