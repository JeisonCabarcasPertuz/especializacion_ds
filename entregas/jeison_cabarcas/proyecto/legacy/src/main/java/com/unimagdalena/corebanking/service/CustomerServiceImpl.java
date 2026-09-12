package com.unimagdalena.corebanking.service;

import com.unimagdalena.corebanking.dto.request.CreateCustomerRequest;
import com.unimagdalena.corebanking.dto.response.CustomerResponse;
import com.unimagdalena.corebanking.entity.Customer;
import com.unimagdalena.corebanking.exception.BusinessException;
import com.unimagdalena.corebanking.exception.ResourceNotFoundException;
import com.unimagdalena.corebanking.mapper.CustomerMapper;
import com.unimagdalena.corebanking.repository.CustomerRepository;
import com.unimagdalena.corebanking.service.interfaces.CustomerService;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

	private final CustomerRepository customerRepository;
	private final CustomerMapper customerMapper;

	public CustomerServiceImpl(CustomerRepository customerRepository, CustomerMapper customerMapper) {
		this.customerRepository = customerRepository;
		this.customerMapper = customerMapper;
	}

	@Override
	@Transactional
	public CustomerResponse create(CreateCustomerRequest request) {
		if (customerRepository.existsByDocumentNumber(request.getDocumentNumber())) {
			throw new BusinessException("CUSTOMER_DOCUMENT_ALREADY_EXISTS", HttpStatus.CONFLICT,
					"A customer with document number " + request.getDocumentNumber() + " already exists");
		}

		Customer customer = Customer.builder()
				.documentType(request.getDocumentType())
				.documentNumber(request.getDocumentNumber())
				.fullName(request.getFullName())
				.email(request.getEmail())
				.build();

		Customer saved = customerRepository.save(customer);
		log.info("customer created id={}", saved.getId());
		return customerMapper.toResponse(saved);
	}

	@Override
	public CustomerResponse getById(UUID customerId) {
		Customer customer = findCustomerOrThrow(customerId);
		return customerMapper.toResponse(customer);
	}

	@Override
	public List<CustomerResponse> findAll() {
		return customerRepository.findAll().stream()
				.map(customerMapper::toResponse)
				.toList();
	}

	private Customer findCustomerOrThrow(UUID customerId) {
		return customerRepository.findById(customerId)
				.orElseThrow(() -> new ResourceNotFoundException("CUSTOMER_NOT_FOUND",
						"Customer " + customerId + " was not found"));
	}
}
