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
package io.herd.common.web.security.oauth2

import io.herd.common.web.security.jwt.JwtService
import io.herd.common.web.security.jwt.authentication.SelfContainedJwtAuthenticationHeaderConfigurer
import lombok.extern.slf4j.Slf4j
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequestEntityConverter
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.util.StringUtils
import org.springframework.web.client.RestTemplate

@Slf4j
class P2fOAuth2UserService : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    companion object {
        private const val MISSING_USER_INFO_URI_ERROR_CODE = "missing_user_info_uri"
        private const val INVALID_USER_INFO_RESPONSE_ERROR_CODE = "invalid_user_info_response"

        private val PARAMETERIZED_RESPONSE_TYPE: ParameterizedTypeReference<List<Map<String, Any>>> =
            object : ParameterizedTypeReference<List<Map<String, Any>>>() {}
    }

    private val requestEntityConverter: Converter<OAuth2UserRequest, RequestEntity<*>> =
        OAuth2UserRequestEntityConverter()

    private lateinit var jwtService: JwtService
    private lateinit var jwtAuthenticationHeaderConfigurer: SelfContainedJwtAuthenticationHeaderConfigurer

    fun setJwtService(jwtService: JwtService) {
        this.jwtService = jwtService
    }

    fun setJwtAuthenticationHeaderConfigurer(
        jwtAuthenticationHeaderConfigurer: SelfContainedJwtAuthenticationHeaderConfigurer
    ) {
        this.jwtAuthenticationHeaderConfigurer = jwtAuthenticationHeaderConfigurer
    }


    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        if (!StringUtils.hasText(userRequest.clientRegistration.providerDetails.userInfoEndpoint.uri)) {
            val oauth2Error =
                OAuth2Error(
                    MISSING_USER_INFO_URI_ERROR_CODE,
                    "Missing required UserInfo Uri in UserInfoEndpoint for Client Registration: " +
                        userRequest.clientRegistration.registrationId,
                    null
                )
            throw OAuth2AuthenticationException(oauth2Error, oauth2Error.toString())
        }

        val request = requestEntityConverter.convert(userRequest)!!
        return getOAuth2User(request)
    }

    private fun getOAuth2User(request: RequestEntity<*>): OAuth2User {
        try {
            return RestTemplate().exchange(request, PARAMETERIZED_RESPONSE_TYPE).let { response ->
                when (response.statusCode) {
                    HttpStatus.OK -> {
                        val authorizationHeader =
                            jwtAuthenticationHeaderConfigurer.resolveAuthorizationHeader(response.headers)
                                ?: throw AuthenticationServiceException(
                                    "OAuth user info provider returned without an authorization header defined!"
                                )

                        val jwtToken =
                            jwtAuthenticationHeaderConfigurer.resolveBearerToken(
                                authorizationHeader
                            )

                        val delegateAuthentication =
                            jwtService.extractFromToken<JwtAuthenticationToken>(jwtToken)

                        P2fOAuth2User(
                            authorizationHeader,
                            delegateAuthentication.authorities,
                            mutableMapOf("principal" to delegateAuthentication.principal),
                            "principal"
                        )
                    }

                    HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN -> throw BadCredentialsException(
                        "Authentication request is rejected because the access token is invalid!"
                    )

                    else -> throw AuthenticationServiceException(
                        "Authentication request is rejected because there's a problem " +
                            "with the OAuth user info provider!"
                    )
                }
            }
        } catch (ex: Exception) {
            val errorMsg = "An error occurred while attempting to retrieve the UserInfo Resource: ${ex.message}"
            val oauth2Error = OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE, errorMsg, null)
            throw OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex)
        }
    }
}
