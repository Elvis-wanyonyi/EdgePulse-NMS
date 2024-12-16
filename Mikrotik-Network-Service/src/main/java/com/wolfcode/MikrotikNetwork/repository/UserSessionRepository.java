package com.wolfcode.MikrotikNetwork.repository;

import com.wolfcode.MikrotikNetwork.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    @Query("SELECT u FROM UserSession u WHERE u.sessionEndTime < :now")
    List<UserSession> findExpiredSessions(@Param("now") LocalDateTime now);

    void deleteByUsername(String username);
}
