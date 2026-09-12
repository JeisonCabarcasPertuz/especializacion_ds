package com.unimagdalena.corebanking.pattern.chain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.unimagdalena.corebanking.entity.BankAccount;
import com.unimagdalena.corebanking.entity.Customer;
import com.unimagdalena.corebanking.enums.AccountStatus;
import com.unimagdalena.corebanking.enums.AccountType;
import com.unimagdalena.corebanking.exception.BusinessException;
import com.unimagdalena.corebanking.pattern.state.AccountStateContext;
import com.unimagdalena.corebanking.pattern.state.AccountStateResolver;
import com.unimagdalena.corebanking.pattern.state.ActiveAccountState;
import com.unimagdalena.corebanking.pattern.state.BlockedAccountState;
import com.unimagdalena.corebanking.pattern.state.ClosedAccountState;
import com.unimagdalena.corebanking.pattern.strategy.SavingsTransactionPolicy;
import com.unimagdalena.corebanking.pattern.template.TransactionContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TransactionValidationChainTest {

	private final AccountStateContext accountStateContext = new AccountStateContext(
			new AccountStateResolver(List.of(new ActiveAccountState(), new BlockedAccountState(), new ClosedAccountState())));
	private final TransactionValidationChainProvider chainProvider = new TransactionValidationChainProvider(accountStateContext);
	private final SavingsTransactionPolicy policy = new SavingsTransactionPolicy();

	private BankAccount account(AccountStatus status, BigDecimal balance) {
		Customer customer = Customer.builder().id(UUID.randomUUID()).build();
		return BankAccount.builder()
				.id(UUID.randomUUID())
				.accountNumber("ACC-TEST")
				.customer(customer)
				.accountType(AccountType.SAVINGS)
				.status(status)
				.balance(balance.setScale(2))
				.currency("COP")
				.build();
	}

	@Test
	void validTransferReachesEndOfChain() {
		TransactionContext context = TransactionContext.builder()
				.sourceAccount(account(AccountStatus.ACTIVE, new BigDecimal("200000")))
				.destinationAccount(account(AccountStatus.ACTIVE, BigDecimal.ZERO))
				.amount(new BigDecimal("75000.00"))
				.fee(new BigDecimal("1000.00"))
				.policy(policy)
				.build();

		assertThatCode(() -> chainProvider.forTransfer().validate(context)).doesNotThrowAnyException();
	}

	@Test
	void sameAccountTransferStopsChain() {
		BankAccount account = account(AccountStatus.ACTIVE, new BigDecimal("200000"));
		TransactionContext context = TransactionContext.builder()
				.sourceAccount(account)
				.destinationAccount(account)
				.amount(new BigDecimal("75000.00"))
				.fee(new BigDecimal("1000.00"))
				.policy(policy)
				.build();

		assertThatThrownBy(() -> chainProvider.forTransfer().validate(context))
				.isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getCode())
				.isEqualTo("SAME_ACCOUNT_TRANSFER");
	}

	@Test
	void insufficientFundsStopsChain() {
		TransactionContext context = TransactionContext.builder()
				.sourceAccount(account(AccountStatus.ACTIVE, new BigDecimal("100.00")))
				.destinationAccount(account(AccountStatus.ACTIVE, BigDecimal.ZERO))
				.amount(new BigDecimal("50000.00"))
				.fee(new BigDecimal("1000.00"))
				.policy(policy)
				.build();

		assertThatThrownBy(() -> chainProvider.forTransfer().validate(context))
				.isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getCode())
				.isEqualTo("INSUFFICIENT_FUNDS");
	}
}
