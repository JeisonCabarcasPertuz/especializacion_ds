package com.unimagdalena.corebanking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

	@Mock
	private BankTransactionRepository transactionRepository;

	@Mock
	private DepositTransactionProcessor depositProcessor;

	@Mock
	private WithdrawalTransactionProcessor withdrawalProcessor;

	@Mock
	private TransferTransactionProcessor transferProcessor;

	private final TransactionMapper transactionMapper = new TransactionMapper();

	private TransactionServiceImpl service() {
		return new TransactionServiceImpl(transactionRepository, transactionMapper, depositProcessor, withdrawalProcessor,
				transferProcessor);
	}

	@Test
	void depositDelegatesToDepositProcessor() {
		UUID accountId = UUID.randomUUID();
		TransactionResponse expected = TransactionResponse.builder().id(UUID.randomUUID()).build();
		when(depositProcessor.process(any(TransactionCommand.class))).thenReturn(expected);

		TransactionResponse response = service().deposit(new DepositRequest(accountId, new BigDecimal("100.00")));

		assertThat(response).isSameAs(expected);
		ArgumentCaptor<TransactionCommand> captor = ArgumentCaptor.forClass(TransactionCommand.class);
		verify(depositProcessor).process(captor.capture());
		assertThat(captor.getValue().getType()).isEqualTo(TransactionType.DEPOSIT);
		assertThat(captor.getValue().getDestinationAccountId()).isEqualTo(accountId);
	}

	@Test
	void withdrawDelegatesToWithdrawalProcessor() {
		UUID accountId = UUID.randomUUID();
		TransactionResponse expected = TransactionResponse.builder().id(UUID.randomUUID()).build();
		when(withdrawalProcessor.process(any(TransactionCommand.class))).thenReturn(expected);

		TransactionResponse response = service().withdraw(new WithdrawalRequest(accountId, new BigDecimal("100.00")));

		assertThat(response).isSameAs(expected);
		ArgumentCaptor<TransactionCommand> captor = ArgumentCaptor.forClass(TransactionCommand.class);
		verify(withdrawalProcessor).process(captor.capture());
		assertThat(captor.getValue().getType()).isEqualTo(TransactionType.WITHDRAWAL);
		assertThat(captor.getValue().getSourceAccountId()).isEqualTo(accountId);
	}

	@Test
	void transferDelegatesToTransferProcessor() {
		UUID sourceId = UUID.randomUUID();
		UUID destinationId = UUID.randomUUID();
		TransactionResponse expected = TransactionResponse.builder().id(UUID.randomUUID()).build();
		when(transferProcessor.process(any(TransactionCommand.class))).thenReturn(expected);

		TransactionResponse response = service().transfer(new TransferRequest(sourceId, destinationId, new BigDecimal("100.00")));

		assertThat(response).isSameAs(expected);
		ArgumentCaptor<TransactionCommand> captor = ArgumentCaptor.forClass(TransactionCommand.class);
		verify(transferProcessor).process(captor.capture());
		assertThat(captor.getValue().getType()).isEqualTo(TransactionType.TRANSFER);
		assertThat(captor.getValue().getSourceAccountId()).isEqualTo(sourceId);
		assertThat(captor.getValue().getDestinationAccountId()).isEqualTo(destinationId);
	}
}
