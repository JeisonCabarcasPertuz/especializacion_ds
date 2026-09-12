package com.unimagdalena.corebanking.repository;

import com.unimagdalena.corebanking.entity.BankAccount;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {

	List<BankAccount> findAllByCustomerId(UUID customerId);
}
