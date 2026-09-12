package com.unimagdalena.corebanking.repository;

import com.unimagdalena.corebanking.entity.BankTransaction;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, UUID> {

	@Query("""
			select t from BankTransaction t
			where t.sourceAccount.id = :accountId
			   or t.destinationAccount.id = :accountId
			order by t.createdAt desc
			""")
	List<BankTransaction> findByAccountIdOrderByCreatedAtDesc(@Param("accountId") UUID accountId);
}
