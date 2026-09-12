package com.unimagdalena.corebanking.service;

import com.unimagdalena.corebanking.dto.request.DepositRequest;
import com.unimagdalena.corebanking.dto.request.TransferRequest;
import com.unimagdalena.corebanking.dto.request.WithdrawalRequest;
import com.unimagdalena.corebanking.dto.response.TransactionResponse;
import com.unimagdalena.corebanking.entity.AuditRecord;
import com.unimagdalena.corebanking.entity.BankAccount;
import com.unimagdalena.corebanking.entity.BankTransaction;
import com.unimagdalena.corebanking.enums.AccountStatus;
import com.unimagdalena.corebanking.enums.AccountType;
import com.unimagdalena.corebanking.enums.TransactionType;
import com.unimagdalena.corebanking.exception.BusinessException;
import com.unimagdalena.corebanking.exception.ResourceNotFoundException;
import com.unimagdalena.corebanking.mapper.TransactionMapper;
import com.unimagdalena.corebanking.repository.AuditRecordRepository;
import com.unimagdalena.corebanking.repository.BankAccountRepository;
import com.unimagdalena.corebanking.repository.BankTransactionRepository;
import com.unimagdalena.corebanking.service.interfaces.TransactionService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

	private final BankAccountRepository accountRepository;
	private final BankTransactionRepository transactionRepository;
	private final AuditRecordRepository auditRecordRepository;
	private final TransactionMapper transactionMapper;

	@Override
	@Transactional
	public TransactionResponse deposit(DepositRequest request) {
		if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new BusinessException("VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
		}

		BankAccount account = findAccountOrThrow(request.getAccountId());

		if (account.getStatus() == AccountStatus.CLOSED) {
			throw new BusinessException("ACCOUNT_CLOSED", HttpStatus.UNPROCESSABLE_CONTENT,
					"Account is closed and cannot receive deposits");
		}

		BigDecimal fee = calculateFee(account.getAccountType(), TransactionType.DEPOSIT, request.getAmount());

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
		if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new BusinessException("VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
		}

		BankAccount account = findAccountOrThrow(request.getAccountId());

		if (account.getStatus() == AccountStatus.CLOSED) {
			throw new BusinessException("ACCOUNT_CLOSED", HttpStatus.UNPROCESSABLE_CONTENT,
					"Account is closed and cannot be debited");
		}
		if (account.getStatus() == AccountStatus.BLOCKED) {
			throw new BusinessException("ACCOUNT_BLOCKED", HttpStatus.UNPROCESSABLE_CONTENT,
					"Account is blocked and cannot be debited");
		}

		BigDecimal limit = calculateLimit(account.getAccountType());
		if (request.getAmount().compareTo(limit) > 0) {
			throw new BusinessException("TRANSACTION_LIMIT_EXCEEDED", HttpStatus.UNPROCESSABLE_CONTENT,
					"Amount exceeds the maximum allowed per operation");
		}

		BigDecimal fee = calculateFee(account.getAccountType(), TransactionType.WITHDRAWAL, request.getAmount());
		BigDecimal totalDebit = request.getAmount().add(fee);

		if (account.getBalance().compareTo(totalDebit) < 0) {
			throw new BusinessException("INSUFFICIENT_FUNDS", HttpStatus.UNPROCESSABLE_CONTENT,
					"Account does not have enough balance");
		}

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
		if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new BusinessException("VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
		}
		if (request.getSourceAccountId().equals(request.getDestinationAccountId())) {
			throw new BusinessException("SAME_ACCOUNT_TRANSFER", HttpStatus.UNPROCESSABLE_CONTENT,
					"Source and destination accounts must be different");
		}

		BankAccount source = findAccountOrThrow(request.getSourceAccountId());
		BankAccount destination = findAccountOrThrow(request.getDestinationAccountId());

		if (source.getStatus() == AccountStatus.CLOSED) {
			throw new BusinessException("ACCOUNT_CLOSED", HttpStatus.UNPROCESSABLE_CONTENT,
					"Source account is closed and cannot be debited");
		}
		if (source.getStatus() == AccountStatus.BLOCKED) {
			throw new BusinessException("ACCOUNT_BLOCKED", HttpStatus.UNPROCESSABLE_CONTENT,
					"Source account is blocked and cannot be debited");
		}
		if (destination.getStatus() == AccountStatus.CLOSED) {
			throw new BusinessException("ACCOUNT_CLOSED", HttpStatus.UNPROCESSABLE_CONTENT,
					"Destination account is closed and cannot receive credits");
		}

		BigDecimal limit = calculateLimit(source.getAccountType());
		if (request.getAmount().compareTo(limit) > 0) {
			throw new BusinessException("TRANSACTION_LIMIT_EXCEEDED", HttpStatus.UNPROCESSABLE_CONTENT,
					"Amount exceeds the maximum allowed per operation");
		}

		BigDecimal fee = calculateFee(source.getAccountType(), TransactionType.TRANSFER, request.getAmount());
		BigDecimal totalDebit = request.getAmount().add(fee);

		if (source.getBalance().compareTo(totalDebit) < 0) {
			throw new BusinessException("INSUFFICIENT_FUNDS", HttpStatus.UNPROCESSABLE_CONTENT,
					"Source account does not have enough balance");
		}

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

	private BigDecimal calculateFee(AccountType accountType, TransactionType transactionType, BigDecimal amount) {
		switch (accountType) {
			case SAVINGS:
				switch (transactionType) {
					case DEPOSIT:
						return zero();
					case WITHDRAWAL:
						return zero();
					case TRANSFER:
						return new BigDecimal("1000.00");
					default:
						return zero();
				}
			case CHECKING:
				switch (transactionType) {
					case DEPOSIT:
						return zero();
					case WITHDRAWAL:
						return new BigDecimal("2000.00");
					case TRANSFER:
						return new BigDecimal("1500.00");
					default:
						return zero();
				}
			default:
				throw new IllegalStateException("Unsupported account type: " + accountType);
		}
	}

	private BigDecimal calculateLimit(AccountType accountType) {
		if (accountType == AccountType.SAVINGS) {
			return new BigDecimal("5000000.00");
		}
		if (accountType == AccountType.CHECKING) {
			return new BigDecimal("10000000.00");
		}
		throw new IllegalStateException("Unsupported account type: " + accountType);
	}

	private BigDecimal zero() {
		return BigDecimal.ZERO.setScale(2);
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
