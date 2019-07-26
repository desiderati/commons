/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.validation;

import br.tech.desiderati.common.validation.constraints.Cnpj;
import org.hibernate.validator.constraints.Mod11Check;
import org.hibernate.validator.internal.constraintvalidators.hv.Mod11CheckValidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Não utilizamos a validação já disponível pelo Hibernate, pois desejamos validar ou com toda formatação,
 * ou sem formatação alguma.
 *
 * @see org.hibernate.validator.internal.constraintvalidators.hv.br.CNPJValidator
 */
public class CnpjValidator implements ConstraintValidator<Cnpj, CharSequence> {

    private static final Pattern DIGITS_ONLY = Pattern.compile("\\d+");

    private final Mod11CheckValidator withSeparatorMod11Validator1 = new Mod11CheckValidator();
    private final Mod11CheckValidator withSeparatorMod11Validator2 = new Mod11CheckValidator();

    private final Mod11CheckValidator withoutSeparatorMod11Validator1 = new Mod11CheckValidator();
    private final Mod11CheckValidator withoutSeparatorMod11Validator2 = new Mod11CheckValidator();

    @Override
    public void initialize(Cnpj constraintAnnotation) {
        // Validates CNPJ strings with separator, eg 91.509.901/0001-69.
        withSeparatorMod11Validator1.initialize(
            0, 14, 16, true, 9, '0',
            '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
        );
        withSeparatorMod11Validator2.initialize(
            0, 16, 17, true, 9, '0',
            '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
        );

        // Validates CNPJ strings without separator, eg 91509901000169.
        withoutSeparatorMod11Validator1.initialize(
            0, 11, 12, true, 9, '0',
            '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
        );
        withoutSeparatorMod11Validator2.initialize(
            0, 12, 13, true, 9, '0',
            '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
        );
    }

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
