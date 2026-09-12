package com.unimagdalena.corebanking.pattern.chain;

import com.unimagdalena.corebanking.pattern.state.AccountStateContext;
import org.springframework.stereotype.Component;

@Component
public class TransactionValidationChainProvider {

	private final AccountStateContext accountStateContext;

	public TransactionValidationChainProvider(AccountStateContext accountStateContext) {
		this.accountStateContext = accountStateContext;
	}

	public TransactionValidator forDeposit() {
		TransactionValidator chain = new PositiveAmountValidator();
		chain.setNext(new CreditAccountStateValidator(accountStateContext));
		return chain;
	}

	public TransactionValidator forWithdrawal() {
		TransactionValidator chain = new PositiveAmountValidator();
		chain.setNext(new DebitAccountStateValidator(accountStateContext))
				.setNext(new TransactionLimitValidator())
				.setNext(new SufficientBalanceValidator());
		return chain;
	}

	public TransactionValidator forTransfer() {
		TransactionValidator chain = new PositiveAmountValidator();
		chain.setNext(new DifferentAccountsValidator())
				.setNext(new DebitAccountStateValidator(accountStateContext))
				.setNext(new CreditAccountStateValidator(accountStateContext))
				.setNext(new TransactionLimitValidator())
				.setNext(new SufficientBalanceValidator());
		return chain;
	}
}
