package io.herd.common.web.graphql

import graphql.kickstart.execution.context.GraphQLKickstartContext
import kotlinx.coroutines.*
import org.dataloader.DataLoaderRegistry
import org.springframework.context.i18n.LocaleContext
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import kotlin.coroutines.CoroutineContext

@Suppress("unused")
suspend fun <T> withContextPropagation(block: CoroutineScope.() -> T): T =
    withContext(Dispatchers.IO
        + RequestContextElement(RequestContextHolder.getRequestAttributes())
        + LocalContextElement(LocaleContextHolder.getLocaleContext())) {
        block()
    }

class AsyncGraphQLContext(
    private var map: MutableMap<Any, Any> = mutableMapOf()
) : GraphQLKickstartContext,
    CoroutineScope by CoroutineScope(Dispatchers.IO
        + RequestContextElement(RequestContextHolder.getRequestAttributes())
        + LocalContextElement(LocaleContextHolder.getLocaleContext())) {

    private var dataLoaderRegistry: DataLoaderRegistry = DataLoaderRegistry()

    override fun getDataLoaderRegistry(): DataLoaderRegistry = dataLoaderRegistry

    override fun getMapOfContext(): Map<Any, Any> = map
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
