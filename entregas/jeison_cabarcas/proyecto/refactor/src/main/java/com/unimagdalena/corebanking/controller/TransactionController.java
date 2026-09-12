package com.unimagdalena.corebanking.controller;

import com.unimagdalena.corebanking.dto.request.DepositRequest;
import com.unimagdalena.corebanking.dto.request.TransferRequest;
import com.unimagdalena.corebanking.dto.request.WithdrawalRequest;
import com.unimagdalena.corebanking.dto.response.TransactionResponse;
import com.unimagdalena.corebanking.service.interfaces.TransactionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {

	private final TransactionService transactionService;

	public TransactionController(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@PostMapping("/api/v1/transactions/deposits")
	public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody DepositRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.deposit(request));
	}

	@PostMapping("/api/v1/transactions/withdrawals")
	public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody WithdrawalRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.withdraw(request));
	}

	@PostMapping("/api/v1/transactions/transfers")
	public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.transfer(request));
	}

	@GetMapping("/api/v1/accounts/{accountId}/transactions")
	public ResponseEntity<List<TransactionResponse>> findByAccountId(@PathVariable UUID accountId) {
		return ResponseEntity.ok(transactionService.findByAccountId(accountId));
	}
}
