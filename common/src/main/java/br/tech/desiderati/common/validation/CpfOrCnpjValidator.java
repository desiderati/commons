/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.validation;

import br.tech.desiderati.common.validation.constraints.CpfOrCnpj;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CpfOrCnpjValidator implements ConstraintValidator<CpfOrCnpj, CharSequence> {

    private CpfValidator cpfValidator = new CpfValidator();
    private CnpjValidator cnpjValidator = new CnpjValidator();

    @Override
    public void initialize(CpfOrCnpj constraintAnnotation) {
        cpfValidator.initialize(null);
        cnpjValidator.initialize(null);
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        return cpfValidator.isValid(value, context) || cnpjValidator.isValid(value, context);
    }
}
