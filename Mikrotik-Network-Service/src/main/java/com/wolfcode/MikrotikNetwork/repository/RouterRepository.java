package com.wolfcode.MikrotikNetwork.repository;

import com.wolfcode.MikrotikNetwork.entity.Routers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RouterRepository extends JpaRepository<Routers, Long> {
    Optional<Routers> findByRouterName(String routerName);

    void deleteByRouterName(String routerName);
}
