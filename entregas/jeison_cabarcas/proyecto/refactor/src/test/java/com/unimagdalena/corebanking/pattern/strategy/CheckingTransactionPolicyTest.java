package com.unimagdalena.corebanking.pattern.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import com.unimagdalena.corebanking.enums.AccountType;
import com.unimagdalena.corebanking.enums.TransactionType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CheckingTransactionPolicyTest {

	private final CheckingTransactionPolicy policy = new CheckingTransactionPolicy();

	@Test
	void supportsCheckingAccountType() {
		assertThat(policy.supportedAccountType()).isEqualTo(AccountType.CHECKING);
	}

	@Test
	void depositHasNoFee() {
		assertThat(policy.calculateFee(TransactionType.DEPOSIT, new BigDecimal("100.00"))).isEqualByComparingTo("0.00");
	}

	@Test
	void withdrawalChargesFixedFee() {
		assertThat(policy.calculateFee(TransactionType.WITHDRAWAL, new BigDecimal("10000.00"))).isEqualByComparingTo("2000.00");
	}

	@Test
	void transferChargesFixedFee() {
		assertThat(policy.calculateFee(TransactionType.TRANSFER, new BigDecimal("75000.00"))).isEqualByComparingTo("1500.00");
	}

	@Test
	void maxDebitAmountIsTenMillion() {
		assertThat(policy.maxDebitAmount()).isEqualByComparingTo("10000000.00");
	}
}
