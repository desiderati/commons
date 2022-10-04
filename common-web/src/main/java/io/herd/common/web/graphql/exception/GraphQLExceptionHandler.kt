/*
 * Copyright (c) 2022 - Felipe Desiderati
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

import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.kickstart.spring.error.ErrorContext
import io.herd.common.exception.ApplicationException
import org.springframework.context.MessageSource
import org.springframework.web.bind.annotation.ExceptionHandler
import java.io.Serializable
import java.lang.reflect.UndeclaredThrowableException
import java.util.*

open class GraphQLExceptionHandler(
    private val messageSource: MessageSource
) {
    companion object {
        private const val DEFAULT_ERROR_CODE = "internal_server_error"
        private const val DEFAULT_ERROR_MESSAGE =
            "The server encountered an unexpected condition that prevented it from fulfilling the request!"

        // TODO Felipe Desiderati: Extrair a localização a partir da requisição.
        private val DEFAULT_LOCALE = Locale("pt", "BR")
    }

    @ExceptionHandler(UndeclaredThrowableException::class)
    open fun handle(
        undeclaredThrowableException: UndeclaredThrowableException,
        errorContext: ErrorContext
    ): GraphQLError {
        return generateGraphQLError(undeclaredThrowableException, errorContext)
    }

    @ExceptionHandler(Throwable::class)
    open fun handle(throwable: Throwable, errorContext: ErrorContext): GraphQLError {
        return generateGraphQLError(throwable, errorContext)
    }

    @ExceptionHandler(ApplicationException::class)
    open fun handle(applicationException: ApplicationException, errorContext: ErrorContext): GraphQLError {
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
        val internalServerError = getMessage(DEFAULT_LOCALE, DEFAULT_ERROR_CODE, DEFAULT_ERROR_MESSAGE)
        return GraphqlErrorBuilder.newError()
            .message(
                getMessage(
                    DEFAULT_LOCALE,
                    message ?: internalServerError,
                    internalServerError
                )
            )
            .locations(errorContext.locations.orEmpty())
            .path(errorContext.path.orEmpty())
            .extensions(errorContext.extensions.orEmpty().plus(extensions))
            .build()
    }

    @Suppress("SameParameterValue")
    private fun getMessage(locale: Locale, code: String, defaultMessage: String, vararg args: Serializable): String {
        return messageSource.getMessage(code, args, defaultMessage, locale)!!
    }
}
