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
package dev.springbloom.core.validation.constraints;

import dev.springbloom.core.validation.DefaultEmailValidator;
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
 * Valida um endereço de email usando uma expressão regular (RegExp). Nós usamos a validação de email padrão do Jakarta,
 * pois queríamos criar uma nova, usando uma expressão regular padrão.
 * <p>
 * Exemplo de RegExp:
 * <pre>
 * ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$
 * </pre>
 * A expressão regular utilizada verifica se o email segue o formato geral - <code>local-part@domain</code>, onde:
 * <ul>
 *   <li>
 *       A parte local (antes do `@`) pode conter letras, números e caracteres especiais
 *       como ".", "_", "%", "+", e "-".
 *   </li>
 *   <li>
 *       O domínio (após o `@`) pode conter letras, números, pontos e hifens, e deve terminar
 *       com um sufixo de pelo menos duas letras.
 *   </li>
 * </ul>
 * <p>
 * Exemplos de emails válidos:
 * <ul>
 *   <li><code>user@domain.com</code></li>
 *   <li><code>user.name@domain.com.br</code></li>
 *   <li><code>user+alias@sub.domain.org</code></li>
 * </ul>
 * <p>
 * Exemplos de emails inválidos:
 * <ul>
 *   <li><code>user@domain</code>: Falta o sufixo, como <code>.com</code>.</li>
 *   <li><code>user@domain..com</code>: Dois pontos consecutivos são inválidos.</li>
 *   <li><code>@domain.com</code>: Falta a parte antes do <code>@</code>.</li>
 * </ul>
 * <p>
 * Observação:
 * Esta RegExp cobre a maioria dos casos práticos, mas não é 100% compatível com o padrão
 * <a href="https://tools.ietf.org/html/rfc5322">RFC 5322</a>, que define as regras completas
 * para endereços de email.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Email_address">Email Address (Wikipedia)</a>
 * @see <a href="https://tools.ietf.org/html/rfc5322">RFC 5322 - Internet Message Format</a>
 */
@Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
@ReportAsSingleViolation
@Documented
@Constraint(validatedBy = {DefaultEmailValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(DefaultEmail.List.class)
public @interface DefaultEmail {

    String message() default "{jakarta.validation.constraints.DefaultEmail.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        DefaultEmail[] value();
    }
}
