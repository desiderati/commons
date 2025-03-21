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

import graphql.Scalars
import graphql.language.*
import graphql.schema.*
import graphql.schema.idl.SchemaDirectiveWiringEnvironment
import io.herd.common.contentNotEquals
import io.herd.common.not
import org.springframework.stereotype.Component
import org.springframework.graphql.execution.ConnectionTypeDefinitionConfigurer
import org.springframework.graphql.execution.TypeDefinitionConfigurer

/**
 * It adds support to pagination using Spring Data Jpa Pageable.
 *
 * We could have implemented the [TypeDefinitionConfigurer] as [ConnectionTypeDefinitionConfigurer] did.
 */
@Component
class PageableDirective : NameSchemaDirectiveWiring {

    override fun getDirectiveName(): String = "content"

    override fun onField(
        environment: SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition>
    ): GraphQLFieldDefinition {
        environment.element.run {
            getAppliedDirective(getDirectiveName()).let { directive ->
                if (not { environment.registry.hasType(getTypeName(directive)) }) {
                    environment.registry.add(createDynamicPageableTypeDefinition(directive))
                    return transform { it.type(createDynamicPageableObjectType(directive)) }
                } else {
                    return transform { element ->
                        element.type(GraphQLTypeReference(getTypeName(directive).name))
                    }
                }
            }
        }
    }

    private fun GraphQLFieldDefinition.createDynamicPageableObjectType(
        directive: GraphQLAppliedDirective
    ): GraphQLObjectType =
        GraphQLObjectType.newObject()
            .name(getTypeName(directive).name)
            .field { it.name("totalPages").type(Scalars.GraphQLInt) }
            .field { it.name("totalElements").type(Scalars.GraphQLInt) }
            .field {
                it.name("content").type(
                    GraphQLNonNull.nonNull(
                        GraphQLList.list(
                            GraphQLNonNull.nonNull(
                                GraphQLTypeReference(directive.forArgument())
                            )
                        )
                    )
                )
            }.build()

    private fun GraphQLFieldDefinition.createDynamicPageableTypeDefinition(
        directive: GraphQLAppliedDirective
    ): ObjectTypeDefinition =
        ObjectTypeDefinition.newObjectTypeDefinition()
            .name(getTypeName(directive).name)
            .fieldDefinition(FieldDefinition("totalPages", NonNullType(TypeName("Int"))))
            .fieldDefinition(FieldDefinition("totalElements", NonNullType(TypeName("Int"))))
            .fieldDefinition(
                FieldDefinition(
                    "content",
                    NonNullType(ListType(NonNullType(TypeName(directive.forArgument()))))
                )
            ).build()

    private fun GraphQLFieldDefinition.getTypeName(
        directive: GraphQLAppliedDirective
    ): TypeName = TypeName(this.type.let {
        if (it is GraphQLNonNull) {
            (it.wrappedType as GraphQLObjectType).name
        } else {
            (it as GraphQLObjectType).name
        }
    }.let { name ->
        if (name contentNotEquals "Pageable") {
            throw IllegalArgumentException(
                "The directive '${getDirectiveName()}' can only be used in fields of type 'Pageable'."
            )
        }
        name + directive.forArgument()
    })

    private fun GraphQLAppliedDirective.forArgument(): String =
        ((this.getArgument("for").argumentValue).value as StringValue).value
}
