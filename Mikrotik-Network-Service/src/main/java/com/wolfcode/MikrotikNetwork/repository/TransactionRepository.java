package com.wolfcode.MikrotikNetwork.repository;

import com.wolfcode.MikrotikNetwork.entity.TransactionsInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionsInfo, Long> {
}
