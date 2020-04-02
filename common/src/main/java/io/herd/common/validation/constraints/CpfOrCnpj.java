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
package io.herd.common.validation.constraints;

import io.herd.common.validation.CpfOrCnpjValidator;

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
 * @see Cpf
 * @see Cnpj
 */
@Pattern(regexp =
    "^([0-9]{3}\\.[0-9]{3}\\.[0-9]{3}-[0-9]{2})$|" +
    "^([0-9]{11})$|" +
    "^([0-9]{2}\\.[0-9]{3}\\.[0-9]{3}/[0-9]{4}-[0-9]{2})$|" +
    "^([0-9]{14})$")

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
@Documented
@Constraint(validatedBy = {CpfOrCnpjValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(CpfOrCnpj.List.class)
@ReportAsSingleViolation
public @interface CpfOrCnpj {

    String message() default "{CpfOrCnpj.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        CpfOrCnpj[] value();
    }
}

