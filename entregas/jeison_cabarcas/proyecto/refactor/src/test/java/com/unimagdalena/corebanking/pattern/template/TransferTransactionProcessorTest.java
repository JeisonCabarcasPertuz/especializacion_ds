package com.unimagdalena.corebanking.pattern.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.unimagdalena.corebanking.dto.response.TransactionResponse;
import com.unimagdalena.corebanking.entity.AuditRecord;
import com.unimagdalena.corebanking.entity.BankAccount;
import com.unimagdalena.corebanking.entity.BankTransaction;
import com.unimagdalena.corebanking.entity.Customer;
import com.unimagdalena.corebanking.enums.AccountStatus;
import com.unimagdalena.corebanking.enums.AccountType;
import com.unimagdalena.corebanking.enums.TransactionType;
import com.unimagdalena.corebanking.mapper.TransactionMapper;
import com.unimagdalena.corebanking.pattern.chain.TransactionValidationChainProvider;
import com.unimagdalena.corebanking.pattern.observer.AuditTransactionObserver;
import com.unimagdalena.corebanking.pattern.observer.NotificationTransactionObserver;
import com.unimagdalena.corebanking.pattern.observer.TransactionEventPublisher;
import com.unimagdalena.corebanking.pattern.state.AccountStateContext;
import com.unimagdalena.corebanking.pattern.state.AccountStateResolver;
import com.unimagdalena.corebanking.pattern.state.ActiveAccountState;
import com.unimagdalena.corebanking.pattern.state.BlockedAccountState;
import com.unimagdalena.corebanking.pattern.state.ClosedAccountState;
import com.unimagdalena.corebanking.pattern.strategy.SavingsTransactionPolicy;
import com.unimagdalena.corebanking.pattern.strategy.TransactionPolicyResolver;
import com.unimagdalena.corebanking.repository.AuditRecordRepository;
import com.unimagdalena.corebanking.repository.BankAccountRepository;
import com.unimagdalena.corebanking.repository.BankTransactionRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransferTransactionProcessorTest {

	@Mock
	private BankAccountRepository accountRepository;

	@Mock
	private BankTransactionRepository transactionRepository;

	@Mock
	private AuditRecordRepository auditRecordRepository;

	private final TransactionPolicyResolver policyResolver = new TransactionPolicyResolver(
			List.of(new SavingsTransactionPolicy()));
	private final AccountStateContext accountStateContext = new AccountStateContext(
			new AccountStateResolver(List.of(new ActiveAccountState(), new BlockedAccountState(), new ClosedAccountState())));
	private final TransactionValidationChainProvider validationChainProvider = new TransactionValidationChainProvider(
			accountStateContext);
	private final TransactionMapper transactionMapper = new TransactionMapper();

	private TransferTransactionProcessor processor() {
		TransactionEventPublisher eventPublisher = new TransactionEventPublisher(
				List.of(new AuditTransactionObserver(auditRecordRepository), new NotificationTransactionObserver()));
		return new TransferTransactionProcessor(accountRepository, transactionRepository, policyResolver,
				validationChainProvider, transactionMapper, eventPublisher);
	}

	private BankAccount account(BigDecimal balance) {
		Customer customer = Customer.builder().id(UUID.randomUUID()).build();
		return BankAccount.builder()
				.id(UUID.randomUUID())
				.accountNumber("ACC-" + UUID.randomUUID())
				.customer(customer)
				.accountType(AccountType.SAVINGS)
				.status(AccountStatus.ACTIVE)
				.balance(balance.setScale(2))
				.currency("COP")
				.build();
	}

	@Test
	void transferDebitsSourceIncludingFeeAndCreditsDestination() {
		BankAccount source = account(new BigDecimal("200000.00"));
		BankAccount destination = account(BigDecimal.ZERO);
		when(accountRepository.findById(source.getId())).thenReturn(Optional.of(source));
		when(accountRepository.findById(destination.getId())).thenReturn(Optional.of(destination));
		when(accountRepository.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));
		when(transactionRepository.save(any(BankTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
		when(auditRecordRepository.save(any(AuditRecord.class))).thenAnswer(inv -> inv.getArgument(0));

		TransactionCommand command = TransactionCommand.builder()
				.type(TransactionType.TRANSFER)
				.sourceAccountId(source.getId())
				.destinationAccountId(destination.getId())
				.amount(new BigDecimal("75000.00"))
				.build();

		TransactionResponse response = processor().process(command);

		assertThat(source.getBalance()).isEqualByComparingTo("124000.00");
		assertThat(destination.getBalance()).isEqualByComparingTo("75000.00");
		assertThat(response.getFee()).isEqualByComparingTo("1000.00");
	}
}
