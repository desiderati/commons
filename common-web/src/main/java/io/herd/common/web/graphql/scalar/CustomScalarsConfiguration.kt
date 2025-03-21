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
package io.herd.common.web.graphql.scalar

import graphql.GraphQLContext
import graphql.execution.CoercedVariables
import graphql.language.Value
import graphql.schema.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class CustomScalarsConfiguration {

    @Bean
    fun voidGraphQLScalarType(): GraphQLScalarType {
        return GraphQLScalarType.newScalar()
            .name("Void")
            .description("Void Scalar")
            .coercing(object : Coercing<Void?, String> {

                @Throws(CoercingSerializeException::class)
                override fun serialize(
                    dataFetcherResult: Any,
                    graphQLContext: GraphQLContext,
                    locale: Locale
                ): String {
                    return ""
                }

                @Throws(CoercingParseValueException::class)
                override fun parseValue(
                    input: Any,
                    graphQLContext: GraphQLContext,
                    locale: Locale
                ): Void? {
                    return null
                }

                @Throws(CoercingParseLiteralException::class)
                override fun parseLiteral(
                    input: Value<*>,
                    variables: CoercedVariables,
                    graphQLContext: GraphQLContext,
                    locale: Locale
                ): Void? {
                    return null
                }
            }).build()
    }
}
