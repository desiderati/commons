package io.herd.common.web.graphql

import graphql.kickstart.execution.context.GraphQLKickstartContext
import graphql.kickstart.servlet.context.DefaultGraphQLServletContextBuilder
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.websocket.Session
import jakarta.websocket.server.HandshakeRequest

/**
 * A custom [DefaultGraphQLServletContextBuilder] that creates an [AsyncGraphQLContext] for each request,
 * which is responsible for propagate the {@link LocaleContext} and {@link RequestAttributes}
 * when a method uses the <b>suspend</b> modifier.
 */
class AsyncGraphQLServletContextBuilder : DefaultGraphQLServletContextBuilder() {

    override fun build(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): GraphQLKickstartContext =
        AsyncGraphQLContext(
            mutableMapOf<Any, Any>().also {
                it[HttpServletRequest::class.java] = request
                it[HttpServletResponse::class.java] = response
            }
        )

    override fun build(
        session: Session,
        handshakeRequest: HandshakeRequest
    ): GraphQLKickstartContext =
        AsyncGraphQLContext(
            mutableMapOf<Any, Any>().also {
                it[Session::class.java] = session
                it[HandshakeRequest::class.java] = handshakeRequest
            }
        )
}
