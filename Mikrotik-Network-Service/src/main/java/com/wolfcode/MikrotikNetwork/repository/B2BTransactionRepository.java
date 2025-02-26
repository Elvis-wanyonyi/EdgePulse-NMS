package com.wolfcode.MikrotikNetwork.repository;

import com.wolfcode.MikrotikNetwork.entity.B2BTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface B2BTransactionRepository extends JpaRepository<B2BTransaction, Long> {

}
