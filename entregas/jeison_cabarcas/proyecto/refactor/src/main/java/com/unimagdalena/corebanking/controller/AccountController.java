package com.unimagdalena.corebanking.controller;

import com.unimagdalena.corebanking.dto.request.CreateAccountRequest;
import com.unimagdalena.corebanking.dto.request.UpdateAccountStatusRequest;
import com.unimagdalena.corebanking.dto.response.AccountResponse;
import com.unimagdalena.corebanking.dto.response.BalanceResponse;
import com.unimagdalena.corebanking.service.interfaces.AccountService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {

	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@PostMapping("/api/v1/customers/{customerId}/accounts")
	public ResponseEntity<AccountResponse> create(@PathVariable UUID customerId,
			@Valid @RequestBody CreateAccountRequest request) {
		AccountResponse response = accountService.create(customerId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/api/v1/customers/{customerId}/accounts")
	public ResponseEntity<List<AccountResponse>> findByCustomerId(@PathVariable UUID customerId) {
		return ResponseEntity.ok(accountService.findByCustomerId(customerId));
	}

	@GetMapping("/api/v1/accounts/{accountId}")
	public ResponseEntity<AccountResponse> getById(@PathVariable UUID accountId) {
		return ResponseEntity.ok(accountService.getById(accountId));
	}

	@GetMapping("/api/v1/accounts/{accountId}/balance")
	public ResponseEntity<BalanceResponse> getBalance(@PathVariable UUID accountId) {
		return ResponseEntity.ok(accountService.getBalance(accountId));
	}

	@PatchMapping("/api/v1/accounts/{accountId}/status")
	public ResponseEntity<AccountResponse> changeStatus(@PathVariable UUID accountId,
			@Valid @RequestBody UpdateAccountStatusRequest request) {
		return ResponseEntity.ok(accountService.changeStatus(accountId, request));
	}
}
