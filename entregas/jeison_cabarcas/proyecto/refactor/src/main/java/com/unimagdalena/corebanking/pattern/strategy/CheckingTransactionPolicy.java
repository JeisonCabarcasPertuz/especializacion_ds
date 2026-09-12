package com.unimagdalena.corebanking.pattern.strategy;

import com.unimagdalena.corebanking.enums.AccountType;
import com.unimagdalena.corebanking.enums.TransactionType;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class CheckingTransactionPolicy implements AccountTransactionPolicy {

	private static final BigDecimal WITHDRAWAL_FEE = new BigDecimal("2000.00");
	private static final BigDecimal TRANSFER_FEE = new BigDecimal("1500.00");
	private static final BigDecimal MAX_DEBIT_AMOUNT = new BigDecimal("10000000.00");
	private static final BigDecimal NO_FEE = BigDecimal.ZERO.setScale(2);

	@Override
	public AccountType supportedAccountType() {
		return AccountType.CHECKING;
	}

	@Override
	public BigDecimal calculateFee(TransactionType transactionType, BigDecimal amount) {
		return switch (transactionType) {
			case WITHDRAWAL -> WITHDRAWAL_FEE;
			case TRANSFER -> TRANSFER_FEE;
			case DEPOSIT -> NO_FEE;
		};
	}

	@Override
	public BigDecimal maxDebitAmount() {
		return MAX_DEBIT_AMOUNT;
	}
}
