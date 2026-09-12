package com.unimagdalena.corebanking.repository;

import com.unimagdalena.corebanking.entity.Customer;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

	boolean existsByDocumentNumber(String documentNumber);
}
