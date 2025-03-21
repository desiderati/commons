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
package io.herd.common.web.graphql

import graphql.schema.DataFetchingEnvironment
import org.springframework.core.MethodParameter
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort.Direction
import org.springframework.graphql.data.method.HandlerMethodArgumentResolver
import org.springframework.stereotype.Component

/**
 * Custom HandlerMethodArgumentResolver for resolving Pageable method parameters in Spring GraphQL controllers.
 */
@Component
class PageableGraphQLArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        Pageable::class.java.isAssignableFrom(parameter.nestedIfOptional().nestedParameterType)

    @Suppress("unchecked_cast")
    override fun resolveArgument(
        parameter: MethodParameter,
        environment: DataFetchingEnvironment
    ): Any? {
        val rawMap =
            environment.getArgument<Map<String, Any>?>(parameter.getParameterName())
                ?: return pageRequestWithDefaults()

        val pageNumber = rawMap["pageNumber"] as Int
        require(pageNumber >= 0) { "Page number must be greater than 0" }

        val pageSize = rawMap["pageSize"] as Int
        require(pageSize > 0) { "Page size must be greater than 0" }

        val sortRawMap = rawMap["sort"] as Map<String, *>?
        return if (sortRawMap != null) {
            val direction: Direction = Direction.valueOf(sortRawMap["direction"] as String)
            val properties: List<String> = sortRawMap["properties"] as List<String>
            PageRequest.of(pageNumber, pageSize, direction, *properties.toTypedArray())
        } else {
            PageRequest.of(pageNumber, pageSize)
        }
    }
}

fun pageRequestWithDefaults(): PageRequest = PageRequest.of(0, 25)
