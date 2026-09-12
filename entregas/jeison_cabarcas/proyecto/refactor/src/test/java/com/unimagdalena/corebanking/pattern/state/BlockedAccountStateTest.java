package com.unimagdalena.corebanking.pattern.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.unimagdalena.corebanking.enums.AccountStatus;
import com.unimagdalena.corebanking.exception.BusinessException;
import org.junit.jupiter.api.Test;

class BlockedAccountStateTest {

	private final BlockedAccountState state = new BlockedAccountState();

	@Test
	void reportsBlockedStatus() {
		assertThat(state.status()).isEqualTo(AccountStatus.BLOCKED);
	}

	@Test
	void rejectsDebit() {
		assertThatThrownBy(state::assertCanDebit)
				.isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getCode())
				.isEqualTo("ACCOUNT_BLOCKED");
	}

	@Test
	void allowsCredit() {
		assertThatCode(state::assertCanCredit).doesNotThrowAnyException();
	}

	@Test
	void allowsTransitionToActiveOrClosed() {
		assertThatCode(() -> state.assertCanTransitionTo(AccountStatus.ACTIVE)).doesNotThrowAnyException();
		assertThatCode(() -> state.assertCanTransitionTo(AccountStatus.CLOSED)).doesNotThrowAnyException();
	}

	@Test
	void rejectsTransitionToSameStatus() {
		assertThatThrownBy(() -> state.assertCanTransitionTo(AccountStatus.BLOCKED))
				.isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getCode())
				.isEqualTo("INVALID_ACCOUNT_STATUS_TRANSITION");
	}
}
