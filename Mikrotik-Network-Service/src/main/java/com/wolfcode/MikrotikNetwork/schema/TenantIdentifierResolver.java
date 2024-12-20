package com.wolfcode.MikrotikNetwork.schema;

import com.wolfcode.MikrotikNetwork.multitenancy.TenantContext;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public class TenantIdentifierResolver  implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {

    public static final String DEFAULT_TENANT = "PUBLIC";

    @Override
    public String resolveCurrentTenantIdentifier() {

        return Objects.requireNonNullElse(TenantContext.getCurrentTenant(), DEFAULT_TENANT);
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}