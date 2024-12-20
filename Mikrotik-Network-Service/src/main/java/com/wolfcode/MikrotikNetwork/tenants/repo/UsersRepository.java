package com.wolfcode.MikrotikNetwork.tenants.repo;

import com.wolfcode.MikrotikNetwork.tenants.domain.Users;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Transactional
public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String username);

    void deleteByName(String name);
}
