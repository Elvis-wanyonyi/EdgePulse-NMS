package com.wolfcode.MikrotikNetwork.schema;

import com.wolfcode.MikrotikNetwork.tenants.dto.TenantRequest;
import com.wolfcode.MikrotikNetwork.tenants.entity.Users;
import com.wolfcode.MikrotikNetwork.tenants.repo.UsersRepository;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;


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
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        Optional<Users> user = usersRepository.findByEmail(request.getEmail());
        if(user.isPresent()) {
            throw new RuntimeException("User already exists");
        }

        try {
            Users tenants = Users.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .createdAt(LocalDateTime.now())
                    .password(request.getPassword())
                    .build();
            usersRepository.save(tenants);

            Flyway flyway = Flyway.configure()
                    .dataSource(jdbcUrlTemplate, username, password)
                    .schemas(request.getName())
                    .locations(migrationPath)
                    .load();
            flyway.migrate();

        } catch (Exception e) {
            throw new RuntimeException("Ran into a problem creating tenant.", e);
        }

    }
}
