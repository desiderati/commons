/*
 * Copyright (c) 2019 - Felipe Desiderati
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

    String message() default "{br.tech.desiderati.common.validation.constraints.Cpf.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        Cpf[] value();
    }
}
