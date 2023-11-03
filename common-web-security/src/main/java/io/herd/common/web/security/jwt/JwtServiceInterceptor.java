package io.herd.common.web.security.jwt;

public interface JwtServiceInterceptor {

    void onBeforeExtractPayload(String token);

}
