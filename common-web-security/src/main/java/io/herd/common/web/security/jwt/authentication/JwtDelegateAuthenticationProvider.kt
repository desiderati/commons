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

package io.herd.common.web.security.jwt.authentication

import io.herd.common.web.security.jwt.authorization.JwtAuthorizationService
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClient

@Slf4j
class JwtDelegateAuthenticationProvider(
    private val jwtDelegateAuthenticationRestClient: RestClient,
    private val jwtDelegateAuthenticationLoginUrl: String
) : AbstractUserDetailsAuthenticationProvider() {

    private var jwtAuthorizationService: JwtAuthorizationService? = null

    @Autowired
    fun setJwtAuthorizationService(jwtAuthorizationService: JwtAuthorizationService?) {
        this.jwtAuthorizationService = jwtAuthorizationService
    }

    override fun additionalAuthenticationChecks(
        userDetails: UserDetails?,
        authentication: UsernamePasswordAuthenticationToken?,
    ) {
        // Do nothing!!!
    }

    override fun retrieveUser(username: String, authentication: UsernamePasswordAuthenticationToken): UserDetails {
        val requestBody: MultiValueMap<String, String> =
            LinkedMultiValueMap<String, String>().apply {
                add(SPRING_SECURITY_FORM_USERNAME_KEY, authentication.principal as String?)
                add(SPRING_SECURITY_FORM_PASSWORD_KEY, authentication.credentials as String?)
            }

        if (authentication !is JwtDelegateAuthenticationToken) throw IllegalArgumentException(
            "Authentication object should be an instance of JwtDelegateAuthenticationToken class!"
        )

        try {
            return jwtDelegateAuthenticationRestClient.post()
                .uri(jwtDelegateAuthenticationLoginUrl)
                .accept(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .exchange { _, response ->
                    when (response.statusCode) {
                        HttpStatus.OK -> {
                            authentication.authorizationHeader =
                                response.headers[JwtAuthorizationService.HEADER_AUTHORIZATION]?.get(0)

                            val delegateAuthentication =
                                jwtAuthorizationService?.getAuthenticationFromAuthorizationHeader(
                                    authentication.authorizationHeader
                                )

                            User(
                                requestBody.getFirst(SPRING_SECURITY_FORM_USERNAME_KEY),
                                requestBody.getFirst(SPRING_SECURITY_FORM_PASSWORD_KEY),
                                delegateAuthentication?.authorities ?: emptyList()
                            )
                        }
                        HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN -> throw BadCredentialsException(
                            "Authentication request is rejected because the credentials are invalid!"
                        )
                        else -> throw BadCredentialsException(
                            "Authentication request is rejected because there's a problem " +
                                "with the delegated authentication provider!"
                        )
                    }
                }
        } catch (ex: Exception) {
            val errorMsg = "Backend authentication repository is unavailable!"
            logger.error(errorMsg, ex)
            throw AuthenticationServiceException(errorMsg, ex)
        }
    }

    override fun createSuccessAuthentication(
        principal: Any?,
        authentication: Authentication,
        user: UserDetails
    ): Authentication {
        if (authentication !is JwtDelegateAuthenticationToken) throw IllegalArgumentException(
            "Authentication object should be an instance of JwtDelegateAuthenticationToken class!"
        )

        // We need to create a new Authentication object, because this one will contain the authorities.
        super.createSuccessAuthentication(principal, authentication, user).let {
            return JwtDelegateAuthenticationToken(
                it.principal,
                it.credentials,
                it.authorities
            ).apply {
                details = it.details
                authorizationHeader = authentication.authorizationHeader
            }
        }
    }
}
