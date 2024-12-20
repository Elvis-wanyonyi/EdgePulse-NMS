package com.wolfcode.MikrotikNetwork.schema;

import com.wolfcode.MikrotikNetwork.tenants.domain.Users;
import com.wolfcode.MikrotikNetwork.tenants.dto.TenantRequest;
import com.wolfcode.MikrotikNetwork.tenants.repo.UsersRepository;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class TenantSchema {

    @Value("${spring.flyway.locations}")
    private String migrationPath;

    @Value("${spring.datasource.url}")
    private String jdbcUrlTemplate;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

   private final UsersRepository  usersRepository;

    public TenantSchema(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }


    public void registerTenant(TenantRequest request) {

        try {

            Flyway flyway = Flyway.configure()
                    .dataSource(jdbcUrlTemplate, username, password)
                    .schemas(request.getName())
                    .locations(migrationPath)
                    .load();
            flyway.migrate();

            Users tenants = Users.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .createdAt(LocalDateTime.now())
                    .password(request.getPassword())
                    .build();
            usersRepository.save(tenants);

        } catch (Exception e) {
            throw new RuntimeException("Ran into a problem creating tenant.", e);
        }

    }
}
