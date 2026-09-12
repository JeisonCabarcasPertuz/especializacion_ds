package com.unimagdalena.corebanking.pattern.observer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unimagdalena.corebanking.entity.AuditRecord;
import com.unimagdalena.corebanking.entity.BankAccount;
import com.unimagdalena.corebanking.entity.BankTransaction;
import com.unimagdalena.corebanking.entity.Customer;
import com.unimagdalena.corebanking.enums.AccountStatus;
import com.unimagdalena.corebanking.enums.AccountType;
import com.unimagdalena.corebanking.enums.TransactionStatus;
import com.unimagdalena.corebanking.enums.TransactionType;
import com.unimagdalena.corebanking.repository.AuditRecordRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditTransactionObserverTest {

	@Mock
	private AuditRecordRepository auditRecordRepository;

	private BankAccount account(String accountNumber) {
		Customer customer = Customer.builder().id(UUID.randomUUID()).build();
		return BankAccount.builder()
				.id(UUID.randomUUID())
				.accountNumber(accountNumber)
				.customer(customer)
				.accountType(AccountType.SAVINGS)
				.status(AccountStatus.ACTIVE)
				.balance(BigDecimal.ZERO.setScale(2))
				.currency("COP")
				.build();
	}

	@Test
	void persistsAuditRecordForCompletedTransaction() {
		BankAccount source = account("ACC-SOURCE");
		BankAccount destination = account("ACC-DEST");
		BankTransaction transaction = BankTransaction.builder()
				.id(UUID.randomUUID())
				.type(TransactionType.TRANSFER)
				.amount(new BigDecimal("75000.00"))
				.fee(new BigDecimal("1000.00"))
				.status(TransactionStatus.COMPLETED)
				.sourceAccount(source)
				.destinationAccount(destination)
				.build();
		TransactionCompletedEvent event = TransactionCompletedEvent.builder()
				.transaction(transaction)
				.sourceAccount(source)
				.destinationAccount(destination)
				.amount(transaction.getAmount())
				.fee(transaction.getFee())
				.build();
		when(auditRecordRepository.save(any(AuditRecord.class))).thenAnswer(inv -> inv.getArgument(0));

		new AuditTransactionObserver(auditRecordRepository).onTransactionCompleted(event);

		ArgumentCaptor<AuditRecord> captor = ArgumentCaptor.forClass(AuditRecord.class);
		verify(auditRecordRepository).save(captor.capture());
		assertThat(captor.getValue().getTransaction()).isEqualTo(transaction);
		assertThat(captor.getValue().getEventType()).isEqualTo("TRANSFER_COMPLETED");
		assertThat(captor.getValue().getDescription()).contains("ACC-SOURCE").contains("ACC-DEST");
	}
}
