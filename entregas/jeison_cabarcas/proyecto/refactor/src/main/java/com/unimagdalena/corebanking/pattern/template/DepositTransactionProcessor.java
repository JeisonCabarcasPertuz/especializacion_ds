package com.unimagdalena.corebanking.pattern.template;

import com.unimagdalena.corebanking.entity.BankAccount;
import com.unimagdalena.corebanking.enums.TransactionType;
import com.unimagdalena.corebanking.mapper.TransactionMapper;
import com.unimagdalena.corebanking.pattern.chain.TransactionValidator;
import com.unimagdalena.corebanking.pattern.chain.TransactionValidationChainProvider;
import com.unimagdalena.corebanking.pattern.strategy.TransactionPolicyResolver;
import com.unimagdalena.corebanking.repository.AuditRecordRepository;
import com.unimagdalena.corebanking.repository.BankAccountRepository;
import com.unimagdalena.corebanking.repository.BankTransactionRepository;
import org.springframework.stereotype.Component;

@Component
public class DepositTransactionProcessor extends AbstractTransactionProcessor {

	public DepositTransactionProcessor(BankAccountRepository accountRepository,
			BankTransactionRepository transactionRepository,
			AuditRecordRepository auditRecordRepository,
			TransactionPolicyResolver policyResolver,
			TransactionValidationChainProvider validationChainProvider,
			TransactionMapper transactionMapper) {
		super(accountRepository, transactionRepository, auditRecordRepository, policyResolver, validationChainProvider,
				transactionMapper);
	}

	@Override
	protected TransactionContext loadContext(TransactionCommand command) {
		BankAccount destination = findAccountOrThrow(command.getDestinationAccountId());
		return TransactionContext.builder()
				.type(TransactionType.DEPOSIT)
				.destinationAccount(destination)
				.amount(command.getAmount())
				.build();
	}

	@Override
	protected TransactionValidator selectValidationChain() {
		return validationChainProvider.forDeposit();
	}

	@Override
	protected void applyMovement(TransactionContext context) {
		BankAccount destination = context.getDestinationAccount();
		destination.setBalance(destination.getBalance().add(context.getAmount()));
		saveAccount(destination);
	}
}
