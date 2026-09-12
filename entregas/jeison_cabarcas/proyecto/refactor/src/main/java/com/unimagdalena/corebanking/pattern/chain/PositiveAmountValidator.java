package com.unimagdalena.corebanking.pattern.chain;

import com.unimagdalena.corebanking.exception.BusinessException;
import com.unimagdalena.corebanking.pattern.template.TransactionContext;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;

public class PositiveAmountValidator extends AbstractTransactionValidator {

	@Override
	protected void checkRule(TransactionContext context) {
		if (context.getAmount() == null || context.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new BusinessException("VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
		}
	}
}
