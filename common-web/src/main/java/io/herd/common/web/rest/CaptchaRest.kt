package io.herd.common.web.rest

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.herd.common.google.GoogleCaptchaService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnProperty("google.captcha.secret-key")
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
class CaptchaRest(
    private val objectMapper: ObjectMapper,
    private val googleCaptchaService: GoogleCaptchaService,
) : PublicRest {

    @PostMapping
    @RequestMapping("/captcha")
    fun validateCaptcha(@RequestBody payload: String): Boolean {
        val payloadNode: JsonNode = objectMapper.readTree(payload)
        val captchaResponse = payloadNode.get("captchaResponse").asText()
        return googleCaptchaService.isCaptchaValid(captchaResponse)
    }
}
