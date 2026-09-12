package com.unimagdalena.corebanking.pattern.observer;

import com.unimagdalena.corebanking.entity.AuditRecord;
import com.unimagdalena.corebanking.repository.AuditRecordRepository;
import org.springframework.stereotype.Component;

@Component
public class AuditTransactionObserver implements TransactionObserver {

	private final AuditRecordRepository auditRecordRepository;

	public AuditTransactionObserver(AuditRecordRepository auditRecordRepository) {
		this.auditRecordRepository = auditRecordRepository;
	}

	@Override
	public void onTransactionCompleted(TransactionCompletedEvent event) {
		AuditRecord auditRecord = AuditRecord.builder()
				.transaction(event.getTransaction())
				.eventType(event.getTransaction().getType() + "_COMPLETED")
				.description(describe(event))
				.build();
		auditRecordRepository.save(auditRecord);
	}

	private String describe(TransactionCompletedEvent event) {
		String source = event.getSourceAccount() != null ? event.getSourceAccount().getAccountNumber() : "-";
		String destination = event.getDestinationAccount() != null ? event.getDestinationAccount().getAccountNumber() : "-";
		return event.getTransaction().getType() + " of " + event.getAmount() + " from " + source + " to " + destination;
	}
}
