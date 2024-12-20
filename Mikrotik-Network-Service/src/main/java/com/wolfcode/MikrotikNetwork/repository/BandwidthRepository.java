package com.wolfcode.MikrotikNetwork.repository;

import com.wolfcode.MikrotikNetwork.entity.BandwidthLimits;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BandwidthRepository extends JpaRepository<BandwidthLimits, Long> {
    Optional<BandwidthLimits> findByName(@NotNull(message = "Choose bandwidth limit") String bandwidthLimit);

}
