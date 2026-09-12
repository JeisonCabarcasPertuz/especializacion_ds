package com.unimagdalena.corebanking.pattern.state;

import com.unimagdalena.corebanking.enums.AccountStatus;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class AccountStateResolver {

	private final Map<AccountStatus, AccountState> statesByStatus;

	public AccountStateResolver(List<AccountState> states) {
		this.statesByStatus = states.stream()
				.collect(Collectors.toUnmodifiableMap(AccountState::status, Function.identity()));
	}

	public AccountState resolve(AccountStatus status) {
		AccountState state = statesByStatus.get(status);
		if (state == null) {
			throw new IllegalStateException("No account state registered for status: " + status);
		}
		return state;
	}
}
