package com.unimagdalena.corebanking.mapper;

import com.unimagdalena.corebanking.dto.response.TransactionResponse;
import com.unimagdalena.corebanking.entity.BankTransaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

	public TransactionResponse toResponse(BankTransaction transaction) {
		return TransactionResponse.builder()
				.id(transaction.getId())
				.type(transaction.getType())
				.amount(transaction.getAmount())
				.fee(transaction.getFee())
				.status(transaction.getStatus())
				.sourceAccountId(transaction.getSourceAccount() != null ? transaction.getSourceAccount().getId() : null)
				.destinationAccountId(
						transaction.getDestinationAccount() != null ? transaction.getDestinationAccount().getId() : null)
				.createdAt(transaction.getCreatedAt())
				.build();
	}
}
