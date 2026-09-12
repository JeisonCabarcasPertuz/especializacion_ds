package com.unimagdalena.corebanking.pattern.template;

import com.unimagdalena.corebanking.dto.response.TransactionResponse;
import com.unimagdalena.corebanking.entity.BankAccount;
import com.unimagdalena.corebanking.entity.BankTransaction;
import com.unimagdalena.corebanking.exception.ResourceNotFoundException;
import com.unimagdalena.corebanking.mapper.TransactionMapper;
import com.unimagdalena.corebanking.pattern.chain.TransactionValidationChainProvider;
import com.unimagdalena.corebanking.pattern.chain.TransactionValidator;
import com.unimagdalena.corebanking.pattern.observer.TransactionCompletedEvent;
import com.unimagdalena.corebanking.pattern.observer.TransactionEventPublisher;
import com.unimagdalena.corebanking.pattern.strategy.TransactionPolicyResolver;
import com.unimagdalena.corebanking.repository.BankAccountRepository;
import com.unimagdalena.corebanking.repository.BankTransactionRepository;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractTransactionProcessor {

	private final BankAccountRepository accountRepository;
	private final BankTransactionRepository transactionRepository;
	private final TransactionPolicyResolver policyResolver;
	protected final TransactionValidationChainProvider validationChainProvider;
	private final TransactionMapper transactionMapper;
	private final TransactionEventPublisher eventPublisher;

	public final TransactionResponse process(TransactionCommand command) {
		TransactionContext context = loadContext(command);
		resolvePolicy(context);
		calculateFee(context);
		validate(context);
		applyMovement(context);
		persistTransaction(context);
		publishCompletedEvent(context);
		TransactionResponse result = buildResult(context);
		log.info("transaction completed id={} type={}", context.getSavedTransaction().getId(), context.getType());
		return result;
	}

	protected abstract TransactionContext loadContext(TransactionCommand command);

	protected abstract TransactionValidator selectValidationChain();

	protected abstract void applyMovement(TransactionContext context);

	private void resolvePolicy(TransactionContext context) {
		BankAccount referenceAccount = context.getSourceAccount() != null
				? context.getSourceAccount()
				: context.getDestinationAccount();
		context.setPolicy(policyResolver.resolve(referenceAccount.getAccountType()));
	}

	private void calculateFee(TransactionContext context) {
		context.setFee(context.getPolicy().calculateFee(context.getType(), context.getAmount()));
	}

	private void validate(TransactionContext context) {
		selectValidationChain().validate(context);
	}

	private void persistTransaction(TransactionContext context) {
		BankTransaction transaction = BankTransaction.builder()
				.type(context.getType())
				.amount(context.getAmount())
				.fee(context.getFee())
				.sourceAccount(context.getSourceAccount())
				.destinationAccount(context.getDestinationAccount())
				.build();
		context.setSavedTransaction(transactionRepository.save(transaction));
	}

	private void publishCompletedEvent(TransactionContext context) {
		TransactionCompletedEvent event = TransactionCompletedEvent.builder()
				.transaction(context.getSavedTransaction())
				.sourceAccount(context.getSourceAccount())
				.destinationAccount(context.getDestinationAccount())
				.amount(context.getAmount())
				.fee(context.getFee())
				.build();
		eventPublisher.publish(event);
	}

	private TransactionResponse buildResult(TransactionContext context) {
		return transactionMapper.toResponse(context.getSavedTransaction());
	}

	protected BankAccount findAccountOrThrow(UUID accountId) {
		return accountRepository.findById(accountId)
				.orElseThrow(() -> new ResourceNotFoundException("ACCOUNT_NOT_FOUND",
						"Account " + accountId + " was not found"));
	}

	protected void saveAccount(BankAccount account) {
		accountRepository.save(account);
	}
}
