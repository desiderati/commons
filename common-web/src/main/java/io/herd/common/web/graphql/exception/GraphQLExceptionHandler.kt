/*
 * Copyright (c) 2023 - Felipe Desiderati
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
package io.herd.common.web.graphql.exception

import graphql.ErrorType
import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.DataFetcherExceptionHandlerResult
import graphql.kickstart.spring.error.ErrorContext
import io.herd.common.exception.ApplicationException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.bind.annotation.ExceptionHandler
import java.io.Serializable
import java.lang.reflect.UndeclaredThrowableException
import java.util.*
import java.util.concurrent.CompletableFuture

open class GraphQLExceptionHandler(
    private val messageSource: MessageSource
) : DataFetcherExceptionHandler {

    companion object {
        private const val DEFAULT_ERROR_CODE = "internal_server_error"
        private const val DEFAULT_ERROR_MESSAGE =
            "The server encountered an unexpected condition that prevented it from fulfilling the request!"
    }

    override fun handleException(
        handlerParameters: DataFetcherExceptionHandlerParameters
    ): CompletableFuture<DataFetcherExceptionHandlerResult> {
        return CompletableFuture.completedFuture(
            DataFetcherExceptionHandlerResult.newResult().error(
                generateGraphQLError(
                    handlerParameters.exception,
                    ErrorContext(
                        listOf(handlerParameters.sourceLocation),
                        handlerParameters.path.toList(),
                        if (handlerParameters.argumentValues.isEmpty()) mapOf() else {
                            mapOf(
                                "argumentValues" to handlerParameters.argumentValues,
                            )
                        },
                        ErrorType.DataFetchingException
                    )
                )
            ).build()
        )
    }

    @ExceptionHandler(UndeclaredThrowableException::class)
    open fun handleException(
        undeclaredThrowableException: UndeclaredThrowableException,
        errorContext: ErrorContext
    ): GraphQLError {
        return generateGraphQLError(undeclaredThrowableException, errorContext)
    }

    @ExceptionHandler(Throwable::class)
    open fun handleException(throwable: Throwable, errorContext: ErrorContext): GraphQLError {
        return generateGraphQLError(throwable, errorContext)
    }

    @ExceptionHandler(ApplicationException::class)
    open fun handleException(applicationException: ApplicationException, errorContext: ErrorContext): GraphQLError {
        return generateGraphQLError(
            applicationException.message,
            errorContext,
            mapOf("applicationException" to applicationException.apply { stackTrace = arrayOf() })
        )
    }

    protected open fun generateGraphQLError(throwable: Throwable, errorContext: ErrorContext): GraphQLError {
        return generateGraphQLError(
            throwable.message,
            errorContext,
            mapOf("originalMessage" to throwable.message)
        )
    }

    protected open fun generateGraphQLError(
        message: String?,
        errorContext: ErrorContext,
        extensions: Map<String, Any?> = mapOf()
    ): GraphQLError {
        val internalServerError = getMessage(LocaleContextHolder.getLocale(), DEFAULT_ERROR_CODE, DEFAULT_ERROR_MESSAGE)
        return GraphqlErrorBuilder.newError()
            .message(
                getMessage(
                    LocaleContextHolder.getLocale(),
                    message ?: DEFAULT_ERROR_CODE,
                    internalServerError
                )
            )
            // If needed in the future, we can add the error location.
            .locations(null)
            .path(errorContext.path.orEmpty())
            .extensions(errorContext.extensions.orEmpty().plus(extensions))
            .build()
    }

    @Suppress("SameParameterValue")
    private fun getMessage(locale: Locale, code: String, defaultMessage: String, vararg args: Serializable): String {
        return messageSource.getMessage(code, args, defaultMessage, locale)!!
    }
}
