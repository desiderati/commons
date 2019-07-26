/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.configuration;

import lombok.Getter;
import lombok.Setter;

import java.util.Base64;

@Getter
@Setter // Nunca esquecer de colocar os setXXX(..) para arquivos de configuração!
@SuppressWarnings("unused")
public abstract class AbstractSwaggerClientProperties {

    private String host;
    private String basepath;
    private String authUser;
    private String authPass;

    public String getBasicAuthorizationHeader() {
        if (authUser != null && authPass != null) {
            return "Basic " + new String(Base64.getEncoder().encode(
                (authUser + ":" + authPass).getBytes()));
        }
        return null;
    }
}
