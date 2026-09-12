package com.unimagdalena.corebanking.service;

import com.unimagdalena.corebanking.dto.request.DepositRequest;
import com.unimagdalena.corebanking.dto.request.TransferRequest;
import com.unimagdalena.corebanking.dto.request.WithdrawalRequest;
import com.unimagdalena.corebanking.dto.response.TransactionResponse;
import com.unimagdalena.corebanking.entity.AuditRecord;
import com.unimagdalena.corebanking.entity.BankAccount;
import com.unimagdalena.corebanking.entity.BankTransaction;
import com.unimagdalena.corebanking.enums.TransactionType;
import com.unimagdalena.corebanking.exception.ResourceNotFoundException;
import com.unimagdalena.corebanking.mapper.TransactionMapper;
import com.unimagdalena.corebanking.pattern.chain.TransactionValidationChainProvider;
import com.unimagdalena.corebanking.pattern.strategy.AccountTransactionPolicy;
import com.unimagdalena.corebanking.pattern.strategy.TransactionPolicyResolver;
import com.unimagdalena.corebanking.pattern.template.TransactionContext;
import com.unimagdalena.corebanking.repository.AuditRecordRepository;
import com.unimagdalena.corebanking.repository.BankAccountRepository;
import com.unimagdalena.corebanking.repository.BankTransactionRepository;
import com.unimagdalena.corebanking.service.interfaces.TransactionService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

	private final BankAccountRepository accountRepository;
	private final BankTransactionRepository transactionRepository;
	private final AuditRecordRepository auditRecordRepository;
	private final TransactionMapper transactionMapper;
	private final TransactionPolicyResolver policyResolver;
	private final TransactionValidationChainProvider validationChainProvider;

	public TransactionServiceImpl(BankAccountRepository accountRepository,
			BankTransactionRepository transactionRepository,
			AuditRecordRepository auditRecordRepository,
			TransactionMapper transactionMapper,
			TransactionPolicyResolver policyResolver,
			TransactionValidationChainProvider validationChainProvider) {
		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
		this.auditRecordRepository = auditRecordRepository;
		this.transactionMapper = transactionMapper;
		this.policyResolver = policyResolver;
		this.validationChainProvider = validationChainProvider;
	}

	@Override
	@Transactional
	public TransactionResponse deposit(DepositRequest request) {
		BankAccount account = findAccountOrThrow(request.getAccountId());
		AccountTransactionPolicy policy = policyResolver.resolve(account.getAccountType());
		BigDecimal fee = policy.calculateFee(TransactionType.DEPOSIT, request.getAmount());

		TransactionContext context = TransactionContext.builder()
				.type(TransactionType.DEPOSIT)
				.destinationAccount(account)
				.amount(request.getAmount())
				.fee(fee)
				.policy(policy)
				.build();
		validationChainProvider.forDeposit().validate(context);

		account.setBalance(account.getBalance().add(request.getAmount()));
		accountRepository.save(account);

		BankTransaction transaction = BankTransaction.builder()
				.type(TransactionType.DEPOSIT)
				.amount(request.getAmount())
				.fee(fee)
				.destinationAccount(account)
				.build();
		BankTransaction saved = transactionRepository.save(transaction);

		createAuditRecord(saved, "DEPOSIT_COMPLETED",
				"Deposit of " + request.getAmount() + " applied to account " + account.getAccountNumber());
		sendSimulatedNotification(account, "Your account received a deposit of " + request.getAmount());

		log.info("transaction completed id={} type=DEPOSIT", saved.getId());
		return transactionMapper.toResponse(saved);
	}

	@Override
	@Transactional
	public TransactionResponse withdraw(WithdrawalRequest request) {
		BankAccount account = findAccountOrThrow(request.getAccountId());
		AccountTransactionPolicy policy = policyResolver.resolve(account.getAccountType());
		BigDecimal fee = policy.calculateFee(TransactionType.WITHDRAWAL, request.getAmount());

		TransactionContext context = TransactionContext.builder()
				.type(TransactionType.WITHDRAWAL)
				.sourceAccount(account)
				.amount(request.getAmount())
				.fee(fee)
				.policy(policy)
				.build();
		validationChainProvider.forWithdrawal().validate(context);

		BigDecimal totalDebit = request.getAmount().add(fee);
		account.setBalance(account.getBalance().subtract(totalDebit));
		accountRepository.save(account);

		BankTransaction transaction = BankTransaction.builder()
				.type(TransactionType.WITHDRAWAL)
				.amount(request.getAmount())
				.fee(fee)
				.sourceAccount(account)
				.build();
		BankTransaction saved = transactionRepository.save(transaction);

		createAuditRecord(saved, "WITHDRAWAL_COMPLETED",
				"Withdrawal of " + request.getAmount() + " applied to account " + account.getAccountNumber());
		sendSimulatedNotification(account, "Your account was debited " + totalDebit);

		log.info("transaction completed id={} type=WITHDRAWAL", saved.getId());
		return transactionMapper.toResponse(saved);
	}

	@Override
	@Transactional
	public TransactionResponse transfer(TransferRequest request) {
		BankAccount source = findAccountOrThrow(request.getSourceAccountId());
		BankAccount destination = findAccountOrThrow(request.getDestinationAccountId());
		AccountTransactionPolicy policy = policyResolver.resolve(source.getAccountType());
		BigDecimal fee = policy.calculateFee(TransactionType.TRANSFER, request.getAmount());

		TransactionContext context = TransactionContext.builder()
				.type(TransactionType.TRANSFER)
				.sourceAccount(source)
				.destinationAccount(destination)
				.amount(request.getAmount())
				.fee(fee)
				.policy(policy)
				.build();
		validationChainProvider.forTransfer().validate(context);

		BigDecimal totalDebit = request.getAmount().add(fee);
		source.setBalance(source.getBalance().subtract(totalDebit));
		destination.setBalance(destination.getBalance().add(request.getAmount()));
		accountRepository.save(source);
		accountRepository.save(destination);

		BankTransaction transaction = BankTransaction.builder()
				.type(TransactionType.TRANSFER)
				.amount(request.getAmount())
				.fee(fee)
				.sourceAccount(source)
				.destinationAccount(destination)
				.build();
		BankTransaction saved = transactionRepository.save(transaction);

		createAuditRecord(saved, "TRANSFER_COMPLETED",
				"Transfer of " + request.getAmount() + " from " + source.getAccountNumber() + " to "
						+ destination.getAccountNumber());
		sendSimulatedNotification(source, "You transferred " + request.getAmount() + " to " + destination.getAccountNumber());
		sendSimulatedNotification(destination, "You received " + request.getAmount() + " from " + source.getAccountNumber());

		log.info("transaction completed id={} type=TRANSFER", saved.getId());
		return transactionMapper.toResponse(saved);
	}

	@Override
	public List<TransactionResponse> findByAccountId(UUID accountId) {
		return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
				.map(transactionMapper::toResponse)
				.toList();
	}

	private void createAuditRecord(BankTransaction transaction, String eventType, String description) {
		AuditRecord record = AuditRecord.builder()
				.transaction(transaction)
				.eventType(eventType)
				.description(description)
				.build();
		auditRecordRepository.save(record);
	}

	private void sendSimulatedNotification(BankAccount account, String message) {
		log.info("simulated notification account={} message={}", account.getAccountNumber(), message);
	}

	private BankAccount findAccountOrThrow(UUID accountId) {
		return accountRepository.findById(accountId)
				.orElseThrow(() -> new ResourceNotFoundException("ACCOUNT_NOT_FOUND",
						"Account " + accountId + " was not found"));
	}
}
