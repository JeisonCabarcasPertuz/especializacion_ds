package com.unimagdalena.corebanking.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.unimagdalena.corebanking.dto.request.CreateAccountRequest;
import com.unimagdalena.corebanking.dto.request.CreateCustomerRequest;
import com.unimagdalena.corebanking.dto.request.DepositRequest;
import com.unimagdalena.corebanking.dto.request.TransferRequest;
import com.unimagdalena.corebanking.dto.response.AccountResponse;
import com.unimagdalena.corebanking.dto.response.BalanceResponse;
import com.unimagdalena.corebanking.dto.response.CustomerResponse;
import com.unimagdalena.corebanking.dto.response.TransactionResponse;
import com.unimagdalena.corebanking.enums.AccountType;
import com.unimagdalena.corebanking.enums.DocumentType;
import com.unimagdalena.corebanking.enums.TransactionStatus;
import com.unimagdalena.corebanking.repository.AuditRecordRepository;
import com.unimagdalena.corebanking.service.interfaces.AccountService;
import com.unimagdalena.corebanking.service.interfaces.CustomerService;
import com.unimagdalena.corebanking.service.interfaces.TransactionService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TransferFlowIntegrationTest {

	@Autowired
	private CustomerService customerService;

	@Autowired
	private AccountService accountService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private AuditRecordRepository auditRecordRepository;

	@Test
	void transferFlowExecutesEndToEndAndLeavesAuditEvidence() {
		CustomerResponse customer = customerService.create(
				new CreateCustomerRequest(DocumentType.CC, "900123456", "Ada Lovelace", "ada@example.com"));

		AccountResponse source = accountService.create(customer.getId(), new CreateAccountRequest(AccountType.SAVINGS));
		AccountResponse destination = accountService.create(customer.getId(), new CreateAccountRequest(AccountType.CHECKING));

		transactionService.deposit(new DepositRequest(source.getId(), new BigDecimal("200000.00")));

		TransactionResponse transfer = transactionService.transfer(
				new TransferRequest(source.getId(), destination.getId(), new BigDecimal("75000.00")));

		BalanceResponse sourceBalance = accountService.getBalance(source.getId());
		BalanceResponse destinationBalance = accountService.getBalance(destination.getId());

		assertThat(sourceBalance.getBalance()).isEqualByComparingTo("124000.00");
		assertThat(destinationBalance.getBalance()).isEqualByComparingTo("75000.00");
		assertThat(transfer.getFee()).isEqualByComparingTo("1000.00");
		assertThat(transfer.getStatus()).isEqualTo(TransactionStatus.COMPLETED);

		boolean auditRecordExists = auditRecordRepository.findAll().stream()
				.anyMatch(auditRecord -> auditRecord.getTransaction().getId().equals(transfer.getId()));
		assertThat(auditRecordExists).isTrue();
	}
}
