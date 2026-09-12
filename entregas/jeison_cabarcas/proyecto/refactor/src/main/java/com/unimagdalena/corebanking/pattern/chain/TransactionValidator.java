package com.unimagdalena.corebanking.pattern.chain;

import com.unimagdalena.corebanking.pattern.template.TransactionContext;

public interface TransactionValidator {

	TransactionValidator setNext(TransactionValidator next);

	void validate(TransactionContext context);
}
