package com.unimagdalena.corebanking.pattern.template;

import com.unimagdalena.corebanking.entity.BankAccount;
import com.unimagdalena.corebanking.entity.BankTransaction;
import com.unimagdalena.corebanking.enums.TransactionType;
import com.unimagdalena.corebanking.pattern.strategy.AccountTransactionPolicy;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionContext {

	private TransactionType type;
	private BankAccount sourceAccount;
	private BankAccount destinationAccount;
	private BigDecimal amount;
	private BigDecimal fee;
	private AccountTransactionPolicy policy;
	private BankTransaction savedTransaction;
}
