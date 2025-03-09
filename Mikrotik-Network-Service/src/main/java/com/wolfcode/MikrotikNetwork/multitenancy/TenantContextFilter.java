package com.wolfcode.MikrotikNetwork.multitenancy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
public class TenantContextFilter extends OncePerRequestFilter {

    private final HttpHeaderTenantResolver httpRequestTenantResolver;

    public TenantContextFilter(HttpHeaderTenantResolver httpRequestTenantResolver) {
        this.httpRequestTenantResolver = httpRequestTenantResolver;
    }


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        var tenantIdentifier = httpRequestTenantResolver.resolveTenantIdentifier(request);

        if (StringUtils.hasText(tenantIdentifier)) {
            TenantContext.setCurrentTenant(tenantIdentifier);
        } else if (!shouldNotFilter(request)) {
            throw new IllegalArgumentException("A valid tenant must be specified for requests to %s".formatted(request.getRequestURI()));
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return requestURI.matches("^/user(/.*)?$") ||
                requestURI.startsWith("/swagger-ui") ||
                requestURI.startsWith("/v3/api-docs") ||
                requestURI.startsWith("/swagger-resources") ||
                requestURI.startsWith("/actuator");
    }

    private void clear() {
        TenantContext.clear();
    }
}