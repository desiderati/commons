/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.security.configuration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;

@EnableWebSecurity
@SpringBootConfiguration
@SuppressWarnings("unused")
public abstract class AbstractWebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    /**
     * Por padrão deixamos todas as requisições abertas.
     */
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        // Não precisamos do CSRF por padrão. Autoriza todas as demais requisições.
        httpSecurity.csrf().disable().authorizeRequests().anyRequest().permitAll();

        // Não desejamos habilitar o uso de sessão uma vez que o Token JWT contém as informações que precisamos.
        httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            // Desabilita o Cache de páginas.
            .and().headers().cacheControl();
    }

    @Override
    public void configure(WebSecurity web) {
        web.httpFirewall(allowUrlEncodedSlashHttpFirewall());
    }

    protected HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        DefaultHttpFirewall firewall = new DefaultHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        return firewall;
    }
}
