package com.unimagdalena.corebanking.dto.response;

import com.unimagdalena.corebanking.enums.TransactionStatus;
import com.unimagdalena.corebanking.enums.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TransactionResponse {

	private UUID id;
	private TransactionType type;
	private BigDecimal amount;
	private BigDecimal fee;
	private TransactionStatus status;
	private UUID sourceAccountId;
	private UUID destinationAccountId;
	private Instant createdAt;
}
