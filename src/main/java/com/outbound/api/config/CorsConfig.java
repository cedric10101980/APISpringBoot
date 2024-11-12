package com.outbound.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurerComposite;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.cors.reactive.CorsWebFilter;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class CorsConfig {
    public static final List<String> ALLOWED_METHODS_LIST =
            Arrays.asList("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD");
    public static final List<String> ALLOWED_HEADERS_LIST =
            Arrays.asList("Authorization", "Requestor-Type", "Content-Type");
    public static final List<String> EXPOSED_HEADERS_LIST = List.of("Access-Control-Allow-Origin", "X-Get-Header", "Authorization" );

    public static final Long MAX_AGE = 3600L;
    public static final String CORS_CONFIGURATION_PATH = "/**";

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.applyPermitDefaultValues();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(ALLOWED_METHODS_LIST);

        configuration.setAllowedHeaders(ALLOWED_HEADERS_LIST);
        configuration.setExposedHeaders(EXPOSED_HEADERS_LIST);
        configuration.setMaxAge(MAX_AGE);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(CORS_CONFIGURATION_PATH, configuration);

        return new CorsWebFilter(source) {
            @Override
            public Mono<Void> filter(ServerWebExchange ctx, WebFilterChain chain) {
                ServerHttpResponse response = ctx.getResponse();
                HttpHeaders headers = response.getHeaders();
                headers.add("Access-Control-Allow-Origin", "*");
                headers.add("Access-Control-Allow-Credentials", "true");
                headers.add("Access-Control-Allow-Methods", "GET,HEAD,OPTIONS,POST,PUT");
                headers.add("Access-Control-Allow-Headers", "Access-Control-Allow-Headers, Origin,Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization, Requestor-Type, Content-Type");

                //headers.add("Access-Control-Allow-Origin", "https://eus2-02.integration.ws-nonprod.avayacloud.com");
                return chain.filter(ctx);
            }
        };
    }
}