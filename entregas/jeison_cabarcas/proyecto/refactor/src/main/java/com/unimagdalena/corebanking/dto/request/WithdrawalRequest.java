package com.unimagdalena.corebanking.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequest {

	@NotNull
	private UUID accountId;

	@NotNull
	@DecimalMin("0.01")
	@Digits(integer = 17, fraction = 2)
	private BigDecimal amount;
}
