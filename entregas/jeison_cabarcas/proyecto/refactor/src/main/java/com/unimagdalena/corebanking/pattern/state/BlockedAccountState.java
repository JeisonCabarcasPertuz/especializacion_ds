package com.unimagdalena.corebanking.pattern.state;

import com.unimagdalena.corebanking.enums.AccountStatus;
import com.unimagdalena.corebanking.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class BlockedAccountState implements AccountState {

	@Override
	public AccountStatus status() {
		return AccountStatus.BLOCKED;
	}

	@Override
	public void assertCanDebit() {
		throw new BusinessException("ACCOUNT_BLOCKED", HttpStatus.UNPROCESSABLE_CONTENT,
				"Account is blocked and cannot be debited");
	}

	@Override
	public void assertCanCredit() {
		// blocked accounts may still receive credits
	}

	@Override
	public void assertCanTransitionTo(AccountStatus target) {
		if (target != AccountStatus.ACTIVE && target != AccountStatus.CLOSED) {
			throw new BusinessException("INVALID_ACCOUNT_STATUS_TRANSITION", HttpStatus.UNPROCESSABLE_CONTENT,
					"Cannot transition account from " + status() + " to " + target);
		}
	}
}
