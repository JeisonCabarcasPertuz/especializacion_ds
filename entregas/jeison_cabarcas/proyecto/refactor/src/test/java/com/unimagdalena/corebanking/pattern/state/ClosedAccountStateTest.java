package com.unimagdalena.corebanking.pattern.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.unimagdalena.corebanking.enums.AccountStatus;
import com.unimagdalena.corebanking.exception.BusinessException;
import org.junit.jupiter.api.Test;

class ClosedAccountStateTest {

	private final ClosedAccountState state = new ClosedAccountState();

	@Test
	void reportsClosedStatus() {
		assertThat(state.status()).isEqualTo(AccountStatus.CLOSED);
	}

	@Test
	void rejectsDebit() {
		assertThatThrownBy(state::assertCanDebit)
				.isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getCode())
				.isEqualTo("ACCOUNT_CLOSED");
	}

	@Test
	void rejectsCredit() {
		assertThatThrownBy(state::assertCanCredit)
				.isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getCode())
				.isEqualTo("ACCOUNT_CLOSED");
	}

	@Test
	void cannotReopenOrChangeToAnyStatus() {
		assertThatThrownBy(() -> state.assertCanTransitionTo(AccountStatus.ACTIVE))
				.isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getCode())
				.isEqualTo("INVALID_ACCOUNT_STATUS_TRANSITION");
		assertThatThrownBy(() -> state.assertCanTransitionTo(AccountStatus.BLOCKED))
				.isInstanceOf(BusinessException.class);
	}
}
