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

import graphql.Scalars;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.herd.common.web.graphql.scalar.GraphQLScalarUtils.extractScalarDefinitions;

@RequiredArgsConstructor
public class GraphQLAliasedScalarsInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

    private static final String GRAPHQL_ALIASED_SCALAR_PREFIX = "spring.graphql.aliased-scalars.";

    private static final String JOINING_SEPARATOR = ", ";

    private static final String NO_BUILT_IN_SCALAR_FOUND = "Scalar(s) '%s' cannot be aliased. "
        + "Only the following scalars can be aliased by configuration: %s. "
        + "Note that custom scalar beans cannot be aliased this way.";

    @Override
    public void initialize(@NonNull final GenericApplicationContext applicationContext) {
        final Map<String, GraphQLScalarType> predefinedScalars
            = extractScalarDefinitions(Scalars.class, ExtendedScalars.class);

        final ConfigurableEnvironment environment = applicationContext.getEnvironment();
        verifyAliasedScalarConfiguration(predefinedScalars, environment);
        predefinedScalars.forEach((scalarName, scalarType) ->
            ((List<?>) environment.getProperty(
                GRAPHQL_ALIASED_SCALAR_PREFIX + scalarName,
                List.class,
                Collections.emptyList())
            ).stream()
                .map(String::valueOf)
                .map(alias -> ExtendedScalars.newAliasedScalar(alias).aliasedScalar(scalarType).build())
                .forEach(aliasedScalar -> applicationContext.registerBean(
                    aliasedScalar.getName(),
                    GraphQLScalarType.class,
                    () -> aliasedScalar)
                )
        );
    }

    private void verifyAliasedScalarConfiguration(
        final Map<String, GraphQLScalarType> predefinedScalars,
        final ConfigurableEnvironment environment
    ) {
        final List<String> invalidScalars = environment.getPropertySources().stream()
            .filter(propertySource -> propertySource instanceof EnumerablePropertySource)
            .map(propertySource -> (EnumerablePropertySource<?>) propertySource)
            .map(EnumerablePropertySource::getPropertyNames)
            .flatMap(Arrays::stream)
            .filter(propertyName -> propertyName.startsWith(GRAPHQL_ALIASED_SCALAR_PREFIX))
            .map(propertyName -> propertyName.replace(GRAPHQL_ALIASED_SCALAR_PREFIX, ""))
            .filter(scalarName -> !predefinedScalars.containsKey(scalarName))
            .sorted().collect(Collectors.toList());

        if (!invalidScalars.isEmpty()) {
            final String validBuildInScalars = predefinedScalars.keySet().stream().sorted()
                .collect(Collectors.joining(JOINING_SEPARATOR));

            throw new ApplicationContextException(String.format(
                NO_BUILT_IN_SCALAR_FOUND,
                String.join(JOINING_SEPARATOR, invalidScalars),
                validBuildInScalars
            ));
        }
    }
}
