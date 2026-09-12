package com.unimagdalena.corebanking.pattern.chain;

import com.unimagdalena.corebanking.pattern.template.TransactionContext;

public abstract class AbstractTransactionValidator implements TransactionValidator {

	private TransactionValidator next;

	@Override
	public TransactionValidator setNext(TransactionValidator next) {
		this.next = next;
		return next;
	}

	@Override
	public final void validate(TransactionContext context) {
		checkRule(context);
		if (next != null) {
			next.validate(context);
		}
	}

	protected abstract void checkRule(TransactionContext context);
}
