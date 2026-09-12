package com.unimagdalena.corebanking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unimagdalena.corebanking.dto.request.CreateCustomerRequest;
import com.unimagdalena.corebanking.dto.response.CustomerResponse;
import com.unimagdalena.corebanking.entity.Customer;
import com.unimagdalena.corebanking.enums.DocumentType;
import com.unimagdalena.corebanking.exception.BusinessException;
import com.unimagdalena.corebanking.mapper.CustomerMapper;
import com.unimagdalena.corebanking.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

	@Mock
	private CustomerRepository customerRepository;

	private final CustomerMapper customerMapper = new CustomerMapper();

	private CustomerServiceImpl customerService;

	private CustomerServiceImpl service() {
		return new CustomerServiceImpl(customerRepository, customerMapper);
	}

	@Test
	void createRejectsDuplicateDocumentNumber() {
		customerService = service();
		CreateCustomerRequest request = new CreateCustomerRequest(DocumentType.CC, "123456", "Ada Lovelace", "ada@example.com");
		when(customerRepository.existsByDocumentNumber("123456")).thenReturn(true);

		assertThatThrownBy(() -> customerService.create(request))
				.isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getCode())
				.isEqualTo("CUSTOMER_DOCUMENT_ALREADY_EXISTS");

		verify(customerRepository, never()).save(any());
	}

	@Test
	void createPersistsNewCustomer() {
		customerService = service();
		CreateCustomerRequest request = new CreateCustomerRequest(DocumentType.CC, "999999", "Grace Hopper", "grace@example.com");
		when(customerRepository.existsByDocumentNumber("999999")).thenReturn(false);
		when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

		CustomerResponse response = customerService.create(request);

		ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
		verify(customerRepository).save(captor.capture());
		assertThat(captor.getValue().getDocumentNumber()).isEqualTo("999999");
		assertThat(response.getFullName()).isEqualTo("Grace Hopper");
	}
}
