package com.unimagdalena.corebanking.pattern.strategy;

import com.unimagdalena.corebanking.enums.AccountType;
import com.unimagdalena.corebanking.enums.TransactionType;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class SavingsTransactionPolicy implements AccountTransactionPolicy {

	private static final BigDecimal TRANSFER_FEE = new BigDecimal("1000.00");
	private static final BigDecimal MAX_DEBIT_AMOUNT = new BigDecimal("5000000.00");
	private static final BigDecimal NO_FEE = BigDecimal.ZERO.setScale(2);

	@Override
	public AccountType supportedAccountType() {
		return AccountType.SAVINGS;
	}

	@Override
	public BigDecimal calculateFee(TransactionType transactionType, BigDecimal amount) {
		return transactionType == TransactionType.TRANSFER ? TRANSFER_FEE : NO_FEE;
	}

	@Override
	public BigDecimal maxDebitAmount() {
		return MAX_DEBIT_AMOUNT;
	}
}
