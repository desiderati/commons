/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.validation;

import br.tech.desiderati.common.validation.constraints.Cpf;
import org.hibernate.validator.constraints.Mod11Check;
import org.hibernate.validator.internal.constraintvalidators.hv.Mod11CheckValidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Não utilizamos a validação já disponível pelo Hibernate, pois desejamos validar ou com toda formatação,
 * ou sem formatação alguma.
 *
 * @see org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator
 */
public class CpfValidator implements ConstraintValidator<Cpf, CharSequence> {

    private static final Pattern DIGITS_ONLY = Pattern.compile("\\d+");

    private final Mod11CheckValidator withSeparatorMod11Validator1 = new Mod11CheckValidator();
    private final Mod11CheckValidator withSeparatorMod11Validator2 = new Mod11CheckValidator();

    private final Mod11CheckValidator withoutSeparatorMod11Validator1 = new Mod11CheckValidator();
    private final Mod11CheckValidator withoutSeparatorMod11Validator2 = new Mod11CheckValidator();

    @Override
    public void initialize(Cpf constraintAnnotation) {
        // Validates CPF strings with separator, eg 134.241.313-00.
        withSeparatorMod11Validator1.initialize(
            0, 10, 12, true, Integer.MAX_VALUE, '0',
            '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
        );
        withSeparatorMod11Validator2.initialize(
            0, 12, 13, true, Integer.MAX_VALUE, '0',
            '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
        );

        // Validates CPF strings without separator, eg 13424131300.
        withoutSeparatorMod11Validator1.initialize(
            0, 8, 9, true, Integer.MAX_VALUE, '0',
            '0', Mod11Check.ProcessingDirection.RIGHT_TO_LEFT
        );
        withoutSeparatorMod11Validator2.initialize(
            0, 9, 10, true, Integer.MAX_VALUE, '0',
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

