package com.unimagdalena.corebanking.mapper;

import com.unimagdalena.corebanking.dto.response.CustomerResponse;
import com.unimagdalena.corebanking.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

	public CustomerResponse toResponse(Customer customer) {
		return CustomerResponse.builder()
				.id(customer.getId())
				.documentType(customer.getDocumentType())
				.documentNumber(customer.getDocumentNumber())
				.fullName(customer.getFullName())
				.email(customer.getEmail())
				.createdAt(customer.getCreatedAt())
				.build();
	}
}
