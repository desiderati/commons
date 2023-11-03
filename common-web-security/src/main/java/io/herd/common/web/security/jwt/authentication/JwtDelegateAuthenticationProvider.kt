package io.herd.common.web.security.jwt.authentication

import com.fasterxml.jackson.databind.JsonNode
import lombok.extern.slf4j.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

@Slf4j
class JwtDelegateAuthenticationProvider(
    private val jwtDelegateAuthenticationRestTemplate: RestTemplate,
    private val jwtDelegateAuthenticationLoginUrl: String
) : AbstractUserDetailsAuthenticationProvider() {

    override fun additionalAuthenticationChecks(
        userDetails: UserDetails?,
        authentication: UsernamePasswordAuthenticationToken?,
    ) {
        // Do nothing!!!
    }

    override fun retrieveUser(username: String, authentication: UsernamePasswordAuthenticationToken): UserDetails {
        val requestBody: MultiValueMap<String, String> =
            LinkedMultiValueMap<String, String>().apply {
                add(
                    UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY,
                    authentication.principal as String?
                )

                add(
                    UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY,
                    authentication.credentials as String?
                )
            }

        if (authentication !is JwtDelegateAuthenticationToken) throw IllegalArgumentException(
            "Authentication object should be an instance of JwtAuthenticationToken class!"
        )

        try {
            val responseEntity =
                jwtDelegateAuthenticationRestTemplate.postForEntity<JsonNode>(
                    jwtDelegateAuthenticationLoginUrl,
                    requestBody
                )

            if (responseEntity.statusCode == HttpStatus.OK) {
                authentication.authorizationHeader =
                    responseEntity.headers[JwtAuthenticationFilter.HEADER_AUTHORIZATION]?.get(0)

                return User(
                    requestBody.getFirst(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY),
                    requestBody.getFirst(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY),
                    emptyList()
                )
            }
        } catch (ex: Exception) {
            val errorMsg = "Backend authentication repository is unavailable!"
            logger.error(errorMsg, ex)
            throw AuthenticationServiceException(errorMsg, ex)
        }

        throw BadCredentialsException("Authentication request is rejected because the credentials are invalid!")
    }

    override fun createSuccessAuthentication(
        principal: Any?,
        authentication: Authentication,
        user: UserDetails
    ): Authentication {
        if (authentication !is JwtDelegateAuthenticationToken) throw IllegalArgumentException(
            "Authentication object should be an instance of JwtAuthenticationToken class!"
        )

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
