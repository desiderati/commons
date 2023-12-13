package io.herd.common.web.configuration.async;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Slf4j
public class RequestAttributesAwareTaskDecorator implements TaskDecorator {

    @NotNull
    @Override
    public Runnable decorate(@NotNull Runnable runnable) {
        RequestAttributes requestAttributes = cloneRequestAttributes(RequestContextHolder.currentRequestAttributes());
        Map<String, String> mdcContextMap = MDC.getCopyOfContextMap();

        return () -> {
            try {
                RequestContextHolder.setRequestAttributes(requestAttributes);
                MDC.setContextMap(mdcContextMap);
                runnable.run();
            } finally {
                MDC.clear();
                RequestContextHolder.resetRequestAttributes();
            }
        };
    }

    @SuppressWarnings("DataFlowIssue")
    private RequestAttributes cloneRequestAttributes(RequestAttributes requestAttributes) {
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
                    requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST),
                    RequestAttributes.SCOPE_REQUEST
                );
            }

            for (String name : requestAttributes.getAttributeNames(RequestAttributes.SCOPE_SESSION)) {
                clonedRequestAttribute.setAttribute(
                    name,
                    requestAttributes.getAttribute(name, RequestAttributes.SCOPE_SESSION),
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
