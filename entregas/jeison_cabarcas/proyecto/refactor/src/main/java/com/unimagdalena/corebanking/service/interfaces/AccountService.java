package com.unimagdalena.corebanking.service.interfaces;

import com.unimagdalena.corebanking.dto.request.CreateAccountRequest;
import com.unimagdalena.corebanking.dto.request.UpdateAccountStatusRequest;
import com.unimagdalena.corebanking.dto.response.AccountResponse;
import com.unimagdalena.corebanking.dto.response.BalanceResponse;
import java.util.List;
import java.util.UUID;

public interface AccountService {

	AccountResponse create(UUID customerId, CreateAccountRequest request);

	AccountResponse getById(UUID accountId);

	List<AccountResponse> findByCustomerId(UUID customerId);

	BalanceResponse getBalance(UUID accountId);

	AccountResponse changeStatus(UUID accountId, UpdateAccountStatusRequest request);
}
