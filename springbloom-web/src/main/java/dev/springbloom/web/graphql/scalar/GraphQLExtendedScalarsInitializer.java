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

package dev.springbloom.web.graphql.scalar;

import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.springbloom.web.graphql.scalar.GraphQLScalarUtils.isGraphQLScalarType;

@NoArgsConstructor
public class GraphQLExtendedScalarsInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

    private static final String GRAPHQL_EXTENDED_SCALAR_PREFIX = "spring.graphql.extended-scalars";

    @Override
    public void initialize(@NonNull final GenericApplicationContext applicationContext) {
        final Collection<String> enabledExtendedScalars = getEnabledExtendedScalars(applicationContext);
        final Collection<String> validScalarNames = new HashSet<>();
        ReflectionUtils.doWithFields(
            ExtendedScalars.class,
            scalarField -> {
                if (isGraphQLScalarType(scalarField)) {
                    final GraphQLScalarType graphQLScalarType = (GraphQLScalarType) scalarField.get(null);
                    if (enabledExtendedScalars.contains(graphQLScalarType.getName())) {
                        applicationContext.registerBean(
                            graphQLScalarType.getName(),
                            GraphQLScalarType.class,
                            () -> graphQLScalarType
                        );
                    }
                    validScalarNames.add(graphQLScalarType.getName());
                }
            });

        verifyEnabledScalars(enabledExtendedScalars, validScalarNames);
    }

    @SuppressWarnings("unchecked")
    private Set<String> getEnabledExtendedScalars(final GenericApplicationContext applicationContext) {
        return (Set<String>) applicationContext.getEnvironment()
            .getProperty(GRAPHQL_EXTENDED_SCALAR_PREFIX, Collection.class, Collections.emptySet())
            .stream().map(String::valueOf).collect(Collectors.toSet());
    }

    private void verifyEnabledScalars(
        final Collection<String> enabledExtendedScalars, final Collection<String> validScalarNames) {
        final Collection<String> invalidScalarNames = new HashSet<>(enabledExtendedScalars);
        invalidScalarNames.removeAll(validScalarNames);
        if (!invalidScalarNames.isEmpty()) {
            throw new ApplicationContextException(
                String.format(
                    "Invalid extended scalar name(s) found: %s. Valid names are: %s.",
                    joinNames(invalidScalarNames), joinNames(validScalarNames)
                )
            );
        }
    }

    private String joinNames(final Collection<String> names) {
        return names.stream().sorted().collect(Collectors.joining(", "));
    }
}
