/*
 * Copyright (c) 2023 - Felipe Desiderati
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

import io.herd.common.validation.constraints.Cpf;
import org.hibernate.validator.constraints.Mod11Check;

/**
 * We didn't use the validation already available by the Hibernate, because we want to validate either
 * using formatting or without any formatting at all.
 *
 * @see org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator
 */
public class CpfValidator extends AbstractCpfCpnpjValidator<Cpf> {

    @Override
    public void initialize(Cpf constraintAnnotation) {
        // Validates CPF strings with separator, eg.: 134.241.313-00.
        withSeparatorMod11Validator1.initialize(
            0, 10, 12, true, Integer.MAX_VALUE, '0',
            '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
        );
        withSeparatorMod11Validator2.initialize(
            0, 12, 13, true, Integer.MAX_VALUE, '0',
            '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
        );

        // Validates CPF strings without separator, eg.: 13424131300.
        withoutSeparatorMod11Validator1.initialize(
            0, 8, 9, true, Integer.MAX_VALUE, '0',
            '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
        );
        withoutSeparatorMod11Validator2.initialize(
            0, 9, 10, true, Integer.MAX_VALUE, '0',
            '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
        );
    }
}

