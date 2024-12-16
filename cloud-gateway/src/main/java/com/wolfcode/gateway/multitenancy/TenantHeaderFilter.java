package com.wolfcode.gateway.multitenancy;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Objects;

@Component
public class TenantHeaderFilter extends AbstractGatewayFilterFactory<TenantHeaderFilter.Config> {

    public TenantHeaderFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String tenantId = extractTenantFromHost(exchange);

            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-TenantId", tenantId)
                    .build();

            return chain.filter(exchange.mutate().request(request).build());
        };
    }

    private String extractTenantFromHost(ServerWebExchange exchange) {
        String host = Objects.requireNonNull(exchange.getRequest().getHeaders().getHost()).getHostName();
        return host.split("\\.")[0];
    }

    public static class Config {
    }
}
