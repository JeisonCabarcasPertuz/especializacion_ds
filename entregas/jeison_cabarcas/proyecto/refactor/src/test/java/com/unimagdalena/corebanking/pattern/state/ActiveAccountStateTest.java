package com.unimagdalena.corebanking.pattern.state;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.unimagdalena.corebanking.enums.AccountStatus;
import com.unimagdalena.corebanking.exception.BusinessException;
import org.junit.jupiter.api.Test;

class ActiveAccountStateTest {

	private final ActiveAccountState state = new ActiveAccountState();

	@Test
	void reportsActiveStatus() {
		org.assertj.core.api.Assertions.assertThat(state.status()).isEqualTo(AccountStatus.ACTIVE);
	}

	@Test
	void allowsDebit() {
		assertThatCode(state::assertCanDebit).doesNotThrowAnyException();
	}

	@Test
	void allowsCredit() {
		assertThatCode(state::assertCanCredit).doesNotThrowAnyException();
	}

	@Test
	void allowsTransitionToBlockedOrClosed() {
		assertThatCode(() -> state.assertCanTransitionTo(AccountStatus.BLOCKED)).doesNotThrowAnyException();
		assertThatCode(() -> state.assertCanTransitionTo(AccountStatus.CLOSED)).doesNotThrowAnyException();
	}

	@Test
	void rejectsTransitionToSameStatus() {
		assertThatThrownBy(() -> state.assertCanTransitionTo(AccountStatus.ACTIVE))
				.isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getCode())
				.isEqualTo("INVALID_ACCOUNT_STATUS_TRANSITION");
	}
}
