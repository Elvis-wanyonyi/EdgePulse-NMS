package com.wolfcode.MikrotikNetwork.tenants.service;

import com.wolfcode.MikrotikNetwork.tenants.dto.TenantsResponse;
import com.wolfcode.MikrotikNetwork.tenants.entity.Users;
import com.wolfcode.MikrotikNetwork.tenants.repo.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final UsersRepository usersRepository;



    public List<TenantsResponse> getAllTenants() {
        List<Users> users = usersRepository.findAll();
        return users.stream().map(this::MapToResponse).toList();
    }

    private TenantsResponse MapToResponse(Users user) {
        return TenantsResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public void removeTenant(String name) {
        usersRepository.deleteByName(name);
    }
}
