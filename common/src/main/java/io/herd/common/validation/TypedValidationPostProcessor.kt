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
package io.herd.common.validation

import org.springframework.aop.Pointcut
import org.springframework.aop.framework.autoproxy.AbstractBeanFactoryAwareAdvisingPostProcessor
import org.springframework.aop.support.DefaultPointcutAdvisor
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut
import org.springframework.beans.factory.InitializingBean
import org.springframework.util.Assert
import org.springframework.validation.method.MethodValidationException
import org.springframework.validation.method.MethodValidationResult

/**
 * Applicable methods have [TypedValidated] annotations on
 * their parameters and/or on their return value (in the latter case specified at
 * the method level, typically as inline annotation).
 *
 * E.g.: `public @TypedValidated Object myValidMethod(@TypedValidated String arg1, int arg2)`
 *
 * In case of validation errors, the interceptor can raise
 * [TypedValidationException], or adapt the violations to
 * [MethodValidationResult] and raise [MethodValidationException].
 *
 * Target classes with such annotated methods need to be annotated with Spring's
 * [TypedValidated] annotation at the type level, for their methods to be searched for
 * inline [TypedValidated] annotations.
 *
 * @author Felipe Desiderati
 * @author Luiz Oliveira
 * @since 4.0.0
 *
 * @see TypedValidationMethodInterceptor
 */
open class TypedValidationPostProcessor : AbstractBeanFactoryAwareAdvisingPostProcessor(), InitializingBean {

    private var validatedAnnotationType: Class<out Annotation> = TypedValidated::class.java

    /**
     * Set the 'validated' annotation type. The default validated annotation type
     * is the [TypedValidated] annotation.
     *
     * This setter property exists so that developers can provide their own
     * (non-Spring-specific) annotation type to indicate that a class is supposed
     * to be validated in the sense of applying method validation.
     *
     * @param validatedAnnotationType the desired annotation type
     */
    @Suppress("unused")
    fun setValidatedAnnotationType(validatedAnnotationType: Class<out Annotation>) {
        Assert.notNull(validatedAnnotationType, "'validatedAnnotationType' must not be null")
        this.validatedAnnotationType = validatedAnnotationType
    }

    override fun afterPropertiesSet() {
        val pointcut: Pointcut = AnnotationMatchingPointcut(validatedAnnotationType, true)
        this.advisor = DefaultPointcutAdvisor(pointcut, TypedValidationMethodInterceptor())
    }
}
