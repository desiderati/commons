/*
 * Copyright (c) 2024 - Felipe Desiderati
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

import graphql.kickstart.tools.TypeDefinitionFactory
import graphql.language.*

/**
 * It adds support to pagination using Spring Data Jpa Pageable.
 *
 * Based on:
 *  - https://www.graphql-java-kickstart.com/tools/relay/
 *  - https://www.graphql-java-kickstart.com/spring-boot/directives/
 *  - https://www.graphql-java-kickstart.com/spring-boot/type-definition-factory/
 */
class PageableTypeDefinitionConnectionFactory : TypeDefinitionFactory {

    class DirectiveWithField(
        val field: FieldDefinition,
        name: String,
        arguments: List<Argument>,
        sourceLocation: SourceLocation,
        comments: List<Comment>
    ) : Directive(name, arguments, sourceLocation, comments, IgnoredChars.EMPTY, emptyMap()) {
        fun getTypeName(): String {
            return field.type.let {
                if (it is NonNullType) {
                    (it.type as TypeName).name
                } else {
                    (it as TypeName).name
                }
            }
        }
    }

    override fun create(existing: MutableList<Definition<*>>): List<Definition<*>> {
        val declaredPageableDirectives = findDeclaredPageableDirectives(existing)
        if (declaredPageableDirectives.isEmpty()) {
            // do not add Relay definitions unless needed
            return emptyList()
        }

        val dynamicPageableTypeDefinitions = mutableListOf<Definition<*>>()
        declaredPageableDirectives.forEach {
            createDynamicPageableTypeDefinition(it).let { definition ->
                dynamicPageableTypeDefinitions.add(definition)
            }
        }

        return dynamicPageableTypeDefinitions
    }

    private fun findDeclaredPageableDirectives(definitions: List<Definition<*>>): List<DirectiveWithField> {
        return definitions.filterIsInstance<ObjectTypeDefinition>()
            .flatMap { it.fieldDefinitions }
            .flatMap { it.getDirectivesWithField() }
            .filter { it.name == "pageable" }
    }

    private fun FieldDefinition.getDirectivesWithField(): List<DirectiveWithField> {
        return this.directives.map { it.withField(this) }
    }

    private fun Directive.withField(field: FieldDefinition): DirectiveWithField {
        return DirectiveWithField(field, this.name, this.arguments, this.sourceLocation, this.comments)
    }

    private fun createDynamicPageableTypeDefinition(directive: DirectiveWithField): ObjectTypeDefinition =
        ObjectTypeDefinition.newObjectTypeDefinition()
            .name(directive.getTypeName())
            .fieldDefinition(FieldDefinition("totalElements", NonNullType(TypeName("Int"))))
            .fieldDefinition(
                FieldDefinition(
                    "content",
                    NonNullType(ListType(NonNullType(TypeName(directive.forTypeName()))))
                )
            ).build()

    private fun Directive.forTypeName(): String {
        return (this.getArgument("for").value as StringValue).value
    }
}
