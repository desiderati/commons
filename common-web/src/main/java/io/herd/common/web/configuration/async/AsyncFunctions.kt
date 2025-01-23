package io.herd.common.web.configuration.async

import io.herd.common.configuration.ApplicationContextProvider
import org.springframework.context.i18n.LocaleContext
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

/**
 * Always use this function to create a [CompletableFuture] that will propagate the [LocaleContext]
 * and [RequestAttributes], instead of using [CompletableFuture.supplyAsync].
 */
@Suppress("unused")
fun <T> supplyAsyncWithContext(
    localAsyncContextPropagationMode: AsyncContextPropagationMode? = null,
    supplier: Supplier<T>
): CompletableFuture<T> {

    val asyncContextPropagationMode = localAsyncContextPropagationMode ?: AsyncContextPropagationMode.valueOf(
        ApplicationContextProvider.getProperty("spring.mvc.async.context-propagation-mode")
            ?: AsyncContextPropagationMode.NON_INHERITABLE.name
    )

    return if (asyncContextPropagationMode == AsyncContextPropagationMode.INHERITABLE) {
        CompletableFuture.supplyAsync { supplier.get() }
    } else {
        val localeContext = LocaleContextHolder.getLocaleContext()
        val requestAttributes = AsyncContextAwareTaskDecorator.cloneRequestAttributes(
            RequestContextHolder.currentRequestAttributes()
        )

        CompletableFuture.supplyAsync {
            try {
                LocaleContextHolder.setLocaleContext(localeContext)
                RequestContextHolder.setRequestAttributes(requestAttributes)
                supplier.get()
            } finally {
                LocaleContextHolder.resetLocaleContext()
                RequestContextHolder.resetRequestAttributes()
            }
        }
    }
}
