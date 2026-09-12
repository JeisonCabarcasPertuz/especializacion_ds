package com.unimagdalena.corebanking.pattern.state;

import com.unimagdalena.corebanking.entity.BankAccount;
import com.unimagdalena.corebanking.enums.AccountStatus;
import org.springframework.stereotype.Component;

@Component
public class AccountStateContext {

	private final AccountStateResolver stateResolver;

	public AccountStateContext(AccountStateResolver stateResolver) {
		this.stateResolver = stateResolver;
	}

	public void assertCanDebit(BankAccount account) {
		stateResolver.resolve(account.getStatus()).assertCanDebit();
	}

	public void assertCanCredit(BankAccount account) {
		stateResolver.resolve(account.getStatus()).assertCanCredit();
	}

	public void assertCanTransitionTo(BankAccount account, AccountStatus target) {
		stateResolver.resolve(account.getStatus()).assertCanTransitionTo(target);
	}
}
