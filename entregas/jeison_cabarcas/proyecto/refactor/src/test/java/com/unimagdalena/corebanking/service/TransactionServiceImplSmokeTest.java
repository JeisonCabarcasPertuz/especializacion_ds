package com.unimagdalena.corebanking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.unimagdalena.corebanking.dto.request.DepositRequest;
import com.unimagdalena.corebanking.dto.request.TransferRequest;
import com.unimagdalena.corebanking.dto.request.WithdrawalRequest;
import com.unimagdalena.corebanking.dto.response.TransactionResponse;
import com.unimagdalena.corebanking.entity.AuditRecord;
import com.unimagdalena.corebanking.entity.BankAccount;
import com.unimagdalena.corebanking.entity.BankTransaction;
import com.unimagdalena.corebanking.entity.Customer;
import com.unimagdalena.corebanking.enums.AccountStatus;
import com.unimagdalena.corebanking.enums.AccountType;
import com.unimagdalena.corebanking.exception.BusinessException;
import com.unimagdalena.corebanking.mapper.TransactionMapper;
import com.unimagdalena.corebanking.repository.AuditRecordRepository;
import com.unimagdalena.corebanking.repository.BankAccountRepository;
import com.unimagdalena.corebanking.repository.BankTransactionRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplSmokeTest {

	@Mock
	private BankAccountRepository accountRepository;

	@Mock
	private BankTransactionRepository transactionRepository;

	@Mock
	private AuditRecordRepository auditRecordRepository;

	private final TransactionMapper transactionMapper = new TransactionMapper();

	private TransactionServiceImpl service() {
		return new TransactionServiceImpl(accountRepository, transactionRepository, auditRecordRepository, transactionMapper);
	}

	private BankAccount account(AccountType type, AccountStatus status, BigDecimal balance) {
		Customer customer = Customer.builder().id(UUID.randomUUID()).build();
		return BankAccount.builder()
				.id(UUID.randomUUID())
				.accountNumber("ACC-" + UUID.randomUUID())
				.customer(customer)
				.accountType(type)
				.status(status)
				.balance(balance.setScale(2))
				.currency("COP")
				.build();
	}

	@Test
	void depositIncreasesBalanceAndRecordsAudit() {
		BankAccount destination = account(AccountType.SAVINGS, AccountStatus.ACTIVE, BigDecimal.ZERO);
		when(accountRepository.findById(destination.getId())).thenReturn(Optional.of(destination));
		when(accountRepository.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));
		when(transactionRepository.save(any(BankTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
		when(auditRecordRepository.save(any(AuditRecord.class))).thenAnswer(inv -> inv.getArgument(0));

		TransactionResponse response = service().deposit(new DepositRequest(destination.getId(), new BigDecimal("200000.00")));

		assertThat(destination.getBalance()).isEqualByComparingTo("200000.00");
		assertThat(response.getFee()).isEqualByComparingTo("0.00");
	}

	@Test
	void successfulTransferDebitsSourceAndCreditsDestination() {
		BankAccount source = account(AccountType.SAVINGS, AccountStatus.ACTIVE, new BigDecimal("200000.00"));
		BankAccount destination = account(AccountType.CHECKING, AccountStatus.ACTIVE, BigDecimal.ZERO);
		when(accountRepository.findById(source.getId())).thenReturn(Optional.of(source));
		when(accountRepository.findById(destination.getId())).thenReturn(Optional.of(destination));
		when(accountRepository.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));
		when(transactionRepository.save(any(BankTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
		when(auditRecordRepository.save(any(AuditRecord.class))).thenAnswer(inv -> inv.getArgument(0));

		TransactionResponse response = service()
				.transfer(new TransferRequest(source.getId(), destination.getId(), new BigDecimal("75000.00")));

		assertThat(source.getBalance()).isEqualByComparingTo("124000.00");
		assertThat(destination.getBalance()).isEqualByComparingTo("75000.00");
		assertThat(response.getFee()).isEqualByComparingTo("1000.00");
	}

	@Test
	void transferWithInsufficientBalanceIsRejected() {
		BankAccount source = account(AccountType.SAVINGS, AccountStatus.ACTIVE, new BigDecimal("100.00"));
		BankAccount destination = account(AccountType.SAVINGS, AccountStatus.ACTIVE, BigDecimal.ZERO);
		when(accountRepository.findById(source.getId())).thenReturn(Optional.of(source));
		when(accountRepository.findById(destination.getId())).thenReturn(Optional.of(destination));

		assertThatThrownBy(() -> service()
				.transfer(new TransferRequest(source.getId(), destination.getId(), new BigDecimal("50000.00"))))
				.isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getCode())
				.isEqualTo("INSUFFICIENT_FUNDS");
	}

	@Test
	void withdrawalFromCheckingAppliesFixedFee() {
		BankAccount account = account(AccountType.CHECKING, AccountStatus.ACTIVE, new BigDecimal("100000.00"));
		when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
		when(accountRepository.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));
		when(transactionRepository.save(any(BankTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
		when(auditRecordRepository.save(any(AuditRecord.class))).thenAnswer(inv -> inv.getArgument(0));

		TransactionResponse response = service().withdraw(new WithdrawalRequest(account.getId(), new BigDecimal("10000.00")));

		assertThat(response.getFee()).isEqualByComparingTo("2000.00");
		assertThat(account.getBalance()).isEqualByComparingTo("88000.00");
	}
}
