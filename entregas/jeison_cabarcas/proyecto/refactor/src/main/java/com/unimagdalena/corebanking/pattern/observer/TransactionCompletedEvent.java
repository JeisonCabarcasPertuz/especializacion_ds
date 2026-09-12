package com.unimagdalena.corebanking.pattern.observer;

import com.unimagdalena.corebanking.entity.BankAccount;
import com.unimagdalena.corebanking.entity.BankTransaction;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCompletedEvent {

	private BankTransaction transaction;
	private BankAccount sourceAccount;
	private BankAccount destinationAccount;
	private BigDecimal amount;
	private BigDecimal fee;
}
