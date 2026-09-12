package com.unimagdalena.corebanking.service;

import com.unimagdalena.corebanking.dto.request.CreateAccountRequest;
import com.unimagdalena.corebanking.dto.request.UpdateAccountStatusRequest;
import com.unimagdalena.corebanking.dto.response.AccountResponse;
import com.unimagdalena.corebanking.dto.response.BalanceResponse;
import com.unimagdalena.corebanking.entity.BankAccount;
import com.unimagdalena.corebanking.entity.Customer;
import com.unimagdalena.corebanking.exception.ResourceNotFoundException;
import com.unimagdalena.corebanking.mapper.AccountMapper;
import com.unimagdalena.corebanking.pattern.state.AccountStateContext;
import com.unimagdalena.corebanking.repository.BankAccountRepository;
import com.unimagdalena.corebanking.repository.CustomerRepository;
import com.unimagdalena.corebanking.service.interfaces.AccountService;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

	private final BankAccountRepository accountRepository;
	private final CustomerRepository customerRepository;
	private final AccountMapper accountMapper;
	private final AccountStateContext accountStateContext;

	@Override
	@Transactional
	public AccountResponse create(UUID customerId, CreateAccountRequest request) {
		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new ResourceNotFoundException("CUSTOMER_NOT_FOUND",
						"Customer " + customerId + " was not found"));

		BankAccount account = BankAccount.builder()
				.customer(customer)
				.accountType(request.getAccountType())
				.build();

		BankAccount saved = accountRepository.save(account);
		log.info("account created id={} type={}", saved.getId(), saved.getAccountType());
		return accountMapper.toResponse(saved);
	}

	@Override
	public AccountResponse getById(UUID accountId) {
		return accountMapper.toResponse(findAccountOrThrow(accountId));
	}

	@Override
	public List<AccountResponse> findByCustomerId(UUID customerId) {
		return accountRepository.findAllByCustomerId(customerId).stream()
				.map(accountMapper::toResponse)
				.toList();
	}

	@Override
	public BalanceResponse getBalance(UUID accountId) {
		return accountMapper.toBalanceResponse(findAccountOrThrow(accountId));
	}

	@Override
	@Transactional
	public AccountResponse changeStatus(UUID accountId, UpdateAccountStatusRequest request) {
		BankAccount account = findAccountOrThrow(accountId);
		var previousStatus = account.getStatus();
		var targetStatus = request.getStatus();

		accountStateContext.assertCanTransitionTo(account, targetStatus);

		account.setStatus(targetStatus);
		BankAccount saved = accountRepository.save(account);
		log.info("account status changed id={} from={} to={}", accountId, previousStatus, targetStatus);
		return accountMapper.toResponse(saved);
	}

	private BankAccount findAccountOrThrow(UUID accountId) {
		return accountRepository.findById(accountId)
				.orElseThrow(() -> new ResourceNotFoundException("ACCOUNT_NOT_FOUND",
						"Account " + accountId + " was not found"));
	}
}
