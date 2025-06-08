package dev.springbloom.core.validation;

import dev.springbloom.core.validation.constraints.DefaultEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class DefaultEmailValidator implements ConstraintValidator<DefaultEmail, String> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return EMAIL_PATTERN.matcher(value).matches();
    }
}
