package com.unimagdalena.corebanking.pattern.template;

import com.unimagdalena.corebanking.entity.BankAccount;
import com.unimagdalena.corebanking.enums.TransactionType;
import com.unimagdalena.corebanking.mapper.TransactionMapper;
import com.unimagdalena.corebanking.pattern.chain.TransactionValidator;
import com.unimagdalena.corebanking.pattern.chain.TransactionValidationChainProvider;
import com.unimagdalena.corebanking.pattern.observer.TransactionEventPublisher;
import com.unimagdalena.corebanking.pattern.strategy.TransactionPolicyResolver;
import com.unimagdalena.corebanking.repository.BankAccountRepository;
import com.unimagdalena.corebanking.repository.BankTransactionRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class TransferTransactionProcessor extends AbstractTransactionProcessor {

	public TransferTransactionProcessor(BankAccountRepository accountRepository,
			BankTransactionRepository transactionRepository,
			TransactionPolicyResolver policyResolver,
			TransactionValidationChainProvider validationChainProvider,
			TransactionMapper transactionMapper,
			TransactionEventPublisher eventPublisher) {
		super(accountRepository, transactionRepository, policyResolver, validationChainProvider, transactionMapper,
				eventPublisher);
	}

	@Override
	protected TransactionContext loadContext(TransactionCommand command) {
		BankAccount source = findAccountOrThrow(command.getSourceAccountId());
		BankAccount destination = findAccountOrThrow(command.getDestinationAccountId());
		return TransactionContext.builder()
				.type(TransactionType.TRANSFER)
				.sourceAccount(source)
				.destinationAccount(destination)
				.amount(command.getAmount())
				.build();
	}

	@Override
	protected TransactionValidator selectValidationChain() {
		return validationChainProvider.forTransfer();
	}

	@Override
	protected void applyMovement(TransactionContext context) {
		BankAccount source = context.getSourceAccount();
		BankAccount destination = context.getDestinationAccount();
		BigDecimal totalDebit = context.getAmount().add(context.getFee());
		source.setBalance(source.getBalance().subtract(totalDebit));
		destination.setBalance(destination.getBalance().add(context.getAmount()));
		saveAccount(source);
		saveAccount(destination);
	}
}
