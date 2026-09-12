package com.unimagdalena.corebanking.service.interfaces;

import com.unimagdalena.corebanking.dto.request.DepositRequest;
import com.unimagdalena.corebanking.dto.request.TransferRequest;
import com.unimagdalena.corebanking.dto.request.WithdrawalRequest;
import com.unimagdalena.corebanking.dto.response.TransactionResponse;
import java.util.List;
import java.util.UUID;

public interface TransactionService {

	TransactionResponse deposit(DepositRequest request);

	TransactionResponse withdraw(WithdrawalRequest request);

	TransactionResponse transfer(TransferRequest request);

	List<TransactionResponse> findByAccountId(UUID accountId);
}
