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

package io.herd.common.web.configuration.async;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.task.TaskDecorator;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

/**
 * A {@link TaskDecorator} that propagates the {@link LocaleContext} and {@link RequestAttributes} from the calling
 * thread to the children threads.
 */
@Slf4j
public class AsyncContextAwareTaskDecorator implements TaskDecorator {

    @NotNull
    @Override
    public Runnable decorate(@NotNull Runnable runnable) {
        LocaleContext localeContext = LocaleContextHolder.getLocaleContext();
        RequestAttributes requestAttributes =
            cloneRequestAttributes(RequestContextHolder.currentRequestAttributes());

        return () -> {
            try {
                LocaleContextHolder.setLocaleContext(localeContext);
                RequestContextHolder.setRequestAttributes(requestAttributes);
                runnable.run();
            } finally {
                LocaleContextHolder.resetLocaleContext();
                RequestContextHolder.resetRequestAttributes();
            }
        };
    }

    public static RequestAttributes cloneRequestAttributes(RequestAttributes requestAttributes) {
        RequestAttributes clonedRequestAttribute;
        try {
            clonedRequestAttribute =
                new ServletRequestAttributes(
                    ((ServletRequestAttributes) requestAttributes).getRequest(),
                    ((ServletRequestAttributes) requestAttributes).getResponse()
                );

            for (String name : requestAttributes.getAttributeNames(RequestAttributes.SCOPE_REQUEST)) {
                clonedRequestAttribute.setAttribute(
                    name,
                    Objects.requireNonNull(requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST)),
                    RequestAttributes.SCOPE_REQUEST
                );
            }

            for (String name : requestAttributes.getAttributeNames(RequestAttributes.SCOPE_SESSION)) {
                clonedRequestAttribute.setAttribute(
                    name,
                    Objects.requireNonNull(requestAttributes.getAttribute(name, RequestAttributes.SCOPE_SESSION)),
                    RequestAttributes.SCOPE_SESSION
                );
            }

            return clonedRequestAttribute;
        } catch (Exception e) {
            log.warn("It was not possible to clone the request attributes!", e);
            return requestAttributes;
        }
    }
}
