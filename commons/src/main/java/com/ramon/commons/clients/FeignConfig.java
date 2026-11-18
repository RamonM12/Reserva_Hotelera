package com.ramon.commons.clients;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    // Copia el header "Authorization: Bearer ..." de la petición original a cada llamada Feign
    @Bean
    public RequestInterceptor reenviarToken() {
        return template -> {
            ServletRequestAttributes atributos =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (atributos != null) {
                String token = atributos.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
                if (token != null) {
                    template.header(HttpHeaders.AUTHORIZATION, token);
                }
            }
        };
    }
}