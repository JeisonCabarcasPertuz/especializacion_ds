package com.unimagdalena.corebanking.pattern.strategy;

import com.unimagdalena.corebanking.enums.AccountType;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class TransactionPolicyResolver {

	private final Map<AccountType, AccountTransactionPolicy> policiesByAccountType;

	public TransactionPolicyResolver(List<AccountTransactionPolicy> policies) {
		this.policiesByAccountType = policies.stream()
				.collect(Collectors.toUnmodifiableMap(AccountTransactionPolicy::supportedAccountType, Function.identity()));
	}

	public AccountTransactionPolicy resolve(AccountType accountType) {
		AccountTransactionPolicy policy = policiesByAccountType.get(accountType);
		if (policy == null) {
			throw new IllegalStateException("No transaction policy registered for account type: " + accountType);
		}
		return policy;
	}
}
