package com.wolfcode.MikrotikNetwork.repository;

import com.wolfcode.MikrotikNetwork.dto.ServiceType;
import com.wolfcode.MikrotikNetwork.entity.Plans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlansRepository extends JpaRepository<Plans, Long> {

    void deleteProfileByPlanName(String name);

    Optional<Plans> findByPlanName(String packageType);

    List<Plans> findAllByServiceType(ServiceType serviceType);
}
