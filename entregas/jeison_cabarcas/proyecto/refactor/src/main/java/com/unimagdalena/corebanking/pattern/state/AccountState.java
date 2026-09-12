package com.unimagdalena.corebanking.pattern.state;

import com.unimagdalena.corebanking.enums.AccountStatus;

public interface AccountState {

	AccountStatus status();

	void assertCanDebit();

	void assertCanCredit();

	void assertCanTransitionTo(AccountStatus target);
}
