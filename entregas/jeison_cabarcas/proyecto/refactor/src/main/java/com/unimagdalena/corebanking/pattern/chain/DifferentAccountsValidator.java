package com.unimagdalena.corebanking.pattern.chain;

import com.unimagdalena.corebanking.exception.BusinessException;
import com.unimagdalena.corebanking.pattern.template.TransactionContext;
import org.springframework.http.HttpStatus;

public class DifferentAccountsValidator extends AbstractTransactionValidator {

	@Override
	protected void checkRule(TransactionContext context) {
		if (context.getSourceAccount().getId().equals(context.getDestinationAccount().getId())) {
			throw new BusinessException("SAME_ACCOUNT_TRANSFER", HttpStatus.UNPROCESSABLE_CONTENT,
					"Source and destination accounts must be different");
		}
	}
}
