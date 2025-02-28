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
package io.herd.common.validation.constraints;

import io.herd.common.validation.CpfValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * We didn't use the validation already available by the Hibernate, because we want to validate either
 * using formatting or without any formatting at all.
 *
 * @see org.hibernate.validator.constraints.br.CPF
 */
@Pattern(regexp = "^(\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$|^(\\d{11})$")

// We need to exclude the cases below as they pass through the module validation, but they are invalid.
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
