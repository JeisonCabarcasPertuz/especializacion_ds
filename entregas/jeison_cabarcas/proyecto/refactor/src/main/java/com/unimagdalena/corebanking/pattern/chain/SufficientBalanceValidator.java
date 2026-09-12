package com.unimagdalena.corebanking.pattern.chain;

import com.unimagdalena.corebanking.exception.BusinessException;
import com.unimagdalena.corebanking.pattern.template.TransactionContext;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;

public class SufficientBalanceValidator extends AbstractTransactionValidator {

	@Override
	protected void checkRule(TransactionContext context) {
		BigDecimal totalDebit = context.getAmount().add(context.getFee());
		if (context.getSourceAccount().getBalance().compareTo(totalDebit) < 0) {
			throw new BusinessException("INSUFFICIENT_FUNDS", HttpStatus.UNPROCESSABLE_CONTENT,
					"Account does not have enough balance");
		}
	}
}
