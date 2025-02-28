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

package io.herd.common.web.graphql.scalar;

import graphql.schema.GraphQLScalarType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.ReflectionUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GraphQLScalarUtils {

    /**
     * Extract scalar field definitions from helper classes. Public static
     * {@link GraphQLScalarType} fields are considered as scalar definitions.
     *
     * @param classes classes that may contain scalar definitions.
     * @return the map of scalar definitions (keys = scalar names, values are scalar type definitions).
     * May return an empty map if no definitions are found. If multiple source classes define GraphQL
     * scalar types with the same definition, then the last one will be included in the map.
     */
    public static Map<String, GraphQLScalarType> extractScalarDefinitions(final Class<?>... classes) {
        final Map<String, GraphQLScalarType> scalarTypes = new HashMap<>();
        Stream.of(classes).forEach(clazz -> extractScalarField(clazz, scalarTypes));
        return scalarTypes;
    }

    public static boolean isGraphQLScalarType(Field field) {
        return Modifier.isPublic(field.getModifiers())
            && Modifier.isStatic(field.getModifiers())
            && field.getType().equals(GraphQLScalarType.class);
    }

    private static void extractScalarField(Class<?> clazz, Map<String, GraphQLScalarType> target) {
        ReflectionUtils.doWithFields(clazz, scalarField -> extractedIfScalarField(target, scalarField));
    }

    private static void extractedIfScalarField(
        Map<String, GraphQLScalarType> target,
        Field field
    ) throws IllegalAccessException {
        if (isGraphQLScalarType(field)) {
            final GraphQLScalarType graphQLScalarType = (GraphQLScalarType) field.get(null);
            target.put(graphQLScalarType.getName(), graphQLScalarType);
        }
    }
}
