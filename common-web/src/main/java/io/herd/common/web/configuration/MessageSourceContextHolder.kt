package io.herd.common.web.configuration

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
