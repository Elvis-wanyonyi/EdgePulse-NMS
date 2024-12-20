package com.wolfcode.MikrotikNetwork.multitenancy;

import org.springframework.lang.Nullable;

@FunctionalInterface
public interface TenantResolver<T> {


    @Nullable
    String resolveTenantIdentifier(T source);

}

