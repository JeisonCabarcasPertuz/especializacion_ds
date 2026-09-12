package com.unimagdalena.corebanking.pattern.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import com.unimagdalena.corebanking.enums.AccountType;
import com.unimagdalena.corebanking.enums.TransactionType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class SavingsTransactionPolicyTest {

	private final SavingsTransactionPolicy policy = new SavingsTransactionPolicy();

	@Test
	void supportsSavingsAccountType() {
		assertThat(policy.supportedAccountType()).isEqualTo(AccountType.SAVINGS);
	}

	@Test
	void depositAndWithdrawalHaveNoFee() {
		assertThat(policy.calculateFee(TransactionType.DEPOSIT, new BigDecimal("100.00"))).isEqualByComparingTo("0.00");
		assertThat(policy.calculateFee(TransactionType.WITHDRAWAL, new BigDecimal("100.00"))).isEqualByComparingTo("0.00");
	}

	@Test
	void transferChargesFixedFee() {
		assertThat(policy.calculateFee(TransactionType.TRANSFER, new BigDecimal("75000.00"))).isEqualByComparingTo("1000.00");
	}

	@Test
	void maxDebitAmountIsFiveMillion() {
		assertThat(policy.maxDebitAmount()).isEqualByComparingTo("5000000.00");
	}
}
