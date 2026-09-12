package com.unimagdalena.corebanking.pattern.state;

import com.unimagdalena.corebanking.enums.AccountStatus;
import com.unimagdalena.corebanking.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ClosedAccountState implements AccountState {

	@Override
	public AccountStatus status() {
		return AccountStatus.CLOSED;
	}

	@Override
	public void assertCanDebit() {
		throw new BusinessException("ACCOUNT_CLOSED", HttpStatus.UNPROCESSABLE_CONTENT,
				"Account is closed and cannot be debited");
	}

	@Override
	public void assertCanCredit() {
		throw new BusinessException("ACCOUNT_CLOSED", HttpStatus.UNPROCESSABLE_CONTENT,
				"Account is closed and cannot receive credits");
	}

	@Override
	public void assertCanTransitionTo(AccountStatus target) {
		throw new BusinessException("INVALID_ACCOUNT_STATUS_TRANSITION", HttpStatus.UNPROCESSABLE_CONTENT,
				"A closed account cannot change its status");
	}
}
