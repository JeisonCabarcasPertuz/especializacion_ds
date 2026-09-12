package com.unimagdalena.corebanking.pattern.chain;

import com.unimagdalena.corebanking.pattern.state.AccountStateContext;
import com.unimagdalena.corebanking.pattern.template.TransactionContext;

public class DebitAccountStateValidator extends AbstractTransactionValidator {

	private final AccountStateContext accountStateContext;

	public DebitAccountStateValidator(AccountStateContext accountStateContext) {
		this.accountStateContext = accountStateContext;
	}

	@Override
	protected void checkRule(TransactionContext context) {
		accountStateContext.assertCanDebit(context.getSourceAccount());
	}
}
