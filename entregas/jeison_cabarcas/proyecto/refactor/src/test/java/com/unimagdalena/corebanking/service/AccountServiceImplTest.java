package com.unimagdalena.corebanking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.unimagdalena.corebanking.dto.request.UpdateAccountStatusRequest;
import com.unimagdalena.corebanking.dto.response.AccountResponse;
import com.unimagdalena.corebanking.entity.BankAccount;
import com.unimagdalena.corebanking.entity.Customer;
import com.unimagdalena.corebanking.enums.AccountStatus;
import com.unimagdalena.corebanking.enums.AccountType;
import com.unimagdalena.corebanking.exception.BusinessException;
import com.unimagdalena.corebanking.mapper.AccountMapper;
import com.unimagdalena.corebanking.pattern.state.AccountStateContext;
import com.unimagdalena.corebanking.pattern.state.AccountStateResolver;
import com.unimagdalena.corebanking.pattern.state.ActiveAccountState;
import com.unimagdalena.corebanking.pattern.state.BlockedAccountState;
import com.unimagdalena.corebanking.pattern.state.ClosedAccountState;
import com.unimagdalena.corebanking.repository.BankAccountRepository;
import com.unimagdalena.corebanking.repository.CustomerRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

	@Mock
	private BankAccountRepository accountRepository;

	@Mock
	private CustomerRepository customerRepository;

	private final AccountMapper accountMapper = new AccountMapper();
	private final AccountStateContext accountStateContext = new AccountStateContext(
			new AccountStateResolver(List.of(new ActiveAccountState(), new BlockedAccountState(), new ClosedAccountState())));

	private AccountServiceImpl service() {
		return new AccountServiceImpl(accountRepository, customerRepository, accountMapper, accountStateContext);
	}

	private BankAccount accountWithStatus(AccountStatus status) {
		Customer customer = Customer.builder().id(UUID.randomUUID()).build();
		return BankAccount.builder()
				.id(UUID.randomUUID())
				.accountNumber("ACC-TEST")
				.customer(customer)
				.accountType(AccountType.SAVINGS)
				.status(status)
				.balance(BigDecimal.ZERO.setScale(2))
				.currency("COP")
				.build();
	}

	@Test
	void activeAccountCanBeBlocked() {
		BankAccount account = accountWithStatus(AccountStatus.ACTIVE);
		when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
		when(accountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AccountResponse response = service().changeStatus(account.getId(), new UpdateAccountStatusRequest(AccountStatus.BLOCKED));

		assertThat(response.getStatus()).isEqualTo(AccountStatus.BLOCKED);
	}

	@Test
	void closedAccountCannotBeReopened() {
		BankAccount account = accountWithStatus(AccountStatus.CLOSED);
		when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

		assertThatThrownBy(() -> service().changeStatus(account.getId(), new UpdateAccountStatusRequest(AccountStatus.ACTIVE)))
				.isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getCode())
				.isEqualTo("INVALID_ACCOUNT_STATUS_TRANSITION");
	}

	@Test
	void sameStatusTransitionIsRejected() {
		BankAccount account = accountWithStatus(AccountStatus.ACTIVE);
		when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

		assertThatThrownBy(() -> service().changeStatus(account.getId(), new UpdateAccountStatusRequest(AccountStatus.ACTIVE)))
				.isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getCode())
				.isEqualTo("INVALID_ACCOUNT_STATUS_TRANSITION");
	}
}
