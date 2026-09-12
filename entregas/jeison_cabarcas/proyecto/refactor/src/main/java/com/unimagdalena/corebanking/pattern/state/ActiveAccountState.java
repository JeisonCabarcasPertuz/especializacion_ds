package com.unimagdalena.corebanking.pattern.state;

import com.unimagdalena.corebanking.enums.AccountStatus;
import com.unimagdalena.corebanking.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ActiveAccountState implements AccountState {

	@Override
	public AccountStatus status() {
		return AccountStatus.ACTIVE;
	}

	@Override
	public void assertCanDebit() {
		// active accounts may always be debited
	}

	@Override
	public void assertCanCredit() {
		// active accounts may always be credited
	}

	@Override
	public void assertCanTransitionTo(AccountStatus target) {
		if (target != AccountStatus.BLOCKED && target != AccountStatus.CLOSED) {
			throw new BusinessException("INVALID_ACCOUNT_STATUS_TRANSITION", HttpStatus.UNPROCESSABLE_CONTENT,
					"Cannot transition account from " + status() + " to " + target);
		}
	}
}
