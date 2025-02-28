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

import io.herd.common.not
import jakarta.validation.*
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.SmartFactoryBean
import org.springframework.lang.Nullable
import org.springframework.util.ClassUtils
import org.springframework.validation.method.MethodValidationException
import org.springframework.validation.method.MethodValidationResult
import java.lang.reflect.Method
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.kotlinFunction

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
 * @see TypedValidationPostProcessor
 */
class TypedValidationMethodInterceptor : MethodInterceptor {

    @Nullable
    @Throws(Throwable::class)
    override fun invoke(invocation: MethodInvocation): Any? {
        // Avoid Validator invocation on FactoryBean.getObjectType/isSingleton
        if (isFactoryBeanMetadataMethod(invocation.method)) {
            return invocation.proceed()
        }

        val method = invocation.method
        val arguments = invocation.arguments
        val entitiesToValidated = method.parameters.filter {
            it.isAnnotationPresent(TypedValidated::class.java)
        }.map {
            method.parameters.indexOf(it)
        }.map {
            if (not { arguments[it] is TypedValidationEntity }) {
                throw IllegalArgumentException("The argument '${method.parameters[it].name}' object " +
                    "does not implements the TypedValidationEntity interface!"
                )
            }
            arguments[it] as TypedValidationEntity
        }

        if (entitiesToValidated.isNotEmpty()) {
            val typedValidationException = invokeValidatorForArguments(entitiesToValidated)
            if (typedValidationException != null) {
                throw typedValidationException
            }
        }

        val returnValue = invocation.proceed()
        val kotlinFunction = method.kotlinFunction
        val returnType = kotlinFunction?.returnType
        val isTypedValidatedPresent = returnType?.findAnnotation<TypedValidated>() != null ||
            method.returnType.isAnnotationPresent(TypedValidated::class.java)

        if (isTypedValidatedPresent) {
            if (not { returnValue is TypedValidationEntity }) {
                throw IllegalArgumentException(
                    "The returned object does not implements the TypedValidationEntity interface!"
                )
            }

            val typedValidationException = invokeValidatorForReturnValue(returnValue as TypedValidationEntity)
            if (typedValidationException != null) {
                throw typedValidationException
            }
        }

        return returnValue
    }

    /**
     * Invoke the validator, and return the resulting typed validation exceptions.
     */
    private fun invokeValidatorForArguments(entities: List<TypedValidationEntity>): TypedValidationException? {
        return entities.map { entity -> invokeValidatorForReturnValue(entity) }.firstOrNull()
    }

    /**
     * Invoke the validator, and return the resulting violations.
     */
    private fun invokeValidatorForReturnValue(entity: TypedValidationEntity): TypedValidationException? {
        return entity.isValid().fold(ifLeft = { it }, ifRight = { null })
    }

    private fun isFactoryBeanMetadataMethod(method: Method): Boolean {
        val clazz = method.declaringClass

        // Call from interface-based proxy handle, allowing for an efficient check?
        if (clazz.isInterface) {
            return ((clazz == FactoryBean::class.java || clazz == SmartFactoryBean::class.java) &&
                method.name != "getObject")
        }

        // Call from CGLIB proxy handle, potentially implementing a FactoryBean method?
        var factoryBeanType: Class<*>? = null
        if (SmartFactoryBean::class.java.isAssignableFrom(clazz)) {
            factoryBeanType = SmartFactoryBean::class.java
        } else if (FactoryBean::class.java.isAssignableFrom(clazz)) {
            factoryBeanType = FactoryBean::class.java
        }
        return (factoryBeanType != null && method.name != "getObject" &&
            ClassUtils.hasMethod(factoryBeanType, method))
    }
}
