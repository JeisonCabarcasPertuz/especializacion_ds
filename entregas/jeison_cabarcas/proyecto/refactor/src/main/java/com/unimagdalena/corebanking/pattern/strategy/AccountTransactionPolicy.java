package com.unimagdalena.corebanking.pattern.strategy;

import com.unimagdalena.corebanking.enums.AccountType;
import com.unimagdalena.corebanking.enums.TransactionType;
import java.math.BigDecimal;

public interface AccountTransactionPolicy {

	AccountType supportedAccountType();

	BigDecimal calculateFee(TransactionType transactionType, BigDecimal amount);

	BigDecimal maxDebitAmount();
}
