/*
 * Copyright (c) 2024 - Felipe Desiderati
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

package io.herd.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.internal.constraintvalidators.hv.Mod11CheckValidator;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

public class AbstractCpfCpnpjValidator<A extends Annotation> implements ConstraintValidator<A, CharSequence> {

    private static final Pattern DIGITS_ONLY = Pattern.compile("\\d+");

    protected final Mod11CheckValidator withSeparatorMod11Validator1 = new Mod11CheckValidator();
    protected final Mod11CheckValidator withSeparatorMod11Validator2 = new Mod11CheckValidator();

    protected final Mod11CheckValidator withoutSeparatorMod11Validator1 = new Mod11CheckValidator();
    protected final Mod11CheckValidator withoutSeparatorMod11Validator2 = new Mod11CheckValidator();

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (DIGITS_ONLY.matcher(value).matches()) {
            return withoutSeparatorMod11Validator1.isValid(value, context)
                && withoutSeparatorMod11Validator2.isValid(value, context);
        } else {
            return withSeparatorMod11Validator1.isValid(value, context)
                && withSeparatorMod11Validator2.isValid(value, context);
        }
    }
}
