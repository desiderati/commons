package io.herd.common.web.graphql

import graphql.ExecutionResult
import graphql.execution.AsyncExecutionStrategy
import graphql.execution.ExecutionContext
import graphql.execution.ExecutionStrategyParameters
import graphql.kickstart.autoconfigure.web.servlet.GraphQLWebAutoConfiguration
import io.herd.common.web.graphql.exception.GraphQLExceptionHandler
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component(GraphQLWebAutoConfiguration.QUERY_EXECUTION_STRATEGY)
class AsyncTransactionalExecutionStrategy(
    graphQLExceptionHandler: GraphQLExceptionHandler,
) : AsyncExecutionStrategy(graphQLExceptionHandler) {
    @Transactional
    override fun execute(
        executionContext: ExecutionContext,
        parameters: ExecutionStrategyParameters,
    ): CompletableFuture<ExecutionResult> {
        return super.execute(executionContext, parameters)
    }
}
