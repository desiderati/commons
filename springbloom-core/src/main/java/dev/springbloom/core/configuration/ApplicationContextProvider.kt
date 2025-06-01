package dev.springbloom.core.configuration

import lombok.Getter
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 * Provides static access to the Spring application context.
 */
@Suppress("unused")
class ApplicationContextProvider : ApplicationContextAware {

    companion object {
        @Getter
        private var context: ApplicationContext? = null

        fun getBean(beanName: String): Any? {
            return context?.getBean(beanName)
        }

        fun getProperty(key: String): String? {
            return context?.environment?.getProperty(key)
        }
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }
}
