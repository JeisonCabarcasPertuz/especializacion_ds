package com.unimagdalena.corebanking.pattern.chain;

import com.unimagdalena.corebanking.exception.BusinessException;
import com.unimagdalena.corebanking.pattern.template.TransactionContext;
import org.springframework.http.HttpStatus;

public class TransactionLimitValidator extends AbstractTransactionValidator {

	@Override
	protected void checkRule(TransactionContext context) {
		if (context.getAmount().compareTo(context.getPolicy().maxDebitAmount()) > 0) {
			throw new BusinessException("TRANSACTION_LIMIT_EXCEEDED", HttpStatus.UNPROCESSABLE_CONTENT,
					"Amount exceeds the maximum allowed per operation");
		}
	}
}
