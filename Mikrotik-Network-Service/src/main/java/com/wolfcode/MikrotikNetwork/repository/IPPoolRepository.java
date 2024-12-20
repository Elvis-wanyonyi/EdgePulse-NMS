package com.wolfcode.MikrotikNetwork.repository;

import com.wolfcode.MikrotikNetwork.entity.IPPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPPoolRepository extends JpaRepository<IPPool,Long> {
    void deletePoolByPoolName(String name);

    IPPool findByPoolName(String name);
}
