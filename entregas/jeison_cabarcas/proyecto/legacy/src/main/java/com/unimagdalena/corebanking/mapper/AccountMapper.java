package com.unimagdalena.corebanking.mapper;

import com.unimagdalena.corebanking.dto.response.AccountResponse;
import com.unimagdalena.corebanking.dto.response.BalanceResponse;
import com.unimagdalena.corebanking.entity.BankAccount;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

	public AccountResponse toResponse(BankAccount account) {
		return AccountResponse.builder()
				.id(account.getId())
				.accountNumber(account.getAccountNumber())
				.customerId(account.getCustomer().getId())
				.accountType(account.getAccountType())
				.status(account.getStatus())
				.balance(account.getBalance())
				.currency(account.getCurrency())
				.createdAt(account.getCreatedAt())
				.updatedAt(account.getUpdatedAt())
				.build();
	}

	public BalanceResponse toBalanceResponse(BankAccount account) {
		return BalanceResponse.builder()
				.accountId(account.getId())
				.balance(account.getBalance())
				.currency(account.getCurrency())
				.build();
	}
}
