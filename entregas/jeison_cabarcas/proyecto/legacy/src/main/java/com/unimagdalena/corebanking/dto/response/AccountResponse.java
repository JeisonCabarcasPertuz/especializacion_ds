package com.unimagdalena.corebanking.dto.response;

import com.unimagdalena.corebanking.enums.AccountStatus;
import com.unimagdalena.corebanking.enums.AccountType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AccountResponse {

	private UUID id;
	private String accountNumber;
	private UUID customerId;
	private AccountType accountType;
	private AccountStatus status;
	private BigDecimal balance;
	private String currency;
	private Instant createdAt;
	private Instant updatedAt;
}
