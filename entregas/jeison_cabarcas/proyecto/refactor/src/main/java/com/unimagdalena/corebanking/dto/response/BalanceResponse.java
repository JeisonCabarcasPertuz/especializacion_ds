package com.unimagdalena.corebanking.dto.response;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BalanceResponse {

	private UUID accountId;
	private BigDecimal balance;
	private String currency;
}
