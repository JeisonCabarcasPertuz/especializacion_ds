package com.unimagdalena.corebanking.pattern.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationTransactionObserver implements TransactionObserver {

	@Override
	public void onTransactionCompleted(TransactionCompletedEvent event) {
		if (event.getSourceAccount() != null) {
			log.info("simulated notification account={} message=Your account was debited {}",
					event.getSourceAccount().getAccountNumber(), event.getAmount().add(event.getFee()));
		}
		if (event.getDestinationAccount() != null) {
			log.info("simulated notification account={} message=Your account received {}",
					event.getDestinationAccount().getAccountNumber(), event.getAmount());
		}
	}
}
