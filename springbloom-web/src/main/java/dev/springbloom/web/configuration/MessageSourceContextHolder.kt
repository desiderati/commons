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

package dev.springbloom.web.configuration

import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.servlet.support.RequestContextUtils
import java.util.*

@Suppress("unused")
sealed class MessageSourceContextHolder {

    companion object {

        fun getMessage(
            messageId: String,
            args: Array<Any>? = null,
            defaultMessage : String? = null,
            locale: Locale? = null
        ): String? {

            // Garantir que está sendo retornado, quando executado dentro de um CompletableFuture.
            val attributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?

            var message: String? = null
            if (attributes != null) {
                val request = attributes.request
                message =
                    RequestContextUtils.findWebApplicationContext(request)?.getMessage(
                        messageId,
                        args,
                        defaultMessage,
                        locale ?: LocaleContextHolder.getLocale()
                    )
            }
            return message
        }
    }
}
