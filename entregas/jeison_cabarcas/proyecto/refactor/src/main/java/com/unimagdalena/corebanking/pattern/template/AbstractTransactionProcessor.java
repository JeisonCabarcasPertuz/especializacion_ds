package com.unimagdalena.corebanking.pattern.template;

import com.unimagdalena.corebanking.dto.response.TransactionResponse;
import com.unimagdalena.corebanking.entity.AuditRecord;
import com.unimagdalena.corebanking.entity.BankAccount;
import com.unimagdalena.corebanking.entity.BankTransaction;
import com.unimagdalena.corebanking.exception.ResourceNotFoundException;
import com.unimagdalena.corebanking.mapper.TransactionMapper;
import com.unimagdalena.corebanking.pattern.chain.TransactionValidator;
import com.unimagdalena.corebanking.pattern.chain.TransactionValidationChainProvider;
import com.unimagdalena.corebanking.pattern.strategy.TransactionPolicyResolver;
import com.unimagdalena.corebanking.repository.AuditRecordRepository;
import com.unimagdalena.corebanking.repository.BankAccountRepository;
import com.unimagdalena.corebanking.repository.BankTransactionRepository;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTransactionProcessor {

	private final BankAccountRepository accountRepository;
	private final BankTransactionRepository transactionRepository;
	private final AuditRecordRepository auditRecordRepository;
	private final TransactionPolicyResolver policyResolver;
	protected final TransactionValidationChainProvider validationChainProvider;
	private final TransactionMapper transactionMapper;

	protected AbstractTransactionProcessor(BankAccountRepository accountRepository,
			BankTransactionRepository transactionRepository,
			AuditRecordRepository auditRecordRepository,
			TransactionPolicyResolver policyResolver,
			TransactionValidationChainProvider validationChainProvider,
			TransactionMapper transactionMapper) {
		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
		this.auditRecordRepository = auditRecordRepository;
		this.policyResolver = policyResolver;
		this.validationChainProvider = validationChainProvider;
		this.transactionMapper = transactionMapper;
	}

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

	protected void publishCompletedEvent(TransactionContext context) {
		createAuditRecord(context);
		notifyInvolvedAccounts(context);
	}

	private void createAuditRecord(TransactionContext context) {
		AuditRecord auditRecord = AuditRecord.builder()
				.transaction(context.getSavedTransaction())
				.eventType(context.getType() + "_COMPLETED")
				.description(describeTransaction(context))
				.build();
		auditRecordRepository.save(auditRecord);
	}

	private void notifyInvolvedAccounts(TransactionContext context) {
		if (context.getSourceAccount() != null) {
			BankAccount source = context.getSourceAccount();
			log.info("simulated notification account={} message=Your account was debited {}",
					source.getAccountNumber(), context.getAmount().add(context.getFee()));
		}
		if (context.getDestinationAccount() != null) {
			BankAccount destination = context.getDestinationAccount();
			log.info("simulated notification account={} message=Your account received {}",
					destination.getAccountNumber(), context.getAmount());
		}
	}

	private String describeTransaction(TransactionContext context) {
		String source = context.getSourceAccount() != null ? context.getSourceAccount().getAccountNumber() : "-";
		String destination = context.getDestinationAccount() != null ? context.getDestinationAccount().getAccountNumber() : "-";
		return context.getType() + " of " + context.getAmount() + " from " + source + " to " + destination;
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
