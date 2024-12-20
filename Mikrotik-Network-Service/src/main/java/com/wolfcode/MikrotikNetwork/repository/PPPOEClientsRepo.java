package com.wolfcode.MikrotikNetwork.repository;

import com.wolfcode.MikrotikNetwork.entity.PPPoEClients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PPPOEClientsRepo extends JpaRepository<PPPoEClients,Long> {
}
