package com.outbound.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class CustomHeaderWebFilter implements WebFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    @Value("${security.secret.key}")
    private String secretKey;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestPath = exchange.getRequest().getPath().toString();

        // Permit all Swagger URLs
        if (requestPath.startsWith("/webjars/swagger-ui/")
                || requestPath.startsWith("/v3/api-docs")
                || requestPath.startsWith("/swagger-resources")
                || requestPath.startsWith("/health")
                || requestPath.startsWith("/.well-known")
                || exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        String headerValue = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION_HEADER);

        if (headerValue == null || !headerValue.equals(secretKey)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }
}