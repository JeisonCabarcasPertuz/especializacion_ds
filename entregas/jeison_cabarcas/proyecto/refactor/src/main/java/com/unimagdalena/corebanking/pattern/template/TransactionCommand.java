package com.unimagdalena.corebanking.pattern.template;

import com.unimagdalena.corebanking.enums.TransactionType;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCommand {

	private TransactionType type;
	private UUID sourceAccountId;
	private UUID destinationAccountId;
	private BigDecimal amount;
}
