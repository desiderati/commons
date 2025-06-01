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
package dev.springbloom.web.configuration.async

import dev.springbloom.core.configuration.ApplicationContextProvider
import kotlinx.coroutines.*
import org.springframework.context.i18n.LocaleContext
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier
import kotlin.coroutines.CoroutineContext

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

@Suppress("unused")
class CoroutineScopeWithContextPropagation : CoroutineScope by CoroutineScope(Dispatchers.IO
    + RequestContextElement(RequestContextHolder.getRequestAttributes())
    + LocalContextElement(LocaleContextHolder.getLocaleContext())
)

@Suppress("unused")
fun CoroutineScope.launchWithContextPropagation(block: suspend CoroutineScope.() -> Unit): Job =
    launch(Dispatchers.IO
        + RequestContextElement(RequestContextHolder.getRequestAttributes())
        + LocalContextElement(LocaleContextHolder.getLocaleContext())) {
        block()
    }

@Suppress("unused")
suspend fun <T> withContextPropagation(block: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.IO
        + RequestContextElement(RequestContextHolder.getRequestAttributes())
        + LocalContextElement(LocaleContextHolder.getLocaleContext())) {
        block()
    }

class RequestContextElement(
    private val requestAttributes: RequestAttributes?
) : ThreadContextElement<RequestAttributes?> {

    companion object Key : CoroutineContext.Key<RequestContextElement>

    override val key: CoroutineContext.Key<*>
        get() = Key

    override fun updateThreadContext(context: CoroutineContext): RequestAttributes? {
        val previous = RequestContextHolder.getRequestAttributes()
        RequestContextHolder.setRequestAttributes(requestAttributes)
        return previous
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: RequestAttributes?) {
        RequestContextHolder.setRequestAttributes(oldState)
    }
}

class LocalContextElement(private val localeContext: LocaleContext?) : ThreadContextElement<LocaleContext?> {

    companion object Key : CoroutineContext.Key<LocalContextElement>

    override val key: CoroutineContext.Key<*>
        get() = Key

    override fun updateThreadContext(context: CoroutineContext): LocaleContext? {
        val previous = LocaleContextHolder.getLocaleContext()
        LocaleContextHolder.setLocaleContext(localeContext)
        return previous
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: LocaleContext?) {
        LocaleContextHolder.setLocaleContext(oldState)
    }
}
