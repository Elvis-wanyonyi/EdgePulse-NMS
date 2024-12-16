package com.wolfcode.MikrotikNetwork.multitenancy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class TenantContext {

    private static final Logger log = LoggerFactory.getLogger(TenantContext.class);

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setCurrentTenant(String tenant) {
        Assert.hasText(tenant, "tenant cannot be empty");
        CURRENT_TENANT.set(tenant);
    }

    @Nullable
    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static String getRequiredTenantIdentifier() {
        var tenant = getCurrentTenant();
        if (!StringUtils.hasText(tenant)) {
            throw new IllegalArgumentException("No tenant found in the current context");
        }
        return tenant;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
