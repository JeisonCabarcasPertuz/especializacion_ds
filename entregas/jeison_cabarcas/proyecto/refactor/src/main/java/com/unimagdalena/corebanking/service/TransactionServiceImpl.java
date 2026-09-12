package com.unimagdalena.corebanking.service;

import com.unimagdalena.corebanking.dto.request.DepositRequest;
import com.unimagdalena.corebanking.dto.request.TransferRequest;
import com.unimagdalena.corebanking.dto.request.WithdrawalRequest;
import com.unimagdalena.corebanking.dto.response.TransactionResponse;
import com.unimagdalena.corebanking.enums.TransactionType;
import com.unimagdalena.corebanking.mapper.TransactionMapper;
import com.unimagdalena.corebanking.pattern.template.DepositTransactionProcessor;
import com.unimagdalena.corebanking.pattern.template.TransactionCommand;
import com.unimagdalena.corebanking.pattern.template.TransferTransactionProcessor;
import com.unimagdalena.corebanking.pattern.template.WithdrawalTransactionProcessor;
import com.unimagdalena.corebanking.repository.BankTransactionRepository;
import com.unimagdalena.corebanking.service.interfaces.TransactionService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionServiceImpl implements TransactionService {

	private final BankTransactionRepository transactionRepository;
	private final TransactionMapper transactionMapper;
	private final DepositTransactionProcessor depositProcessor;
	private final WithdrawalTransactionProcessor withdrawalProcessor;
	private final TransferTransactionProcessor transferProcessor;

	public TransactionServiceImpl(BankTransactionRepository transactionRepository,
			TransactionMapper transactionMapper,
			DepositTransactionProcessor depositProcessor,
			WithdrawalTransactionProcessor withdrawalProcessor,
			TransferTransactionProcessor transferProcessor) {
		this.transactionRepository = transactionRepository;
		this.transactionMapper = transactionMapper;
		this.depositProcessor = depositProcessor;
		this.withdrawalProcessor = withdrawalProcessor;
		this.transferProcessor = transferProcessor;
	}

	@Override
	@Transactional
	public TransactionResponse deposit(DepositRequest request) {
		TransactionCommand command = TransactionCommand.builder()
				.type(TransactionType.DEPOSIT)
				.destinationAccountId(request.getAccountId())
				.amount(request.getAmount())
				.build();
		return depositProcessor.process(command);
	}

	@Override
	@Transactional
	public TransactionResponse withdraw(WithdrawalRequest request) {
		TransactionCommand command = TransactionCommand.builder()
				.type(TransactionType.WITHDRAWAL)
				.sourceAccountId(request.getAccountId())
				.amount(request.getAmount())
				.build();
		return withdrawalProcessor.process(command);
	}

	@Override
	@Transactional
	public TransactionResponse transfer(TransferRequest request) {
		TransactionCommand command = TransactionCommand.builder()
				.type(TransactionType.TRANSFER)
				.sourceAccountId(request.getSourceAccountId())
				.destinationAccountId(request.getDestinationAccountId())
				.amount(request.getAmount())
				.build();
		return transferProcessor.process(command);
	}

	@Override
	public List<TransactionResponse> findByAccountId(UUID accountId) {
		return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
				.map(transactionMapper::toResponse)
				.toList();
	}
}
