package com.unimagdalena.corebanking.service.interfaces;

import com.unimagdalena.corebanking.dto.request.CreateCustomerRequest;
import com.unimagdalena.corebanking.dto.response.CustomerResponse;
import java.util.List;
import java.util.UUID;

public interface CustomerService {

	CustomerResponse create(CreateCustomerRequest request);

	CustomerResponse getById(UUID customerId);

	List<CustomerResponse> findAll();
}
