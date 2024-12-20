package com.wolfcode.MikrotikNetwork.repository;

import com.wolfcode.MikrotikNetwork.entity.PPPoEPlans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PPPoERepository extends JpaRepository<PPPoEPlans, Long> {
    PPPoEPlans findByName(String name);

    void deleteProfileByName(String name);
}
